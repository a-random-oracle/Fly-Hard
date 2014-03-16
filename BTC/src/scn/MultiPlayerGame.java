package scn;

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
	@Deprecated
	public void initializeAircraftArray() {
	}

}