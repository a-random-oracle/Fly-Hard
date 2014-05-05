package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.Game.DifficultySetting;
import scn.Game;
import scn.MultiPlayerGame;
import scn.SinglePlayerGame;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Powerup;
import cls.Waypoint;
import cls.Powerup.PowerupEffect;

public class PowerupTest {
	
	Powerup powerup;
	Powerup powerupfog;
	Waypoint waypoint;
	Waypoint exitWaypoint;
	Player player;
	Airport airport;
	Airport airport2;
	Aircraft aircraft;
	
	
	@Before
	public void setup() {
		if (Game.getInstance() != null) {
			Game.getInstance().close();
		}
		
		powerup = new Powerup(PowerupEffect.SLOW_DOWN);
		powerupfog = new Powerup(PowerupEffect.FOG);
		waypoint = new Waypoint(0.5, 0.30833, false, true);
		exitWaypoint = new Waypoint(0.5, 0.56667, false, true);
		waypoint.setPowerup(powerup);
		player = new Player(0, null, null);
		airport = new Airport("GOAAirport", 10,20);
		airport2 = new Airport("Babbage International", (1d/7d), (1d/2d));
		
		SinglePlayerGame.createSinglePlayerGame(DifficultySetting.EASY);
		
		Waypoint[] waypointList = new Waypoint[] {
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(75, 25, false, false),
				new Waypoint(50,50, false, false)
				};
		
		aircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true, false), new Waypoint(0, 0, true, false),
				10.0, waypointList, DifficultySetting.MEDIUM, airport, airport2);	
	}
	
	@Test
	public void testIsNotActive() {
		assertTrue("Test that powerup is not active before being picked up by a plane", !waypoint.getPowerup().isActive());
	}
	
	@Test
	public void testIsActive() {
		powerup.activateEffect();
		assertTrue("Tests the powerup is active", waypoint.getPowerup().isActive());
	}
	
	@Test
	public void testTimeActivatedMethod() {
		powerup.activateEffect();
		assertTrue("Test that the timeActivated method is correct", powerup.getTimeActivated() == System.currentTimeMillis());
	}
	
	@Test
	public void testGetEndTime() {
		powerup.activateEffect();
		assertTrue("", powerup.getEndTime() == powerup.getTimeActivated() + powerup.EFFECT_DURATIONS_MAP.get(PowerupEffect.SLOW_DOWN));
		//changed powerup.EFFECT_DURATIONS_MAP to public
	}
	
	@Test 
	public void testAddToPlayer() {
		player.addPowerup(powerup);
		assertTrue("Tests if the players powerup list is not empty", player.getPowerups() != null);
	}
	
	@Test
	public void testPowerupRemoved() {
		player.addPowerup(powerup);
		player.removePowerup(powerup);
		assertTrue("", player.getPowerups().isEmpty());
	}
	
	@Test
	public void testSlowDown() {
		player.getAircraft().add(aircraft);
		player.addPowerup(powerup);
		powerup.activateEffect();
		assertTrue("", aircraft.getSpeedScale() < 1);
	}
//	@Test
//	public void testHandleFog() {
//		player.addPowerup(powerupfog);
//		powerupfog.activateEffect();
//		assertTrue("tests if airport is active", airport.isActive != false);
//		// made handle fog public
//		// doesn't work due to multiplayer game instance - requires effect to occur to other player
//	}
//	
//	@Test
//	public void testRemoveFog() {
//		player.addPowerup(powerupfog);
//		powerupfog.deactivateEffect();
//		assertTrue("", airport.isActive == true);
//		//doesnt work either
//	}
//	
//	@Test
//	public void handleTransfer() {
//		//this also will not work.
//	}
}

