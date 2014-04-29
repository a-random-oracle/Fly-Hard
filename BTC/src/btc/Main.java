package btc;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import net.NetworkManager;

import org.lwjgl.Sys;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;

import scn.Scene;
import scn.Title;
import lib.jog.*;

/**
 * <h1>Main</h1>
 * <p>
 * Main class that is run when game is run.
 * Handles the scenes (game states).
 * </p>
 */
public class Main implements input.EventHandler {

	/**
	 * Creates a new instance of Main, starting a new game.
	 * @param args any command-line arguments.
	 */
	public static void main(String[] args) {
		Main.testing = false;
		new Main(false);
	}

	/** The title to display in the game window */
	private final String TITLE = "Bear Traffic Controller: GOA Edition";

	/** The target window width */
	public final static int TARGET_WIDTH = 1280;

	/** The target window height */
	public final static int TARGET_HEIGHT = 960;

	/** The default size of the gap between the window edge and the left edge of the screen */
	public final static int WIDTH_GAP = 50;

	/** The default size of the gap between the window edge and the top edge of the screen */
	public final static int HEIGHT_GAP = 50;

	/** The scale the game has been resized to in the horizontal plane */
	private static double xScale = 1;

	/** The scale the game has been resized to in the vertical plane */
	private static double yScale = 1;

	/** The random instance to use to synchronise across the network */
	private static Random random;

	/** Whether the game isbeing exited */
	private static boolean exiting;

	/** The locations of the icon files */
	final private String[] ICON_FILENAMES = {
		"gfx" + File.separator + "icon16.png",
		"gfx" + File.separator + "icon32.png",
		"gfx" + File.separator + "icon64.png",
	};

	/** Whether the game is currently being tested or not */
	public static boolean testing = true;

	private double lastFrameTime;
	private double timeDifference;
	private static Stack<Scene> sceneStack;
	private static Scene currentScene;
	private int fpsCounter;
	private long lastFpsTime;

	/**
	 * Constructor for Main. Initialises the jog library classes, and then
	 * begins the game loop, calculating time between frames, and then when
	 * the window is closed it releases resources and closes the program
	 * @param fullscreen - should the game run fullscreen
	 */
	private Main(boolean fullscreen) {
		double xOffset = 0;
		double yOffset = 0;

		// Set up the random instance
		random = new Random();

		// Get screen dimensions
		Rectangle windowBounds = GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();

		double actualWidth = windowBounds.width;
		double actualHeight = windowBounds.height;

		double width = actualWidth;
		double height = actualHeight;

		if (fullscreen) {
			xScale = actualWidth / TARGET_WIDTH;
			yScale = actualHeight / TARGET_HEIGHT;
		} else {
			xScale = (actualWidth - (WIDTH_GAP * 2)) / TARGET_WIDTH;
			yScale = (actualHeight - (HEIGHT_GAP * 2)) / TARGET_HEIGHT;

			// Scale the width and height by the values derived above
			width = (TARGET_WIDTH - (WIDTH_GAP * 2)) * xScale;
			height = (TARGET_HEIGHT - (HEIGHT_GAP * 2)) * yScale;

			// Scale the X and Y offsets by the values derived above
			xOffset = (int)((actualWidth - width) / 2);
			yOffset = (int)((actualHeight - height) / 2);
		}

		start(width, height, xOffset, yOffset, fullscreen);

		while(!window.isClosed() && !exiting) {
			timeDifference = getTimeSinceLastFrame();
			update(timeDifference);
			draw();
		}
		quit();
	}

	/**
	 * Creates window, initialises jog classes and sets starting values to variables.
	 */
	private void start(double width, double height, double xOffset,
			double yOffset, boolean fullscreen) {
		window.setIcon(ICON_FILENAMES);
		window.initialise(TITLE, (int)(width),(int)(height),
				(int)(xOffset), (int)(yOffset), fullscreen);
		graphics.initialise();
		
		//graphics.Font font = graphics.newBitmapFont("gfx" + File.separator + "font.png",
		//		("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz" +
		//				"1234567890.,_-!?()[]><#~:;/\\^'\"{}+=@@@@@@@@`"));
		
		java.awt.Font font = null;
		
		try {
			font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
						ResourceLoader.getResourceAsStream(
								"gfx" + File.separator + "Roboto-Black.ttf"))
								.deriveFont(12F);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		graphics.setFont(new TrueTypeFont(font, false));

		sceneStack = new Stack<Scene>();
		setScene(new Title());

		lastFrameTime = (double)(Sys.getTime()) / Sys.getTimerResolution();
		lastFpsTime = Sys.getTime()* 1000 / Sys.getTimerResolution(); // Set to current Time
	}

	/**
	 * Updates audio, input handling, the window, the current scene and FPS.
	 * @param timeDifference - the time elapsed since the last frame.
	 */
	private void update(double timeDifference) {
		audio.update();
		input.update(this);
		window.update();
		currentScene.update(timeDifference);
		updateFPS();
	}

	/**
	 * Calculates the time since the last frame in seconds as a double-precision
	 * floating point number.
	 * @return the time in seconds since the last frame.
	 */
	private double getTimeSinceLastFrame() {
		double currentTime = (double)(Sys.getTime()) / Sys.getTimerResolution();
	    double delta = currentTime - lastFrameTime;
	    lastFrameTime = currentTime; // Update last frame time
	    return delta;
	}

	/**
	 * Clears the graphical viewport and calls the draw function of the current
	 * scene.
	 */
	private void draw() {
		graphics.clear();
		currentScene.draw();
	}

	/**
	 * Closes the current scene, closes the window, releases the audio
	 * resources and quits the process.
	 */
	public static void quit() {
		currentScene.close();
		NetworkManager.stopThread();
		window.dispose();
		audio.dispose();
		System.exit(0);
	}

	/**
	 * Closes the current scene, adds new scene to scene stack and starts it
	 * @param newScene - the scene to set as current scene
	 */
	public static void setScene(Scene newScene) {
		if (currentScene != null) currentScene.close();
		// Add new scene to scene stack and set to current scene
		currentScene = sceneStack.push(newScene);
		currentScene.start();
	}

	/**
	 * Closes the current scene, pops it from the stack and sets current
	 * scene to top of stack.
	 */
	public static void closeScene() {
		currentScene.close();
		sceneStack.pop();
		currentScene = sceneStack.peek();
	}

	/**
	 * Updates the FPS - increments the FPS counter.
	 * <p>
	 * If it has been over a second since the FPS was updated, update it.
	 * </p>
	 */
	public void updateFPS() {
		long current_time = ((Sys.getTime()* 1000) / Sys.getTimerResolution());
		if (current_time - lastFpsTime > 1000) { // Update once per second
			window.setTitle(TITLE + " - FPS: " + fpsCounter);
			fpsCounter = 0; // Reset the FPS counter
			lastFpsTime += current_time - lastFpsTime; // Add on the time difference
		}
		fpsCounter++;
	}

	public static double getXScale() {
		return xScale;
	}

	public static double getYScale() {
		return yScale;
	}

	public static Random getRandom() {
		return random;
	}

	public static void setRandomSeed(int seed) {
		random.setSeed(seed);
	}

	public static void setExiting() {
		exiting = true;
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		currentScene.mousePressed(key, x, y);
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		currentScene.mouseReleased(key, x, y);
	}

	@Override
	public void keyPressed(int key) {
		currentScene.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		currentScene.keyReleased(key);
	}

}
