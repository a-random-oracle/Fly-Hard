package net;

import java.util.ArrayList;

/**
 * Thread used to transfer data in parallel with the game.
 * <p>
 * This thread provides no guarantees regarding the time between
 * data being passed to the queue and data being sent.
 * </p>
 * <p>
 * However, it is guaranteed that if data is passed in the order
 * {a, b, c}, the data will be sent in that order.
 * </p>
 */
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
	
	/**
	 * Sends the next object in the data buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the sent data
	 * will be removed from the data buffer after being sent.
	 * </p>
	 */
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
		
		if (data != null) {
			// Send the post request to the server, and read the response
			Object receivedData = NetworkManager.postObject(data);
			
			// Write the response to the response buffer
			synchronized(responseBufferMutex) {
				responseBuffer.add(receivedData);
			}
		}
	}
	
	/**
	 * Writes data to the data buffer.
	 * @param data
	 * 			the object to write to the data buffer
	 */
	public void writeData(Object data) {
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			// Write data to the buffer
			dataBuffer.add(data);
		}
	}
	
	/**
	 * Reads the next response from the received buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the read data
	 * will be removed from the response buffer after being read.
	 * </p>
	 * @return the next object in the response buffer
	 */
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
	
	/**
	 * Reads all data from the response buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the read data
	 * will be removed from the response buffer after being read.
	 * </p>
	 * @return all the objects in the response buffer
	 */
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
	
	/**
	 * Stops the thread.
	 */
	public void end() {
		this.status = false;
	}
	
}
