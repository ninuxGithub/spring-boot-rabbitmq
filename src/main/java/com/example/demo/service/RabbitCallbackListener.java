package com.example.demo.service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.User;
import com.example.demo.config.RabbitConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Component
public class RabbitCallbackListener implements ChannelAwareMessageListener {
	private static final Logger logger = LoggerFactory.getLogger(RabbitCallbackListener.class);

	public static volatile AtomicInteger index = new AtomicInteger(0);
	@Autowired
	private MessagePropertiesConverter messagePropertiesConverter;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		if (null == message) {
			// 这个代码没有用到，只是为了测试的时候使用的
			channel.basicConsume(RabbitConfig.QUEUENAME, false, "consumerTag", new DefaultConsumer(channel) {

				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {
					super.handleDelivery(consumerTag, envelope, properties, body);
					String routingKey = envelope.getRoutingKey();
					String contentType = properties.getContentType();
					String replyTo = properties.getReplyTo();
					long deliveryTag = envelope.getDeliveryTag();
					channel.basicAck(deliveryTag, false);
					logger.info("routingKey : [{}]", routingKey);
					logger.info("contentType : [{}]", contentType);
					logger.info("deliveryTag : [{}]", deliveryTag);
					logger.info("replyTo : [{}]", replyTo);
					logger.info("messageProperties : [{}]", properties);
					logger.info(" index: {} || 收到消息 : [{}]", index.incrementAndGet(), new String(body));
				}
			});
		}

		// rabbitmq 接受消息的监听器------>真正接收消息的地方
		byte[] body = message.getBody();
		String json = new String(body);
		User user = JSONObject.parseObject(json, User.class);
		if (user.getId().longValue() == 100) {
			MessageProperties messageProperties = message.getMessageProperties();
			AMQP.BasicProperties rabbitMQProperties = messagePropertiesConverter
					.fromMessageProperties(messageProperties, "UTF-8");
			String numberContent = null;
			numberContent = new String(message.getBody(), "UTF-8");
			System.out.println("The received number is:" + numberContent);
			String consumerTag = messageProperties.getConsumerTag();
			// int number = Integer.parseInt(numberContent);

			String result = "result";// factorial(number);

			AMQP.BasicProperties replyRabbitMQProps = new AMQP.BasicProperties("text/plain", "UTF-8", null, 2, 0,
					rabbitMQProperties.getCorrelationId(), null, null, null, null, null, null, consumerTag, null);
//			Envelope replyEnvelope = new Envelope(messageProperties.getDeliveryTag(), true,
//					RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY);

			MessageProperties replyMessageProperties = messagePropertiesConverter
					.toMessageProperties(replyRabbitMQProps, null, "UTF-8");

			Message replyMessage = MessageBuilder.withBody(result.getBytes()).andProperties(replyMessageProperties)
					.build();

			rabbitTemplate.send(RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY, replyMessage);
		}
		logger.info("[RabbitMQ] receive msg : " + json);
		channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 确认消息成功消费

	}

}
