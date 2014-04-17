package cls;

import org.newdawn.slick.Image;

import lib.jog.graphics;

public class Powerup {
	
	/** The vector position of the powerup */
	private Vector powerupLocation;
	
	/** The name of the powerup */
	public String name;
	
	/** The duration of the powerup */
	private int duration;

	/** Image used for powerup */
	private Image image;

	
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
	public Powerup(double x, double y, String name, Image image, int duration){
		this.powerupLocation = new Vector(x, y, 0);
		this.name = name;
		this.duration = duration;
		this.image = image;
	}
	
	//draw the powerup icon, depending on waypoint
	public static void draw(double x, double y, String name) {
		graphics.rectangle(false, 500, 200, 10, 10);
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
	
	
}
