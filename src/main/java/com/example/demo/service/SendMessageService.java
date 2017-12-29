package com.example.demo.service;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.User;
import com.example.demo.config.RabbitConfig;
import com.rabbitmq.client.AMQP.BasicProperties;

@Component
public class SendMessageService /*implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback*/ {

	private static final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private MessagePropertiesConverter messagePropertiesConverter;
	

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
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTINGKEY, content, correlationId);
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
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTINGKEY, json, correlationData);
	}
	/**
	 * 发送对象类型的数据
	 */
	public Object sendAndReceive(User user) {
		CorrelationData correlationData = new CorrelationData(user.getUuid());
		String json = JSONObject.toJSONString(user);
		return rabbitTemplate.sendAndReceive(RabbitConfig.EXCHANGE, RabbitConfig.ROUTINGKEY, new Message(json.getBytes(), new MessageProperties()), correlationData);
	}
	
	public void convertAndSend(User user) {
		CorrelationData correlationData = new CorrelationData(user.getUuid());
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,RabbitConfig.ROUTINGKEY, user, correlationData);
	}

	public Message sendReply(User user) {
		CorrelationData correlationData = new CorrelationData(user.getUuid());
		String json = JSONObject.toJSONString(user);
		BasicProperties basicProperties = new BasicProperties("text/plain", "UTF-8", null, 2, 0,
				correlationData.toString(), RabbitConfig.REPLY_EXCHANGE_NAME, 
				null, null, new Date(), null, null, "SpringProducer",null);

		MessageProperties messageProperties = messagePropertiesConverter.toMessageProperties(basicProperties, null,"UTF-8");
		messageProperties.setReceivedExchange(RabbitConfig.REPLY_EXCHANGE_NAME);
		messageProperties.setReceivedRoutingKey(RabbitConfig.REPLY_MESSAGE_KEY);
		messageProperties.setRedelivered(true);

		Message sendMessage = MessageBuilder.withBody(json.getBytes()).andProperties(messageProperties).build();
		Message replyMessage = rabbitTemplate.sendAndReceive(RabbitConfig.SEND_EXCHANGE_NAME, 
				RabbitConfig.SEND_MESSAGE_KEY,sendMessage,correlationData);
//		Message replyMessage = rabbitTemplate.sendAndReceive(RabbitConfig.EXCHANGE, 
//				RabbitConfig.ROUTINGKEY,new Message(json.getBytes(), new MessageProperties()),correlationData);
		return replyMessage;
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
