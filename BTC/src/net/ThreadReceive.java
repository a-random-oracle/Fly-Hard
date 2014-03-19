package net;

import java.io.IOException;
import java.io.ObjectInputStream;

import scn.Game;
import scn.MultiPlayerGame;

public class ThreadReceive extends Thread {
	
	ObjectInputStream inStream;
	MultiPlayerGame multiPlayerGame;
		
	public ThreadReceive(MultiPlayerGame multiPlayerGame, ObjectInputStream inStream) {
		this.inStream = inStream;
		this.multiPlayerGame = multiPlayerGame;
		start();
	}
	
	@Override
	public void run() {
		try {
			multiPlayerGame = (MultiPlayerGame) inStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Game getInstance() {
		return (Game) multiPlayerGame;
	}
}
