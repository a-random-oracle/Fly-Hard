package scn;

import java.io.File;

import org.newdawn.slick.Color;

import btc.Main;
import cls.Aircraft;
import cls.FlightStrip;
import cls.Vector;
import cls.Player;
import lib.SpriteAnimation;
import lib.TextBox;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.window;

public class GameOver extends Scene {

	/** Text box to write the details of the game failure */
	private TextBox textBox;

	// Used to position the explosion, and provide graphical feedback of how and where the player failed
	/** The first plane involved in the collision */
	private Aircraft aircraft1;
	private FlightStrip flightStrip1;

	/** The second plane involved in the collision */
	private Aircraft aircraft2;
	private FlightStrip flightStrip2;


	/** A vector storing the point which distances should be measured relative to */
	private Vector origin;

	/** A random number of deaths caused by the crash */
	private int deaths;

	/** The score the player achieved */
	private int score;

	/** The position of the crash - the vector midpoint of the positions of the two crashed planes */
	private Vector crash;

	/** A sprite animation to handle the frame by frame drawing of the explosion */
	private SpriteAnimation explosionAnim;

	/** The explosion image to use for the animation */
	private Image explosion;

	/** The value corresponding to the key which has most recently been pressed */
	private int keyPressed;

	/** Timer to allow for explosion and plane to be shown for a period, followed by the text box */
	private double timer;

	private Player player;
	
	/** Integer offset to centre vertically */
	private int yBorder = (window.height() - 440) / 2 - 20;

	/**
	 * Constructor for the Game Over scene.
	 * @param plane1
	 * 			the first plane involved in the crash
	 * @param plane2
	 * 			the second plane involved in the crash
	 * @param score
	 * 			the score achieved by the player
	 */
	public GameOver(Aircraft plane1, Aircraft plane2, FlightStrip fs1, FlightStrip fs2, int score, Player player) {
		super();
		this.player = player;
		// The number of frames in each dimension of the animation image
		int framesAcross = 8;
		int framesDown = 4;

		aircraft1 = plane1;
		aircraft2 = plane2;
		flightStrip1 = fs1;
		flightStrip2 = fs2;
		origin = new Vector(Game.getXOffset(), Game.getYOffset(), 0);

		if (aircraft1 != null && aircraft2 != null) {
			crash = plane1.getPosition().add(new Vector((plane1.getPosition().getX()
							- plane2.getPosition().getX()) / 2,
					(plane1.getPosition().getY()
							- plane2.getPosition().getY()) / 2, 0)).add(origin);
		}


		this.score = score;

		// Load explosion animation image
		explosion = graphics.newImage("gfx" + File.separator + "ani" + File.separator + "explosionFrames.png");

		if (aircraft1 != null && aircraft2 != null) {
			Vector midPoint = aircraft1.getPosition().add(aircraft2.getPosition())
					.scaleBy(0.5).add(origin);
			Vector explosionPos = midPoint.sub(new Vector(explosion.width()/(framesAcross*2),
					explosion.height()/(framesDown*2), 0));

			explosionAnim = new SpriteAnimation(explosion,
					(int)explosionPos.getX(), (int)explosionPos.getY(),
					6, 16, framesAcross, framesDown, false);
			if(fs1 != null){
				flightStrip1 = fs1;
			}
			if(fs2 != null){
				flightStrip2 = fs2;
			}
		}
	}

	/**
	 * Initialises the random number of deaths, timer, and text box with strings
	 * to be written about the game failure.
	 */
	@Override
	public void start() {
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
	}

	/**
	 * If it runs before the explosion has finished, update the explosion
	 * otherwise, update text box instead.
	 */
	@Override
	public void update(double timeDifference) {
		if (aircraft1 != null && aircraft2 != null) {
			if (explosionAnim.hasFinished()){
				timer += timeDifference;
			} else {
				explosionAnim.update(timeDifference);
			}
		}

		for (FlightStrip fs : player.getFlightStrips()) {
			fs.update(timeDifference);
			System.out.println(fs.positionY);
		}

	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	/**
	 * Tracks if any keys are pressed when the game over screen begins.
	 * Prevents the scene instantly ending due to a key press from previous scene.
	 */
	@Override
	public void keyPressed(int key) {
		keyPressed = key;
	}

	/**
	 * Ends the scene if any key is released.
	 */
	@Override
	public void keyReleased(int key) {
		if (key == keyPressed) {
			Main.closeScene();
		}
	}

	@Override
	/**
	 * Draws game over
	 * If explosion has finished, draw the textbox; otherwise, draw the planes and explosion.
	 */
	public void draw() {
		
		if (aircraft1 != null && aircraft2 != null) {
			if (explosionAnim.hasFinished()) {				
				graphics.setColour(graphics.safetyOrange);
				graphics.rectangle(true, window.height()/3 - 40, yBorder - 2,
						(window.width() - (2 * window.height()/3 - 80)), 70);
				graphics.setColour(Color.black);
				graphics.setFont(Main.menuTitleFont);
				graphics.print("Game Over", window.height()/3,  yBorder);
				graphics.setColour(graphics.safetyOrange);
				graphics.setFont(Main.flightstripFontMid);
				if (aircraft1 != null && aircraft2 != null) {
					graphics.printCentred(aircraft1.getName() + " crashed into " + aircraft2.getName()
							+ ".", window.width()/2, window.height()/4, 2, 0);
				}

				graphics.printCentred("Total score: " + String.valueOf(score), window.width()/2, window.height()/3, 4, 0);

				
				flightStrip1.draw(window.width()/2 - 180, window.height()/2, true);
				flightStrip2.draw(window.width()/2 + 20, window.height()/2, true);
				
			} else {
				aircraft1.draw(new Integer[] {255, 255, 255},
						(int) aircraft1.getPosition().getZ(), origin);
				aircraft2.draw(new Integer[] {255, 255, 255},
						(int) aircraft1.getPosition().getZ(), origin);

				double radius = 20; // Radius of explosion
				graphics.setColour(graphics.red);
				graphics.circle(false, crash.getX() - 5, crash.getY() - 5, radius);

				explosionAnim.draw();
			}
		}

		graphics.setColour(graphics.safetyOrange);
		graphics.printCentred("Press any key to continue", 0, 2 * window.height()/3, 1, window.width());
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
