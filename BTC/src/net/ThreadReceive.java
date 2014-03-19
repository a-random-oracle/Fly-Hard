package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import cls.Player;

public class ThreadReceive extends Thread {
	
	private ObjectInputStream inStream;
	private ArrayList<Player> players;
		
	public ThreadReceive(ObjectInputStream inStream) {
		this.inStream = inStream;
	}
	
	@Override
	public void run() {
		while (players == null) {
			try {
				players = (ArrayList<Player>) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<Player> getPlayers() {
		return players;
	}
}
