//package com.example.demo.service;
//
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageBuilder;
//import org.springframework.amqp.core.MessageProperties;
//import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
//import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
//import org.springframework.amqp.rabbit.support.CorrelationData;
//import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.fastjson.JSONObject;
//import com.example.demo.bean.User;
//import com.example.demo.config.RabbitConfig;
//import com.rabbitmq.client.AMQP;
//import com.rabbitmq.client.AMQP.BasicProperties;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.DefaultConsumer;
//import com.rabbitmq.client.Envelope;
//
///**
// * 消息接收
// * 消息确认
// * 以及route错误后返回的消息
// */
//@Component
//public class RabbitCallbackListener2 implements ChannelAwareMessageListener, ConfirmCallback, ReturnCallback {
//	private static final Logger logger = LoggerFactory.getLogger(RabbitCallbackListener2.class);
//
//	public static volatile AtomicInteger index = new AtomicInteger(0);
//	@Autowired
//	private MessagePropertiesConverter messagePropertiesConverter;
//
//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//
//	/**
//	 * basic.ack
//	 * basic.nack
//	 * basic.reject
//	 * 
//	 * 如果exchange 参数错误会报错
//	 * 
//	 * @param message
//	 * @param channel
//	 */
//	@Override
//	public void onMessage(Message message, Channel channel) {
//		try {
//			if (null == message) {
//				// 这个代码没有用到，只是为了测试的时候使用的
//				channel.basicConsume(RabbitConfig.QUEUENAME, false, "consumerTag", new DefaultConsumer(channel) {
//
//					@Override
//					public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
//							byte[] body) throws IOException {
//						super.handleDelivery(consumerTag, envelope, properties, body);
//						String routingKey = envelope.getRoutingKey();
//						String contentType = properties.getContentType();
//						String replyTo = properties.getReplyTo();
//						long deliveryTag = envelope.getDeliveryTag();
//						channel.basicAck(deliveryTag, false);
//						logger.info("routingKey : [{}]", routingKey);
//						logger.info("contentType : [{}]", contentType);
//						logger.info("deliveryTag : [{}]", deliveryTag);
//						logger.info("replyTo : [{}]", replyTo);
//						logger.info("messageProperties : [{}]", properties);
//						logger.info(" index: {} || 收到消息 : [{}]", index.incrementAndGet(), new String(body));
//					}
//				});
//			}
//
//			// rabbitmq 接受消息的监听器------>真正接收消息的地方
//			byte[] body = message.getBody();
//			String json = new String(body);
//			User user = JSONObject.parseObject(json, User.class);
//			//TODO 如何返回消息还不是很清楚
//			//if (user.getId().longValue() == 100) {
//				MessageProperties messageProperties = message.getMessageProperties();
//				AMQP.BasicProperties rabbitMQProperties = messagePropertiesConverter.fromMessageProperties(messageProperties, "UTF-8");
//				String numberContent = null;
//				numberContent = new String(message.getBody(), "UTF-8");
//				System.out.println("The received number is:" + numberContent);
//				String consumerTag = messageProperties.getConsumerTag();
//				// int number = Integer.parseInt(numberContent);
//
//				String result = "result";// factorial(number);
//
//				AMQP.BasicProperties replyRabbitMQProps = new AMQP.BasicProperties("text/plain", "UTF-8", null, 2, 0,
//						rabbitMQProperties.getCorrelationId(), null, null, null, null, null, null, consumerTag, null);
//				// Envelope replyEnvelope = new Envelope(messageProperties.getDeliveryTag(),
//				// true,
//				// RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY);
//
//				MessageProperties replyMessageProperties = 
//						messagePropertiesConverter.toMessageProperties(replyRabbitMQProps, null, "UTF-8");
//
//				Message replyMessage = MessageBuilder
//						.withBody(result.getBytes())
//						.andProperties(replyMessageProperties)
//						.build();
//
//				rabbitTemplate.send(RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY, replyMessage);
//				
//				rabbitTemplate.receiveAndReply(callback, exchange, routingKey)
//			//}
//			logger.info("[RabbitMQ] receive msg : " + json);
//			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 确认消息成功消费
//		} catch (Exception e) {
//			logger.info("[RabbitMQ] receive msg failed "+ e.getMessage());
//			try {
//				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//
//		}
//
//	}
//
//	/**
//	 * 返回消息:如果rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTINGKEY, content, correlationId);
//	 * 的routeKey错误，那么returnedMessage会执行
//	 */
//	@SuppressWarnings("deprecation")
//	@Override
//	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//		String msgId = "";
//		if (message.getMessageProperties().getCorrelationId() != null) {
//			msgId = new String(message.getMessageProperties().getCorrelationId());
//		}
//		logger.info("[returnedMessage]: msgId:" + msgId + ",msgBody:" + new String(message.getBody())
//				+ ",replyCode:" + replyCode + ",replyText:" + replyText + ",exchange:" + exchange + ",routingKey:"
//				+ routingKey);
//	}
//
//	/**
//	 * 消息确认：都会执行的	 * 
//	 * 只是ack:true , 成功接收
//	 * 当ack:false, 没有接收到消息
//	 */
//	@Override
//	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//		logger.info("[Product Confirm]:correlationData:" + correlationData + ",ack:" + ack + ",cause:" + cause);
//	}
//
//}
