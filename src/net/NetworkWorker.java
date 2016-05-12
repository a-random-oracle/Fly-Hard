package net;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Worker used to transfer data in parallel with the game.
 * <p>
 * The worker provides no guarantees regarding the time between
 * data being passed to the queue and data being sent.
 * </p>
 * <p>
 * There is also no guarantee that data written to the worker
 * will ever get sent. This is due to an effort to only send the
 * most up-to-date data.
 * </p>
 * <p>
 * A priority buffer is provided to ensure that specific data will be sent.
 * </p>
 */
public class NetworkWorker implements Runnable {

	/** The data still to be sent */
	private TreeMap<Long, Serializable> dataBuffer;
	
	/** The priority data still to be sent */
	private LinkedList<Serializable> priorityDataBuffer;
	
	/** The data still to be read */
	private TreeMap<Long, Serializable> responseBuffer;
	
	/** The priority data still to be read */
	private LinkedList<Serializable> priorityResponseBuffer;
	
	/** The most recent data received so far */
	private long mostRecent;
	
	/** The thread's status */
	private boolean status;
	
	/** The status mutex */
	private Object statusMutex;
	
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkWorker() {
		this.dataBuffer = new TreeMap<Long, Serializable>();
		this.priorityDataBuffer = new LinkedList<Serializable>();
		this.responseBuffer = new TreeMap<Long, Serializable>();
		this.priorityResponseBuffer = new LinkedList<Serializable>();
		this.mostRecent = 0;
		this.status = true;
		this.statusMutex = new Object();
	}
	
	
	/**
	 * Sends data and messages.
	 */
	@Override
	public void run() {
		// Repeat while the worker is running
		while (getStatus()) {
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
		TreeMap<Long, Serializable> transientMap =
				new TreeMap<Long, Serializable>();
		Entry<Long, Serializable> dataEntry = null;
		
		// Obtain a lock on the priority data buffer
		synchronized (priorityDataBuffer) {
			if (priorityDataBuffer != null
					&& priorityDataBuffer.size() > 0) {
				transientMap.put(-1L, priorityDataBuffer.removeFirst());
				dataEntry = (transientMap.firstEntry());
			}
		}
		
		// If there was no priority data
		if (dataEntry == null) {
			// Obtain a lock on the data buffer
			synchronized (dataBuffer) {
				if ((dataBuffer.size() != 0)
						&& (dataBuffer.lastEntry() != null)) {
					// Get the next data element
					dataEntry = dataBuffer.lastEntry();

					// Clear the data buffer
					dataBuffer.clear();
				}
			}
		}

		// Send the post request to the server and read the response
		Entry<Long, byte[]> receivedData =
				NetworkManager.postObject(dataEntry);

		// If the entry's key equals -1, add it to the priority response
		// buffer
		if (receivedData != null && receivedData.getKey() == -1) {
			// Obtain a lock on the priority response buffer
			synchronized(priorityResponseBuffer) {
				priorityResponseBuffer.add(NetworkManager
						.deserialiseData(receivedData.getValue()));
			}
		}

		// Write the response to the response buffer
		if (receivedData != null) {
			// Obtain a lock on the response buffer
			synchronized(responseBuffer) {
				Serializable deserialisedData = NetworkManager
						.deserialiseData(receivedData.getValue());

				responseBuffer.put(receivedData.getKey(), deserialisedData);
			}
		}
	}
	
	/**
	 * Writes data to the data buffer.
	 * <p>
	 * A {@link #timeValid} value of -1 will cause the data to be treated
	 * as priority data.
	 * </p>
	 * @param timeValid - the time at which the data was valid
	 * @param data - the data to write to the data buffer
	 */
	public void writeData(long timeValid, Serializable data) {
		// Check if data is priority data
		if (timeValid == -1) {
			// Obtain a lock on the priority data buffer
			synchronized(priorityDataBuffer) {
				if (data != null) {
					// Write the data to the priority data buffer
					priorityDataBuffer.add(data);
				}
			}
		} else {
			// Obtain a lock on the data buffer
			synchronized(dataBuffer) {
				if (data != null) {
					// Write the data to the data buffer
					dataBuffer.put(timeValid, data);
				}
			}
		}
	}
	
	/**
	 * Reads the next response from the received buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the response buffer
	 * will be cleared after being read.
	 * </p>
	 * @return the next object in the response buffer
	 */
	public Serializable readResponse() {
		Serializable data = null;
		
		// Obtain a lock on the priority response buffer
		synchronized(priorityResponseBuffer) {
			// Read data from the buffer
			if (priorityResponseBuffer != null
					&& priorityResponseBuffer.size() > 0) {
				data = priorityResponseBuffer.removeFirst();
			}
		}
		
		// If there was no priority data
		if (data == null) {
			// Obtain a lock on the response buffer
			synchronized(responseBuffer) {
				// Read data from the buffer
				if (responseBuffer != null
						&& responseBuffer.size() > 0) {
					if (responseBuffer.lastEntry() != null) {
						// Check if the data in the buffer is up-to-date
						if (responseBuffer.lastEntry().getKey() > mostRecent) {
							// Update the most recent value
							mostRecent = responseBuffer.lastEntry().getKey();

							// Data is more up-to-date than any seen so far,
							// so return it
							data = responseBuffer.lastEntry().getValue();
						}
					}

					// Clear the response buffer
					responseBuffer.clear();
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Gets the thread's status.
	 * @return <code>true</code> if the thread is currently running,
	 * 			otherwise <code>false</code>
	 */
	private boolean getStatus() {
		// Obtain a lock on the status attribute
		synchronized (statusMutex) {
			return status;
		}
	}
	
	/**
	 * Stops the worker.
	 */
	public void end() {
		// Obtain a lock on the status attribute
		synchronized (statusMutex) {
			status = false;
		}
	}
	
}
