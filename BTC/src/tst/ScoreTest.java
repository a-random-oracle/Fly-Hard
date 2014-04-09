package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Score;
import cls.Waypoint;

public class ScoreTest {
	Score testScore;
	Aircraft testAircraft;
	
	
	@Before
	public void setUp(){
		testScore = new Score();
		
		Waypoint[] waypointList = new Waypoint[]{
				new Waypoint(0, 0, true),
				new Waypoint(100, 100, true),
				new Waypoint(25, 75, false),
				new Waypoint(75, 25, false),
				new Waypoint(50,50, false)};
		
		testAircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true), new Waypoint(0, 0, true),
				10.0, waypointList, DifficultySetting.MEDIUM, null, null);	
	}
	
	//Test that the score starts at 0
	@Test
	public void testScoreStartsZero(){
		assertTrue("Score does not start at zero", testScore.getScore()==0);
	}
	//Test addScore
	@Test
	public void testAddScore(){
		testScore.addScore(testAircraft);
		assertTrue("Score not added successfully", testScore.getScore()==100);
	}
	
	
	//Test aircraft score decrement for altering path
	@Test
	public void testScoreDecrementAlterPath(){
		testAircraft.alterPath(1, new Waypoint(25, 75, false));
		assertTrue("Score not decremented successfully", testAircraft.getScore()==98);
	}
}
