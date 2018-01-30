package com.example.demo.bean;

public class TestObserver {
	
	public static void main(String[] args) {
		Subject subject = new Subject();
		
		new BinaryObserver(subject);
		new OctalObserver(subject);
		
		subject.setStatus(15);
		
		subject.setStatus(10);
	}

}
