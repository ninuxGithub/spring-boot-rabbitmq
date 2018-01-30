package com.example.demo.bean;

public class OctalObserver extends Observer {

	public OctalObserver(Subject subject) {
		this.subject = subject;
		this.subject.attach(this);
	}

	@Override
	public void update() {
		System.out.println(this.getClass().getName() + " " + Integer.toOctalString(subject.getStatus()));
	}

}
