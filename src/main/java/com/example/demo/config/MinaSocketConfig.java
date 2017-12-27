package com.example.demo.config;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.core.MinaCodeFactory;
import com.example.demo.core.ReceiveMinaHandle;

/**
 * mina配置
 */
@Configuration
@ConfigurationProperties(prefix="mina")
public class MinaSocketConfig {

	private static int port;

	private static final Logger logger = LoggerFactory.getLogger(MinaSocketConfig.class);

//	private Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<>();
//	@Bean
//	public CustomEditorConfigurer customEditorConfigurer() {
//		customEditors.put(SocketAddress.class, InetSocketAddressEditor.class);
//		CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
//		customEditorConfigurer.setCustomEditors(customEditors);
//		return customEditorConfigurer;
//	}

	@Bean(initMethod = "bind", destroyMethod = "unbind")
	public NioSocketAcceptor nioSocketAcceptor() {
		NioSocketAcceptor nioSocketAcceptor = new NioSocketAcceptor();
		nioSocketAcceptor.setDefaultLocalAddress(new InetSocketAddress(getPort()));
		nioSocketAcceptor.setReuseAddress(true);
		nioSocketAcceptor.setFilterChainBuilder(defaultIoFilterChainBuilder());
		nioSocketAcceptor.setHandler(receiveMinaHandler());
		// 设置读取数据的缓冲区大小
		//nioSocketAcceptor.getSessionConfig().setReadBufferSize(2048);
        // 读写通道10秒内无操作进入空闲状态
		//nioSocketAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		
		//kaf.setKeepAliveRequestInterval(30); 
		
		/*********************************加入心跳**************************************************/
        /**检测每一个连接的IoSession的心跳包，定时进入Idle状态，用一下方法，以上备注不可行**/
        KeepAliveMessageFactory kamfi = new KeepAliveMessageFactory();
        KeepAliveFilter kaf = new KeepAliveFilter(kamfi);
        kaf.setRequestInterval(30);
        nioSocketAcceptor.getFilterChain().addLast("heart", kaf);
        /***************************************************************************************/
		
		return nioSocketAcceptor;
	}
	class KeepAliveMessageFactory implements org.apache.mina.filter.keepalive.KeepAliveMessageFactory{

		@Override
		public boolean isRequest(IoSession session, Object message) {
			return false;
		}

		@Override
		public boolean isResponse(IoSession session, Object message) {
			return false;
		}

		@Override
		public Object getRequest(IoSession session) {
			return null;
		}

		@Override
		public Object getResponse(IoSession session, Object request) {
			return null;
		}
		
	}
	

	@Bean
	public DefaultIoFilterChainBuilder defaultIoFilterChainBuilder() {
		DefaultIoFilterChainBuilder defaultIoFilterChainBuilder = new DefaultIoFilterChainBuilder();
		Map<String, IoFilter> filters = new LinkedHashMap<>();
		filters.put("executor", executorFilter());
		filters.put("mdcInjectionFilter", mdcInjectionFilter());
		filters.put("codecFilter", protocolCodecFilter());
		filters.put("loggingFilter", loggingFilter());
		defaultIoFilterChainBuilder.setFilters(filters);
		return defaultIoFilterChainBuilder;
	}

	@Bean
	public ExecutorFilter executorFilter() {
		return new ExecutorFilter();
	}

	@Bean
	public MdcInjectionFilter mdcInjectionFilter() {
		return new MdcInjectionFilter(MdcInjectionFilter.MdcKey.remoteAddress);
	}

	@Bean
	public ProtocolCodecFilter protocolCodecFilter() {
		return new ProtocolCodecFilter(minaCodeFactory());
	}
	
	@Bean
	public ReceiveMinaHandle receiveMinaHandler() {
		return new ReceiveMinaHandle();
	}
	
	@Bean
	public MinaCodeFactory minaCodeFactory() {
		return new MinaCodeFactory();
	}

	@Bean
	public LoggingFilter loggingFilter() {
		return new LoggingFilter();
	}

	public static int getPort() {
		return port;
	}

	@SuppressWarnings("static-access")
	public void setPort(int port) {
		this.port = port;
	}
}
