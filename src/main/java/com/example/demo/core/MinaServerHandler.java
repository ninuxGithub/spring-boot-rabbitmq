package com.example.demo.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * socket接收处理类
 */
public class MinaServerHandler extends IoHandlerAdapter {
	private static Logger logger = LoggerFactory.getLogger(MinaServerHandler.class);
	
	
	ThreadLocal<DateTimeFormatter> timeFormatter = new ThreadLocal<DateTimeFormatter>() {

		@Override
		protected DateTimeFormatter initialValue() {
			return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		}
		
	};

	public MinaServerHandler() {
		logger.info("ReceiveMinaHandle init");
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		try {
			logger.info(String.format("ReceiveMinaHandle 接收到消息 : %s", message.toString()));
			session.write(timeFormatter.get().format(LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("session :" + session.getId());
		super.sessionClosed(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.info(String.format("sessionIdle session [%s] ,status [%s]", session.getId(), status.toString()));
		super.sessionIdle(session, status);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		//super.exceptionCaught(session, cause);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
		try {
			logger.info(String.format("发送的消息是: %s", message.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}

}
