package cls;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.newdawn.slick.Color;

import btc.Main;
import lib.jog.graphics;
import lib.jog.graphics.Image;

public class Powerup implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = -6039580715789835236L;

	/** <p>The <code>PowerUp</code> enum contains all power-ups featured in the game.
	 * </p>This enum gives access to a random power-up value; to be used in spawning
	 * a random powerup during run time.</p>
	 * The power-ups are as follows:</p>
	 * <code>PowerUp.FOG - "description here"<p>
	 * <code>PowerUp.SLOW_DOWN - "description here"<p>
	 * <code>PowerUp.SPEED_UP - "description here"<p>
	 * <code>PowerUp.TRANSFER - "description here"<p>
	 *  */
	public enum PowerUp {
		FOG,
		SLOW_DOWN,
		SPEED_UP,
		TRANSFER;

		private static final PowerUp[] POWER_UP_NAMES = values();
		private static final int SIZE = POWER_UP_NAMES.length;
		
		public static PowerUp randomPowerUp()  {
			return POWER_UP_NAMES[Main.getRandom().nextInt(SIZE)]; 
		 }
	}
	
	private static final HashMap<PowerUp, Integer> DURATIONS = new HashMap<PowerUp, Integer>();

	/** The name of the powerup */
	private String name;
	
	/** The powerup's type */
	private PowerUp type;
	
	
	/**
	 * Constructor for powerup.
	 * @param name - the name of the powerup
	 */
	public Powerup(String name) {
		this.name = name;
		this.type = PowerUp.randomPowerUp();
		
		// Set up the durations map
		DURATIONS.put(PowerUp.FOG,  5000);
		DURATIONS.put(PowerUp.SLOW_DOWN,  5000);
		DURATIONS.put(PowerUp.SPEED_UP,  5000);
		DURATIONS.put(PowerUp.TRANSFER,  5000);
	}

	
	/**
	 * Draws the powerup.
	 */
	public void draw(Vector location) {
		graphics.setColour(Color.white);
		graphics.draw(getPowerUpImage(), location.getX(), location.getY());
	}

	/**
	 * Initialises the power-up's image to the appropriate image.
	 */
	private Image getPowerUpImage() {
		switch (type) {
		case FOG: 
			return graphics.newImage("gfx/pUp" + File.separator + "fog9a.png");
		case SLOW_DOWN:
			return graphics.newImage("gfx/pUp" + File.separator + "slow2a.png");
		case SPEED_UP:
			return graphics.newImage("gfx/pUp" + File.separator + "speed3a.png");
		case TRANSFER:
			return graphics.newImage("gfx/pUp" + File.separator + "transfer1a.png");
		default:
			return null;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public PowerUp getType() {
		return type;
	}
	
	public int getDuration() {
		return DURATIONS.get(type);
	}
	
}
