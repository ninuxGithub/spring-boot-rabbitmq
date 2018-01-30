package com.example.demo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 被观察的对象
 */
public class Subject implements Serializable {

	private static final long serialVersionUID = 4709799612001747408L;

	private int status;

	private List<Observer> ovbservers = new ArrayList<>();

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
		notifyAllObservers();

	}

	public void attach(Observer observer) {
		this.ovbservers.add(observer);
	}

	public void notifyAllObservers() {
		for (Observer observer : this.ovbservers) {
			observer.update();
		}
	}

}
