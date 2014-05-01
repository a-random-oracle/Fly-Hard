package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Waypoint;

public class ScoreTest {
	
	/** The test score */
	int testScore;
	
	/** The test aircraft */
	Aircraft testAircraft;
	
	
	/**
	 * Sets up the tests.
	 */
	@Before
	public void setUp() {
		testScore = 0;
		
		Waypoint[] waypointList = new Waypoint[] {
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(75, 25, false, false),
				new Waypoint(50,50, false, false)
				};
		
		testAircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true, false), new Waypoint(0, 0, true, false),
				10.0, waypointList, DifficultySetting.MEDIUM, null, null);	
	}
	
	/**
	 * Tests that score is decremented when an aircraft's flight path is altered.
	 */
	@Test
	public void testScoreDecrementAlterPath() {
		testAircraft.alterPath(1, new Waypoint(25, 75, false, false));
		assertTrue("Score not decremented successfully", testAircraft.getScore()==99);
	}
	
}
