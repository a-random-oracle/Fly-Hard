package scn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import net.ThreadReceive;
import net.ThreadSend;

import lib.ButtonText;
import lib.jog.graphics;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

public class MultiPlayerGame extends Game {

	/** The URL to the arbitration server */
	private static final String SERVER_URL =
			"http://tomcat-teamgoa.rhcloud.com/pool";
	
	/** The number of times to attempt to connect before aborting */
	private static int abortAfter;

	/** The server-side socket */
	private ServerSocket server;

	/** The client socket */
	private Socket client;

	/** The socket used by the client */
	private Socket testSocket;

	/** The host's ip address */
	private String hostIP = "144.32.46.36";

	/** Fixed port number to connect to */
	private static final int PORT = 25560;

	/** The input stream to read in data */
	private ObjectInputStream inStream;

	/** The output stream to write data */
	private ObjectOutputStream outStream;

	/** The time frame to send data across the network */
	protected double timeToUpdate;

	/** Player ID */
	private int playerID;

	/** The y-coordinate at which the middle zone borders begin */
	public static int yStart = window.height() - yOffset;

	/** The y-coordinate at which the middle zone borders end */
	public static int yEnd = yOffset;

	/** The x-coordinate at which the left middle zone border is located */
	public static int leftEntryX = (int) (window.width() * (3d/7d));

	/** The x-coordinate at which the right middle zone border is located */
	public static int rightEntryX = window.width() - leftEntryX;


	/**
	 * Creates a new instance of a multi-player game.
	 * <p>
	 * If an instance of Game already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 * @return the multi-player game instance
	 */
	public static MultiPlayerGame createMultiPlayerGame(DifficultySetting difficulty) {
		if (instance == null) {
			return new MultiPlayerGame(difficulty);
		} else {
			Exception e = new Exception("Attempting to create a " +
					"second instance of Game");
			e.printStackTrace();
			return (MultiPlayerGame) instance;
		}
	}

	/**
	 * Constructs a multi-player game.
	 * @param difficulty
	 * 			the difficulty the scene is to be initialised with
	 */
	private MultiPlayerGame(DifficultySetting difficulty) {
		super(difficulty);
		instance = this;
		abortAfter = 15;
	}

	@Override
	public void start() {
		super.start();

		// Assign location waypoints to the player
		locationWaypointMap.put(0, 0);
		locationWaypointMap.put(1, 0);
		locationWaypointMap.put(2, 1);
		locationWaypointMap.put(3, 1);
		locationWaypointMap.put(4, 0);
		locationWaypointMap.put(5, 1);

		// Communicate with arbitration server to determine the host
		boolean isHost = false; //TODO
		/*String arbitrationResult = connectToArbitrationServer();
		System.out.println("RESULT: " + arbitrationResult);

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
			testSocket = null;
			try {
				testSocket = new Socket(InetAddress.getByName(hostIP), PORT);
			} catch (IOException e) {
				isHost = true;
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}

		if (isHost) {
			System.out.println("Will host on " + hostIP + ":" + PORT);
			playerID = 0;
			setUpGame();
		} else {
			System.out.println("Will join to " + hostIP + ":" + PORT);
			playerID = 1;
		}

		// Set up and initialise the network
		establishNetworkConnection(isHost);

		// Create the manual control buttons
		manualControlButtons = new ButtonText[players.size()];

		ButtonText.Action manual0 = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(players.get(0));
			}
		};

		ButtonText.Action manual1 = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(players.get(1));
			}
		};

		manualControlButtons[players.get(0).getID()]
				= new ButtonText(" Take Control", manual0,
						(window.width() - 128 - (2 * xOffset)) / 2,
						32, 128, 32, 8, 4);

		manualControlButtons[players.get(1).getID()]
				= new ButtonText(" Take Control", manual1,
						(window.width() - 128 - (2 * xOffset)) / 2,
						32, 128, 32, 8, 4);

		// Reset game attributes for each player
		deselectAircraft(players.get(0));
		deselectAircraft(players.get(1));

		// Set the appropriate player
		if (isHost) {
			player = players.get(0);
		} else {
			player = players.get(1);
		}
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
		} while (attempts < abortAfter);
		
		return null;
	}

	private void setUpGame() {
		// Generate the lists of waypoints to pass to the players
		Waypoint[] player0Waypoints = new Waypoint[5 + 3];
		Waypoint[] player1Waypoints = new Waypoint[5 + 3];

		player0Waypoints[0] = airspaceWaypoints[0];
		player0Waypoints[1] = airspaceWaypoints[1];
		player0Waypoints[2] = airspaceWaypoints[2];
		player0Waypoints[3] = airspaceWaypoints[3];
		player0Waypoints[4] = airspaceWaypoints[4];

		player1Waypoints[0] = airspaceWaypoints[5];
		player1Waypoints[1] = airspaceWaypoints[6];
		player1Waypoints[2] = airspaceWaypoints[7];
		player1Waypoints[3] = airspaceWaypoints[8];
		player1Waypoints[4] = airspaceWaypoints[9];

		// Add in location waypoints
		player0Waypoints[5] = locationWaypoints[0];
		player0Waypoints[6] = locationWaypoints[1];
		player0Waypoints[7] = locationWaypoints[4];

		player1Waypoints[5] = locationWaypoints[2];
		player1Waypoints[6] = locationWaypoints[3];
		player1Waypoints[7] = locationWaypoints[5];

		// Add airports to lists
		Airport[] player0Airports = new Airport[1];
		Airport[] player1Airports = new Airport[1];

		player0Airports[0] = airports[0];
		player1Airports[0] = airports[1];

		// Set up the player
		Player player0 = new Player(getNewPlayerID(), "Bob1", true, hostIP,
				player0Airports, player0Waypoints);
		players.add(player0);
		Player player1 = new Player(getNewPlayerID(), "Bob2", false, "127.0.0.1",
				player1Airports, player1Waypoints);
		players.add(player1);

		player0.setScore(50);
		player1.setScore(20);
	}

	@Override
	public void update(double timeDifference) {
		super.update(timeDifference);

		// Deselect any aircraft which are inside the airspace of the other player
		// This ensures that players can't keep controlling aircraft
		// after they've entered another player's airspace
		returnToAirspace();

		checkLives();

		//System.out.println(players.get(1).getAircraft().size());

		// Increment the time before the next data send
		timeToUpdate += timeDifference;

		if (timeToUpdate > 0.5) {
			timeToUpdate = 0;

		}

		if (player.isHosting()) {
			sendPlayerData();
			receivePlayerData();
		} else {
			receivePlayerData();
			sendPlayerData();
		}
	}

	@Override
	public void draw() {
		super.draw();

		//Draw additional features that are specific to multi-player
		drawMiddleZone();

		//Draw the power-ups 
	}

	/**
	 * Draws the middle zone in the game.
	 * <p>
	 * The middle zone is shared by both players.
	 * Players are forced to take manual control when in this zone
	 * and are not permitted to fly into the other player's flight area.
	 * <p>
	 * It is in this zone that the power-ups spawn.
	 */
	protected void drawMiddleZone() {
		graphics.setColour(graphics.green);

		//Draw the two lines
		graphics.line(leftEntryX, yStart, leftEntryX, yEnd);
		graphics.line(rightEntryX, yStart, rightEntryX, yEnd);
	}

	/**
	 * Removes control of an aircraft from the player when 
	 * their aircraft goes into the other player's airspace.
	 */
	public void returnToAirspace() {
		for (Aircraft airc : player.getAircraft()) {
			if (!airc.isAtDestination()) {
				if (airc.isOutOfPlayersAirspace()) {
					deselectAircraft(airc, player);
				}
			}
		}
	}

	public boolean checkLives() {
		for (Player player : players) {
			if (player.getLives() == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkCollisions(double timeDifference) {
		for (Aircraft plane : player.getAircraft()) {
			int collisionState = plane.updateCollisions(timeDifference,
					getAllAircraft());
			if (collisionState >= 0) {
				int lives = player.getLives();
				player.setLives(lives--);
				return;
			}
		}
	}

	@Override
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		if (checkLives()) {
			super.gameOver(plane1, plane2);
		}
	}


	// Networking -----------------------------------------------------------------------

	public void establishNetworkConnection(boolean isHost) {
		try {
			if (isHost) {
				// Set up a server socket
				System.out.println("Setting up server...");
				server = new ServerSocket(PORT, 4,
						InetAddress.getByName(getHost().getIPAddress()));
				System.out.println("Server set up.");

				// Wait for the client to connect
				System.out.println("Awaiting client...");
				client = server.accept();
				System.out.println("Client connected.");

				// Set up the input/output streams
				System.out.println("Setting up streams...");
				outStream = new ObjectOutputStream(client.getOutputStream());
				inStream = new ObjectInputStream(client.getInputStream());
				System.out.println("Streams set up.");

				// Flush the streams
				outStream.reset();
				outStream.flush();

				// Send the list of players
				//System.out.println("Sending data...");
				ThreadSend ts = new ThreadSend(outStream);
				ts.start();
				ts.join();
				//System.out.println("Data sent.");

				System.out.println("Creating multiplayer game.");	
			} else {
				// Connect to he host
				System.out.println("Connecting to host...");
				inStream = new ObjectInputStream(testSocket.getInputStream());
				outStream = new ObjectOutputStream(testSocket.getOutputStream());
				System.out.println("Connected.");

				// Receive the player array from the host
				//System.out.println("Receiving data...");
				ThreadReceive tr = new ThreadReceive(inStream);
				tr.start();
				tr.join();
				//System.out.println("Data received.");

				// Used for debugging
				//for(Player p : players) System.out.println(p.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendPlayerData() {
		try {
			// Flush the streams
			outStream.reset();
			outStream.flush();

			// Send the list of players
			System.out.println("Sending data...");
			ThreadSend ts = new ThreadSend(outStream, playerID);
			ThreadReceive tr = new ThreadReceive(inStream, playerID);
			ts.start();
			ts.join();
			tr.start();
			tr.join();
			System.out.println("Data sent.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void receivePlayerData() {
		try {
			// Flush the streams
			outStream.flush();

			// Receive the player array from the host
			//System.out.println("Receiving data...");
			ThreadReceive tr = new ThreadReceive(inStream, playerID);
			ThreadSend ts = new ThreadSend(outStream, playerID);
			tr.start();
			tr.join();
			ts.start();
			ts.join();
			//System.out.println("Data received.");

			// Used for debugging
			//for(Player p : players) System.out.println(p.getName());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Close ----------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		super.close();

		try {
			// If server is open, close it
			if (server != null) server.close();

			// If sockets are open, close them too
			if (client != null) client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Deprecated -----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();
		Player player1 = new Player(getNewPlayerID(), "Test Player 1", true, hostIP, null, null);
		players.add(player1);
		Player player2 = new Player(getNewPlayerID(), "Test Player 2", true, "127.0.0.1", null, null);
		players.add(player2);
		player1.setAircraft(new ArrayList<Aircraft>());
		player2.setAircraft(new ArrayList<Aircraft>());
	}

}
