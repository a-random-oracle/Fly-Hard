package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.Game;

public class ThreadSend extends Thread {
	
	private ObjectOutputStream outStream;

	public ThreadSend(ObjectOutputStream outStream) {
		this.outStream = outStream;
	}
	
	@Override
	public void run() {
		try {
			outStream.writeObject(Game.getInstance().getPlayers());
			System.out.println("OUT: " + Game.getInstance().getPlayers()
					.toString());
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
