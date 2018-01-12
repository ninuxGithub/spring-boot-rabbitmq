

使用rabbitmq

使用mina:启动项目后，采用telnet 127.0.0.1 8888 链接的server


	  
##NioSocketAcceptor调用bind方法都做了些什么呢？	  
	  1.NioSocketAcceptor accept , open
	  		> 通过new NioSocketAcceptor() 的时候，（public final class NioSocketAcceptor extends AbstractPollingIoAcceptor） 父类的构造函数也会
	  		> 跟随着运行， 进而会调用AbstractPollingIoAcceptor 
	  		> 中的构造方法AbstractPollingIoAcceptor
	  		> -->init()--->open()
	  2.bind()方法是定义在接口IoAcceptor中的,
	  		bind-->AbstractPollingIoAcceptor.bindInternal()-->startupAcceptor()-->executeWorker()
	  		
	  		
	  		
访问地址：http://localhost:8080/sendMsg	  		
	  
	 

