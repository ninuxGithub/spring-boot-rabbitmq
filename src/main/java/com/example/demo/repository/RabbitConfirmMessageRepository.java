package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.bean.RabbitConfirmMessage;

@Repository
public interface RabbitConfirmMessageRepository extends JpaRepository<RabbitConfirmMessage, String> {
	
	
	public RabbitConfirmMessage findByCorrelationData(String correlationData);

}
