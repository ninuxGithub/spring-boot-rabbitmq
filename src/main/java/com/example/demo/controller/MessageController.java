package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.bean.User;
import com.example.demo.core.MinaConnection;
import com.example.demo.service.SendMessageService;

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

	// http://localhost:8080/sendMsg
	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage() {
		return "index";
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
