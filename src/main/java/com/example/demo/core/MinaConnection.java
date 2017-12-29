package com.example.demo.core;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.MinaSocketConfig;

@Component
@SuppressWarnings("static-access")
public class MinaConnection {
	private static final Logger logger = LoggerFactory.getLogger(MinaConnection.class);

	@Autowired
	private MinaSocketConfig minaSocketConfig;

	@Autowired
	private MinaClientHandler minaClientHandler;

	/**
	 * 通过mina发送消息    + 心跳
	 * 
	 * @param message
	 */
	public void minaSendMessage(Object message) {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(minaClientHandler);
		// 编写过滤器
		connector.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),
						LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));

		//connector.getFilterChain().addLast("logging", new LoggingFilter());
		KeepAliveMessageFactory kamfi = new KeepAliveMessageFactory() {

			@Override
			public boolean isRequest(IoSession session, Object message) {
				 //logger.info("请求心跳包信息: " + message);
				if (message.equals(minaSocketConfig.getHeartBeatRequest()))
					return true;
				return false;
			}

			@Override
			public boolean isResponse(IoSession session, Object message) {
				 //logger.info("响应心跳包信息: " + message);
				if (message.equals(minaSocketConfig.getHeartBeatResponse()))
					return true;
				return false;
			}

			@Override
			public Object getRequest(IoSession session) {
				/** 返回预设语句 */
				return minaSocketConfig.getHeartBeatRequest();
			}

			@Override
			public Object getResponse(IoSession session, Object request) {
				/** 返回预设语句 */
				return minaSocketConfig.getHeartBeatResponse();
			}
		};
		KeepAliveFilter heartBeat = new KeepAliveFilter(kamfi, IdleStatus.BOTH_IDLE,
				new KeepAliveRequestTimeoutHandler() {

					@Override
					public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
						logger.info("KeepAliveRequestTimeoutHandler: [心跳超时了]");
					}

				});
		// 设置是否forward到下一个filter
		heartBeat.setForwardEvent(true);
		// 设置心跳频率
		heartBeat.setRequestInterval(minaSocketConfig.getHeartBeatRate());
		connector.getFilterChain().addLast("heartbeat", heartBeat);
		// 创建连接
		IoSession session = null;
		try {
			ConnectFuture connect = connector.connect(new InetSocketAddress(minaSocketConfig.getPort()));
			// 等待连接创建完成
			connect.awaitUninterruptibly();
			// 获取session
			session = connect.getSession();
			try {
				WriteFuture write = session.write(message.toString());
				if (write.isDone()) {

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("客户端连接异常");
			e.printStackTrace();
		} finally {
			if (null != session) {
				// session.getCloseFuture().awaitUninterruptibly(); //---> 除非主动关闭server端
				session.closeNow();
			}
			if (null != connector) {
				connector.dispose();
			}
		}
	}

	public IoSession getMinaSession() {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new MinaServerHandler());
		/** 默认是一分钟 */
		// 设置连接超时时间 分钟
		// connector.setConnectTimeout(30000);
		// connector.setConnectTimeoutCheckInterval(30);

		// 添加过滤器和日志组件
		IoFilter filter = new ProtocolCodecFilter(new TextLineCodecFactory());
		connector.getFilterChain().addLast("codec", filter);
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
		} catch (Exception e) {
			logger.info("客户端连接异常");
			e.printStackTrace();
		}

		return session;
	}

	/*
	 * public void closeSession(IoSession session) { if(null != session) {
	 * session.getCloseFuture().awaitUninterruptibly(); connector.dispose(); } }
	 */

	public void connectionTest() {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new MinaServerHandler());
		// 设置连接超时时间 分钟
		connector.setConnectTimeoutMillis(1);
		// 添加过滤器和日志组件
		IoFilter filter = new ProtocolCodecFilter(new TextLineCodecFactory());
		connector.getFilterChain().addLast(" codec", filter);
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
		} catch (Exception e) {
			System.err.println("客户端连接异常");
		}
		session.getCloseFuture().awaitUninterruptibly();
		connector.dispose();

	}

}
