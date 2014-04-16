package net;

import java.io.Serializable;
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
	private ArrayList<Serializable> dataBuffer;
	
	/** The messages still to be sent */
	private String messages;
	
	/** The data still to be read */
	private ArrayList<Serializable> responseBuffer;
	
	/** The data buffer mutex */
	private Object dataBufferMutex;
	
	/** The message string mutex */
	private Object messageStringMutex;
	
	/** The response buffer mutex */
	private Object responseBufferMutex;
	
	/** The thread's status */
	private boolean status;
	
	/** The status mutex */
	private Object statusMutex;
	
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkThread() {
		this.dataBuffer = new ArrayList<Serializable>();
		this.messages = "";
		this.responseBuffer = new ArrayList<Serializable>();
		this.dataBufferMutex = new Object();
		this.messageStringMutex = new Object();
		this.responseBufferMutex = new Object();
		this.status = true;
		this.statusMutex = new Object();
	}
	
	
	/**
	 * Sends data in the data buffer.
	 */
	@Override
	public void run() {
		while (true) {
			sendNextData();
			sendMessages();
			
			synchronized (statusMutex) {
				if (!status) break;
			}
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
		Serializable data = null;
		
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			if ((dataBuffer.size() == 0) || (dataBuffer.get(0) == null)) {
				// Nothing to send, so exit
				return;
			} else {
				// Get the next data element, and remove it from the
				// data buffer
				data = dataBuffer.get(0);
				dataBuffer.remove(0);
			}
		}
		
		// Send the post request to the server, and read the response
		Serializable receivedData = NetworkManager.postObject(data);

		// Write the response to the response buffer
		synchronized(responseBufferMutex) {
			responseBuffer.add(receivedData);
		}
	}
	
	/**
	 * Sends the messages in the message string.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the sent messages
	 * will be removed from the message string after being sent.
	 * </p>
	 */
	private void sendMessages() {
		String messageString = null;
		
		// Obtain a lock on the message string
		synchronized(messageStringMutex) {
			if (messages.length() == 0) {
				// Nothing to send, so exit
				return;
			} else {
				// Get (and clear) the message string
				messageString = messages;
				messages = "";
			}
		}
		// Send the post request to the server, and read the response
		String receivedMessages = NetworkManager
				.postMessage(messageString);
			
		// Handle the received message(s)
		InstructionHandler.handleInstruction(receivedMessages);
	}
	
	/**
	 * Writes data to the data buffer.
	 * @param data
	 * 			the data to write to the data buffer
	 */
	public void writeData(Serializable data) {
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			if (data != null) {
				// Write the data to the data buffer
				dataBuffer.add(data);
			}
		}
	}
	
	/**
	 * Writes a message to the message string.
	 * @param message
	 * 			the message to write to the message string
	 */
	public void writeMessage(String message) {
		// Obtain a lock on the message string
		synchronized(messageStringMutex) {
			if (message != null && !message.equals("")) {
				// Write the message to the message string
				messages += message;
			}
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
	public Serializable readResponse() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				Serializable response = responseBuffer.get(0);
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
	public ArrayList<Serializable> readAllResponses() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				ArrayList<Serializable> allResponses =
						new ArrayList<Serializable>(responseBuffer);
				responseBuffer.clear();
				return allResponses;
			}
		}
	}
	
	/**
	 * Stops the thread.
	 */
	public void end() {
		synchronized (statusMutex) {
			status = false;
		}
	}
	
}
