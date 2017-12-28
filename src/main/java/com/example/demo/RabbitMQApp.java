package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.config.MinaSocketConfig;

@SpringBootApplication
public class RabbitMQApp implements CommandLineRunner{
	
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQApp.class);
	
	@Autowired
	private MinaSocketConfig minaSocketConfig;

	public static void main(String[] args) {
		SpringApplication.run(RabbitMQApp.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("minaSocketConfig is {}",minaSocketConfig == null?"[null]":"[config correctly!]");
	}

	/*@Bean
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
	}*/
}
