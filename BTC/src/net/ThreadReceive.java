package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import scn.Game;

import cls.Player;

public class ThreadReceive extends Thread {
	
	private ObjectInputStream inStream;
	
	public ThreadReceive(ObjectInputStream inStream) {
		this.inStream = inStream;
	}
	
	@Override
	public void run() {
		ArrayList<Player> players = null;

		//while (players == null) {
		try {
			players = (ArrayList<Player>) inStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//}

		if (players != null) {
			System.out.println("IN: " + players.toString());

			// Load the received players
			System.out.println("Loading data...");
			Game.getInstance().setPlayers(players);
			System.out.println("Data loaded.");
		}
	}
	
}
