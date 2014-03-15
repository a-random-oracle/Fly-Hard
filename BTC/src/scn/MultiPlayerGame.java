package scn;

import java.util.ArrayList;

import btc.Main;

import cls.Aircraft;
import cls.Waypoint;

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
		
	}

	@Override
	public void update(double timeDifference) {
	}

	@Override
	public void draw() {
	}

	@Override
	public void mousePressed(int key, int x, int y) {
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
	}

	@Override
	public void keyPressed(int key) {
	}

	@Override
	public void keyReleased(int key) {
	}

	@Override
	protected void checkCollisions(double timeDifference) {
	}

	@Override
	protected void gameOver(Aircraft plane1, Aircraft plane2) {
	}

	@Override
	protected void generateFlight() {
	}

	@Override
	protected Aircraft createAircraft() {
		return null;
	}

	@Override
	protected void deselectAircraft() {
	}

	@Override
	public void takeOffWaitingAircraft() {
	}

	@Override
	public ArrayList<Waypoint> getAvailableEntryPoints() {
		return null;
	}

	@Override
	protected boolean isAirportName(String name) {
		return false;
	}

	@Override
	protected boolean compassClicked(int x, int y) {
		return false;
	}

	@Override
	protected Aircraft findClickedAircraft(int x, int y) {
		return null;
	}

	@Override
	protected Waypoint findClickedWaypoint(int x, int y) {
		return null;
	}

	@Override
	@Deprecated
	public void initializeAircraftArray() {
	}

}
