package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import com.example.demo.config.MinaSocketConfig;


@SpringBootApplication
@ImportResource("applicationContext.xml")
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
	
}
