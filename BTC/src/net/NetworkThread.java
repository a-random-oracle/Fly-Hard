package net;

import java.util.ArrayList;

import cls.Player;

public class NetworkThread extends Thread {

	/** The data still to be sent */
	private ArrayList<Object> dataBuffer;
	
	/** The data still to be read */
	private ArrayList<Object> responseBuffer;
	
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
		this.dataBuffer = new ArrayList<Object>();
		this.responseBuffer = new ArrayList<Object>();
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
		Object data = null;
		
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			if ((dataBuffer.size() == 0) || (dataBuffer.get(0) == null)) {
				// Nothing to write, so exit
				return;
			} else {
				// Get the standard headers, along with a new header
				// containing the data to send
				data = dataBuffer.get(0);
				dataBuffer.remove(0);
			}
		}
		
		// Send the post request to the server, and read the response
		if (data != null) {
			Object receivedData = NetworkManager.postObject(data);

			if (receivedData != null && receivedData instanceof Player) {
				System.out.println(((Player) receivedData).getName());
			} else {
				try {
					System.out.println("Not a player: " + receivedData.getClass().toString());
				} catch (Exception e) {
					//
				}
				
				System.out.println("Second try: " + (Player.class.cast(receivedData)));
			}
			
			// Write the response to the response buffer
			synchronized(responseBufferMutex) {
				responseBuffer.add(receivedData);
			}
		}
	}
	
	public void writeData(Object data) {
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			// Write data to the buffer
			dataBuffer.add(data);
		}
	}
	
	public Object readResponse() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				Object response = responseBuffer.get(0);
				responseBuffer.remove(0);
				return response;
			}
		}
	}
	
	public ArrayList<Object> readAllResponses() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				ArrayList<Object> allResponses = responseBuffer;
				responseBuffer.clear();
				return allResponses;
			}
		}
	}
	
	public void end() {
		this.status = false;
	}
	
}
