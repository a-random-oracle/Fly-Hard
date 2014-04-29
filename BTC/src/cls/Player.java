package cls;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = 8301743865822241010L;

	/** The list of colours to associate with aircraft */
	public static final Integer[][] AIRCRAFT_COLOURS = new Integer[][] {
		new Integer[] {42, 51, 200},	// Blue
		new Integer[] {202, 0, 5}		// Red
	};
	
	/** The default maximum number of aircraft */
	private static final int DEFAULT_MAX_AIRCRAFT = 4;
	
	/** The possible turning states - not turning, turning left and turning right */
	public enum TurningState {NOT_TURNING, TURNING_LEFT, TURNING_RIGHT};
	
	/** The player's unique ID */
	private int id;
	
	/** The maximum number of aircraft which this player can control */
	private int maxAircraft;
	
	/** The list of airports which this player is controlling */
	private Airport[] airports;
	
	/** The aircraft under the player's control */
	private ArrayList<Aircraft> aircraft;
	
	/** Array list of flight-strips and ting */
	private ArrayList<FlightStrip> flightStrips;
	
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
	
	/** The player's current turning state */
	private TurningState turningState;
	
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
	
	/**
	 * Creates a player.
	 * <p>
	 * Players store the data which can be modified during gameplay
	 * by the user.
	 * </p>
	 * <p>
	 * In the multiplayer game, this is the data which is sent over
	 * the network to keep the games in sync.
	 * </p>
	 * @param id - the player's <b>unique</b> ID
	 * @param airports - the airports under this player's control
	 * @param waypoints - the waypoints under this player's control
	 */
	public Player(int id, Airport[] airports, Waypoint[] waypoints) {
		this.id = id;
		this.airports = airports;
		this.waypoints = waypoints;
		
		this.maxAircraft = Player.DEFAULT_MAX_AIRCRAFT;
		this.selectedAircraft = null;
		this.selectedWaypoint = null;
		this.selectedPathpoint = -1;
		this.compassClicked = false;
		this.waypointClicked = false;
		this.flightGenerationTimeElapsed = 6;
		this.controlAltitude = 30000;
		this.lives = 3;
		this.score = new Score();
		this.aircraft = new ArrayList<Aircraft>();
		this.flightStrips = new ArrayList<FlightStrip>();
		this.powerups = new ArrayList<Powerup>();
		
		
		// Set aircraft colour
		// Default is white
		aircraftColour = (id < AIRCRAFT_COLOURS.length)
				? AIRCRAFT_COLOURS[id] : new Integer[] {255, 255, 255};
	}
	
	
	/**
	 * Gets the player's unique ID.
	 * @return the player's unique ID
	 */
	public int getID() {
		return id;
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
	
	public ArrayList<FlightStrip> getFlightStrips() {
		return flightStrips;
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
	 * Gets whether the player is currently instructing an aircraft to turn left.
	 * @return <code>true</code> if the player is instructing an aircraft to
	 * 			turn left, otherwise <code>false</code>
	 */
	public boolean isTurningLeft() {
		return (turningState == TurningState.TURNING_LEFT) ? true : false;
	}
	
	/**
	 * Gets whether the player is currently instructing an aircraft to turn right.
	 * @return <code>true</code> if the player is instructing an aircraft to
	 * 			turn right, otherwise <code>false</code>
	 */
	public boolean isTurningRight() {
		return (turningState == TurningState.TURNING_RIGHT) ? true : false;
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
	
	
	/**
	 * Sets the list of aircraft under the player's control.
	 * @param aircraft - the new list of aircraft
	 */
	public void setAircraft(ArrayList<Aircraft> aircraft) {
		this.aircraft = aircraft;
	}
	
	/**
	 * Sets the selected aircraft.
	 * @param aircraft - the aircraft to select
	 */
	public void setSelectedAircraft(Aircraft aircraft) {
		this.selectedAircraft = aircraft;
	}
	
	/**
	 * Sets the selected waypoint.
	 * @param waypoint - the waypoint to select
	 */
	public void setSelectedWaypoint(Waypoint waypoint) {
		this.selectedWaypoint = waypoint;
	}
	
	/**
	 * Sets the selected pathpoint.
	 * @param pathpoint - the pathpoint to select
	 */
	public void setSelectedPathpoint(int pathpoint) {
		this.selectedPathpoint = pathpoint;
	}
	
	/**
	 * Sets whether the player has clicked a waypoint.
	 * @param clicked - whether a waypoint has been clicked or not
	 */
	public void setWaypointClicked(boolean clicked) {
		this.waypointClicked = clicked;
	}
	
	/**
	 * Sets whether the player has clicked an aircraft's compass.
	 * @param clicked - whether a compass has been clicked or not
	 */
	public void setCompassClicked(boolean clicked) {
		this.compassClicked = clicked;
	}
	
	/**
	 * Sets the player's turning state.
	 * <p>
	 * <ul>
	 * <li>NOT_TURNING indicates that the currently seleted aircraft is not
	 * being turned</li>
	 * <li>TURNING_LEFT indicates that the player is turning the currently
	 * selected aircraft to the left</li>
	 * <li>TURNING_RIGHT indicates that the player is turning the currently
	 * selected aircraft to the right</li>
	 * @param state - the state to set
	 */
	public void setTurningState(TurningState state) {
		this.turningState = state;
	}
	
	/**
	 * Sets the time since an aircraft was last generated for the player.
	 * @param time - the time to set
	 */
	public void setFlightGenerationTimeElapsed(double time) {
		this.flightGenerationTimeElapsed = time;
	}
	
	/**
	 * Sets the player's control altitude.
	 * @param altitude - the altitude to set to be highlighted
	 */
	public void setControlAltitude(int altitude) {
		this.controlAltitude = altitude;
	}
	
	/**
	 * Addsa powerup to the player.
	 * @param powerup - the powerup to add
	 */
	public void addPowerup(Powerup powerup) {
		powerups.add(powerup);
	}
	
	/**
	 * Removes a powerup from the player.
	 * @param powerup - the powerup to remove
	 */
	public void removePowerup(Powerup powerup) {
		for (int i = (powerups.size() - 1); i >= 0; i--) {
			if (powerups.get(i).equals(powerup)) {
				powerups.remove(i);
			}
		}
	}
	
	/**
	 * Removes all powerups from the player.
	 */
	public void clearPowerups() {
		powerups = new ArrayList<Powerup>();
	}
	
	/**
	 * Sets the player's score.
	 * @param score - the new score
	 */
	public void setScore(Score score) {
		this.score = score;
	}
	
	/**
	 * Sets the player's lives.
	 * @param lives - the new amount of lives
	 */
	public void setLives(int lives) {
		this.lives = lives;
	}
	
	
	@Override
	public String toString() {
		return "Player [id=" + id
				+ ", aircraft=" + aircraft.size()
				+ ", compassClicked=" + compassClicked
				+ ", waypointClicked=" + waypointClicked
				+ ", flightGenerationTimeElapsed=" + flightGenerationTimeElapsed
				+ ", controlAltitude=" + controlAltitude
				+ ", score=" + score + "]";
	}
	
}
