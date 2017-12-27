package com.example.demo.controller;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.bean.User;
import com.example.demo.config.MinaSocketConfig;
import com.example.demo.core.ReceiveMinaHandle;
import com.example.demo.service.SendMessageService;

@Controller
public class MessageController {
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	@Autowired
	private SendMessageService sendMessageService;	
	
	@Autowired
	MinaSocketConfig minaSocketConfig;

	// http://localhost:8080/sendMsg
	@RequestMapping(value = "/sendMsg", method = RequestMethod.GET)
	public String sendMessage() {
		return "index";
	}
	
	
	@SuppressWarnings({ "deprecation", "static-access" })
	@ResponseBody
	@RequestMapping(value = "/sendMsg", method = RequestMethod.POST)
	public User postMessage(User user) {
		user.setId(18l);
		
		/**测试RabbitMQ发送消息*/
		sendMessageService.sendMsg(user);
		
		/**测试mina发送消息*/
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new ReceiveMinaHandle());
		// 设置连接超时时间 分钟
		connector.setConnectTimeout(3000);
		// 编写过滤器
		connector.getFilterChain().addLast("codec",
						new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),
                        LineDelimiter.WINDOWS.getValue(), 
                        LineDelimiter.WINDOWS.getValue())));
		
		connector.getFilterChain().addLast("logging", new LoggingFilter());
		// 创建连接
		IoSession session = null;
		try {
			ConnectFuture connect = connector.connect(new InetSocketAddress(minaSocketConfig.getPort()));
			// 等待连接创建完成
			connect.awaitUninterruptibly();
			// 获取session
			session = connect.getSession();
			session.write("客户端连接测试成功!");
			session.write(user.toString());			
		} catch (Exception e) {
			logger.error("客户端连接异常");
		}finally {
			if(null != session) {
				//session.getCloseFuture().awaitUninterruptibly(); //---> 除非主动关闭server端
				session.closeNow();
				logger.info("session close");
			}
			if(null != connector) {
				connector.dispose();
				logger.info("connector close");
			}
		}
		return user;
		
		//System.out.println(sendMessageService.sendAndReceive(user));
		//sendMessageService.convertAndSend(user);
		
	}

}
