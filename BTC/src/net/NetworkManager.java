package net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import scn.Game;

public class NetworkManager {

	/** The server's URL */
	public static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com";

	/** The post extension */
	public static final String POST_EXT = "/post";

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
	public NetworkManager(boolean verbose, String connectionType) {
		NetworkManager.verbose = verbose;
		
		if (connectionType.toUpperCase().contains("GET")) {
			// TODO <- implement get connection
		} else if (connectionType.toUpperCase().contains("POST")) {
			openPostConnection();
		}
		
		// Start by sending an INIT request containing the client's
		// public IP address
		String initialResponse = null;
		try {
			initialResponse = (String) postObject("INIT:"
					+ "127.0.0.1"/*InetAddress.getLocalHost()*/);
		//} catch (UnknownHostException e) {
		//	e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		
		// Process the response
		if (initialResponse != null && initialResponse.startsWith("SETID")) {
			// Get the player ID of the player to set from the response
			int playerIDToSet = Integer.parseInt(initialResponse.split(":")[1]);
			
			// Set the current player
			Game.getInstance().setCurrentPlayer(
					Game.getInstance().getPlayers().get(playerIDToSet));
			
			System.out.println("Playing as: player" + playerIDToSet);
		}
		
		// Create a network thread for handling asynchronous data passing
		networkThread = new NetworkThread();
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

	public static HashMap<String, String> setupHeaders() {
		// Form the request headers
		HashMap<String, String> headers = new HashMap<String, String>();

		// Set user agent so that the server recognises this as a valid
		// request
		headers.put("User-Agent", "Fly-Hard");

		// Add client's IP to the headers
		try {
			headers.put("ip", InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Return the headers
		return headers;
	}
	
	private boolean openPostConnection() {
		// Set up a boolean to track whether the connection
		// has been established
		boolean connectionSuccessful = false;
		
		try {
			// Open a new HTTP connection
			connection = (HttpURLConnection) (new URL(SERVER_URL + POST_EXT))
					.openConnection();
			
			// Set the connection settings
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			// If code execution has reached this stage, the connection
			// must have opened successfully
			connectionSuccessful = true;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return connectionSuccessful;
	}
	
	public static Object postObject(Object data) {
		ObjectInputStream inputStream = null;
		ObjectOutputStream outputStream = null;

		try {
			// Set up output stream and use it to write the data
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			outputStream.writeObject(data);
			outputStream.flush();
			outputStream.close();

			// Set up the input stream
			inputStream = new ObjectInputStream(connection.getInputStream());

			// Get the received data
			Object receivedData = (Object) inputStream.readObject();

			// Return the data
			return receivedData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {			
			// Close the input stream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Close the output stream
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	// Helper Methods -------------------------------------------------------------------

	/**
	 * Gets the current player's ID.
	 * <p>
	 * If the current player had not been set, returns -1.
	 * </p>
	 * <p>
	 * If the game instance has not yet been set, returns -2.
	 * </p>
	 * @return the current player's ID
	 */
	private int getCurrentPlayerID() {
		if (Game.getInstance() == null) {
			return -2;
		} else {
			if (Game.getInstance().getCurrentPlayer() == null) {
				return -1;
			} else {
				return Game.getInstance().getCurrentPlayer().getID();
			}
		}
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
	
	
	// Close ----------------------------------------------------------------------------

	/**
	 * Closes any open connections.
	 */
	public void close() {
		networkThread.end();
	}

}
