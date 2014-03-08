package scn;

import btc.Main;
import lib.jog.audio.Sound;

public abstract class Scene implements lib.jog.input.EventHandler {

	protected Main main;

	/**
	 * Top level constructor for a scene.
	 * @param main the main class holding the scene, i.e. the running instance of the game
	 */
	protected Scene(Main main) {
		this.main = main; 
	}

	/**
	 * Handles initialisation of a scene. Only runs at the start of the scene.
	 */
	public abstract void start();

	/**
	 * Handles updates of all objects requiring updates in the scene.
	 * <p>
	 * Called regularly by main.
	 * </p>
	 * @param timeDifference the time since the last update was carried out
	 */
	public abstract void update(double timeDifference);

	/**
	 * Handles drawing of all objects in the scene to the window.
	 * <p>
	 * Called regularly by main.
	 * </p>
	 */
	public abstract void draw();

	/**
	 * Used to cleanly exit a scene, e.g. halting the scene's
	 * music so it does not overlap the next scene's music.
	 * <p>
	 * Runs once only when a scene is closed.
	 * </p>
	 */
	public abstract void close();

	/**
	 * Plays a sound effect.
	 * <p>
	 * Can be used by a scene's object to request a sound to be played,
	 * e.g. aircraft can request a warning beep.
	 * </p>
	 * @param sound the sound to be played
	 */
	public abstract void playSound(Sound sound);

}



