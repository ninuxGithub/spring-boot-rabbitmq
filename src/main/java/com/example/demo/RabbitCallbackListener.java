package com.example.demo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

@Component
public class RabbitCallbackListener implements ChannelAwareMessageListener{
	private static final Logger logger = LoggerFactory.getLogger(RabbitCallbackListener.class);

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		channel.basicConsume(RabbitMQApp.QUEUENAME, false,"consumerTag", new DefaultConsumer(channel){

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
				super.handleDelivery(consumerTag, envelope, properties, body);
				String routingKey = envelope.getRoutingKey();
				String contentType = properties.getContentType();
				long deliveryTag = envelope.getDeliveryTag();
				String replyTo = properties.getReplyTo();
				channel.basicAck(deliveryTag, false);
				logger.info("routingKey : [{}]", routingKey);
				logger.info("contentType : [{}]", contentType);
				logger.info("deliveryTag : [{}]", deliveryTag);
				logger.info("replyTo : [{}]", replyTo);
				logger.info("messageProperties : [{}]", properties);
				logger.info(" index: {} || 收到消息 : [{}]", SendMessageService.index.incrementAndGet(), new String(body));
			}
			
		});
		
	}

}
