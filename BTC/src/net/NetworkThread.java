package net;

import java.util.ArrayList;
import java.util.HashMap;

import cls.Player;

public class NetworkThread extends Thread {

	/** The data still to be sent */
	private ArrayList<Player> dataBuffer;
	
	/** The data still to be read */
	private ArrayList<Player> responseBuffer;
	
	/** The data buffer mutex */
	private Object dataBufferMutex;
	
	/** The response buffer mutex */
	private Object responseBufferMutex;
	
	/** The thread's status */
	private boolean status;
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkThread() {
		this.dataBuffer = new ArrayList<Player>();
		this.responseBuffer = new ArrayList<Player>();
		this.dataBufferMutex = new Object();
		this.responseBufferMutex = new Object();
		this.status = true;
	}
	
	/**
	 * Sends data in the data buffer.
	 */
	@Override
	public void run() {
		while (status) {
			sendNextData();
		}
	}
	
	private void sendNextData() {
		HashMap<String, String> headers = null;
		
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			if ((dataBuffer.size() == 0) || (dataBuffer.get(0) == null)) {
				// Nothing to write, so exit
				return;
			} else {
				// Get the standard headers, along with a new header
				// containing the data to send
				headers = NetworkManager.setupHeaders(dataBuffer.get(0));
				dataBuffer.remove(0);
			}
		}
		
		// Send the post request to the server, and read the response
		Player receivedplayer = NetworkManager.httpPostPlayer(headers);

		// Write the response to the response buffer
		synchronized(responseBufferMutex) {
			responseBuffer.add(receivedplayer);
			
			NetworkManager.print("<RECEIVED>");
			NetworkManager.print("        " + receivedplayer.toString());
			NetworkManager.print("</RECEIVED>");
		}
	}
	
	public void writeData(Player data) {
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			// Write data to the buffer
			dataBuffer.add(data);
		}
	}
	
	public Player readResponse() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				Player response = responseBuffer.get(0);
				responseBuffer.remove(0);
				return response;
			}
		}
	}
	
	public ArrayList<Player> readAllResponses() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				ArrayList<Player> allResponses = responseBuffer;
				responseBuffer.clear();
				return allResponses;
			}
		}
	}
	
	public void end() {
		this.status = false;
	}
	
}
