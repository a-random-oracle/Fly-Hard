package scn;

import java.util.ArrayList;

import cls.Aircraft;
import cls.Player;
import cls.Waypoint;

public class SinglePlayerGame extends Game {
	
	/**
	 * Creates a new instance of a single player game.
	 * <p>
	 * If an instance of Game already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param difficulty
	 * 			the difficulty the scene is to be initialised with
	 * @return the single player game instance
	 */
	public static SinglePlayerGame createSinglePlayerGame(DifficultySetting difficulty) {
		if (instance == null) {
			return new SinglePlayerGame(difficulty);
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
	private SinglePlayerGame(DifficultySetting difficulty) {
		super(difficulty);
		
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
		player = new Player(0, airports, playersWaypoints);

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
		player = new Player(0, null, null);
		player.setAircraft(new ArrayList<Aircraft>());
	}

}
