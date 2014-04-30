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
			setUpPlayerAffectedMap();
	
	/** The mapping between powerup effects and their durations */
	private static HashMap<PowerupEffect, Integer> EFFECT_DURATIONS_MAP =
			setUpEffectDurationsMap();
	
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
	
	/** The time at which the powerup was activated */
	private long timeActivated;
	
	
	/**
	 * Constructor for powerups.
	 * <p>
	 * The constructed powerup will be assigned a random effect.
	 * </p>
	 */
	public Powerup() {
		this.effect = PowerupEffect.randomEffect();
		this.timeActivated = -1;
	}
	
	/**
	 * Constructor for powerups.
	 * @param powerup - the powerup to clone
	 */
	public Powerup(Powerup powerup) {
		this.effect = powerup.effect;
		this.timeActivated = powerup.timeActivated;
	}
	
	
	/**
	 * Sets up the player affected map.
	 * <p>
	 * When setting up the player affected map, use '0' to indicate
	 * the current player, and '1' to indicate the opposing player.
	 * </p>
	 */
	private static HashMap<PowerupEffect, Integer> setUpPlayerAffectedMap() {
		HashMap<PowerupEffect, Integer> playerAffectedMap =
				new HashMap<PowerupEffect, Integer>();
		playerAffectedMap.put(PowerupEffect.FOG, 1);
		playerAffectedMap.put(PowerupEffect.SPEED_UP, 1);
		playerAffectedMap.put(PowerupEffect.SLOW_DOWN, 0);
		playerAffectedMap.put(PowerupEffect.TRANSFER, 0);
		return playerAffectedMap;
	}
	
	/**
	 * Sets up the effect duration map.
	 */
	private static HashMap<PowerupEffect, Integer> setUpEffectDurationsMap() {
		HashMap<PowerupEffect, Integer> effectDurationsMap =
				new HashMap<PowerupEffect, Integer>();
		effectDurationsMap.put(PowerupEffect.FOG, 5000);
		effectDurationsMap.put(PowerupEffect.SPEED_UP, 5000);
		effectDurationsMap.put(PowerupEffect.SLOW_DOWN, 5000);
		effectDurationsMap.put(PowerupEffect.TRANSFER, 0);
		return effectDurationsMap;
	}
	
	public PowerupEffect getEffect() {
		return effect;
	}
	
	/**
	 * Gets whether the powerup has been activated or not.
	 * @return <code>true</code> if the powerup has been activated,
	 * 			otherwise <code>false</code>
	 */
	public boolean isActive() {
		return !(timeActivated == -1);
	}
	
	/**
	 * Gets the time the powerup as activated at.
	 * @return the time the powerup as activated at
	 */
	public long getTimeActivated() {
		return timeActivated;
	}
	
	/**
	 * Gets the time at which this powerup's effect should end.
	 * @return the time at which this powerup's effect should end
	 */
	public long getEndTime() {
		return getTimeActivated() + EFFECT_DURATIONS_MAP.get(effect);
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
			NetworkManager.sendData(-1, new Powerup(this));
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
		// Store the time at which the powerup was activated
		timeActivated = System.currentTimeMillis();
		
		switch (effect) {
		case FOG:
			handleFog();
			System.out.println("FOG");
			break;
		/*case SPEED_UP:
			handleSpeedUp();
			System.out.println("SPEED+");
			break;
		case SLOW_DOWN:
			handleSlowDown();
			System.out.println("SPEED-");
			break;*/
		case TRANSFER:
			handleTransfer();
			System.out.println("TRANSFER");
			break;	
		}
	}
	
	public void deactivateEffect() {
		// Remove the powerup
		Game.getInstance().getPlayer().removePowerup(this);
		
		switch (effect) {
		case FOG:
			removeFog();
			System.out.println("FOG");
			break;
		/*case SPEED_UP:
			removeSpeedUp();
			System.out.println("SPEED+");
			break;
		case SLOW_DOWN:
			removeSlowDown();
			System.out.println("SPEED-");
			break;*/
		case TRANSFER:
			break;
		}
	}
	
	/**
	 * Stops planes from taking off/landing.
	 */
	private void handleFog() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		for (Airport a : gameInstance.getPlayer().getAirports()) {
			a.setIsActive(true);
		}
		
	}
	
	/** 
	 * Removes fog effect.
	 */
	private void removeFog() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		for (Airport a : gameInstance.getPlayer().getAirports()) {
			a.setIsActive(false);
		}
		
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
	
	/**
	 * Remove powerup speed up.
	 */
	private void removeSpeedUp() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		for (Aircraft a : gameInstance.getPlayer().getAircraft()) {
			a.getVelocity().scaleByAndSet(0.5);
		}
	}
	
	//Subject to change on which player it affects/percentages
	/**
	 * Slows down your aircraft to half their current speed.
	 */
	private void handleSlowDown() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		// Set each aircraft's velocity to be half as large
		for (Aircraft a : gameInstance.getPlayer().getAircraft()) {
			a.getVelocity().scaleByAndSet(0.5);
		}
	}
	
	/**
	 * Remove slow down effect.
	 */
	private void removeSlowDown() {
		MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
		
		// Set each aircraft's velocity to be half as large
		for (Aircraft a : gameInstance.getPlayer().getAircraft()) {
			a.getVelocity().scaleByAndSet(2);
		}
	}
	
	/**
	 * Transfers control of a player's plane to the other player.
	 */
	private void handleTransfer() {
		if (aircraft != null) {
			// Get the running game instance
			MultiPlayerGame gameInstance = ((MultiPlayerGame) Game.getInstance());
			
			// Refresh the aircraft's route
			String destinationName;
			Waypoint destinationPoint;
			Airport destinationAirport = null;
			
			// Get a list of this player's location waypoints
			Waypoint[] playersLocationWaypoints = gameInstance
					.getLocationWaypoints(gameInstance.getOpposingPlayer());
			
			int destination = Main.getRandom()
					.nextInt((playersLocationWaypoints.length - 1) + 1);
			destinationName = playersLocationWaypoints[destination].getName();
			destinationPoint = playersLocationWaypoints[destination];

			// If destination is an airport, flag it
			if (destinationPoint instanceof Airport) {
				destinationAirport = (Airport) destinationPoint;
			}
			
			aircraft.generateFlightPlan(
					gameInstance.getOpposingPlayer().getWaypoints(),
					destinationName, destinationPoint, destinationAirport);
			
			for (Waypoint wp : aircraft.getFlightPlan().getRoute()) {
				System.out.println(wp.getLocation().toString());
			}
			
			// Add the aircraft to the list of aircraft under transfer
			gameInstance.getAircraftUnderTransfer().add(aircraft);
			
			// Add the aircraft to the opposing player's list of aircraft
			gameInstance.getOpposingPlayer().getAircraft().add(aircraft);
			gameInstance.getOpposingPlayer().getFlightStrips()
					.add(new FlightStrip(aircraft));
			
			// Remove the aircraft from the current player's control
			gameInstance.getPlayer().getAircraft().remove(aircraft);
			gameInstance.getPlayer().getFlightStrips()
					.remove(gameInstance.getFlightStripFromAircraft(aircraft));

			// Send *both* players' data to the other player
			NetworkManager.sendData(-1, new Player[] {
					gameInstance.getPlayer(),
					gameInstance.getOpposingPlayer()
			});
			
			// Reset the selected aircraft
			gameInstance.deselectAircraft(gameInstance.getPlayer());
		}
	}
	
}
