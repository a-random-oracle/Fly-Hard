package net;

import java.io.IOException;
import java.io.ObjectInputStream;

import scn.MultiPlayerGame;

public class ThreadRecieve extends Thread {
	
	ObjectInputStream inStream;
	MultiPlayerGame multiPlayerGame;
		
	public ThreadRecieve(MultiPlayerGame multiPlayerGame, ObjectInputStream inStream) {
		this.inStream = inStream;
		this.multiPlayerGame = multiPlayerGame;
		start();
		System.out.println(multiPlayerGame.getPlayers().get(0).getScore());
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				multiPlayerGame.getPlayers().get(0).setScore((
						(MultiPlayerGame) inStream.readObject()).getPlayers().get(0).getScore());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
