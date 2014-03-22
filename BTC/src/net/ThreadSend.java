package net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import scn.Game;

public class ThreadSend extends Thread {
	
	/** The output stream to use to send data */
	private ObjectOutputStream outStream;
	
	/** Whether to send individual players, or the full array */
	private boolean sendPlayerArray;
	
	/** Whether to output data to the standard output */
	private boolean verbose;

	
	/**
	 * Constructs a new thread for sending player data.
	 * <p>
	 * Status and connection information will <b>not</b>
	 * be written to the standard output.
	 * </p>
	 * @param outStream
	 * 			the output stream to write data to
	 * @param sendFullArray
	 * 			<code>true</code> if the sending the full player array
	 * 			(rather than a single player)
	 */
	public ThreadSend(ObjectOutputStream outStream,
			boolean sendFullArray) {
		this.outStream = outStream;
		this.sendPlayerArray = sendFullArray;
		this.verbose = false;
	}
	
	/**
	 * Constructs a new thread for sending player data.
	 * @param outStream
	 * 			the output stream to write data to
	 * @param sendFullArray
	 * 			<code>true</code> if the sending the full player array
	 * 			(rather than a single player)
	 * @param verbose
	 * 			<code>true</code> indicates that the thread
	 * 			should output status and connection information to
	 * 			the standard output
	 */
	public ThreadSend(ObjectOutputStream outStream,
			boolean sendFullArray, boolean verbose) {
		this.outStream = outStream;
		this.sendPlayerArray = sendFullArray;
		this.verbose = verbose;
	}
	
	/**
	 * Sends the data.
	 */
	@Override
	public void run() {
		if (sendPlayerArray) {
			try {
				//outStream.reset();
				outStream.writeObject(Game.getInstance().getPlayers());
				print("SENDING : " + Game.getInstance().getPlayers().toString());
				//outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				//outStream.reset();
				outStream.writeObject(Game.getInstance().getCurrentPlayer());
				print("SENDING: " + Game.getInstance().getCurrentPlayer());
				//outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
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
