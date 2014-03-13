package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import cls.Aircraft;


public class ThreadSend extends Thread {
	
	//no longer sending an instance of demo
	/*
	 * for now
	 * send aircraft
	 * 
	 * possibly...:
	 */
	
	ObjectOutputStream outStream;
	Aircraft aircraft;

	public ThreadSend(Aircraft aircraft, ObjectOutputStream outStream) {
		this.aircraft = aircraft;
		this.outStream = outStream;
	}
	
	@Override
	public void run() {
		
	}
}
