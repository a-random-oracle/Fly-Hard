package cls;

import java.io.Serializable;
import java.util.ArrayList;

import lib.jog.window;
import scn.Game;

public class Player implements Serializable {
	
	// TODO last updated: 2014.03.19 23:45
	private static final long serialVersionUID = 8301743865822241010L;

	/** The list of colours to associate with aircraft */
	public static final Integer[][] AIRCRAFT_COLOURS = new Integer[][] {
		new Integer[] {42, 51, 200},	// Blue
		new Integer[] {202, 0, 5}		// Red
	};
	
	/** The player's unique ID */
	private int id;
	
	/** The player's screen name */
	private String name;
	
	/** The width of the player's window */
	private double windowWidth;
	
	/** The height of the player's window */
	private double windowHeight;
	
	/** The maximum number of aircraft which this player can control */
	private int maxAircraft;
	
	/** The list of airports which this player is controlling */
	private Airport[] airports;
	
	/** The aircraft under the player's control */
	private ArrayList<Aircraft> aircraft;
	
	/** The waypoints under the player's control */
	private Waypoint[] waypoints;
	
	/** The colour to draw this player's aircraft */
	private Integer[] aircraftColour;
	
	/** The aircraft which this player has selected */
	private Aircraft selectedAircraft;
	
	/** The currently selected waypoint */
	private Waypoint selectedWaypoint;

	/** The position of the selected waypoint in the selected aircraft's route */
	private int selectedPathpoint;
	
	/** Tracks if the manual heading compass of an aircraft has been clicked */
	private boolean compassClicked;
	
	/** Tracks if a waypoint in the selected aircraft's flight plan has been clicked */
	private boolean waypointClicked;
	
	/** The time elapsed since the last flight was generated */
	private double flightGenerationTimeElapsed;
	
	/** The player's current control altitude */
	private int controlAltitude;
	
	/** The powerups this player currently has */
	private ArrayList<Powerup> powerups;
	
	/** The player's score */
	private Score score;
	
	/** The player's remaining lives */
	private int lives;
	
	
	// Constructor: ---------------------------------------------------------------------
	
	public Player(int id, String name, boolean hosting, String ipAddress,
			Airport[] airports, Waypoint[] waypoints) {
		
		// Reset values
		this.maxAircraft = Game.DEFAULT_MAX_AIRCRAFT;
		this.selectedAircraft = null;
		this.selectedWaypoint = null;
		this.selectedPathpoint = -1;
		this.compassClicked = false;
		this.waypointClicked = false;
		this.flightGenerationTimeElapsed = 6;
		this.controlAltitude = 30000;
		this.lives = 3;
		
		// Initialise arrays
		this.aircraft = new ArrayList<Aircraft>();
		this.powerups = new ArrayList<Powerup>();
		
		// Set attributes
		this.id = id;
		this.name = name;
		this.windowWidth = window.width();
		this.windowHeight = window.height();
		this.airports = airports;
		this.waypoints = waypoints;
		this.score = new Score();
		
		// Set aircraft colour
		// Default is white
		aircraftColour = (id < AIRCRAFT_COLOURS.length)
				? AIRCRAFT_COLOURS[id] : new Integer[] {255, 255, 255};
	}
	
	
	// Accessors: -----------------------------------------------------------------------
	
	/**
	 * Gets the player's unique ID.
	 * @return the player's unique ID
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Gets the player's screen name.
	 * @return the player's screen name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the player's window's width.
	 * @return the window's width
	 */
	public double getWindowWidth() {
		return windowWidth;
	}
	
	/**
	 * Gets the player's window's height.
	 * @return the window's height
	 */
	public double getWindowHeight() {
		return windowHeight;
	}
	
	/**
	 * Gets the maximum number of aircraft allowed for this player.
	 * @return the maximum number of aircraft allowed for this player
	 */
	public int getMaxAircraft() {
		return maxAircraft;
	}
	
	/**
	 * Gets a list of the player's airports.
	 * @return a list of the player's airports
	 */
	public Airport[] getAirports() {
		return airports;
	}

	/**
	 * Gets a list of the player's aircraft.
	 * @return a list of the player's aircraft
	 */
	public ArrayList<Aircraft> getAircraft() {
		return aircraft;
	}
	
	/**
	 * Gets a list of the player's waypoints.
	 * @return a list of the player's waypoints
	 */
	public Waypoint[] getWaypoints() {
		return waypoints;
	}
	
	/**
	 * Gets the colour to draw the player's aircraft.
	 * @return the colour to draw the player's aircraft
	 */
	public Integer[] getAircraftColour() {
		return aircraftColour;
	}

	/**
	 * Gets the aircraft the player has selected.
	 * @return the selected aircraft
	 */
	public Aircraft getSelectedAircraft() {
		return selectedAircraft;
	}

	/**
	 * Gets the waypoint the player has selected.
	 * @return the selected waypoint
	 */
	public Waypoint getSelectedWaypoint() {
		return selectedWaypoint;
	}

	/**
	 * Gets the pathpoint the player has selected.
	 * @return the selected pathpoint
	 */
	public int getSelectedPathpoint() {
		return selectedPathpoint;
	}

	/**
	 * Gets whether the player has clicked the compass or not.
	 * @return whether the compass has been clicked or not
	 */
	public boolean isCompassClicked() {
		return compassClicked;
	}

	/**
	 * Gets whether the player has clicked a waypoint or not.
	 * @return whether a waypoint has been clicked or not
	 */
	public boolean isWaypointClicked() {
		return waypointClicked;
	}
	
	/**
	 * Gets the time since flight generation was last reset.
	 * @return the time since flight generation was last reset
	 */
	public double getFlightGenerationTimeElapsed() {
		return flightGenerationTimeElapsed;
	}

	/**
	 * Gets the player's control altitude.
	 * @return the player's control altitude
	 */
	public int getControlAltitude() {
		return controlAltitude;
	}
	
	/**
	 * Gets the player's score.
	 * @return the player's score
	 */
	public Score getScore() {
		return score;
	}
	
	/** 
	 * Gets the player's remaining lives.
	 * @return the player's remaining lives
	 */
	public int getLives() {
		return lives;
	}
	
	/**
	 * Gets a list of the player's active powerups.
	 * @return a list of the player's active powerups
	 */
	public ArrayList<Powerup> getPowerups() {
		return powerups;
	}
	
	
	// Mutators: ------------------------------------------------------------------------
	
	/**
	 * @param aircraft
	 * 			the new list of aircraft
	 */
	public void setAircraft(ArrayList<Aircraft> aircraft) {
		this.aircraft = aircraft;
	}
	
	/**
	 * @param aircraft
	 * 			the aircraft to select
	 */
	public void setSelectedAircraft(Aircraft aircraft) {
		this.selectedAircraft = aircraft;
	}
	
	/**
	 * @param waypoint
	 * 			the waypoint to select
	 */
	public void setSelectedWaypoint(Waypoint waypoint) {
		this.selectedWaypoint = waypoint;
	}
	
	/**
	 * @param pathpoint
	 * 			the pathpoint to select
	 */
	public void setSelectedPathpoint(int pathpoint) {
		this.selectedPathpoint = pathpoint;
	}
	
	/**
	 * @param clicked
	 * 			whether a waypoint has been clicked or not
	 */
	public void setWaypointClicked(boolean clicked) {
		this.waypointClicked = clicked;
	}
	
	/**
	 * @param clicked
	 * 			whether the compass has been clicked or not
	 */
	public void setCompassClicked(boolean clicked) {
		this.compassClicked = clicked;
	}
	
	/**
	 * @param time
	 * 			the time to set
	 */
	public void setFlightGenerationTimeElapsed(double time) {
		this.flightGenerationTimeElapsed = time;
	}
	
	/**
	 * @param altitude
	 * 			the altitude to set to be highlighted
	 */
	public void setControlAltitude(int altitude) {
		this.controlAltitude = altitude;
	}
	
	/**
	 * @param powerup
	 * 			the powerup to add
	 */
	public void addPowerup(Powerup powerup) {
		powerups.add(powerup);
	}
	
	/**
	 * @param powerup
	 * 			the powerup to remove
	 */
	public void removePowerup(Powerup powerup) {
		for (int i = (powerups.size() - 1); i >= 0; i--) {
			if (powerups.get(i).equals(powerup)) {
				powerups.remove(i);
			}
		}
	}
	
	/**
	 * Removes all powerups from this player.
	 */
	public void clearPowerups() {
		powerups = new ArrayList<Powerup>();
	}
	
	/**
	 * @param score
	 * 			the new score
	 */
	public void setScore(Score score) {
		this.score = score;
	}
	
	/**
	 * Mutator for the player's lives
	 * @param lives
	 * 				the new amount of lives
	 */
	public void setLives(int lives) {
		this.lives = lives;
	}
	
	
	// Other ----------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "Player [id=" + id
				+ ", name=" + name
				+ ", aircraft=" + aircraft.size()
				+ ", compassClicked=" + compassClicked
				+ ", waypointClicked=" + waypointClicked
				+ ", flightGenerationTimeElapsed=" + flightGenerationTimeElapsed
				+ ", controlAltitude=" + controlAltitude
				+ ", score=" + score + "]";
	}
	
}
