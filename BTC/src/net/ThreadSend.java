package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.Game;

public class ThreadSend extends Thread {
	
	private ObjectOutputStream outStream;
	
	private boolean sendPlayerArray;
	
	private int playerID;
	
	public ThreadSend(ObjectOutputStream outStream) {
		this.outStream = outStream;
		this.sendPlayerArray = true;
	}

	public ThreadSend(ObjectOutputStream outStream, int playerID) {
		this.outStream = outStream;
		this.sendPlayerArray = false;
		this.playerID = playerID;
	}
	
	@Override
	public void run() {
		if (sendPlayerArray) {
			try {
				outStream.writeObject(Game.getInstance().getPlayers());
				System.out.println("OUT: " + Game.getInstance().getPlayers().toString());
				outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				outStream.writeObject(Game.getInstance().getPlayers().get(playerID));
				System.out.println("OUT: " + Game.getInstance().getPlayers().get(playerID).toString());
				outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
