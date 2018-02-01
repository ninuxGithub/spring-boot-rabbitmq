package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.bean.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	
	public List<Order> findAll();
	
	
	public void delete(Long id);
	

}
