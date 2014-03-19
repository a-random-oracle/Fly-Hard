package scn;

import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.window;

import cls.Aircraft;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public class SinglePlayerGame extends Game {


	/**
	 * Creates a new instance of a single player game.
	 * <p>
	 * If an instance of Game already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 * @return the single player game instance
	 */
	public static SinglePlayerGame createSinglePlayerGame(Main main,
			DifficultySetting difficulty) {
		if (instance == null) {
			return new SinglePlayerGame(main, difficulty);
		} else {
			Exception e = new Exception("Attempting to create a " +
					"second instance of Game");
			e.printStackTrace();
			return (SinglePlayerGame) instance;
		}
	}
	
	/**
	 * Constructor for SinglePlayerGame.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	private SinglePlayerGame(Main main, DifficultySetting difficulty) {
		super(main, difficulty);
		
		instance = this;
	}


	// Implemented methods --------------------------------------------------------------

	/**
	 * Initialise and begin music, initialise background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		super.start();
		
		// Assign location waypoints to the player
		locationWaypointMap.put(0, 0);
		locationWaypointMap.put(1, 0);
		locationWaypointMap.put(2, 0);
		locationWaypointMap.put(3, 0);
		locationWaypointMap.put(4, 0);
		locationWaypointMap.put(5, 0);
		
		// Generate list of waypoints to pass to the player
		Waypoint[] playersWaypoints = new Waypoint[airspaceWaypoints.length
		                                           + locationWaypoints.length];
		
		for (int i = 0; i < airspaceWaypoints.length; i++) {
			playersWaypoints[i] = airspaceWaypoints[i];
		}
		
		// Add in location waypoints
		for (int i = 0; i < locationWaypoints.length; i++) {
			playersWaypoints[airspaceWaypoints.length + i]
					= locationWaypoints[i];
		}
		
		// Set up the player
		player = new Player(getNewPlayerID(), "Bob1", true, "127.0.0.1",
				airports, playersWaypoints);
		players.add(player);

		// Create the manual control button
		manualControlButtons = new ButtonText[players.size()];
		
		ButtonText.Action manual = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player);
			}
		};

		manualControlButtons[player.getID()]
				= new ButtonText(" Take Control", manual,
						(window.width() - 128 - (2 * xOffset)) / 2,
						32, 128, 32, 8, 4);

		// Reset game attributes for each player
		deselectAircraft(player);
	}


	// Deprecated -----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();
		player = new Player(getNewPlayerID(), "Test Player",
				true, "127.0.0.1", null, null);
		players.add(this.player);
		player.setAircraft(new ArrayList<Aircraft>());
	}

}
