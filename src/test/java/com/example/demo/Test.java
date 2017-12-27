/*package com.example.demo;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.example.demo.core.BossMinaHandle;

public class Test {

	private static final int PORT = 8888;

	public static void main(String[] args) throws IOException {
		// 构造接收器
		IoAcceptor acceptor = new NioSocketAcceptor();
		BossMinaHandle handler = new BossMinaHandle();
		acceptor.setHandler(handler);
		// 读写通道10秒内无操作进入空闲状态
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		// 添加过滤器和日志组件
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory()));
		acceptor.getFilterChain().addLast("logging", new LoggingFilter());
		// 启动服务
		acceptor.bind(new InetSocketAddress(PORT));
		System.out.println("MinaServer started on port " + PORT);
	}

}
*/