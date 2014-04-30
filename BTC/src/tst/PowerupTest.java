package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cls.Aircraft;
import cls.Player;
import cls.Powerup;
import cls.Waypoint;

public class PowerupTest {
	
	Powerup powerup;
	Waypoint waypoint;
	Waypoint exitWaypoint;
	Player player;
	
	@Before
	public void setup() {
		powerup = new Powerup();
		waypoint = new Waypoint(0.5, 0.30833, false, true);
		exitWaypoint = new Waypoint(0.5, 0.56667, false, true);
		waypoint.setPowerup(powerup);
		player = new Player(0, null, null);
	}
	
	@Test
	public void testIsNotActive() {
		assertTrue("Test that powerup is not active before being picked up by a plane", !waypoint.getPowerup().isActive());
	}
	
	@Test
	public void testIsActive() {
		
	}
	
}
