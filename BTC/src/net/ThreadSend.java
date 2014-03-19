package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.Game;

public class ThreadSend extends Thread {
	
	ObjectOutputStream outStream;

	public ThreadSend(ObjectOutputStream outStream) {
		this.outStream = outStream;
	}
	
	@Override
	public void run() {
		try {
			outStream.writeObject(Game.getInstance().getPlayers());
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
