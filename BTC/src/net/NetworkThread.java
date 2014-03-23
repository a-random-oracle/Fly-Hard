package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

public class NetworkThread extends Thread {
	
	/** The data still to be sent */
	private ArrayList<String> dataBuffer;
	
	/** The buffer mutex */
	private Object bufferMutex;
	
	/** The thread's status */
	private boolean status;
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkThread() {
		this.dataBuffer = new ArrayList<String>();
		this.bufferMutex = new Object();
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
	
	public void writeData(String data) {
		// Obtain a lock on the data buffer
		synchronized(bufferMutex) {
			// Write data to the buffer
			dataBuffer.add(data);
		}
	}
	
	private void sendNextData() {
		HashMap<String, String> headers = null;
		
		// Obtain a lock on the data buffer
		synchronized(bufferMutex) {
			if ((dataBuffer.size() == 0) || (dataBuffer.get(0) == null)) {
				// Nothing to write, so exit
				return;
			} else {
				// Get the standard headers, along with a new header
				// containing the data to send
				headers = setupHeaders(dataBuffer.get(0));
				dataBuffer.remove(0);
			}
		}
		
		ArrayList<String> response = null;
		try {
			// Send the post request to the server, and read the response
			response = NetworkManager.httpPost(NetworkManager.SERVER_URL
					+ NetworkManager.POST_EXT, headers);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Output the response XXX <- Debugging
		NetworkManager.print("");
		for (String string : response) {
			NetworkManager.print(string);
		}
	}
	
	public HashMap<String, String> setupHeaders(String data) {
		// Form the request headers
		HashMap<String, String> headers = new HashMap<String, String>();

		// Set user agent so that the server recognises this as a valid
		// request
		headers.put("user-agent", "Fly-Hard");

		// Add client's IP to the headers
		try {
			headers.put("ip", InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Add the data to the headers
		headers.put("data", data);
		
		// Return the headers
		return headers;
	}
	
	public void end() {
		this.status = false;
	}
	
}
