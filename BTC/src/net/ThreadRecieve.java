package net;

import java.io.IOException;
import java.io.ObjectInputStream;

import cls.Aircraft;


public class ThreadRecieve extends Thread {
	
	ObjectInputStream inStream;
	Aircraft aircraft;
		
	public ThreadRecieve(Aircraft aircraft, ObjectInputStream inStream) {
		this.aircraft = aircraft;
		this.inStream = inStream;
	}
	
	@Override
	public void run() {
		
	}
}
