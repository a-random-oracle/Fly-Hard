package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import scn.Game;

import cls.Player;

public class ThreadReceive extends Thread {
	
	private ObjectInputStream inStream;
	
	private boolean receivePlayerArray;
	
	private int playerID;
	
	public ThreadReceive(ObjectInputStream inStream) {
		this.inStream = inStream;
		this.receivePlayerArray = true;
	}
	
	public ThreadReceive(ObjectInputStream inStream, int playerID) {
		this.inStream = inStream;
		this.receivePlayerArray = false;
		this.playerID = playerID;
	}
	
	@Override
	public void run() {
		if (receivePlayerArray) {
			ArrayList<Player> players = null;

			try {
				players = (ArrayList<Player>) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (players != null) {
				System.out.println("IN: " + players.toString());

				// Load the received players
				System.out.println("Loading data...");
				Game.getInstance().setPlayers(players);
				System.out.println("Data loaded.");
			}
		} else {
			Player player = null;

			try {
				player = (Player) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (player != null) {
				System.out.println("IN: " + player.toString());

				// Load the received players
				System.out.println("Loading data...");
				Game.getInstance().setPlayer((playerID + 1) % 2, player);
				System.out.println("Data loaded.");
			}
		}
	}
	
}
