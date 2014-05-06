package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.Game.DifficultySetting;
import scn.Game;
import scn.SinglePlayerGame;

import cls.Waypoint;

import cls.Vector;

public class WaypointTest {
	// Test Get Functions
	// Test get position function
	Waypoint testWaypoint;
	Waypoint testWaypoint2;
	Vector resultVector;
	Vector comparisonVector;
	
	@Before
	public void setup() {
		if (Game.getInstance() != null) {
			Game.getInstance().close();
		}
		
		SinglePlayerGame.createSinglePlayerGame(DifficultySetting.EASY);
		testWaypoint = new Waypoint(10, 10, true, false);
		resultVector = testWaypoint.getLocation();
		comparisonVector = new Vector(10, 10, 0);
		testWaypoint2 = new Waypoint(2, 2, true, false);
	}
	@Test
	public void testGetPosition() {
	
		assertTrue("Position = (10, 10, 0) {scaled}",
				(resultVector.getX() == comparisonVector.getX())
				&& (resultVector.getY() == comparisonVector.getY())
				&& (resultVector.getZ() == comparisonVector.getZ()));
	}

	// Test isEntryOrExit function
	@Test
	public void testIsEntryOrExit() {
		assertFalse("Entry/Exit = false", testWaypoint.isEntryOrExit());
	}

	@Test
	public void testIsEntryOrExit2() {
		
		assertTrue("Entry/Exit = true", testWaypoint.isEntryOrExit());
	}

	// Test mouseOver checking
	@Test
	public void testIsMouseOver() {
		Waypoint testWaypoint = new Waypoint(5, 5, true, false);
		assertTrue("Mouse over = true", testWaypoint.isMouseOver(10,10));
	}

	@Test
	public void testIsMouseOver2() {
		Vector mouse = new Vector(10, 10, 0);
		assertFalse("Mouse over = false", testWaypoint.isMouseOver((int) mouse.getX(), (int) mouse.getY()));
	}

	// Test getCost function
	@Test
	public void testGetCost() {
		Waypoint testWaypoint = new Waypoint(2, 4, true, false);
		
		double result = testWaypoint.getCost(testWaypoint2);
		assertTrue("Cost = 2", result == 2);
	}

	@Test
	public void testGetCost2() {
		double result = testWaypoint.getCost(testWaypoint2);

		assertTrue("Cost = 9", result == 9);
	}

	// Test getCostBetween function
	@Test
	public void testGetCostBetween() {
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 2", result == 2);
	}

	@Test
	public void testGetCostBetween2() {
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 9", result == 9);
	}

}
