package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.bean.User;
import com.example.demo.service.SendMessageService;

@Controller
public class MessageController {

	@Autowired
	private SendMessageService sendMessageService;

	// http://localhost:8080/sendMsg
	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage() {
		return "index";
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/sendMsg", method = RequestMethod.POST)
	public User postMessage(User user) {
		user.setId(18l);
		
		//异步将消息传递出去
		/*new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendMessageService.sendMsg(user);
			}
		}).start();*/
		
		sendMessageService.sendMsg(user);
		//System.out.println(sendMessageService.sendAndReceive(user));
		//sendMessageService.convertAndSend(user);
		
		return user;
	}

}
