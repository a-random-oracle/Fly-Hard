package cls;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import net.NetworkManager;

import org.newdawn.slick.Color;

import scn.Game;
import scn.MultiPlayerGame;
import btc.Main;
import lib.jog.graphics;
import lib.jog.graphics.Image;

public class Powerup implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = -6039580715789835236L;

	/** The image used for the fog powerup effect */
	private static final Image FOG_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "fog9a.png");
	
	/** The image used for the speed up powerup effect */
	private static final Image SPEED_UP_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "speed3a.png");
	
	/** The image used for the slow down powerup effect */
	private static final Image SLOW_DOWN_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "slow2a.png");
	
	/** The image used for the transfer powerup effect */
	private static final Image TRANSFER_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "transfer1a.png");
	
	/** The mapping between powerup effects and they player which they affect */
	private static final HashMap<PowerupEffect, Integer> PLAYER_AFFECTED_MAP =
			new HashMap<PowerupEffect, Integer>();
	
	/** The mapping between powerup effects and their durations */
	private static final HashMap<PowerupEffect, Integer> EFFECT_DURATIONS_MAP =
			new HashMap<PowerupEffect, Integer>();
	
	/**
	 * The powerup effects featured in the game.
	 * <p>
	 * This also gives access to a random powerup effect.
	 * </p>
	 * <p>
	 * The powerup effects are as follows:
	 * <ul>
	 * <li>FOG - Adds a region of fog to the map, through which the player
	 * 		can't see</li>
	 * <li>SLOW_DOWN - Causes the opponent's aircraft to slow down</li>
	 * <li>SPEED_UP - Causes the opponent's aircraft to speed up</li>
	 * <li>TRANSFER - Causes an aircraft to transfer between players</li>
	 * </ul>
	 * </p>
	 */
	public enum PowerupEffect {
		FOG,
		SPEED_UP,
		SLOW_DOWN,
		TRANSFER;

		/** A list of the powerup effect names */
		private static final PowerupEffect[] POWER_UP_NAMES = values();
		
		/** The size of the powerup effect enum */
		private static final int SIZE = POWER_UP_NAMES.length;
		
		/**
		 * Gets a random powerup effect.
		 * @return a random powerup effect
		 */
		public static PowerupEffect randomEffect()  {
			return POWER_UP_NAMES[Main.getRandom().nextInt(SIZE)]; 
		 }
	}
	
	/** The powerup's effect */
	private PowerupEffect effect;
	
	
	/**
	 * Constructor for powerups.
	 * <p>
	 * The constructed powerup will be assigned a random effect.
	 * </p>
	 */
	public Powerup() {
		this.effect = PowerupEffect.randomEffect();
		
		// Set up the two maps
		setUpMaps();
	}
	
	
	/**
	 * Sets up the player affected and effect duration maps.
	 * <p>
	 * When setting up the player affected map, use '0' to indicate
	 * the current player, and '1' to indicate the opposing player.
	 * </p>
	 */
	private void setUpMaps() {
		// Set up the player affected map
		PLAYER_AFFECTED_MAP.put(PowerupEffect.FOG, 0);
		PLAYER_AFFECTED_MAP.put(PowerupEffect.SPEED_UP, 1);
		PLAYER_AFFECTED_MAP.put(PowerupEffect.SLOW_DOWN, 1);
		PLAYER_AFFECTED_MAP.put(PowerupEffect.TRANSFER, 0);
		
		// Set up the effect duration map
		EFFECT_DURATIONS_MAP.put(PowerupEffect.FOG, 5000);
		EFFECT_DURATIONS_MAP.put(PowerupEffect.SPEED_UP, 5000);
		EFFECT_DURATIONS_MAP.put(PowerupEffect.SLOW_DOWN, 5000);
		EFFECT_DURATIONS_MAP.put(PowerupEffect.TRANSFER, 5000);
	}
	
	/**
	 * Draws the powerup.
	 * <p>
	 * The powerup will be centred about the location given.
	 * </p>
	 */
	public void draw(double x, double y) {
		graphics.setColour(Color.white);
		
		// Get the image to draw
		Image image = getPowerUpImage();
		graphics.draw(getPowerUpImage(),
				x - (image.width() / 2),
				y - (image.height() / 2));
	}

	/**
	 * Gets the powerup image based on the powerup's effect.
	 */
	private Image getPowerUpImage() {
		switch (effect) {
		case FOG: 
			return FOG_IMAGE;
		case SPEED_UP:
			return SPEED_UP_IMAGE;
		case SLOW_DOWN:
			return SLOW_DOWN_IMAGE;
		case TRANSFER:
			return TRANSFER_IMAGE;
		default:
			return null;
		}
	}
	
	/**
	 * Adds a powerup to the appropriate player.
	 */
	public void addToPlayer() {
		// Get the running game instance
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		if (PLAYER_AFFECTED_MAP.get(effect) == 0) {
			gameInstance.getPlayer().addPowerup(this);
		} else if (PLAYER_AFFECTED_MAP.get(effect) == 1) {
			NetworkManager.sendData(-1, this);
		}
	}
	
	/**
	 * Gets the powerup's effect.
	 * @return the powerup's effect
	 */
	public PowerupEffect getEffect() {
		return effect;
	}
	
	/**
	 * Performs the powerup's effect.
	 * @param aircraft - the aircraft gaining the powerup
	 */
	public void activateEffect(Aircraft aircraft) {
		switch (effect) {
		case FOG:
			handleFog();
			break;
		case SPEED_UP:
			handleSpeedUp();
			break;
		case SLOW_DOWN:
			handleSlowDown();
			break;
		case TRANSFER:
			handleTransfer(aircraft);
			break;	
		}
	}
	
	private void handleFog() {
		
	}
	
	//Subject to change on which player it affects/percentages
	/**
	 * Speeds up the other player's aircraft by 2x.
	 */
	private void handleSpeedUp() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		// Set each aircraft's velocity to be twice as large
		for (Aircraft a : gameInstance.getPlayer().getAircraft()) {
			a.getVelocity().scaleByAndSet(2);
		}
	}
	
	//Subject to change on which player it affects/percentages
	/**
	 * Slows down the other players aircraft to half their current speed.
	 */
	private void handleSlowDown() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		// Set each aircraft's velocity to be half as large
		for (Aircraft a : gameInstance.getPlayer().getAircraft()) {
			a.getVelocity().scaleByAndSet(0.5);
		}
	}
	
	/**
	 * Transfers control of a players plane to the other player
	 */
	private void handleTransfer(Aircraft aircraft) {
		if (aircraft != null) {
			// Get the running game instance
			MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
			
			// Add the aircraft to the list of aircraft under transfer
			gameInstance.getAircraftUnderTransfer().add(aircraft);
			
			// Add the aircraft to the opposing player's list of aircraft
			gameInstance.getOpposingPlayer().getAircraft().add(aircraft);
			
			// Remove the aircraft from the current player's control
			gameInstance.getPlayer().getAircraft().remove(aircraft);

			// Send *both* players' data to the other player
			NetworkManager.sendData(-1, new Player[] {
					gameInstance.getPlayer(),
					gameInstance.getOpposingPlayer()
			});
		}
	}
	
}
