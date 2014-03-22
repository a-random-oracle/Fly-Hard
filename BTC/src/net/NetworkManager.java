package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import scn.MultiPlayerGame;

public class NetworkManager {

	/** The URL to the arbitration server */
	private static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com/pool";

	/** The port to use for the application */
	private static final int PORT = 25560;

	/** The number of times to attempt to connect before aborting */
	private static final int ABORT_AFTER = 15;

	/** The server-side socket */
	private ServerSocket server;

	/** The client socket */
	private Socket client;

	/** The socket used by the client */
	private Socket testSocket;

	/** The host's IP address */
	private String hostIP = "127.0.0.1";//"144.32.46.36";

	/** The input stream to read in data */
	private ObjectInputStream inStream;

	/** The output stream to write data */
	private ObjectOutputStream outStream;

	/** Whether to output data to the standard output */
	private boolean verbose;


	/**
	 * Constructs a new network manager.
	 * @param verbose
	 * 			<code>true</code> indicates that the network manager
	 * 			should output status and connection information to
	 * 			the standard output
	 */
	public NetworkManager(boolean verbose) {
		this.verbose = verbose;
		//start(); //TODO change this so that start is run automatically
	}

	/**
	 * Starts a new connection.
	 * <p>
	 * This will attempt to connect to the arbitration server to determine
	 * which player is hosting the game.
	 * </p>
	 * @return <code>true</code> if the calling player is the host
	 * 			otherwise returns <code>false</code>
	 */
	public boolean start() {
		// Communicate with arbitration server to determine the host
		boolean isHost = false; //TODO
		/*String arbitrationResult = connectToArbitrationServer();
			print("Received: '" + arbitrationResult + "' from the server");

			// Assign a host based on the result of the arbitration
			if (arbitrationResult == null) {
				// Ensure that the result returned was valid
				// If not, terminate the scene
				isHost = false;
				close();
			} else if (arbitrationResult.contains("HOST")) {
				isHost = true;
				hostIP = arbitrationResult.replace("HOST:", "");
			} else {
				isHost = false;
				hostIP = arbitrationResult;
			}*/

		//if (!isHost) {
		try {
			System.out.println("HERE1.5");
			testSocket = new Socket(InetAddress.getByName(hostIP), PORT);
			System.out.println("HERE1.75");
		} catch (IOException e) {
			isHost = true;
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//}

		if (isHost) {
			print("Will host on " + hostIP + ":" + PORT);
		} else {
			print("Will join to " + hostIP + ":" + PORT);
		}
		
		return isHost;
	}

	/**
	 * Connects to the arbitration server.
	 * <p>
	 * The arbitration server will return "HOST:" followed by the incoming IP 
	 * to the first player, and the host's IP address to the second player.
	 * </p>
	 * <p>
	 * This will attempt to connect to the arbitration server 
	 * {@link MultiPlayerGame#abortAfter} times, before canceling the 
	 * connection attempt.
	 * </p>
	 * @return "HOST:" followed by the host's IP address if the player
	 * 			is to be the host, the host's IP address if the player
	 * 			is to be the client, or null if the connection is
	 * 			unsuccessful
	 */
	private String connectToArbitrationServer() {
		// Track the number of attempts made to connect to
		// the server
		int attempts = 0;

		// Keep attempting to connect until either:
		//   - a valid result is returned, or
		//   - the number of attempts made exceeds the abort limit
		do {
			// Create a client to manage the request to the
			// arbitration server
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(SERVER_URL);

			// Set user agent so that the arbitration server
			// recognises this as a valid request
			request.addHeader("user-agent", "Fly-Hard");
			request.addHeader("ip", "XXX"); //TODO add in ip gen code
			request.addHeader("pk", "1123581321");

			// Try to send a request to the arbitration server
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (response.getStatusLine().getStatusCode() == 200) {
				// If the response is valid, try to read it
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}

				// Create a validator to check that the returned IP
				// address is valid
				InetAddressValidator validator = new InetAddressValidator();

				// Check if any part of the response contains:
				//   - the term "HOST", or
				//   - a valid IP address
				String line = null;
				String linePart = null;
				try {
					while ((line = reader.readLine()) != null) {
						if (line.contains("HOST")) {
							linePart = line.replace("HOST:", "");

							if (validator.isValid(linePart)) {
								return line;
							}
						} else if (validator.isValid(line)) {
							return line;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// Couldn't connect to the arbitration server
				Exception e = new Exception("Connection to arbitration service"
						+ "could not be established.");
				e.printStackTrace();
			}

			attempts++;
		} while (attempts < ABORT_AFTER);

		return null;
	}

	/**
	 * Sets up a connection between the client and the server.
	 * <p>
	 * This will also pass the initial player array from the
	 * server to the client.
	 * <p>
	 * @param isHost
	 * 			is the current player the host
	 */
	public void establishNetworkConnection(boolean isHost) {
		try {
			if (isHost) {
				// Set up a server socket
				print("Setting up server...");
				server = new ServerSocket(PORT, 4,
						InetAddress.getByName(hostIP));
				print("Server set up.");

				// Wait for the client to connect
				print("Awaiting client...");
				client = server.accept();
				print("Client connected.");

				// Set up the input/output streams
				print("Setting up streams...");
				outStream = new ObjectOutputStream(client.getOutputStream());
				inStream = new ObjectInputStream(client.getInputStream());
				print("Streams set up.");

				// Flush the streams
				outStream.reset();
				outStream.flush();

				// Send the list of players
				print("Sending data...");
				ThreadSend ts = new ThreadSend(outStream, true, verbose);
				ts.start();
				ts.join();
				print("Data sent.");

				print("Creating multiplayer game.");	
			} else {
				// Connect to he host
				print("Connecting to host...");
				inStream = new ObjectInputStream(testSocket.getInputStream());
				outStream = new ObjectOutputStream(testSocket.getOutputStream());
				print("Connected.");

				// Receive the player array from the host
				print("Receiving data...");
				ThreadReceive tr = new ThreadReceive(inStream, true, verbose);
				tr.start();
				tr.join();
				print("Data received.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends one player's data to the other player.
	 * @param playerID
	 * 			the calling player's ID
	 */
	public void sendPlayerData(int playerID) {
		try {
			// Flush the streams
			//outStream.reset();
			//outStream.flush();
			//inStream.reset();

			// Send the list of players
			print("Sending data...");
			ThreadSend ts = new ThreadSend(outStream, false, verbose);
			ThreadReceive tr = new ThreadReceive(inStream, false, verbose);
			ts.start();
			ts.join();
			tr.start();
			tr.join();
			print("Data sent.");
		//} catch (IOException e) {
		//	e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives one player's data.
	 * @param playerID
	 * 			the calling player's ID
	 */
	public void receivePlayerData(int playerID) {
		try {
			// Flush the streams
			//outStream.reset();
			//outStream.flush();
			//inStream.reset();

			// Receive the player array from the host
			//print("Receiving data...");
			ThreadReceive tr = new ThreadReceive(inStream, false, verbose);
			ThreadSend ts = new ThreadSend(outStream, false, verbose);
			tr.start();
			tr.join();
			ts.start();
			ts.join();
			//print("Data received.");
		//} catch (IOException e) {
		//	e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	private void print(String string) {
		if (verbose) System.out.println(string);
	}

	/**
	 * Closes any open ports and connections.
	 */
	public void close() {
		try {
			// If server is open, close it
			if (server != null) {
				print("Closing server.");
				server.close();
			}

			// If sockets are open, close them too
			if (client != null) {
				print("Closing client socket.");
				client.close();
			}
			
			if (testSocket != null) {
				print("Closing test socket.");
				testSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the IP assigned as the host IP.
	 * @return the host's IP address
	 */
	public String getHostIP() {
		return hostIP;
	}

}
