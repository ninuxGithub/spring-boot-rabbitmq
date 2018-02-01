package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.bean.Order;
import com.example.demo.bean.UserEntity;
import com.example.demo.repository.UserRepository;

/**
 * 
 * 如果不开启事物，即使是有异常发生，也会保存到数据库
 */
//@Transactional(noRollbackFor=RuntimeException.class)  
//抛出unchecked异常，触发事物，noRollbackFor=RuntimeException.class,不回滚  

 
//事物传播行为是PROPAGATION_NOT_SUPPORTED，以非事务方式运行，不会存入数据库  
//@Transactional(propagation=Propagation.NOT_SUPPORTED) 

//@Transactional 可以作用于接口、接口方法、类以及类方法上。当作用于类上时，该类的所有 public 方法将都具有该类型的事务属性，同时，我们也可以在方法级别使用该标注来覆盖类级别的定义。
//虽然 @Transactional 注解可以作用于接口、接口方法、类以及类方法上，但是 Spring 建议不要在接口或者接口方法上使用该注解，因为这只有在使用基于接口的代理时它才会生效。
//另外， @Transactional 注解应该只被应用到 public 方法上，这是由 Spring AOP 的本质决定的。如果你在 protected、private 或者默认可见性的方法上使用 @Transactional 注解，
//这将被忽略，也不会抛出任何异常。



/**
 * 无论调用的时候有没有try  --- 如果内部service 采用  propagation = Propagation.REQUIRED  外部抛出异常，都回滚
 * 
 * 无论调用的时候有没有try  --- 如果内部service 采用  propagation = Propagation.REQUIRES_NEW  外层回滚，内层成功提交
 * 					 ---1.如果内存抛异常，那么内外都会回滚
 * 					 ---2.如果内存抛异常，那么内部保存，外部回滚
 * 
 * 无论调用的时候有没有try  --- 如果内部service 采用  propagation = Propagation.NOT_SUPPORTED  外层回滚，内层成功提交
 * 					 ---1.如果内存抛异常，那么内外都会回滚
 * 					 ---2.如果内存抛异常，那么内部保存，外部回滚
 * 
 * 	
 * 
 * 
 * @author shenzm
 *
 */
//PROPAGATION_REQUIRES_NEW: 会将现有的事物挂起，开启一个全新的事物，来完成事物的提交，不依赖外部的事物， 所以在内部记录提交到数据库的时候可能都没有执行到外部事物的相关的数据操作（没有保存数据）


//PROPAGATION_NESTED: 内部事物对外部事物有依赖性，是外部事物的子事物， 不管内部事物有没有成功的提交，都会等待外部事物完成然后在一起提交或者回滚



//由此可见, PROPAGATION_REQUIRES_NEW 和 PROPAGATION_NESTED 的最大区别在于, PROPAGATION_REQUIRES_NEW 完全是一个新的事务, 
//而 PROPAGATION_NESTED 则是外部事务的子事务, 如果外部事务 commit, 嵌套事务也会被 commit, 这个规则同样适用于 roll back. 
//savePoint 概念
//这种方式也是潜套事务最有价值的地方, 它起到了分支执行的效果, 如果 ServiceB.methodB 失败, 那么执行 ServiceC.methodC(), 
//而 ServiceB.methodB 已经回滚到它执行之前的 SavePoint, 所以不会产生脏数据(相当于此方法从未执行过), 这种特性可以用在某些特殊的业务中, 
//而 PROPAGATION_REQUIRED 和 PROPAGATION_REQUIRES_NEW 都没有办法做到这一点. 





@Service
@Transactional
public class UserEntityService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderService orderService;
	
	public void saveUserEntity(UserEntity userEntity) {
//		int a=1;
//		if(a == 1) {
//			throw new RuntimeException("运行时异常");
//		}
		try {
			Order order = new Order();
			order.setPrice(100d);
			orderService.saveOrder(order);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.out.println("如果orderService保存失败会回到savepoint");
			System.out.println("do other service!!! ");
		}finally {
			userRepository.save(userEntity);	
			System.out.println("run over");
		}
		
	
	}
	
	public List<UserEntity> findAllUserEntity(){
		return userRepository.findAll();
	}

}
