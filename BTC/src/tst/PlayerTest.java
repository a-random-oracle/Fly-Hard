package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cls.Airport;
import cls.Player;
import cls.Waypoint;

public class PlayerTest {
	
	Airport airports[];
	Player testPlayer;

	@Before
	public void setup() {
		
		Waypoint[] waypointList = new Waypoint[] {
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(75, 25, false, false),
				new Waypoint(50,50, false, false)
				};
		airports = new Airport[] {
				new Airport("Babbage International", (1d/7d), (1d/2d)),
				new Airport("Eboracum Airport", (6d/7d), (1d/2d))
		};
		
		testPlayer = new Player(0, airports, waypointList);
	}
	
	@Test
	public void testClone() {
		Player testPlayerClone = testPlayer.clone();
		assertTrue("something", testPlayerClone != testPlayer);
		assertTrue("", testPlayer.equals(testPlayerClone));
	}
	
}
