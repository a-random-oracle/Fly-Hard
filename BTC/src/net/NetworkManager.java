package net;

import java.io.EOFException;
import java.io.FileNotFoundException;
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

import scn.Game;
import btc.Main;

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
	
	/** The mutex used to protect the header fields */
	private static Object headerMutex = new Object();

	/** The task which sends and receives */
	private static NetworkWorker networkWorker = new NetworkWorker();
	
	/** The thread to send and receive data on */
	private static Thread networkThread = new Thread(networkWorker);
	
	/** A map for temporarily storing data in order to make use of entries */
	private static TreeMap<Long, byte[]> transientDataBuffer =
			new TreeMap<Long, byte[]>();
	
	/** Whether to output data to the standard output */
	private static boolean verbose = true;

	
	/**
	 * Starts the network thread and network worker.
	 */
	public static void startThread() {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			networkWorker = new NetworkWorker();
			networkThread = new Thread(networkWorker);
			networkThread.start();
		}
	}
	
	/**
	 * Stops the network thread and network worker.
	 */
	public static void stopThread() {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			networkWorker.end();
		}
	}
	
	/**
	 * Resets the ID, name and host attributes.
	 */
	public static void resetConnectionProperties() {
		// Obtain a lock on the header fields
		synchronized (headerMutex) {
			setID(-1L);
			setName("");
			setHost(false);
		}
	}
	
	
	/**
	 * Adds data to the network worker.
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
			networkWorker.writeData(timeValid, data);
		}
	}
	
	/**
	 * Retrieve the next response from the network worker.
	 */
	public static Serializable receiveData() {
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			return networkWorker.readResponse();
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
			connection.setRequestProperty("user-agent", Main.VERSION);
			
			// Obtain a lock on the header fields
			synchronized (headerMutex) {
				connection.setRequestProperty("fh-client-id",
						String.valueOf(id));
				connection.setRequestProperty("fh-client-name",
						name);
				connection.setRequestProperty("fh-client-host",
						String.valueOf(isHost));
			}
			
			if (Game.getInstance() != null
					&& Game.getInstance().getPlayer() != null) {
				connection.setRequestProperty("fh-client-lives",
						String.valueOf(Game.getInstance().getPlayer().getLives()));
				connection.setRequestProperty("fh-client-score",
						String.valueOf(Game.getInstance().getPlayer().getScore()));
			} else {
				connection.setRequestProperty("fh-client-lives", "0");
				connection.setRequestProperty("fh-client-score", "0");
			}
			
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
			try {
				inputStream = new ObjectInputStream(connection.getInputStream());
			} catch (FileNotFoundException e) {
				print(e);
			}

			// Get the received data
			if (inputStream != null) {
				receivedMessages = (String) inputStream.readObject();
			}

			if (!message.equals("NULL") && receivedMessages != null) {
				print("Received response: " + receivedMessages);
			}

			// Get the response headers
			// Obtain a lock on the header fields
			synchronized (headerMutex) {
				if (connection.getHeaderField("fh-client-id") != null) {
					setID(Long.parseLong(connection
							.getHeaderField("fh-client-id")));
				}
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
			// Obtain a lock on the header fields
			synchronized (headerMutex) {
				if (connection.getHeaderField("fh-client-id") != null
						&& !"".equals(connection
								.getHeaderField("fh-client-id"))) {
					setID(Long.parseLong(connection
							.getHeaderField("fh-client-id")));
				}
				
				if (connection.getHeaderField("fh-client-messages") != null
						&& !"".equals(connection
								.getHeaderField("fh-client-messages"))) {
					
					print("Received response: " + connection
							.getHeaderField("fh-client-messages"));
					InstructionHandler.handleInstruction(connection
							.getHeaderField("fh-client-messages"));
				}
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
	protected static byte[] serialiseData(Serializable data) {
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
	protected static Serializable deserialiseData(byte[] data) {
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
		// Obtain a lock on the network thread
		synchronized (networkThread) {
			return networkThread.getId();
		}
	}
	
	/**
	 * Gets the player's name.
	 * @return the player's name
	 */
	public static String getName() {
		// Obtain a lock on the header fields
		synchronized (headerMutex) {
			return name;
		}
	}
	
	/**
	 * Sets the player's ID.
	 * @param name - the player's ID
	 */
	public static void setID(long id) {
		// Obtain a lock on the header fields
		synchronized (headerMutex) {
			NetworkManager.id = id;
		}
	}
	
	/**
	 * Sets the player's name.
	 * @param name - the player's name
	 */
	public static void setName(String name) {
		// Obtain a lock on the header fields
		synchronized (headerMutex) {
			NetworkManager.name = name;
		}
	}
	
	/**
	 * Sets whether the player is a host.
	 * @param isHost - <code>true</code> if a player should be a host,
	 * 			otherwise <code>false</code>
	 */
	public static void setHost(boolean isHost) {
		// Obtain a lock on the header fields
		synchronized (headerMutex) {
			NetworkManager.isHost = isHost;
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

}
