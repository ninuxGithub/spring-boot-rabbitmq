package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MessageController {

	@Autowired
	private SendMessageService sendMessageService;

	// http://localhost:8080/sendMsg?msg=java

	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage(@RequestParam("msg") String msg) {
		return "index";
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/sendMsg", method = RequestMethod.POST)
	public User postMessage(User user) {
		user.setId(18l);
		//sendMessageService.sendMsg(user);
		Object receive = sendMessageService.sendAndReceive(user);
		System.out.println(receive);
		return user;
	}

}
