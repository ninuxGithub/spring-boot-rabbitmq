package com.example.demo.bean;

public class BinaryObserver extends Observer {

	public BinaryObserver(Subject subject) {
		this.subject = subject;
		this.subject.attach(this);
	}

	@Override
	public void update() {
		System.out.println(this.getClass().getName() + " " + Integer.toBinaryString(subject.getStatus()));
	}

}
