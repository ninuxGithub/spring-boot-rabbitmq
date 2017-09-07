package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MessageController {

	@Autowired
	private SendMessageService sendMessageService;

	// http://localhost:8080/sendMsg?msg=java

	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage(@RequestParam("msg") String msg) {
		sendMessageService.sendMsg(msg);
		return "index";
	}

}
