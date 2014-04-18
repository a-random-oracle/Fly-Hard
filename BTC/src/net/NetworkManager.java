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

public class NetworkManager {

	/** The server's URL */
	public static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com";
	
	/** The message transfer extension */
	public static final String MSG_EXT = "/msg";
	
	/** The data transfer extension */
	public static final String DATA_EXT = "/data";
	
	/** The connection ID to the server */
	private static int id;

	/** The thread to send data on */
	private NetworkThread networkThread;
	
	private static TreeMap<Long, byte[]> transientDataBuffer;
	
	/** Whether to output data to the standard output */
	private static boolean verbose;


	// Constructor ----------------------------------------------------------------------

	/**
	 * Constructs a new network manager.
	 * @param verbose - <code>true</code> indicates that the network manager
	 * 					should output status and connection information to
	 * 					the standard output
	 */
	public NetworkManager(boolean verbose) {
		NetworkManager.id = -1;
		NetworkManager.verbose = verbose;
		NetworkManager.transientDataBuffer = new TreeMap<Long, byte[]>();
		
		// Create a network thread for handling asynchronous data passing
		networkThread = new NetworkThread();
		networkThread.start();
		
		// Start by sending an initial request
		sendMessage("START");
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
	public void sendData(long timeValid, Serializable data) {
		networkThread.writeData(timeValid, data);
	}
	
	/**
	 * Retrieve the next response from the network thread.
	 */
	public Serializable receiveData() {
		return networkThread.readResponse();
	}
	
	/**
	 * Adds a message to the network thread.
	 * <p>
	 * The message will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param message - the message to send
	 */
	public void sendMessage(String message) {
		networkThread.writeMessage(message);
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
			connection.addRequestProperty("user-agent", "Fly-Hard");
			connection.addRequestProperty("client-id", String.valueOf(id));
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
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		String receivedMessages = null;
		
		print("Sending message: " + message);
		
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
			print("Received response: " + receivedMessages);

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
		print(e.toString());
		
		for (int i = 0; i < e.getStackTrace().length; i++) {
			print("at " + e.getStackTrace()[i].toString());
		}
	}
	
	
	/**
	 * Gets the ID for the current connection to the server.
	 * @return the current sever ID
	 */
	public static int getID() {
		return id;
	}
	
	/**
	 * Sets the ID for the current connection to the server.
	 * @param id - the server ID to set
	 */
	public static void setID(int id) {
		NetworkManager.id = id;
	}
	

	/**
	 * Closes any open connections.
	 */
	public void close() {
		// Send a message to the opponent to let
		// them know we're closing
		NetworkManager.postMessage("END");

		if (networkThread != null) networkThread.end();
	}

}
