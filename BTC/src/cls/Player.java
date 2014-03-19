package cls;

import java.io.Serializable;
import java.util.ArrayList;

import scn.Game;

public class Player implements Serializable {
	
	// TODO last updated: 2014.03.17 22:50
	private static final long serialVersionUID = -1355541630946018678L;

	/** The list of colours to associate with aircraft */
	public static final Integer[][] AIRCRAFT_COLOURS = new Integer[][] {
		new Integer[] {42, 51, 159},	// Blue
		new Integer[] {202, 0, 5}		// Red
	};
	
	/** The player's unique ID */
	private int id;
	
	/** The player's screen name */
	private String name;
	
	/** Whether the player is hosting the connection or not */
	private boolean hosting;
	
	/** The player's IP address */
	private String ipAddress;
	
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
	private int score;
	
	
	// Constructor: -----------------------------------------------------------
	
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
		
		// Initialise arrays
		this.aircraft = new ArrayList<Aircraft>();
		this.powerups = new ArrayList<Powerup>();
		
		// Set attributes
		this.id = id;
		this.name = name;
		this.hosting = hosting;
		this.ipAddress = ipAddress;
		this.airports = airports;
		this.waypoints = waypoints;
		
		// Set aircraft colour
		// Default is white
		aircraftColour = (id < AIRCRAFT_COLOURS.length)
				? AIRCRAFT_COLOURS[id] : new Integer[] {255, 255, 255};
	}
	
	
	// Accessors: -------------------------------------------------------------
	
	/**
	 * @return the player's unique ID
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * @return the player's screen name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return whether the player is currently the host
	 */
	public boolean isHosting() {
		return hosting;
	}
	
	/**
	 * @return the player's IP address
	 */
	public String getIPAddress() {
		return ipAddress;
	}
	
	/**
	 * @return the maximum number of aircraft allowed for this player
	 */
	public int getMaxAircraft() {
		return maxAircraft;
	}
	
	/**
	 * @return a list of this player's airports
	 */
	public Airport[] getAirports() {
		return airports;
	}

	/**
	 * @return a list of this player's aircraft
	 */
	public ArrayList<Aircraft> getAircraft() {
		return aircraft;
	}
	
	/**
	 * @return a list of this player's waypoints
	 */
	public Waypoint[] getWaypoints() {
		return waypoints;
	}
	
	/**
	 * @return the colour to draw this player's aircraft
	 */
	public Integer[] getAircraftColour() {
		return aircraftColour;
	}

	/**
	 * @return the selected aircraft
	 */
	public Aircraft getSelectedAircraft() {
		return selectedAircraft;
	}

	/**
	 * @return the selected waypoint
	 */
	public Waypoint getSelectedWaypoint() {
		return selectedWaypoint;
	}

	/**
	 * @return the selected pathpoint
	 */
	public int getSelectedPathpoint() {
		return selectedPathpoint;
	}

	/**
	 * @return whether the compass has been clicked or not
	 */
	public boolean isCompassClicked() {
		return compassClicked;
	}

	/**
	 * @return whether a waypoint has been clicked or not
	 */
	public boolean isWaypointClicked() {
		return waypointClicked;
	}
	
	/**
	 * @return the time since flight generation was last reset
	 */
	public double getFlightGenerationTimeElapsed() {
		return flightGenerationTimeElapsed;
	}

	/**
	 * @return the player's control altitude
	 */
	public int getControlAltitude() {
		return controlAltitude;
	}
	
	/**
	 * @return the player's score
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * @return a list of this player's active powerups
	 */
	public ArrayList<Powerup> getPowerups() {
		return powerups;
	}
	
	
	// Mutators: --------------------------------------------------------------
	
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
	public void setScore(int score) {
		this.score = score;
	}
	
}
