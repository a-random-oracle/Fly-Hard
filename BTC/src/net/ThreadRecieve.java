package net;

import java.io.IOException;
import java.io.ObjectInputStream;

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
				multiPlayerGame.getPlayers().get(0).setScore(((Player) inStream.readObject()).getScore()); 
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}
}
