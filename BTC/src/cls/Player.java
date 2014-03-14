package cls;

import java.util.ArrayList;

import org.newdawn.slick.Color;

import scn.Game;

public class Player {
	
	/** The list of colours to associate with aircraft */
	public static final Color[] AIRCRAFT_COLOURS = new Color[] {
		Color.red, Color.blue
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
	
	/** The colour to draw this player's aircraft */
	private Color aircraftColour;
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Player(String name, boolean hosting, String ipAddress,
			Airport[] airports) {
		setDefaults();
		
		this.name = name;
		this.hosting = hosting;
		this.ipAddress = ipAddress;
		this.airports = airports;
	}
	
	private void setDefaults() {
		// Get a unique player ID
		id = Game.getNewPlayerID();
		
		// Set aircraft colour
		aircraftColour = (id < AIRCRAFT_COLOURS.length)
				? AIRCRAFT_COLOURS[id] : Color.white;
		
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
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
	 * @param score
	 * 			the new score
	 */
	public void setScore(int score) {
		this.score = score;
	}
	
}
