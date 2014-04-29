package net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class NetworkManager {

	/** The server's URL */
	public static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com";
	
	/** The message transfer extension */
	public static final String MSG_EXT = "/msg";
	
	/** The data transfer extension */
	public static final String DATA_EXT = "/data";
	
	/** The connection ID to the server */
	private static long id = -1;
	
	/** The connection name */
	private static String name = "";
	
	/** The connection host status */
	private static boolean isHost = false;

	/** The thread to send data on */
	private static NetworkThread networkThread;
	
	/** A map for temporarily storing data in order to make use of entries */
	private static TreeMap<Long, byte[]> transientDataBuffer =
			new TreeMap<Long, byte[]>();
	
	/** Whether to output data to the standard output */
	private static boolean verbose = true;

	
	/**
	 * Initialises the new network manager.
	 * <p>
	 * This starts the network thread.
	 * </p>
	 */
	public static void startThread() {
		networkThread = new NetworkThread();
	}
	
	
	/**
	 * Adds data to the network thread.
	 * <p>
	 * The data will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param timeValid - the time at which the data was valid
	 * @param data - the data to send
	 */
	public static void sendData(long timeValid, Serializable data) {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			networkThread.writeData(timeValid, data);
		}
	}
	
	/**
	 * Retrieve the next response from the network thread.
	 */
	public static Serializable receiveData() {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			return networkThread.readResponse();
		}
	}

	/**
	 * Adds a message to the network thread.
	 * <p>
	 * The message will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param message - the message to send
	 */
	public static void sendMessage(String message) {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			networkThread.writeMessage(message);
		}
	}
	
	
	/**
	 * Opens an HTTP POST connection to the server,
	 * <p>
	 * This initialises the connection to the URL specified, and sets request
	 * properties such as the user agent and client ID.
	 * </p>
	 * @param url - the URL to connect to
	 * @return a connection object representing the connection to the URL
	 */
	private static HttpURLConnection openPostConnection(String url) {
		HttpURLConnection connection = null;
		
		try {
			// Open a new HTTP connection
			connection = (HttpURLConnection) (new URL(url)).openConnection();
			
			// Set the connection settings
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			// Set request properties and headers
			connection.setRequestProperty("user-agent",
					"Fly-Hard");
			connection.setRequestProperty("fh-client-id",
					String.valueOf(id));
			connection.setRequestProperty("fh-client-name",
					name);
			connection.setRequestProperty("fh-client-host",
					String.valueOf(isHost));
			
		} catch (ProtocolException e) {
			print(e);
		} catch (MalformedURLException e) {
			print(e);
		} catch (IOException e) {
			print(e);
		}
		
		return connection;
	}
	
	/**
	 * Sends a message to the server.
	 * @param message - the message to send
	 * @return the data the server responded with
	 */
	public static String postMessage(String message) {
		String receivedMessages = null;

		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;

		if (message != null && !message.equals("")) {
			print("Sending message: " + message);
		}
		
		if (message == null || "".equals(message)) {
			message = "NULL";
		}

		// Open the connection
		HttpURLConnection connection = openPostConnection(SERVER_URL + MSG_EXT);

		try {
			// Set up the output stream
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			
			// Write the data
			outputStream.writeObject(message);

			// Connect to the server
			connection.connect();

			// Set up the input stream
			inputStream = new ObjectInputStream(connection.getInputStream());

			// Get the received data
			receivedMessages = (String) inputStream.readObject();

			if (!message.equals("NULL")) {
				print("Received response: " + receivedMessages);
			}

			// Get the response headers
			if (connection.getHeaderField("fh-client-id") != null) {
				setID(Long.parseLong(connection
						.getHeaderField("fh-client-id")));
			}
			
			// Flush the output stream
			outputStream.flush();

			// Close the connection
			connection.disconnect();
		} catch (EOFException e) {
			// Do not print the error message
		} catch (Exception e) {
			print(e);
		}

		// Handle the received message(s)
		InstructionHandler.handleInstruction(receivedMessages);

		return receivedMessages;
	}
	
	/**
	 * Sends an object to the server.
	 * @param dataEntry - the data entry to send
	 * @return the data entry the server responded with
	 */
	@SuppressWarnings("unchecked")
	public static Entry<Long, byte[]> postObject(
			Entry<Long, Serializable> dataEntry) {
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		Entry<Long, byte[]> receivedData = null;
		
		// Open the connection
		HttpURLConnection connection = openPostConnection(SERVER_URL + DATA_EXT);
		
		try {
			// Set up the output stream
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			
			// Serialise the data
			if (dataEntry != null && dataEntry.getValue() != null) {
				transientDataBuffer.put(dataEntry.getKey(),
						serialiseData(dataEntry.getValue()));
			}
			
			// Write the data to the output stream
			outputStream.writeObject(transientDataBuffer.lastEntry());
			
			// Clear the transient data buffer
			transientDataBuffer.clear();
			
			// Connect to the server
			connection.connect();

			// Set up the input stream
			inputStream = new ObjectInputStream(connection.getInputStream());
			
			// Get the received data
			receivedData = (Entry<Long, byte[]>) inputStream.readObject();
			
			// Get the response headers
			if (connection.getHeaderField("fh-client-id") != null) {
				setID(Long.parseLong(connection
						.getHeaderField("fh-client-id")));
			}
			
			// Flush the output stream
			outputStream.flush();
			
			// Close the connection
			connection.disconnect();
		} catch (EOFException e) {
			// Do not print the error message
		} catch (Exception e) {
			print(e);
		}
		
		// Return the data
		return receivedData;
	}
	
	
	/**
	 * Serialises data to a byte array.
	 * @param data - the data to serialise
	 * @return the data in a serialised form
	 */
	public static byte[] serialiseData(Serializable data) {
		ByteArrayOutputStream byteArrayOutputStream = null;
		ObjectOutputStream serializeOutputStream = null;
		
		if (data != null) {
			try {
				byteArrayOutputStream = new ByteArrayOutputStream();
				serializeOutputStream = new ObjectOutputStream(
						byteArrayOutputStream);
				serializeOutputStream.writeObject(data);
				serializeOutputStream.close();
				return byteArrayOutputStream.toByteArray();
			} catch (IOException e) {
				print(e);
			}
		}
		
		return null;
	}
	
	/**
	 * Deserialises data from a byte array.
	 * @param data - the byte array to deserialise
	 * @return the deserialised data entry
	 */
	public static Serializable deserialiseData(byte[] data) {
		ByteArrayInputStream byteArrayInputStream = null;
		ObjectInputStream deserializeInputStream = null;
		
		if (data != null) {
			try {
				byteArrayInputStream = new ByteArrayInputStream(data);
				deserializeInputStream = new ObjectInputStream(
						byteArrayInputStream);
				return (Serializable) deserializeInputStream.readObject();
			} catch (IOException e) {
				print(e);
			} catch (ClassNotFoundException e) {
				print(e);
			}
		}
		
		return null;
	}
	
	
	/**
	 * Gets the network thread's ID.
	 * @return the network thread's ID
	 */
	public static long getNetworkThreadID() {
		if (networkThread == null) networkThread = new NetworkThread();
		
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			return networkThread.getId();
		}
	}
	
	/**
	 * Sets the player's ID.
	 * @param name - the player's ID
	 */
	public static synchronized void setID(long id) {
		NetworkManager.id = id;
	}
	
	/**
	 * Sets the player's name.
	 * @param name - the player's name
	 */
	public static synchronized void setName(String name) {
		NetworkManager.name = name;
	}
	
	/**
	 * Sets whether the player is a host.
	 * @param isHost - <code>true</code> if a player should be a host,
	 * 			otherwise <code>false</code>
	 */
	public static synchronized void setHost(boolean isHost) {
		NetworkManager.isHost = isHost;
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
	 * @param string - the string to output
	 */
	public static void print(String string) {
		if (verbose) System.out.println(string);
	}
	
	/**
	 * Prints error messages to the standard output.
	 * <p>
	 * Uses {@link #print(String)} to print stack traces.
	 * </p>
	 * @param e - the exception to output
	 */
	public static void print(Exception e) {
		if (verbose) System.err.println(e.toString());
		
		for (int i = 0; i < e.getStackTrace().length; i++) {
			if (verbose) {
				System.err.println("at " + e.getStackTrace()[i].toString());
			}
		}
	}
	
	
	/**
	 * Pauses the network thread.
	 */
	public static void pause() {
		if (networkThread != null) networkThread.end();
		setID(-1L);
	}

	/**
	 * Closes any open connections.
	 */
	public static void close() {
		// Send a message to the opponent to let
		// them know we're closing
		
		if (networkThread != null) networkThread.end();
	}

}
