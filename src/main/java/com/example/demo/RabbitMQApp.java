package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class RabbitMQApp {

	private static final Logger logger = LoggerFactory.getLogger(RabbitMQApp.class);

	public static final String EXCHANGE = "my-exchange";
	public static final String ROUTINGKEY = "my-routing-key";
	public static final String QUEUENAME = "my-queue";

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses("192.168.1.100:5672");
		connectionFactory.setUsername("admin");
		connectionFactory.setPassword("admin");
		connectionFactory.setVirtualHost("/VirtualHost");
		connectionFactory.setPublisherConfirms(true); // 必须要设置
		return connectionFactory;
	}

	// 必须是prototype类型
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		return rabbitTemplate;

	}

	/**
	 * 针对消费者配置 1. 设置交换机类型 2. 将队列绑定到交换机
	 * 
	 * 
	 * FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念 HeadersExchange
	 * ：通过添加属性key-value匹配 DirectExchange:按照routingkey分发到指定队列
	 * TopicExchange:多关键字匹配
	 */
	@Bean
	public DirectExchange defaultExchange() {
		return new DirectExchange(EXCHANGE);
	}

	@Bean
	public Queue queue() {
		return new Queue(QUEUENAME, true); // 队列持久

	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queue()).to(defaultExchange()).with(ROUTINGKEY);
	}

	@Bean
	public SimpleMessageListenerContainer messageContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
		container.setQueues(queue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(1);
		container.setConcurrentConsumers(1);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置确认模式手工确认
		container.setMessageListener(new ChannelAwareMessageListener() {

			public void onMessage(Message message, com.rabbitmq.client.Channel channel) throws Exception {
				byte[] body = message.getBody();
				logger.info("收到消息 : [{}] index: {}", new String(body), SendMessageService.index.incrementAndGet());
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 确认消息成功消费
			}

		});
		return container;
	}

	public static void main(String[] args) {
		SpringApplication.run(RabbitMQApp.class, args);

	}

	// add
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(new Jackson2JsonMessageConverter());
		return template;
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(new Jackson2JsonMessageConverter());
		return factory;
	}
}
