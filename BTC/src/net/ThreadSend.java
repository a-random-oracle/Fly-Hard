package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.MultiPlayerGame;

public class ThreadSend extends Thread {
	
	ObjectOutputStream outStream;
	MultiPlayerGame multiPlayerGame;

	public ThreadSend(MultiPlayerGame multiPlayerGame, ObjectOutputStream outStream) {
		this.outStream = outStream;
		this.multiPlayerGame = multiPlayerGame;
	}
	
	@Override
	public void run() {
		
	}
}
