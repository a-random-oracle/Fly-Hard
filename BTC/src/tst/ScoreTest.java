package tst;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import scn.Demo;
import cls.Aircraft;
import cls.Score;
import cls.Waypoint;

public class ScoreTest {
	Aircraft testAircraft;
	Score testScore;
	
	@Before
	public void setUp() {
		Waypoint[] waypointList = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		testAircraft = new Aircraft("testAircraft", "Berlin", "Dublin", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 10.0, waypointList, 1);
		testScore = new Score();
	}

	// Test is simplified (assuming difficulty sections had been implemented correctly)
		// Testing score feature 

	@Test
	public void testScore() {
		Demo testDemo = new Demo(1);
		testDemo.initializeAircraftArray();
		testDemo.aircraftList().add(testAircraft);
		Aircraft plane = testDemo.aircraftList().get(0);

		assertTrue(testScore.getTotalScore() == 0);
		assertTrue(testScore.getMultiplier() == 1);
		assertTrue(testScore.getMultiplierLevel() == 1);
		assertTrue(plane.getBaseScore() == 150);

		// Simulating Demo class' update from here (calling that function would otherwise interfere with testing):

		testScore.increaseTotalScore(testScore.getMultiplier() * plane.getBaseScore());
		while(testScore.getTargetScore() != testScore.getTotalScore()) testScore.update();
			
		assertTrue(testScore.getTotalScore() == 150);
		}

	// Testing multiplier 
	// tests the multiplier meter will not decrease below 0 at multiplier_level 1
	@Test
	public void testMeterLowerBound() {
		Demo testDemo = new Demo(1);
		testDemo.initializeAircraftArray();
		testDemo.aircraftList().add(testAircraft);
			
		assertTrue(testScore.getMultiplierLevel() == 1);
		assertTrue(testScore.getMultiplier() == 1);
		assertTrue(testScore.getMeterFill() == 0);
			
		testScore.increaseMeterFill(-1);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 1);
		assertTrue(testScore.getMultiplier() == 1);
		assertTrue(testScore.getMeterFill() == 0);
	}
		
	// tests maxing the meter_fill (256) to increase multiplier_level
	@Test
	public void testMultiplierLevelIncrease() {
		testScore.resetMultiplier();
		testScore.increaseMeterFill(256);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update(); // need to set current_meter_fill to target_meter_fill

		assertTrue(testScore.getMultiplierLevel() == 2);
		assertTrue(testScore.getMultiplier() == 3);
		assertTrue(testScore.getMeterFill() == 0);
	}
	
	@Test
	public void testMultiplierLevelDecrease() {
		// sets meter_fill to 0 at multiplier_level 2
		testScore.resetMultiplier();
		testScore.increaseMeterFill(256);
		
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 2);
		assertTrue(testScore.getMultiplier() == 3);
		assertTrue(testScore.getMeterFill() == 0);
			
		// tests decreasing the meter beyond it's lower bound and lowering the multiplier_level
		testScore.increaseMeterFill(-1);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 1);
		assertTrue(testScore.getMultiplier() == 1);
		assertTrue(testScore.getMeterFill() == 255);
			
			
		}
		
	// tests an increase beyond the bound of the meter
	@Test
	public void testLargeMeterFill() {
		testScore.resetMultiplier();
		testScore.increaseMeterFill(513);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 3);
		assertTrue(testScore.getMultiplier() == 5);
		assertTrue(testScore.getMeterFill() == 1);
	}
			
	// Checks the upper bound of the meter at max multiplier_level
	@Test
	public void testMeterUpperBound() {
		// initialises the meter/multiplier_level to their max values
		testScore.resetMultiplier();
		testScore.increaseMeterFill(5*256);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 5);
		assertTrue(testScore.getMultiplier() == 10);
		assertTrue(testScore.getMeterFill() == 256);
	
		// tests the meter will not increase beyond 256 at multiplier_level 5
		testScore.increaseMeterFill(1);
		while(testScore.getTargetMeterFill() != testScore.getMeterFill()) testScore.update();
			
		assertTrue(testScore.getMultiplierLevel() == 5);
		assertTrue(testScore.getMultiplier() == 10);
		assertTrue(testScore.getMeterFill() == 256);
	}
}
