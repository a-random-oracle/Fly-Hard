package cls;

import java.io.File;

import org.newdawn.slick.Color;

import btc.Main;

import lib.jog.graphics;
import lib.jog.graphics.Image;

public class Powerup {
	
	/** The vector position of the powerup */
	private Vector powerupLocation;
	
	/** The name of the powerup */
	public String name;
	
	/** The duration of the powerup */
	private int duration;

	/** Image used for powerup */
	private Image image;
	
	/** <p>The <code>PowerUp</code> enum contains all power-ups featured in the game.
	 * </p>This enum gives access to a random power-up value; to be used in spawning
	 * a random powerup during run time.</p>
	 * The power-ups are as follows:</p>
	 * <code>PowerUp.FOG - "description here"<p>
	 * <code>PowerUp.SLOW_DOWN - "description here"<p>
	 * <code>PowerUp.SPEED_UP - "description here"<p>
	 * <code>PowerUp.TRANSFER - "description here"<p>
	 *  */
	protected enum PowerUp {
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
	
	/**
	 * Constructor for powerup.
	 * @param x
	 * 			the x coordinate of the powerup
	 * @param y
	 * 			the y coordinate of the powerup
	 * @param name
	 * 			the name of the powerup
	 * @param duration
	 * 			the length the powerup is active for
	 */
	public Powerup(double x, double y, String name, int duration){
		this.powerupLocation = new Vector(x, y, 0);
		this.name = name;
		this.duration = duration;
		this.setPowerUpImage();
	}
	
	//draw the powerup icon, depending on waypoint
	public void draw() {
		graphics.setColour(Color.white);
		graphics.draw(this.image, powerupLocation.getX(), powerupLocation.getY());
	}

	
	public void makePowerup(){
		// choose randompowerup & randomwaypoint
		// 
		// tells draw what to draw
	}
	
	public void chooseRandomPowerup() {
		
	}
	
	public void chooseRandomWaypoint() {
		
	}
	
	private void setUpPowerUp() {
		
	}
	
	/**
	 * The <code>setPowerUpImage</code> method initialises the power-up's image
	 * to the appropriate image based on the value randomly chosen by the <code>PowerUp</code> enum.
	 */
	private void setPowerUpImage() {
		PowerUp powerUp = PowerUp.randomPowerUp();
		
		switch (powerUp) {
		case FOG: 
			image = graphics.newImage("gfx/pUp" + File.separator + "fog9a.png");
			break;
		case SLOW_DOWN:
			image = graphics.newImage("gfx/pUp" + File.separator + "slow2a.png");
			break;
		case SPEED_UP:
			image = graphics.newImage("gfx/pUp" + File.separator + "speed3a.png");
			break;
		case TRANSFER:
			image = graphics.newImage("gfx/pUp" + File.separator + "transfer1a.png");
			break;
		}
	}
	
	
}
