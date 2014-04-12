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

import java.util.ArrayList;

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
	
	/** Whether to output data to the standard output */
	private static boolean verbose;


	// Constructor ----------------------------------------------------------------------

	/**
	 * Constructs a new network manager.
	 * @param verbose
	 * 			<code>true</code> indicates that the network manager
	 * 			should output status and connection information to
	 * 			the standard output
	 */
	public NetworkManager(boolean verbose) {
		NetworkManager.id = -1;
		NetworkManager.verbose = verbose;

		initialiseClient();
		
		// Create a network thread for handling asynchronous data passing
		networkThread = new NetworkThread();
		networkThread.start();
	}
	
	/**
	 * Sends an initialisation request to the server.
	 * <p>
	 * This sets the current player.
	 * </p>
	 */
	private void initialiseClient() {
		// Start by sending an initial request containing the client's
		// public IP address
		String response = postMessage("INIT");

		// Process the response
		InstructionHandler.handleInstruction(response);
	}

	// Data Send and Receive ------------------------------------------------------------

	/**
	 * Adds data to the network thread.
	 * <p>
	 * The data will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param data
	 * 			the data to send
	 */
	public void sendData(Serializable data) {
		networkThread.writeData(data);
	}
	
	/**
	 * Adds a message to the network thread.
	 * <p>
	 * The message will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param message
	 * 			the message to send
	 */
	public void sendMessage(String message) {
		networkThread.writeMessage(message);
	}

	/**
	 * Retrieve the next response from the network thread.
	 */
	public Serializable receiveData() {
		return networkThread.readResponse();
	}

	/**
	 * Retrieve all unread responses from the network thread.
	 */
	public ArrayList<Serializable> receiveAllData() {
		return networkThread.readAllResponses();
	}
	
	
	// HTTP Methods ---------------------------------------------------------------------
	
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
			connection.addRequestProperty("client-name", "");
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
	 * Sends an object to the server to add to the server-side data store.
	 * @param data
	 * 			the data to send
	 * @return the data the server responded with
	 */
	public static String postMessage(String message) {
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		String receivedData = null;
		
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
			receivedData = (String) inputStream.readObject();
			print("Received response: " + receivedData);

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
	 * Sends an object to the server to add to the server-side data store.
	 * @param data
	 * 			the data to send
	 * @return the data the server responded with
	 */
	public static Serializable postObject(Serializable data) {
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		Serializable receivedData = null;
		
		// Open the connection
		HttpURLConnection connection = openPostConnection(SERVER_URL + DATA_EXT);
		
		try {
			// Set up the output stream
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			
			// Serialise the data, then write it to the output stream
			outputStream.writeObject(serialiseData(data));
			
			// Connect to the server
			connection.connect();

			// Set up the input stream
			inputStream = new ObjectInputStream(connection.getInputStream());
			
			// Get the received data and deserialise it
			receivedData = deserialiseData((byte[]) inputStream.readObject());
			
			// Flush the output stream
			outputStream.flush();
			
			// Close the connection
			connection.disconnect();
		} catch (EOFException e) {
			// Do not print the error message
		} catch (Exception e) {
			print(e);
		}
		
		// If the received data is a string, treat it as an instruction
		// and process it immediately
		/*if (receivedData instanceof String) { XXX
			print("Received response: " + ((String) receivedData));
			InstructionHandler.handleInstruction((String) receivedData);
		}*/
		
		// Return the data
		return receivedData;
	}
	
	
	// Helper Methods -------------------------------------------------------------------
	
	/**
	 * Serialises data to a byte array.
	 * @param data
	 * 			the data to serialise
	 * @return
	 */
	private static byte[] serialiseData(Serializable data) {
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
	
	private static Serializable deserialiseData(byte[] data) {
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
	 * @param string
	 * 			the string to output
	 */
	public static void print(String string) {
		if (verbose) System.out.println(string);
	}
	
	/**
	 * Prints error messages to the standard output.
	 * <p>
	 * Uses {@link #print(String)} to print stack traces.
	 * </p>
	 * @param e
	 * 			the exception to output
	 */
	public static void print(Exception e) {
		print(e.toString());
		
		for (int i = 0; i < e.getStackTrace().length; i++) {
			print("at " + e.getStackTrace()[i].toString());
		}
	}
	
	
	// Accessors ------------------------------------------------------------------------
	
	/**
	 * Gets the ID for the current connection to the server.
	 * @return the current sever ID
	 */
	public static int getID() {
		return id;
	}
	
	/**
	 * Sets the ID for the current connection to the server.
	 * @param id
	 * 			the server ID to set
	 */
	public static void setID(int id) {
		NetworkManager.id = id;
	}
	
	// Close ----------------------------------------------------------------------------

	/**
	 * Closes any open connections.
	 */
	public void close() {
		if (networkThread != null) networkThread.end();
	}

}
