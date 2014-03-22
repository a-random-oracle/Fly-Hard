package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import scn.Game;

import cls.Player;

public class ThreadReceive extends Thread {
	
	/** The input stream to use to receive data */
	private ObjectInputStream inStream;
	
	/** Whether to receive individual players, or the full array */
	private boolean receivePlayerArray;
	
	/** Whether to output data to the standard output */
	private boolean verbose;
	
	
	/**
	 * Constructs a new thread for receiving player data.
	 * <p>
	 * Status and connection information will <b>not</b>
	 * be written to the standard output.
	 * </p>
	 * @param inStream
	 * 			the input stream to receive data from
	 * @param receiveFullArray
	 * 			<code>true</code> if the data on {@link #inStream} is going
	 * 			to be an array of players (rather than a single player)
	 */
	public ThreadReceive(ObjectInputStream inStream,
			boolean receiveFullArray) {
		this.inStream = inStream;
		this.receivePlayerArray = receiveFullArray;
		this.verbose = false;
	}
	
	/**
	 * Constructs a new thread for receiving player data.
	 * @param inStream
	 * 			the input stream to receive data from
	 * @param receiveFullArray
	 * 			<code>true</code> if the data on {@link #inStream} is going
	 * 			to be an array of players (rather than a single player)
	 * @param verbose
	 * 			<code>true</code> indicates that the thread
	 * 			should output status and connection information to
	 * 			the standard output
	 */
	public ThreadReceive(ObjectInputStream inStream,
			boolean receiveFullArray, boolean verbose) {
		this.inStream = inStream;
		this.receivePlayerArray = receiveFullArray;
		this.verbose = false;
	}
	
	/**
	 * Receives the data, and loads it into the game.
	 */
	@Override
	public void run() {
		if (receivePlayerArray) {
			try {
				// There's no way for the compiler to check that the ArrayList
				// received is of type Player.
				// Because the application is designed to ONLY send ArrayLists
				// of Players, in this instance this can be safely ignored.
				@SuppressWarnings("unchecked") 
				ArrayList<Player> players = (ArrayList<Player>) inStream.readObject();
				
				if (players != null) {
					print("RECEIVED: " + players.toString());

					// Load the received players
					print("Loading data...");
					Game.getInstance().setPlayers(players);
					print("Data loaded.");
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
				print("RECEIVED: " + player.toString());

				// Load the received players
				print("Loading data...");
				Game.getInstance().setPlayer((Game.getInstance()
						.getCurrentPlayer().getID() + 1) % 2, player);
				print("Data loaded.");
			}
		}
	}
	
	/**
	 * Prints strings to the standard output.
	 * <p>
	 * If {@link #verbose} is set to <code>true</code>, this will
	 * function in the same was as {@link System.out#println()}.
	 * </p>
	 * <p>
	 * Otherwise this will do nothing.
	 * </p>
	 * @param string
	 * 			the string to output
	 */
	private void print(String string) {
		if (verbose) System.out.println(string);
	}
	
}
