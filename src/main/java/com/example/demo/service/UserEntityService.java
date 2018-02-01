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



//事物成功总结
//1、内外都无try Catch的时候，外部异常，全部回滚。
//2、内外都无try Catch的时候，内部异常，全部回滚。
//3、外部有try Catch时候，内部异常，全部回滚
//4、内部有try Catch，外部异常，全部回滚
//5、友情提示：外层方法中调取其他接口，或者另外开启线程的操作，一定放到最后！！！(因为调取接口不能回滚，一定要最后来处理)
//
//总结：由于上面的异常被捕获导致，很多事务回滚失败。如果一定要将捕获，请捕获后又抛出RuntimeException（默认为异常捕获RuntimeException）。



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

@Service
@Transactional
public class UserEntityService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderService orderService;
	
	public void saveUserEntity(UserEntity userEntity) {
		userRepository.save(userEntity);	
		Order order = new Order();
		order.setPrice(100d);
		orderService.saveOrder(order);
		int a=1;
		if(a == 1) {
			throw new RuntimeException("运行时异常");
		}
	
	}
	
	public List<UserEntity> findAllUserEntity(){
		return userRepository.findAll();
	}

}
