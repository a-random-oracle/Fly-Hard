package net;

import java.io.IOException;
import java.io.ObjectInputStream;

import scn.Demo;

public class ThreadRecieve extends Thread {
	
	ObjectInputStream in;
	Demo demo;
		
	public ThreadRecieve(Demo d, ObjectInputStream inStream) {
		demo = d;
		in = inStream;
	}
	
	@Override
	public void run() {
		
	}
}
