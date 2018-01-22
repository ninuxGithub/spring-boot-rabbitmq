package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.bean.User;
import com.example.demo.bean.UserEntity;
import com.example.demo.core.MinaConnection;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SendMessageService;
import com.example.demo.thread.CallableUtil;

@Controller
public class MessageController {
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
	
	public MessageController() {
		logger.info("[MessageController init...]");
	}

	@Autowired
	private SendMessageService sendMessageService;	
	
	@Autowired
	private MinaConnection minaConnection;
	
	@Autowired
	private UserRepository userRepository;
	
	

	// http://localhost:8080/sendMsg
	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage() {
		List<UserEntity> users = userRepository.findAll();
		Map<String, UserEntity> callableCaculation = CallableUtil.callableCaculation("id", users);
		System.out.println(callableCaculation==null);
		return "index";
	}
	@RequestMapping(value = "/klinePage", method = RequestMethod.GET)
	public String kline() {
		return "kline";
	}
	@RequestMapping(value = "/barPage", method = RequestMethod.GET)
	public String bar() {
		return "bar";
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/sendMsg", method = RequestMethod.POST)
	public User postMessage(User user) {
		user.setId(18l);
		
		/**测试RabbitMQ发送消息*/
		sendMessageService.sendMsg(user);
		
		
		
		/**测试mina发送消息*/
		minaConnection.minaSendMessage(user);
		return user;
		
		//System.out.println(sendMessageService.sendAndReceive(user));
		//sendMessageService.convertAndSend(user);
		
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/sendReply", method = RequestMethod.POST)
	public User sendReply(User user) {
		user.setId(100l);
		
		/**测试RabbitMQ发送消息*/
		Message replyMessage = sendMessageService.sendReply(user);
		if(null != replyMessage) {
			user = new User();
			user.setName(new String(replyMessage.getBody()));
		}
		return user;
		
	}


	

}
