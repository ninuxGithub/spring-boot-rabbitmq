package com.example.demo.bean;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "rabbit_confirm_message")
public class RabbitConfirmMessage implements Serializable {

	private static final long serialVersionUID = -4404807934631610183L;

	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid") // 这个是hibernate的注解
	@GeneratedValue(generator = "idGenerator") // 使用uuid的生成策略
	private String id;

	private String correlationData;

	private boolean ack;

	private String cause;

	public RabbitConfirmMessage() {
	}

	public RabbitConfirmMessage(String correlationData, boolean ack, String cause) {
		this.correlationData = correlationData;
		this.ack = ack;
		this.cause = cause;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCorrelationData() {
		return correlationData;
	}

	public void setCorrelationData(String correlationData) {
		this.correlationData = correlationData;
	}

	public boolean getAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

}
