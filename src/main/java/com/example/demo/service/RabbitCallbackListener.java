package com.example.demo.service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import com.example.demo.config.RabbitConfig;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Component
public class RabbitCallbackListener implements ChannelAwareMessageListener {
	private static final Logger logger = LoggerFactory.getLogger(RabbitCallbackListener.class);

	public static volatile AtomicInteger index = new AtomicInteger(0);

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		if (null == message) {
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

		byte[] body = message.getBody();
		System.out.println("receive msg : " + new String(body));
		channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 确认消息成功消费

	}

}
