package net;

import java.io.IOException;
import java.io.ObjectInputStream;

import scn.Game;
import scn.MultiPlayerGame;
import cls.Player;

public class ThreadRecieve extends Thread {
	
	ObjectInputStream inStream;
	MultiPlayerGame multiPlayerGame;
		
	public ThreadRecieve(MultiPlayerGame multiPlayerGame, ObjectInputStream inStream) {
		this.inStream = inStream;
		this.multiPlayerGame = multiPlayerGame;
		start();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Game.getPlayers().get(0).setScore(((MultiPlayerGame) inStream.readObject()).getPlayers().get(0)); 
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}
}
