package scn;

import java.util.ArrayList;

import net.NetworkManager;

import lib.ButtonText;
import lib.jog.graphics;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

public class MultiPlayerGame extends Game {

	/** The network manager responsible for managing network requests */
	private NetworkManager networkManager;
	
	/** The time frame to send data across the network */
	private double timeToUpdate;

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
	public static MultiPlayerGame createMultiPlayerGame(
			DifficultySetting difficulty) {
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
	}
	
	/**
	 * {@inheritDoc}
	 */
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
		
		// Set up the game
		setUpGame();
		
		// Set up the network manager
		networkManager = new NetworkManager(true);

		// Create the manual control buttons XXX
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
	}

	/**
	 * Sets up the game.
	 * <p>
	 * This creates the waypoints and airports to assign to the
	 * players, and then creates the players.
	 * </p>
	 */
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

		// Set up the players
		Player player0 = new Player(getNewPlayerID(), "Bob1", true,
				"127.0.0.1", player0Airports, player0Waypoints);
		players.add(player0);
		Player player1 = new Player(getNewPlayerID(), "Bob2", false,
				"127.0.0.1", player1Airports, player1Waypoints);
		players.add(player1);

		player0.setScore(50);
		player1.setScore(20);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(double timeDifference) {
		// Increment the time before the next data send
		timeToUpdate += timeDifference;

		if (timeToUpdate > 0.01) {
			// Get data from the server
			Object data = networkManager.receiveData();
			
			if (data != null && data instanceof Player) {
				// Set the received player data
				//System.out.println("Received player: "
				//		+ ((Player) data).getName());
				players.set((player.getID() + 1) % 2, (Player) data);
			}
			
			// Send current player's data to the server
			networkManager.sendData(player);

			timeToUpdate = 0;
		}
		
		super.update(timeDifference);
		
		// Deselect any aircraft which are inside the airspace of the other player
		// This ensures that players can't keep controlling aircraft
		// after they've entered another player's airspace
		returnToAirspace();

		checkLives();
	}

	@Override
	public void draw() {
		super.draw();

		// Draw additional features that are specific to multi-player
		drawMiddleZone();

		// Draw the power-ups
		// TODO
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

		// Draw the two lines
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
	

	// Close ----------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		super.close();
		networkManager.close();
	}

	// Deprecated -----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();
		
		Player player1 = new Player(getNewPlayerID(),
				"Test Player 1", true, "127.0.0.1",
				null, null);
		players.add(player1);
		
		Player player2 = new Player(getNewPlayerID(),
				"Test Player 2", true, "127.0.0.1",
				null, null);
		players.add(player2);
		
		player1.setAircraft(new ArrayList<Aircraft>());
		player2.setAircraft(new ArrayList<Aircraft>());
	}

}
