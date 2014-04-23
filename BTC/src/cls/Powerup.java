package cls;

import java.io.Serializable;
import java.util.HashMap;

import net.NetworkManager;

import org.newdawn.slick.Color;

import scn.Game;
import scn.MultiPlayerGame;
import btc.Main;
import lib.jog.graphics;

public class Powerup implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = -6039580715789835236L;
	
	/** The mapping between powerup effects and they player which they affect */
	private static HashMap<PowerupEffect, Integer> PLAYER_AFFECTED_MAP =
			new HashMap<PowerupEffect, Integer>();
	
	/** The mapping between powerup effects and their durations */
	private static HashMap<PowerupEffect, Integer> EFFECT_DURATIONS_MAP =
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
	
	/** The aircraft which retrieved the powerup */
	private Aircraft aircraft;
	
	
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
		
		switch (effect) {
		case FOG: 
			graphics.draw(MultiPlayerGame.FOG_IMAGE,
					x - (MultiPlayerGame.FOG_IMAGE.width() / 2),
					y - (MultiPlayerGame.FOG_IMAGE.height() / 2));
			break;
		case SPEED_UP:
			graphics.draw(MultiPlayerGame.SPEED_UP_IMAGE,
					x - (MultiPlayerGame.SPEED_UP_IMAGE.width() / 2),
					y - (MultiPlayerGame.SPEED_UP_IMAGE.height() / 2));
			break;
		case SLOW_DOWN:
			graphics.draw(MultiPlayerGame.SLOW_DOWN_IMAGE,
					x - (MultiPlayerGame.SLOW_DOWN_IMAGE.width() / 2),
					y - (MultiPlayerGame.SLOW_DOWN_IMAGE.height() / 2));
			break;
		case TRANSFER:
			graphics.draw(MultiPlayerGame.TRANSFER_IMAGE,
					x - (MultiPlayerGame.TRANSFER_IMAGE.width() / 2),
					y - (MultiPlayerGame.TRANSFER_IMAGE.height() / 2));
			break;
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
			//NetworkManager.sendData(-1, this);
		}
	}
	
	/**
	 * Registers an aircraft as the aircraft which claimed the powerup.
	 * @param aircraft - the aircraft which claimed the aircraft
	 */
	public void registerAircraft(Aircraft aircraft) {
		this.aircraft = aircraft;
	}
	
	/**
	 * Performs the powerup's effect.
	 */
	public void activateEffect() {
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
			handleTransfer();
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
	private void handleTransfer() {
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
