package com.example.demo.service;

import java.io.UnsupportedEncodingException;
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
import com.example.demo.bean.RabbitConfirmMessage;
import com.example.demo.bean.User;
import com.example.demo.config.RabbitConfig;
import com.example.demo.repository.RabbitConfirmMessageRepository;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

@Component
public class SendMessageService {

	private static final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private MessagePropertiesConverter messagePropertiesConverter;
	
	@Autowired
	private RabbitConfirmMessageRepository rabbitConfirmMessageRepository;

	/**
	 * 构造方法注入
	 */
	@Autowired
	public SendMessageService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
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
		
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//判断消息是否被成功的消费了,是根据数据库存储的消费记录来判断的
		
		RabbitConfirmMessage confirmMessage = rabbitConfirmMessageRepository.findByCorrelationData(correlationData.getId());
		if(confirmMessage != null) {
			System.err.println(correlationData.getId()+(confirmMessage.getAck()?"被成功消费了！":"消费失败"));
		}
		
		
		//from :http://blog.csdn.net/u011126891/article/details/54376179 
		
		
		//sendAndReceive 返回空
		
//		testSendAndReceiveMessage(correlationData, json);
	}

	
	//TODO 有待探索
	/***
	 * 测试发送并且接受消息的回复
	 * @param correlationData
	 * @param json
	 */
	public void testSendAndReceiveMessage(CorrelationData correlationData, String json) {
		Date sendTime = new Date();  
        String correlationId = UUID.randomUUID().toString();  
   
   
        AMQP.BasicProperties props =  
                new AMQP.BasicProperties("text/plain",  
                        "UTF-8",  
                        null,  
                        2,  
                        0, correlationId, RabbitConfig.REPLY_EXCHANGE_NAME, null,  
                        null, sendTime, null, null,  
                        "SpringProducer", null);  
   
        MessageProperties sendMessageProperties =  
                messagePropertiesConverter.toMessageProperties(props, null,"UTF-8");  
        sendMessageProperties.setReceivedExchange(RabbitConfig.REPLY_EXCHANGE_NAME);  
        sendMessageProperties.setReceivedRoutingKey(RabbitConfig.REPLY_MESSAGE_KEY);  
        sendMessageProperties.setRedelivered(true);  
   
        Message sendMessage = MessageBuilder.withBody(json.getBytes())  
                .andProperties(sendMessageProperties)  
                .build();  
   
        rabbitTemplate.expectedQueueNames(); //this.isListener = true; 不然会报错：RabbitTemplate is not configured as MessageListener -
        Message replyMessage =  
                rabbitTemplate.sendAndReceive(RabbitConfig.EXCHANGE,  RabbitConfig.ROUTINGKEY, sendMessage,correlationData);  
   
        String replyMessageContent = null;  
        try {  
        	if(replyMessage != null) {
        		
        		replyMessageContent = new String(replyMessage.getBody(),"UTF-8");  
        	}
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }finally {
        	System.out.println("replyMessageContent "+replyMessageContent);
        }
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
				correlationData.toString(), RabbitConfig.REPLY_QUEUE_NAME, 
				null, null, new Date(), null, null, "SpringProducer",null);

		MessageProperties messageProperties = messagePropertiesConverter.toMessageProperties(basicProperties, null,"UTF-8");
		messageProperties.setReceivedExchange(RabbitConfig.REPLY_EXCHANGE_NAME);
		messageProperties.setReceivedRoutingKey(RabbitConfig.REPLY_MESSAGE_KEY);
		messageProperties.setRedelivered(true);

		Message sendMessage = 
				MessageBuilder.withBody(json.getBytes())
				.andProperties(messageProperties)
				.build();
		//sendMessage.getMessageProperties().setReplyTo(RabbitConfig.REPLY_QUEUE_NAME);
		rabbitTemplate.expectedQueueNames();
		Message replyMessage=null;
		try {
			rabbitTemplate.setReplyAddress(RabbitConfig.REPLY_QUEUE_NAME);
			replyMessage = 
					rabbitTemplate.sendAndReceive(RabbitConfig.SEND_EXCHANGE_NAME, 
							RabbitConfig.SEND_MESSAGE_KEY,sendMessage,correlationData);
			System.err.println(replyMessage);
		} catch (Exception e) {
			replyMessage = new Message(e.getMessage().getBytes(), new MessageProperties());
			e.printStackTrace();
		}
		return replyMessage;
	}

}
