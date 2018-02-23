package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import com.example.demo.service.SendMessageService;
import com.example.demo.service.UserEntityService;
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
	private UserEntityService userService;

	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public UserEntity index() {
		UserEntity user = new UserEntity();
		user.setName("测试事物");
		
		userService.saveUserEntity(user);
//		try {
//			userService.saveUserEntity(user);
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
		return user;
	}

	// http://localhost:8080/sendMsg
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/sendMsg", method = { RequestMethod.GET})
	public String sendMessage() {
		List<UserEntity> users = userService.findAllUserEntity();
		System.out.println("目标的集合的size："+ users.size());
		
		//方法一
		Map<String, UserEntity> callResult = CallableUtil.callableCaculation("id", users, 1L);
		if(null != callResult) {
//			for(String key: callResult.keySet()) {
//				System.out.println("key is :"+ key +  "   value is : "+ callResult.get(key));
//			}
			
			System.out.println("多线程返回的结果的size： "+callResult.size());
		}
		
		//方法二
		Map callResult2 = CallableUtil.callableCaculation(new Callable<Map>() {
			
			@Override
			public Map call() throws Exception {
				Map<String,UserEntity> map = new HashMap<>();
				for(UserEntity user : users) {
					map.put(user.getId(), user);
				}
				return map;
			}
		});
		
//		for (Object key : callResult2.keySet()) {
//			System.out.println(key + " "+ callResult2.get(key));
//		}
		System.out.println("多线程返回的结果的size： "+callResult2.size());
		
		
		
		//代码完成
		long start = System.currentTimeMillis();
		Map<String,UserEntity> map = new HashMap<>();
		for(UserEntity user : users) {
			map.put(user.getId(), user);
		}
		long end = System.currentTimeMillis();
		System.out.println("main 消耗的时间是："+(end - start ) + "ms");
		
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

		/** 测试RabbitMQ发送消息 */
		sendMessageService.sendMsg(user);

		/** 测试mina发送消息 */
		minaConnection.minaSendMessage(user);
		return user;

		// System.out.println(sendMessageService.sendAndReceive(user));
		// sendMessageService.convertAndSend(user);

	}

	@ResponseBody
	@RequestMapping(value = "/sendReply", method = RequestMethod.POST)
	public User sendReply(User user) {
		user.setId(100l);

		/** 测试RabbitMQ发送消息 */
		Message replyMessage = sendMessageService.sendReply(user);
		if (null != replyMessage) {
			user = new User();
			user.setName(new String(replyMessage.getBody()));
		}
		return user;

	}

}
