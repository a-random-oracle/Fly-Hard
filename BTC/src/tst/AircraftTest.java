package tst;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import scn.Game;
import scn.SinglePlayerGame;
import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Waypoint;
import cls.Vector;

@SuppressWarnings("deprecation")

public class AircraftTest {	
	Aircraft testAircraft;
	
	@Before
	public void setUp() {
		Waypoint[] waypointList = new Waypoint[]{
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(75, 25, false, false),
				new Waypoint(50, 50, false, false)};
		
		testAircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true, false), new Waypoint(0, 0, true, false),
				10.0, waypointList, DifficultySetting.MEDIUM, null, null);
	}
	
	// Test get functions
	// Test getPosition function
	@Test
	public void testGetPosition() {
		Vector resultPosition = testAircraft.getPosition();
		assertTrue("x >= -128 and xy <= 27, y = 0, z = 28,000 or z = 30,000",
				((0 == resultPosition.getY())
						&& (128 >= resultPosition.getX())
						&& (-128 <= resultPosition.getX())
						&& ((28000 == resultPosition.getZ())
								|| (30000 == resultPosition.getZ()))));
	}
	
	// Test getName function
	@Test
	public void testGetName() {
		String name = testAircraft.getName();
		assertTrue("Name = testAircraft", "testAircraft" == name);
	}
	
	// Test getOriginName function
	@Test
	public void testGetOriginName(){
		String name = testAircraft.getFlightPlan().getOriginName();
		assertTrue("Origin name = Dublin", "Dublin" == name);
	}
	
	// Test getDestinationName function
	@Test
	public void testGetDestinationName(){
		String name = testAircraft.getFlightPlan().getDestinationName();
		assertTrue("Destination name = Berlin", "Berlin" == name);
	}
	
	// Test getIsFinished function
	@Test
	public void testGetIsFinishedName(){
		boolean status = testAircraft.isFinished();
		assertTrue("Finished = false", false == status);
	}
	
	// Test getIsManuallyControlled function
	@Test
	public void testIsManuallyControlled(){
		boolean status = testAircraft.isManuallyControlled();
		assertTrue("Manually controlled = false", false == status);
	}
	
	// Test getSpeed function
	@Test
	public void testGetSpeed(){
		double speed = (int) (testAircraft.getSpeed() + 0.5);
		assertTrue("Speed = 20", speed == 20.0);
	}
	
	// Test getAltitudeState
	@Test
	public void testAltitudeState(){
		testAircraft.setAltitudeState(1);
		int altState = testAircraft.getAltitudeState();
		assertTrue("Altitude State = 1", altState == 1);
	}
	
	// Test outOfBounds
	@Test
	public void testOutOfBounds() {
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
		
		assertTrue("Out of bounds = false", testAircraft.isOutOfAirspaceBounds());
	}
	
	// Test set methods
	// Test setAltitudeState
	@Test
	public void testSetAltitudeState(){
		testAircraft.setAltitudeState(1);
		int altState = testAircraft.getAltitudeState();
		assertTrue("Altitude State = 1", altState == 1);
	}

	// Testing totalDistanceInFlightPlan 
	@Test
	public void totalDistanceInFlightPlan() {
		SinglePlayerGame testDemo = SinglePlayerGame.createSinglePlayerGame(DifficultySetting.MEDIUM);
		testDemo.initializeAircraftArray();
		Game.getInstance().getPlayer().getAircraft().add(testAircraft);
		Aircraft plane = Game.getInstance().getPlayer().getAircraft().get(0);
		int distance = 0;
		
		for (int i = 0; i < plane.getFlightPlan().getRoute().length - 1; i++) {
			distance += Waypoint.getCostBetween(plane.getFlightPlan().getRoute()[i],
					plane.getFlightPlan().getRoute()[i + 1]);
		}
		
		assertTrue(distance == plane.getFlightPlan().getTotalDistance());
	}
	
	//Testing isCloseToEntry
	@Test
	public void isCloseToEntry() {
		Waypoint[] waypointList = new Waypoint[] {
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(675, 125, false, false),
				new Waypoint(530,520, false, false)
		};
		
		testAircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true, false), new Waypoint(0, 0, true, false),
				10.0, waypointList, DifficultySetting.MEDIUM, null, null);
		
		assertTrue(testAircraft.isCloseToEntry(waypointList[0].getLocation()));			
		assertTrue(testAircraft.isCloseToEntry(waypointList[1].getLocation()));
		assertTrue(testAircraft.isCloseToEntry(waypointList[2].getLocation()));
		assertFalse(testAircraft.isCloseToEntry(waypointList[3].getLocation()));
		assertFalse(testAircraft.isCloseToEntry(waypointList[4].getLocation()));
	}
	
	//Test decrementScore
	
	//test score starts at 100
	@Test
	public void correctStartScore() {
		assertTrue("Incorrect starting score", testAircraft.getScore()==100);
	}
	
	//Test decrementScore
	@Test
	public void decrementScoreSmall() {
		testAircraft.decrementScoreSmall();
		assertTrue("Score not successfully decremented", testAircraft.getScore()==99);
	}
	
	@Test
	public void decrementScoreLarge() {
		testAircraft.decrementScoreLarge();
		assertTrue("Score not successfully decremented", testAircraft.getScore()==90);
	}

}