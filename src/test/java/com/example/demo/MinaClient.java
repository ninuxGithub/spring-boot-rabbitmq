package com.example.demo;

import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.example.demo.core.MinaServerHandler;

public class MinaClient {
	private static final String HOST = "127.0.0.1";
    private static final int PORT = 8888;
    
    @SuppressWarnings("deprecation")
	public static void main(String[] args) {
        IoConnector connector = new NioSocketConnector();
        connector.setHandler(new MinaServerHandler());
        // 设置连接超时时间 单位毫秒   
        connector.setConnectTimeout(30000);   
        //添加过滤器和日志组件
        IoFilter filter = new ProtocolCodecFilter(new TextLineCodecFactory());
        connector.getFilterChain().addLast(" codec", filter);
        connector.getFilterChain().addLast("logging", new LoggingFilter());
        // 创建连接   
        IoSession session = null;   
        try {   
            ConnectFuture connect = connector.connect(new InetSocketAddress(HOST, PORT));   
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
