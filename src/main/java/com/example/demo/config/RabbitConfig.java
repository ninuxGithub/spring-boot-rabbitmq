package com.example.demo.config;

import java.util.HashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.example.demo.RabbitMQApp;
import com.example.demo.service.RabbitCallbackListener;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConfig {
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQApp.class);

	public static final String EXCHANGE = "my-exchange";

	public static final String ROUTINGKEY = "my-routing-key";

	public static final String QUEUENAME = "my-queue";

	public static final String REPLY_QUEUE_NAME = "replyQueueName";

	public static final String REPLY_EXCHANGE_NAME = "replyExchangeName";

	public static final String REPLY_MESSAGE_KEY = "replyMessageKey";

	public static final String SEND_QUEUE_NAME = "sendQueueName";

	public static final String SEND_MESSAGE_KEY = "sendMessageKey";

	public static final String SEND_EXCHANGE_NAME = "sendExchangeName";

	@Autowired
	private RabbitProperties rabbitProperties;

	@Bean
	public ConnectionFactory getConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses(rabbitProperties.getAddresses());
		connectionFactory.setPort(rabbitProperties.getPort());
		connectionFactory.setUsername(rabbitProperties.getUsername());
		connectionFactory.setPassword(rabbitProperties.getPassword());
		connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
		connectionFactory.setPublisherConfirms(true); // 必须要设置
		connectionFactory.setExecutor(Executors.newFixedThreadPool(5));
		connectionFactory.setChannelCacheSize(100);
		logger.info("config Rabbitmq ConnectionFactory successfully....");
		return connectionFactory;
	}

	// 必须是prototype类型
	// old edition
	// @Bean
	// @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	// public RabbitTemplate rabbitTemplate() {
	// RabbitTemplate rabbitTemplate = new RabbitTemplate(getConnectionFactory());
	// rabbitTemplate.setReplyTimeout(2000);
	// return rabbitTemplate;
	//
	// }

	@Bean(name = "rabbitTemplate")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RabbitTemplate getRabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(getConnectionFactory());
		rabbitTemplate.setUseTemporaryReplyQueues(false);
		rabbitTemplate.setMessageConverter(getMessageConverter());
		rabbitTemplate.setMessagePropertiesConverter(getMessagePropertiesConverter());
		rabbitTemplate.setReplyAddress(RabbitConfig.REPLY_QUEUE_NAME);
		rabbitTemplate.setReceiveTimeout(60000);
		rabbitTemplate.setConfirmCallback(rabbitCallbackListener);
		rabbitTemplate.setReturnCallback(rabbitCallbackListener);
		rabbitTemplate.setMandatory(true);
		return rabbitTemplate;
	}

	@Bean(name = "rabbitAdmin")
	public RabbitAdmin getRabbitAdmin() {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(getConnectionFactory());
		rabbitAdmin.setAutoStartup(true);
		return rabbitAdmin;
	}

	@Bean(name = "serializerMessageConverter")
	public MessageConverter getMessageConverter() {
		return new SimpleMessageConverter();
	}

	@Bean(name = "messagePropertiesConverter")
	public MessagePropertiesConverter getMessagePropertiesConverter() {
		return new DefaultMessagePropertiesConverter();
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

	@Bean(name = "springMessageQueue")
	public Queue createQueue(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		Queue sendQueue = new Queue(RabbitConfig.SEND_QUEUE_NAME, true, false, false);
		rabbitAdmin.declareQueue(sendQueue);
		return sendQueue;
	}

	@Bean(name = "springMessageExchange")
	public Exchange createExchange(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		DirectExchange sendExchange = new DirectExchange(RabbitConfig.SEND_EXCHANGE_NAME, true, false);
		rabbitAdmin.declareExchange(sendExchange);
		return sendExchange;
	}

	@Bean(name = "springMessageBinding")
	public Binding createMessageBinding(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		Binding sendMessageBinding = new Binding(RabbitConfig.SEND_QUEUE_NAME, Binding.DestinationType.QUEUE,
				RabbitConfig.SEND_EXCHANGE_NAME, RabbitConfig.SEND_MESSAGE_KEY, new HashMap<String, Object>());
		rabbitAdmin.declareBinding(sendMessageBinding);
		return sendMessageBinding;
	}

	@Bean(name = "springReplyMessageQueue")
	public Queue createReplyQueue(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		Queue replyQueue = new Queue(RabbitConfig.REPLY_QUEUE_NAME, true, false, false);
		rabbitAdmin.declareQueue(replyQueue);
		return replyQueue;
	}

	@Bean(name = "springReplyMessageExchange")
	public Exchange createReplyExchange(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		DirectExchange replyExchange = new DirectExchange(RabbitConfig.REPLY_EXCHANGE_NAME, true, false);
		rabbitAdmin.declareExchange(replyExchange);
		return replyExchange;
	}

	@Bean(name = "springReplyMessageBinding")
	public Binding createReplyMessageBinding(@Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
		Binding replyMessageBinding = new Binding(RabbitConfig.REPLY_QUEUE_NAME, Binding.DestinationType.QUEUE,
				RabbitConfig.REPLY_EXCHANGE_NAME, RabbitConfig.REPLY_MESSAGE_KEY, new HashMap<String, Object>());
		rabbitAdmin.declareBinding(replyMessageBinding);
		return replyMessageBinding;
	}

	@Autowired
	private RabbitCallbackListener rabbitCallbackListener;

//	@Bean
//	public SimpleMessageListenerContainer messageContainer() {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(getConnectionFactory());
//		container.setQueues(queue());
//		container.setExposeListenerChannel(true);
//		container.setMaxConcurrentConsumers(3);
//		container.setConcurrentConsumers(2);
//		container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置确认模式手工确认
//		container.setRabbitAdmin(new RabbitAdmin(getConnectionFactory()));
//		container.setMessageListener(rabbitCallbackListener);
//		return container;
//	}

	// @Bean(name = "replyMessageListenerContainer")
	// public SimpleMessageListenerContainer createReplyListenerContainer() {
	// SimpleMessageListenerContainer listenerContainer = new
	// SimpleMessageListenerContainer();
	// listenerContainer.setConnectionFactory(getConnectionFactory());
	// listenerContainer.setQueueNames(RabbitConfig.REPLY_QUEUE_NAME);
	// listenerContainer.setMessageConverter(getMessageConverter());
	// listenerContainer.setMessagePropertiesConverter(getMessagePropertiesConverter());
	// // listenerContainer.setMessageListener(getRabbitTemplate());
	// listenerContainer.setRabbitAdmin(getRabbitAdmin());
	// listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
	// listenerContainer.setMessageListener(rabbitCallbackListener);
	// return listenerContainer;
	// }
	
	@Bean(name = "replyMessageListenerContainer")
	public SimpleMessageListenerContainer createReplyListenerContainer() {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
		listenerContainer.setConnectionFactory(getConnectionFactory());
		//listenerContainer.setQueues(createReplyQueue(getRabbitAdmin()),queue(),createQueue(getRabbitAdmin()));
		listenerContainer.setQueueNames(RabbitConfig.REPLY_QUEUE_NAME,RabbitConfig.QUEUENAME, RabbitConfig.SEND_QUEUE_NAME);
		listenerContainer.setMessageListener(rabbitCallbackListener);
		listenerContainer.setMessageConverter(getMessageConverter());
		listenerContainer.setMessagePropertiesConverter(getMessagePropertiesConverter());
		listenerContainer.setRabbitAdmin(getRabbitAdmin());
		listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return listenerContainer;
	}

}
