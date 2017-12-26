package com.example.demo;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.BasicProperties;

@Component
public class SendMessageService /*implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback*/ {

	private static final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

	private RabbitTemplate rabbitTemplate;

	public static volatile AtomicInteger index = new AtomicInteger(0);

	/**
	 * 构造方法注入
	 */
	@Autowired
	public SendMessageService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
		// rabbitTemplate如果为单例的话，那回调就是最后设置的内容
//		this.rabbitTemplate.setConfirmCallback(this);
//		this.rabbitTemplate.setReturnCallback(this);
		logger.info("constructor init...");
	}

	public void sendMsg(String content) {
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(RabbitMQApp.EXCHANGE, RabbitMQApp.ROUTINGKEY, content, correlationId);
	}


	/**
	 * 发送对象类型的数据
	 */
	public void sendMsg(User user) {
		CorrelationData correlationData = new CorrelationData(user.getUuid());
		String json = JSONObject.toJSONString(user);
		
		/**
		 * convertAndSend 可以传递Object参数
		 */
		rabbitTemplate.convertAndSend(RabbitMQApp.EXCHANGE, RabbitMQApp.ROUTINGKEY, json, correlationData);
	}
	/**
	 * 发送对象类型的数据
	 */
	public Object sendAndReceive(User user) {
		CorrelationData correlationData = new CorrelationData(user.getUuid());
		String json = JSONObject.toJSONString(user);
		return rabbitTemplate.sendAndReceive(RabbitMQApp.EXCHANGE, RabbitMQApp.ROUTINGKEY, new Message(json.getBytes(), new MessageProperties()), correlationData);
	}
	
	
//	/**
//	 * 回调
//	 */
//	@Override
//	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//		logger.info(" index : {} -- 回调correlationDataID:[{}]", index.get(), correlationData);
//		if (ack) {
//			logger.info("消息成功消费");
//		} else {
//			logger.info("消息消费失败:[{}]", cause);
//		}
//	}
//
//	@Override
//	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//		logger.info("message:{}", new String(message.getBody()));
//		logger.info("replyCode:{}", replyCode);
//		logger.info("replyText:{}", replyText);
//		logger.info("exchange:{}", exchange);
//		logger.info("routingKey:{}", routingKey);
//		
//	}

}
