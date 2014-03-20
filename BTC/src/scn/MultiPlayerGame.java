package scn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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

	/** The server-side socket */
	private ServerSocket server;

	/** The client socket */
	private Socket client;

	/** The socket used by the client */
	private Socket testSocket;

	private static final String HOST_IP = "144.32.179.129";

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
		} else {		// Set the appropriate player
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

		boolean isHost = false;
		testSocket = null;

		try {
			testSocket = new Socket(InetAddress.getByName(HOST_IP), PORT);

			// Need to finalise to a better solution (with a specific exception)
		} catch (IOException e) {
			isHost = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isHost) {
			System.out.println("Will host on " + HOST_IP + ":" + PORT);
			playerID = 0;
			setUpGame();
		} else {
			System.out.println("Will join to " + HOST_IP + ":" + PORT);
			playerID = 1;
		}

		// Set up and initialise the network
		establishNetworkConnection(isHost, testSocket);

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
		Player player0 = new Player(getNewPlayerID(), "Bob1", true, HOST_IP,
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

	/**
	 * Forces any plane within the middle zone to be under
	 * manual control.
	 */
	public void forceControl() {
		for (Aircraft airc : player.getAircraft()) {
			if (!airc.isAtDestination()) {
				if (airc.isInMiddleZone()) {
					airc.toggleManualControl();
				}
			}
		}
	}


	// Networking -----------------------------------------------------------------------

	public void establishNetworkConnection(boolean isHost, Socket socket) {
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
		Player player1 = new Player(getNewPlayerID(), "Test Player 1", true, HOST_IP, null, null);
		players.add(player1);
		Player player2 = new Player(getNewPlayerID(), "Test Player 2", true, "127.0.0.1", null, null);
		players.add(player2);
		player1.setAircraft(new ArrayList<Aircraft>());
		player2.setAircraft(new ArrayList<Aircraft>());
	}

}
