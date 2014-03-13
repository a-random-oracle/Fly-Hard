package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.Demo;

public class ThreadSend extends Thread {
	
	ObjectOutputStream out;
	Demo demo;

	public ThreadSend(Demo d, ObjectOutputStream outStream) {
		demo = d;
		out = outStream;
	}
	
	@Override
	public void run() {
		
	}
}
