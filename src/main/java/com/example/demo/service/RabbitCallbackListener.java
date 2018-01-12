package com.example.demo.service;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.RabbitConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

/**
 * 消息接收 消息确认 以及route错误后返回的消息
 */
@Component
public class RabbitCallbackListener implements ChannelAwareMessageListener, ConfirmCallback, ReturnCallback {
	private static final Logger logger = LoggerFactory.getLogger(RabbitCallbackListener.class);

	public static volatile AtomicInteger index = new AtomicInteger(0);
	@Autowired
	private MessagePropertiesConverter messagePropertiesConverter;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	/**
	 * basic.ack basic.nack basic.reject
	 * 
	 * 如果exchange 参数错误会报错
	 * 
	 * @param message
	 * @param channel
	 */
	@Override
	public void onMessage(Message message, Channel channel) {

		try {
			// rabbitmq 接受消息的监听器------>真正接收消息的地方
			byte[] body = message.getBody();
			String json = new String(body);
			
			logger.info("[RabbitMQ] receive msg : " + json);
			
			
//			MessageProperties basicProperties = new MessageProperties();
//			basicProperties.setReplyTo(RabbitConfig.REPLY_QUEUE_NAME);
//			Message sendMessage = MessageBuilder.withBody("java is good language".getBytes()).andProperties(basicProperties).build();
//			rabbitTemplate.send(RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY, sendMessage);

//			boolean receiveAndReply = rabbitTemplate.receiveAndReply(RabbitConfig.REPLY_QUEUE_NAME,new ReceiveAndReplyCallback<String, String>() {
//
//				@Override
//				public String handle(String payload) {
//					System.out.println(payload);
//					return "i am reply message";
//				}
//			});
//			System.err.println(receiveAndReply);
			
			
			
//			MessageProperties messageProperties = message.getMessageProperties();  
//	        AMQP.BasicProperties rabbitMQProperties =  
//	                messagePropertiesConverter.fromMessageProperties(messageProperties, "UTF-8");  
//	        String numberContent = null;  
//	        numberContent = new String(message.getBody(),"UTF-8");  
//	        System.out.println("The received number is:" + numberContent);  
//	        String consumerTag = messageProperties.getConsumerTag();  
//	   
//	        String result = "100";  
//	   
//	        AMQP.BasicProperties replyRabbitMQProps =  
//	                new AMQP.BasicProperties("text/plain",  
//	                        "UTF-8",  
//	                        null,  
//	                        2,  
//	                        0, rabbitMQProperties.getCorrelationId(), null, null,  
//	                        null, null, null, null,  
//	                        consumerTag, null);  
//	        Envelope replyEnvelope =  
//	                new Envelope(messageProperties.getDeliveryTag(), true, RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY);  
//	   
//	        MessageProperties replyMessageProperties =  
//	                messagePropertiesConverter.toMessageProperties(replyRabbitMQProps,  
//	                        replyEnvelope,"UTF-8");  
//	   
//	        Message replyMessage = MessageBuilder.withBody(result.getBytes())  
//	                .andProperties(replyMessageProperties)  
//	                .build();  
//	   
//	        rabbitTemplate.send(RabbitConfig.REPLY_EXCHANGE_NAME,RabbitConfig.REPLY_MESSAGE_KEY, replyMessage);  
		} catch (Exception e2) {
			logger.warn("[RabbitMQ]  reply failed " + e2.getMessage());
		}
		
		
		try {
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 确认消息成功消费
		} catch (Exception e) {
			logger.warn("[RabbitMQ] receive msg failed " + e.getMessage());
			try {
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	/**
	 * 返回消息:如果rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
	 * RabbitConfig.ROUTINGKEY, content, correlationId);
	 * 的routeKey错误，那么returnedMessage会执行
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		String msgId = "";
		if (message.getMessageProperties().getCorrelationId() != null) {
			msgId = new String(message.getMessageProperties().getCorrelationId());
		}
		logger.info("[returnedMessage]: msgId:" + msgId + ",msgBody:" + new String(message.getBody()) + ",replyCode:"
				+ replyCode + ",replyText:" + replyText + ",exchange:" + exchange + ",routingKey:" + routingKey);
	}

	/**
	 * 消息确认：都会执行的 * 只是ack:true , 成功接收 当ack:false, 没有接收到消息
	 */
	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		logger.info("[Product Confirm]:correlationData:" + correlationData + ",ack:" + (ack?"接收消息成功":"接收消息失败") + ",cause:" + cause);
	}

}
