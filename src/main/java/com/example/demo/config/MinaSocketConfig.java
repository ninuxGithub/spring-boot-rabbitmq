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
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.core.MinaClientHandler;
import com.example.demo.core.MinaCodeFactory;
import com.example.demo.core.MinaServerHandler;

/**
 * mina配置
 */
@Configuration
@ConfigurationProperties(prefix = "mina")
public class MinaSocketConfig {
	private static final Logger logger = LoggerFactory.getLogger(MinaSocketConfig.class);

	private static int port;

	/** 30秒后超时 */
	private static int idelTimeout;
	/** 15秒发送一次心跳包 */
	private static int heartBeatRate;
	/** 心跳包内容 */
	private static String heartBeatRequest;
	
	private static String heartBeatResponse;

	// private Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new
	// HashMap<>();
	// @Bean
	// public CustomEditorConfigurer customEditorConfigurer() {
	// customEditors.put(SocketAddress.class, InetSocketAddressEditor.class);
	// CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
	// customEditorConfigurer.setCustomEditors(customEditors);
	// return customEditorConfigurer;
	// }

	
	/**
	 * 
	 * NioSocketAcceptor调用bind方法都做了些什么呢？
	 * 1.NioSocketAcceptor accept , open
	 * 		> 通过new NioSocketAcceptor() 的时候，（public final class NioSocketAcceptor extends AbstractPollingIoAcceptor） 父类的构造函数也会
	 * 		> 跟随着运行， 进而会调用AbstractPollingIoAcceptor 
	 * 		> 中的构造方法AbstractPollingIoAcceptor
	 * 		> -->init()--->open()
	 * 2.bind()方法是定义在接口IoAcceptor中的,
	 * 		bind-->AbstractPollingIoAcceptor.bindInternal()-->startupAcceptor()-->executeWorker()
	 * 
	 */
	@Bean(initMethod = "bind", destroyMethod = "unbind")
	public NioSocketAcceptor nioSocketAcceptor() {
		logger.info("[nioSocketAcceptor begin to init]");
		NioSocketAcceptor nioSocketAcceptor = new NioSocketAcceptor();
		nioSocketAcceptor.setDefaultLocalAddress(new InetSocketAddress(getPort()));
		nioSocketAcceptor.setReuseAddress(true);
		nioSocketAcceptor.setFilterChainBuilder(defaultIoFilterChainBuilder());
		nioSocketAcceptor.setHandler(receiveMinaHandler());
		// 设置读取数据的缓冲区大小
		 nioSocketAcceptor.getSessionConfig().setReadBufferSize(2048);
		// 读写通道30秒内无操作进入空闲状态
		 nioSocketAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, idelTimeout);

		/********************************* 加入心跳**************************************************/
		KeepAliveMessageFactory kamfi = new KeepAliveMessageFactory() {

			@Override
			public boolean isRequest(IoSession session, Object message) {
				//logger.info("请求心跳包信息: " + message);
				if (message.equals(heartBeatRequest))
					return true;
				return false;
			}

			@Override
			public boolean isResponse(IoSession session, Object message) {
				//logger.info("响应心跳包信息: " + message);
				if (message.equals(heartBeatResponse))
					return true;
				return false;
			}

			@Override
			public Object getRequest(IoSession session) {
				//logger.info("请求预设信息: " + heartBeatRequest);
				/** 返回预设语句 */
				return heartBeatRequest;
			}

			@Override
			public Object getResponse(IoSession session, Object request) {
				//logger.info("响应预设信息: " + heartBeatResponse);
				/** 返回预设语句 */
				return heartBeatResponse;
			}
		};


		KeepAliveFilter heartBeat = new KeepAliveFilter(kamfi, IdleStatus.BOTH_IDLE, new KeepAliveRequestTimeoutHandler() {

			@Override
			public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
				logger.info("KeepAliveRequestTimeoutHandler: [心跳超时了]");
			}
			
		});
		// 设置是否forward到下一个filter
		heartBeat.setForwardEvent(true);
		// 设置心跳频率
		heartBeat.setRequestInterval(heartBeatRate);
		nioSocketAcceptor.getFilterChain().addLast("heartbeat", heartBeat);
		/***************************************************************************************/

		return nioSocketAcceptor;
	}

	@Bean
	public DefaultIoFilterChainBuilder defaultIoFilterChainBuilder() {
		DefaultIoFilterChainBuilder defaultIoFilterChainBuilder = new DefaultIoFilterChainBuilder();
		Map<String, IoFilter> filters = new LinkedHashMap<>();
		filters.put("executor", executorFilter());
		filters.put("mdcInjectionFilter", mdcInjectionFilter());
		filters.put("codecFilter", protocolCodecFilter());
		//filters.put("loggingFilter", loggingFilter());
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
	public MinaServerHandler receiveMinaHandler() {
		return new MinaServerHandler();
	}
	@Bean
	public MinaClientHandler minaClientHandler() {
		return new MinaClientHandler();
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

	public static void setPort(int port) {
		MinaSocketConfig.port = port;
	}

	public static int getIdelTimeout() {
		return idelTimeout;
	}

	public static void setIdelTimeout(int idelTimeout) {
		MinaSocketConfig.idelTimeout = idelTimeout;
	}

	public static int getHeartBeatRate() {
		return heartBeatRate;
	}

	public static void setHeartBeatRate(int heartBeatRate) {
		MinaSocketConfig.heartBeatRate = heartBeatRate;
	}

	public static String getHeartBeatRequest() {
		return heartBeatRequest;
	}

	public static void setHeartBeatRequest(String heartBeatRequest) {
		MinaSocketConfig.heartBeatRequest = heartBeatRequest;
	}

	public static String getHeartBeatResponse() {
		return heartBeatResponse;
	}

	public static void setHeartBeatResponse(String heartBeatResponse) {
		MinaSocketConfig.heartBeatResponse = heartBeatResponse;
	}
	
	
}
