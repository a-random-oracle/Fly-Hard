package net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import btc.Main;
import scn.Game;

public class NetworkManager {

	/** The server's URL */
	public static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com";

	/** The initialisation extension */
	public static final String INIT_EXT = "/init";
	
	/** The data transfer extension */
	public static final String DATA_EXT = "/data";

	/** The thread to send data on */
	private NetworkThread networkThread;
	
	/** The HTTP connection to the server */
	private static HttpURLConnection connection;
	
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
		NetworkManager.verbose = verbose;

		// Start by sending an initial request containing the client's
		// public IP address
		String initialResponse = null;
		try {
			initialResponse = (String) postObject("INIT", true);
		} catch (ClassCastException e) {
			print(e);
		}
		
		// Process the response
		if (initialResponse != null) {
			// Get the player ID of this player
			int playerIDToSet = Integer.parseInt(initialResponse.split(":")[1]);
			
			// Set the current player
			Game.getInstance().setCurrentPlayer(
					Game.getInstance().getPlayers().get(playerIDToSet));
			
			System.out.println("Playing as: " + Game.getInstance()
					.getCurrentPlayer().getName());
		}
		
		// Create a network thread for handling asynchronous data passing
		networkThread = new NetworkThread();
		networkThread.start();
	}

	// Data Send and Receive ------------------------------------------------------------

	/**
	 * Add data to the network thread.
	 * <p>
	 * The data will then be sent to the server after an arbitrary length
	 * of time.
	 * </p>
	 * @param data
	 * 			the data to send
	 */
	public void sendData(Object data) {
		networkThread.writeData(data);
	}

	/**
	 * Retrieve the next response from the network thread.
	 */
	public Object receiveData() {
		return networkThread.readResponse();
	}

	/**
	 * Retrieve all unread responses from the network thread.
	 */
	public ArrayList<Object> receiveAllData() {
		return networkThread.readAllResponses();
	}
	
	
	// HTTP Methods ---------------------------------------------------------------------
	
	private static boolean openPostConnection(boolean initialConnection) {
		// Set up a boolean to track whether the connection
		// has been established
		boolean connectionSuccessful = false;
		
		try {
			// Construct the server's URL
			// If this is an initial connection, use the initialisation
			// extension, otherwise use the data transfer extension
			URL serverURL = new URL(SERVER_URL
					+ ((initialConnection) ? INIT_EXT : DATA_EXT));
			
			// Open a new HTTP connection
			connection = (HttpURLConnection) (serverURL).openConnection();
			
			// Set the connection settings
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			// Set request properties and headers
			connection.addRequestProperty("user-agent", "Fly-Hard");
			connection.addRequestProperty("ip-address", Main.getIPAddress());
			
			// If code execution has reached this stage, the connection
			// must have opened successfully
			connectionSuccessful = true;
		} catch (ProtocolException e) {
			print(e);
		} catch (MalformedURLException e) {
			print(e);
		} catch (IOException e) {
			print(e);
		}
		
		return connectionSuccessful;
	}
	
	public static Object postObject(Object data, boolean initialConnection) {
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		Object receivedData = null;
		
		// Open the connection
		openPostConnection(initialConnection);
		
		try {
			// Set up the output stream
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			
			// Write the data
			if (initialConnection) {
				// If this is an initial connection, send the data as-is
				outputStream.writeObject(data);
			} else {
				// Otherwise, serialise the data
				outputStream.writeObject(serialiseData(data));
			}
			
			// Connect to the server
			connection.connect();

			// Set up the input stream
			inputStream = new ObjectInputStream(connection.getInputStream());

			// Get the received data
			if (initialConnection) {
				// If this is an initial connection, just read the data
				receivedData = inputStream.readObject();
			} else {
				// Otherwise, the data needs to be deserialised first
				receivedData = deserialiseData((byte[]) inputStream.readObject());
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
	
	
	// Helper Methods -------------------------------------------------------------------
	
	private static byte[] serialiseData(Object data) {
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
	
	private static Object deserialiseData(byte[] data) {
		ByteArrayInputStream byteArrayInputStream = null;
		ObjectInputStream deserializeInputStream = null;
		
		if (data != null) {
			try {
				byteArrayInputStream = new ByteArrayInputStream(data);
				deserializeInputStream = new ObjectInputStream(
						byteArrayInputStream);
				return (Object) deserializeInputStream.readObject();
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
	
	
	// Close ----------------------------------------------------------------------------

	/**
	 * Closes any open connections.
	 */
	public void close() {
		if (networkThread != null) networkThread.end();
	}

}
