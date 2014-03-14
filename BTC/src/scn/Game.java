package scn;

import java.text.DecimalFormat;
import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Music;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;

import cls.Aircraft;
import cls.Airport;
import cls.OrdersBox;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public abstract class Game extends Scene {
	
	protected static ArrayList<Player> players;
	
	// Due to the way the airspace elements are drawn (graphics.setviewport)
	// these variables are needed to manually adjust mouse listeners and elements
	// drawn outside the airspace so that they align with the airspace elements.
	// These variables can be used to adjust the size of the airspace view.
	
	/** The distance between the left edge of the screen and the map area */
	protected static int xOffset;
	
	/** The distance between the top edge of the screen and the map area */
	protected static int yOffset;
	
	// PLEASE DO NOT REMOVE - this is very useful for debugging
	protected OrdersBox out;
	
	/** Difficulty settings: easy, medium and hard */
	public enum DifficultySetting {EASY, MEDIUM, HARD}
	
	/** The default maximum number of aircraft */
	public static final int DEFAULT_MAX_AIRCRAFT = 5;
	
	/** The current difficulty setting */
	protected DifficultySetting difficulty;
	
	/** Time since the scene began */
	protected static double timeElapsed;
	
	/** A list of aircraft which are waiting to take off */
	protected static ArrayList<Aircraft> aircraftWaitingToTakeOff;
	
	/** A list of aircraft which have recently left the airspace */
	protected ArrayList<Aircraft> recentlyDepartedAircraft;
	
	/** The image to use for aircraft */
	protected Image aircraftImage;
	
	/** Music to play during the game scene */
	protected Music music;
	
	/** The background to draw in the airspace */
	protected Image background;
	
	/** The manual control buttons */
	protected ButtonText[] manualControlButtons;
	
	/** Is the game paused */
	protected boolean paused;
	
	
	// Constructors ---------------------------------------------------------------------
	
	/**
	 * Constructor for Demo.
	 * @param main
	 * 			the main containing the scene
	 * @param difficulty
	 * 			the difficulty the scene is to be initialised with
	 */
	public Game(Main main, DifficultySetting difficulty) {
		super(main);
		this.difficulty = difficulty;
	}
	
	
	// Implemented methods --------------------------------------------------------------
	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		// Set screen offsets
		xOffset = 196;
		yOffset = 48;
		
		// Set up players
		players = new ArrayList<Player>();
	}
	
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
	 * Draws aircraft.
	 * <p>
	 * Calls the aircraft.draw() method for each aircraft.
	 * </p>
	 * <p>
	 * Also draws flight paths, and the manual control compass.
	 * </p>
	 */
	protected void drawAircraft(ArrayList<Aircraft> aircraftInAirspace,
			Aircraft selectedAircraft, Waypoint clickedWaypoint,
			int selectedPathpoint, int highlightedAltitude) {
		graphics.setColour(255, 255, 255);

		// Draw all aircraft, and show their routes if the mouse is hovering
		// above them
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.draw(highlightedAltitude);
			
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
		
		// Handle the selected aircraft
		if (selectedAircraft != null) {
			// Draw the compass around the selected aircraft, but only if it is
			// being manually controlled
			if (selectedAircraft.isManuallyControlled()) {
				selectedAircraft.drawCompass();
			}

			// If the selected aircraft's flight path is being manipulated,
			// draw the manipulated path
			if (clickedWaypoint != null && !selectedAircraft.isManuallyControlled()) {
				selectedAircraft.drawModifiedPath(selectedPathpoint,
						input.mouseX() - xOffset,
						input.mouseY() - yOffset);
			}

			// Draw the selected aircraft's flight path
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
		}
	}

	/**
	 * Draws waypoints.
	 * <p>
	 * Calls the waypoint.draw() method for each waypoint, excluding airport
	 * waypoints.
	 * </p>
	 * <p>
	 * Also prints the names of the entry/exit points.
	 * </p>
	 */
	protected void drawWaypoints(Waypoint[] airspaceWaypoints,
			Waypoint[] locationWaypoints) {
		// Draw all waypoints, except airport waypoints
		for (Waypoint waypoint : airspaceWaypoints) {
			if (!(waypoint instanceof Airport)) {
				waypoint.draw();
			}
		}

		// Draw entry/exit points
		graphics.setColour(graphics.green);
		
		graphics.print(locationWaypoints[0].getName(),
				locationWaypoints[0].getLocation().getX() + 9,
				locationWaypoints[0].getLocation().getY() - 6);
		graphics.print(locationWaypoints[1].getName(),
				locationWaypoints[1].getLocation().getX() + 9,
				locationWaypoints[1].getLocation().getY() - 6);
		graphics.print(locationWaypoints[2].getName(),
				locationWaypoints[2].getLocation().getX() - 141,
				locationWaypoints[2].getLocation().getY() - 6);
		graphics.print(locationWaypoints[3].getName(),
				locationWaypoints[3].getLocation().getX() - 91,
				locationWaypoints[3].getLocation().getY() - 6);
	}

	/**
	 * Draws airports.
	 * <p>
	 * Calls the airport.draw() method for each airport.
	 * </p>
	 * <p>
	 * Also prints the names of the airports.
	 * </p>
	 */
	protected void drawAirports(Airport[] airports,
			Waypoint[] locationWaypoints) {
		// Draw the airports
		for (Airport airport : airports) {
			graphics.setColour(255, 255, 255, 64);
			airport.draw();
		}
		
		// Draw the airport names
		graphics.setColour(graphics.green);
		
		graphics.print(locationWaypoints[4].getName(),
				locationWaypoints[4].getLocation().getX() - 20,
				locationWaypoints[4].getLocation().getY() + 25);
		graphics.print(locationWaypoints[5].getName(),
				locationWaypoints[5].getLocation().getX() - 20,
				locationWaypoints[5].getLocation().getY() + 25);
	}

	/**
	 * Draws the manual control button.
	 */
	protected void drawManualControlButton(Player player) {
		if (player.getSelectedAircraft() != null) {
			graphics.setColour(graphics.green);
			// Display the manual control button
			graphics.setColour(graphics.black);
			graphics.rectangle(true,
					(window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false,
					(window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32);
			manualControlButtons[player.getID()].draw();
		}
	}

	/**
	 * Draws a readout of the time the game has been played for, and number of planes
	 * in the sky.
	 */
	protected void drawAdditional(int aircraftCount) {
		graphics.setColour(graphics.green);
		
		// Reset the viewport - these statistics can appear outside the game
		// area
		graphics.setViewport();
		
		// Get the time the game has been played for
		int hours = (int)(timeElapsed / (60 * 60));
		int minutes = (int)(timeElapsed / 60);
		minutes %= 60;
		double seconds = timeElapsed % 60;

		// Display this in the form 'hh:mm:ss'
		DecimalFormat df = new DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes)
				+ df.format(seconds);

		// Print this to the screen
		graphics.print(timePlayed, window.width() - xOffset
				- (timePlayed.length() * 8 + 32), 32);

		// Print the highlighted altitude to the screen TODO <- check with Mark
		//graphics.print(String.valueOf("Highlighted altitude: " + Integer
		//		.toString(highlightedAltitude)) , 32 + xOffset, 15);

		// Print the number of aircraft in the airspace to the screen
		graphics.print(String.valueOf(aircraftCount)
				+ " aircraft in the airspace.", 32 + xOffset, 32);
	}
	
	/**
	 * Plays the music attached to the game.
	 */
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}
	
	
	// Event handling -------------------------------------------------------------------

	/**
	 * Handles mouse click events.
	 */
	@Override
	public abstract void mousePressed(int key, int x, int y);
	
	/**
	 * Handles mouse release events.
	 */
	@Override
	public abstract void mouseReleased(int key, int x, int y);
	
	/**
	 * Handles key press events.
	 */
	@Override
	public abstract void keyPressed(int key);

	/**
	 * Handles key release events.
	 */
	@Override
	public abstract void keyReleased(int key);
	
	
	// Game ending ----------------------------------------------------------------------
	
	/**
	 * Check if any aircraft in the airspace have collided.
	 * @param timeDifference
	 * 			the time since the last collision check
	 */
	protected abstract void checkCollisions(double timeDifference);
	
	/**
	 * Handle game over.
	 * @param plane1
	 * 			the first plane involved in the collision
	 * @param plane2
	 * 			the second plane in the collision
	 */
	protected abstract void gameOver(Aircraft plane1, Aircraft plane2);
	
	/**
	 * Cleanly exit by stopping the scene's music.
	 */
	@Override
	public void close() {
		music.stop();
	}
	
	
	// Helper methods -------------------------------------------------------------------
	
	/**
	 * The interval in seconds to generate flights after.
	 */
	protected int getFlightGenerationInterval(Player player) {
		switch (difficulty) {
		case MEDIUM:
			// Planes move 2x faster on medium so this makes them spawn
			// 2 times as often to keep the ratio
			return (30 / (player.getMaxAircraft() * 2));
		case HARD:
			// Planes move 3x faster on hard so this makes them spawn
			// 3 times as often to keep the ratio 
			return (30 / (player.getMaxAircraft() * 3) );
		default:
			return (30 / player.getMaxAircraft());
		}	
	}
	
	/**
	 * Creates a new aircraft object and introduces it to the airspace. 
	 */
	protected abstract void generateFlight();
	
	/**
	 * Handle aircraft creation.
	 * @return the created aircraft object
	 */
	protected abstract Aircraft createAircraft();
	
	/**
	 * Causes an aircraft to call methods to handle deselection.
	 */
	protected abstract void deselectAircraft();
	
	/**
	 * Causes a player's selected aircraft to call methods to toggle manual control.
	 */
	protected void toggleManualControl(Player player) {
		Aircraft selectedAircraft = player.getSelectedAircraft();
		
		if (selectedAircraft == null) return;

		selectedAircraft.toggleManualControl();
		manualControlButtons[player.getID()].setText(
				(selectedAircraft.isManuallyControlled() ?
						"Remove" : " Take") + " Control");
	}
	
	/**
	 * Sets the airport to busy, and adds any aircraft waiting to take off to the game,
	 * where they begins their flight plans starting at the airport.
	 */
	public abstract void takeOffWaitingAircraft();
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane.
	 */	
	public abstract ArrayList<Waypoint> getAvailableEntryPoints();
	
	/**
	 * Returns whether a given name is an airport or not.
	 * @param name
	 * 			the name to test
	 * @return <code>true</code> if the name matches an airport name,
	 * 			otherwise <code>false</code>
	 */
	protected abstract boolean isAirportName(String name);
	
	
	// Click event helpers --------------------------------------------------------------
	
	/**
	 * Gets whether the manual control compass has been clicked or not.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return <code>true</code> if the compass has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected abstract boolean compassClicked(int x, int y);
	
	/**
	 * Gets whether an aircraft has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return <code>true</code> if an aircraft has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected boolean aircraftClicked(int x, int y) {
		return (findClickedAircraft(x, y) != null);
	}
	
	/**
	 * Gets the aircraft which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return if an aircraft was clicked, returns the corresponding aircraft object,
	 * 			otherwise returns null
	 */
	protected abstract Aircraft findClickedAircraft(int x, int y);
	
	/**
	 * Gets whether a waypoint in an aircraft's flight plan has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return <code>true</code> if a waypoint in an aircraft's flight plan
	 * 			has been clicked, otherwise <code>false</code>
	 */
	protected boolean waypointInFlightplanClicked(int x, int y, Aircraft aircraft) {
		Waypoint clickedWaypoint = findClickedWaypoint(x, y);
		return (clickedWaypoint != null) && (aircraft != null)
				&& (aircraft.getFlightPlan().indexOfWaypoint(clickedWaypoint) > -1);
	}
	
	/**
	 * Gets the waypoint which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return if a waypoint was clicked, returns the corresponding waypoint object,
	 * 			otherwise returns null
	 */
	protected abstract Waypoint findClickedWaypoint(int x, int y);
	
	/**
	 * Gets whether the manual control button has been clicked.
	 * @param x
	 * 			the mouse's x position
	 * @param y
	 * 			the mouse's y position
	 * @param player
	 * 			the player to check this for
	 * @return <code>true</code> if the manual control button has been
	 * 			pressed, otherwise <code>false</code>
	 */
	protected boolean manualOverridePressed(int x, int y, Player player) {
		return manualControlButtons[player.getID()]
				.isMouseOver(x - xOffset, y - yOffset);
	}
	
	
	// Accessors ------------------------------------------------------------------------
	
	/**
	 * Gets the horizontal offset of the game region.
	 * @return the horizontal offset of the game region
	 */
	public static int getXOffset() {
		return xOffset;
	}
	
	/**
	 * Gets the vertical offset of the game region.
	 * @return the vertical offset of the game region
	 */
	public static int getYOffset() {
		return yOffset;
	}
	
	/**
	 * Gets how long the game has been played for.
	 * @return the length of time the game has been running for
	 */
	public static double getTime() {
		return timeElapsed;
	}
	
	/**
	 * Generates a new unique player ID.
	 * <p>
	 * This will be one higher than the highest ID currently
	 * in use.
	 * </p>
	 * @return a new unique player ID
	 */
	public static int getNewPlayerID() {
		// Start player ID's at 0
		int maxPlayerID = -1;
		
		// Search through list of players to find highest ID
		// currently in use
		for (Player player : players) {
			if (player.getID() > maxPlayerID) maxPlayerID = player.getID();
		}
		
		// Return an ID one higher than the highest ID currently
		// in use
		return maxPlayerID + 1;
	}
	
	
	// Mutators -------------------------------------------------------------------------
	
	protected static ArrayList<Player> getPlayers() {
		return players;
	}
	
	/**
	 * Adds an aircraft to the list of aircraft waiting to take off. TODO
	 * @param aircraft
	 * 			the aircraft to add
	 */
	public static void addAircraftWaitingToTakeOff(Aircraft aircraft) {
		aircraftWaitingToTakeOff.add(aircraft);
	}
	
	
	// Deprecated -----------------------------------------------------------------------
	
	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class).
	 * Its purpose is to initialize array where aircraft are stored. 
	 */	
	@Deprecated
	public abstract void initializeAircraftArray();
	
}
