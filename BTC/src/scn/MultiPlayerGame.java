package scn;

import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public class MultiPlayerGame extends Game {
	
	/** The unique instance of this class */
	private static MultiPlayerGame instance = null;
	
	/**
	 * Creates a new instance of a multi-player game.
	 * <p>
	 * If an instance of MultiPlayerGame already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 * @return the multi-player game instance
	 */
	public static MultiPlayerGame createMultiPlayerGame(Main main,
			DifficultySetting difficulty) {
		if (instance == null) {
			return new MultiPlayerGame(main, difficulty);
		} else {
			Exception e = new Exception("Attempting to create a " +
					"second instance of MultiPlayerGame");
			e.printStackTrace();
			return instance;
		}
	}

	/**
	 * Constructs a multi-player game.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	private MultiPlayerGame(Main main, DifficultySetting difficulty) {
		super(main, difficulty);
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
		player0 = new Player("Bob1", true, "127.0.0.1",
				player0Airports, player0Waypoints);
		players.add(player0);
		player1 = new Player("Bob2", false, "127.0.0.1",
				player1Airports, player1Waypoints);
		players.add(player1);

		// Create the manual control button
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
						(window.width() - 128 - (2 * xOffset)) / 3,
						32, 128, 32, 8, 4);
		
		manualControlButtons[players.get(1).getID()]
				= new ButtonText(" Take Control", manual1,
						2 * (window.width() - 128 - (2 * xOffset)) / 3,
						32, 128, 32, 8, 4);

		// Reset game attributes for each player
		deselectAircraft(players.get(0));
		deselectAircraft(players.get(1));
	}



	// Deprecated -----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();
		Player player1 = new Player("Test Player 1", true, "127.0.0.1", null, null);
		players.add(player1);
		Player player2 = new Player("Test Player 2", true, "127.0.0.1", null, null);
		players.add(player2);
		player1.setAircraft(new ArrayList<Aircraft>());
		player2.setAircraft(new ArrayList<Aircraft>());
	}

}
