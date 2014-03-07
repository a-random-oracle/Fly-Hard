package scn;

import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.audio.Music;
import lib.jog.graphics.Image;

import cls.Aircraft;
import cls.Airport;
import cls.OrdersBox;
import cls.Waypoint;

import btc.Main;

public abstract class Game extends Scene {
	
	/** The maximum number of aircraft allowed in the airspace simultaneously */
	protected static int maxAircraft = 5;
	
	// Due to the way the airspace elements are drawn (graphics.setviewport) these variables are needed to manually
	// adjust mouse listeners and elements drawn outside the airspace so that they align with the airspace elements.
	// These variables can be used to adjust the size of the airspace view.
	
	/** The distance between the left edge of the screen and the map area */
	public static int xOffset;
	
	/** The distance between the top edge of the screen and the map area */
	public static int yOffset;
	
	// PLEASE DO NOT REMOVE - this is very useful for debugging
	public static OrdersBox out;
	
	// Static constants for difficulty settings
	// Difficulty of demo scene determined by difficulty selection scene
	
	/** The easiest difficulty setting */
	public final static int DIFFICULTY_EASY = 0;
	
	/** The medium difficulty setting */
	public final static int DIFFICULTY_MEDIUM = 1;
	
	/** The hardest difficulty setting */
	public final static int DIFFICULTY_HARD = 2;
	
	/** The current difficulty setting */
	public static int difficulty;
	
	/** Time since the scene began */
	protected static double timeElapsed;
	
	/** The currently selected aircraft */
	protected Aircraft selectedAircraft;
	
	/** The currently selected waypoint */
	protected Waypoint clickedWaypoint;
	
	/** Selected path point, in an aircraft's route, used for altering the route */
	protected int selectedPathpoint;
	
	/** A list of aircraft present in the airspace */
	public static ArrayList<Aircraft> aircraftInAirspace;
	
	/** A list of aircraft which have recently left the airspace */
	public ArrayList<Aircraft> recentlyDepartedAircraft;
	
	/** The image to use for aircraft */
	protected Image aircraftImage;
	
	/** A button to start and end manual control of an aircraft */
	protected ButtonText manualOverrideButton;
	
	/** Tracks if manual heading compass of a manually controlled aircraft has been clicked */
	protected boolean compassClicked;
	
	/** Tracks if waypoint of a manually controlled aircraft has been clicked */
	protected boolean waypointClicked;
	
	/** The time elapsed since the last flight was generated */
	protected double flightGenerationTimeElapsed;
	
	/** The current control altitude of the ATCO - initially 30,000 */
	protected int highlightedAltitude;
	
	/** Music to play during the game scene */
	protected Music music;
	
	/** The background to draw in the airspace */
	protected Image background;
	
	/** Array of the airports in the airspace */
	public static Airport[] airports;
	
	/** The set of waypoints in the airspace which are entry/exit points */
	public static Waypoint[] locationWaypoints;

	/** All waypoints in the airspace, including locationWaypoints */
	public static Waypoint[] airspaceWaypoints;
	
	/** Is the game paused */
	protected boolean paused;
	
	/**
	 * Constructor for Demo.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Game(Main main) {
		super(main);
	}
	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public abstract void start();
	
	/**
	 * Update all objects within the scene, e.g. aircraft.
	 * <p>
	 * Also runs collision detection and generates a new flight if flight
	 * generation interval has been exceeded.
	 * </p>
	 */
	@Override
	public abstract void update(double timeDifference);
	
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints.
	 */
	@Override
	public abstract void draw();
	
	/**
	 * Draw waypoints, and route of a selected aircraft between waypoints.
	 * <p>
	 * Also prints waypoint names next to waypoints.
	 * </p>
	 */
	protected abstract void drawMap();
	
	/**
	 * Causes a selected aircraft to call methods to toggle manual control.
	 */
	protected abstract void toggleManualControl();
	
	/**
	 * Causes an aircraft to call methods to handle deselection.
	 */
	protected abstract void deselectAircraft();
	
	/**
	 * Cause all planes in airspace to update collisions
	 * Catch and handle a resultant game over state
	 * @param timeDifference delta time since last collision check
	 */
	protected abstract void checkCollisions(double timeDifference);
	
	/**
	 * Handle a game over caused by two planes colliding
	 * Create a gameOver scene and make it the current scene
	 * @param plane1 the first plane involved in the collision
	 * @param plane2 the second plane in the collision
	 */
	protected abstract void gameOver(Aircraft plane1, Aircraft plane2);
	
	protected abstract boolean compassClicked();
	
	protected abstract boolean aircraftClicked(int x, int y);
	
	protected abstract Aircraft findClickedAircraft(int x, int y);
	
	protected abstract boolean waypointInFlightplanClicked(int x, int y, Aircraft a);
	
	protected abstract Waypoint findClickedWaypoint(int x, int y);

	/**
	 * Handle mouse input
	 */
	@Override
	public abstract void mousePressed(int key, int x, int y);
	
	protected abstract boolean manualOverridePressed(int x, int y);

	@Override
	public abstract void mouseReleased(int key, int x, int y);

	@Override
	public abstract void keyPressed(int key);

	/**
	 * Handle keyboard input
	 */
	@Override
	public abstract void keyReleased(int key);
	
	/**
	 * Draw a readout of the time the game has been played for, and number of planes in the sky.
	 */
	protected abstract void drawAdditional();
	
	/**
	 * Creates a new aircraft object and introduces it to the airspace. 
	 */
	protected abstract void generateFlight();
	
	/**
	 * Sets the airport to busy, adds the aircraft passed to the airspace,
	 * where it begins its flight plan starting at the airport.
	 * @param aircraft the aircraft to take off
	 */
	public static void takeOffSequence(Aircraft aircraft) {
		aircraftInAirspace.add(aircraft);
		
		for (Airport airport : airports) {
			if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
				airport.isActive = false;
				return;
			}
		}
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane.
	 * <p>
	 * Specifically, returns points where no plane is currently going to exit the
	 * airspace there, also it is not too close to any plane.
	 * </p>
	 */	
	public abstract ArrayList<Waypoint> getAvailableEntryPoints();
	
	/**
	 * Handle nitty gritty of aircraft creating
	 * including randomisation of entry, exit, altitude, etc.
	 * @return the created aircraft object
	 */
	protected abstract Aircraft createAircraft();
	
	/**
	 * Cleanly exit by stopping the scene's music
	 */
	@Override
	public abstract void close();

	/**
	 * Gets how long the game has been played for.
	 * @return the length of time the game has been running for
	 */
	public static double getTime() {
		return timeElapsed;
	}
	
	/**
	 * Getter for aircraft list.
	 * @return the arrayList of aircraft in the airspace
	 */
	public abstract ArrayList<Aircraft> aircraftList();
	
	/**
	 * The interval in seconds to generate flights after.
	 */
	public abstract int getFlightGenerationInterval();
	
	/**
	 * Returns whether a given name is an airport or not.
	 * @param name the name to test
	 * @return <code>true</code> if the name matches an airport name,
	 * 			otherwise <code>false</code>
	 */
	protected abstract boolean isAirportName(String name);
	
}
