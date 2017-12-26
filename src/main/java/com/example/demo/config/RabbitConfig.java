package com.example.demo.config;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.example.demo.RabbitMQApp;
import com.example.demo.service.RabbitCallbackListener;

@Configuration
public class RabbitConfig {
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQApp.class);

	public static final String EXCHANGE = "my-exchange";
	public static final String ROUTINGKEY = "my-routing-key";
	public static final String QUEUENAME = "my-queue";

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses("10.1.51.96");
		connectionFactory.setUsername("admin");
		connectionFactory.setPassword("admin");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setPublisherConfirms(true); // 必须要设置
		connectionFactory.setExecutor(Executors.newFixedThreadPool(5));
		logger.info("config Rabbitmq ConnectionFactory successfully....");
		return connectionFactory;
	}

	// 必须是prototype类型
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setReplyTimeout(2000);
		return rabbitTemplate;

	}

	/**
	 * 针对消费者配置 1. 设置交换机类型 2. 将队列绑定到交换机
	 * 
	 * 
	 * FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念 HeadersExchange
	 * ：通过添加属性key-value匹配 DirectExchange:按照routingkey分发到指定队列 TopicExchange:多关键字匹配
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

	@Autowired
	private RabbitCallbackListener rabbitCallbackListener;

	@Bean
	public SimpleMessageListenerContainer messageContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
		container.setQueues(queue());
		container.setExposeListenerChannel(true);
		container.setMaxConcurrentConsumers(3);
		container.setConcurrentConsumers(2);
		container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置确认模式手工确认
		container.setRabbitAdmin(new RabbitAdmin(connectionFactory()));
		container.setMessageListener(rabbitCallbackListener);
		return container;
	}

}
