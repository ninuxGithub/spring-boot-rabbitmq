package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class UserEntityService {
	
	@Autowired
	private UserRepository userRepository;
	
	public void saveUserEntity(UserEntity userEntity) {
		userRepository.save(userEntity);		
		throw new RuntimeException("运行时异常");
	
	}
	
	public List<UserEntity> findAllUserEntity(){
		return userRepository.findAll();
	}

}
