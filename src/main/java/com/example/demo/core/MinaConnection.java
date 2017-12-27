package com.example.demo.core;

import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.MinaSocketConfig;

@Component
public class MinaConnection {
	private static final Logger logger = LoggerFactory.getLogger(MinaConnection.class);

	@Autowired
	MinaSocketConfig minaSocketConfig;

	public IoSession getMinaSession() {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new ReceiveMinaHandle());
		// 设置连接超时时间 分钟
		connector.setConnectTimeout(30000);
		// 添加过滤器和日志组件
		IoFilter filter = new ProtocolCodecFilter(new TextLineCodecFactory());
		connector.getFilterChain().addLast("codec", filter);
		connector.getFilterChain().addLast("logging", new LoggingFilter());
		// 创建连接
		IoSession session = null;
		try {
			System.out.println(minaSocketConfig.getPort());
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
	
	/*public void closeSession(IoSession session) {
		if(null != session) {
			session.getCloseFuture().awaitUninterruptibly();
			connector.dispose();
		}
	}*/

	public void connectionTest() {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new ReceiveMinaHandle());
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
