package com.example.demo;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageService implements RabbitTemplate.ConfirmCallback {

	private static final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

	private RabbitTemplate rabbitTemplate;
	
	public static AtomicInteger index = new AtomicInteger(0);

	/**
	 * 构造方法注入
	 */
	@Autowired
	public SendMessageService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
		// rabbitTemplate如果为单例的话，那回调就是最后设置的内容
		this.rabbitTemplate.setConfirmCallback(this); 
		logger.info("constructor init...");
	}

	public void sendMsg(String content) {
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(RabbitMQApp.EXCHANGE, RabbitMQApp.ROUTINGKEY, content, correlationId);
	}

	/**
	 * 回调
	 */
	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		logger.info(" index : {} -- 回调correlationDataID:[{}]  ",index.get(), correlationData);
		if (ack) {
			logger.info("消息成功消费");
		} else {
			logger.info("消息消费失败:[{}]", cause);
		}
	}

}
