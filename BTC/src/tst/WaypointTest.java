package tst;

import static org.junit.Assert.*;

import org.junit.Test;

import cls.Waypoint;

import cls.Vector;

public class WaypointTest {
	// Test Get Functions
	// Test get position function
	@Test
	public void testGetPosition() {
		Waypoint testWaypoint = new Waypoint(10, 10, false, 0);
		Vector resultVector = testWaypoint.getLocation();
		Vector comparisonVector = new Vector(10, 10, 0).remapPosition();

		assertTrue("Position = (10, 10, 0) {scaled}",
				(resultVector.getX() == comparisonVector.getX())
				&& (resultVector.getY() == comparisonVector.getY())
				&& (resultVector.getZ() == comparisonVector.getZ()));
	}

	// Test isEntryOrExit function
	@Test
	public void testIsEntryOrExit() {
		Waypoint testWaypoint = new Waypoint(10, 10, false, 0);
		assertFalse("Entry/Exit = false", testWaypoint.isEntryOrExit());
	}

	@Test
	public void testIsEntryOrExit2() {
		Waypoint testWaypoint = new Waypoint(0, 0, true, 0);
		assertTrue("Entry/Exit = true", testWaypoint.isEntryOrExit());
	}

	// Test mouseOver checking
	@Test
	public void testIsMouseOver() {
		Waypoint testWaypoint = new Waypoint(5, 5, true, 0);
		assertTrue("Mouse over = true", testWaypoint.isMouseOver(10,10));
	}

	@Test
	public void testIsMouseOver2() {
		Waypoint testWaypoint = new Waypoint(50, 50, true, 0);
		assertFalse("Mouse over = false", testWaypoint.isMouseOver(10,10));
	}

	// Test getCost function
	@Test
	public void testGetCost() {
		Waypoint testWaypoint = new Waypoint(2, 4, true, 0);
		Waypoint testWaypoint2 = new Waypoint(2, 2, true, 0);
		double result = testWaypoint.getCost(testWaypoint2);
		assertTrue("Cost = 2", result == 2);
	}

	@Test
	public void testGetCost2() {
		Waypoint testWaypoint = new Waypoint(6, 15, true, 0);
		Waypoint testWaypoint2 = new Waypoint(15, 15, true, 0);
		double result = testWaypoint.getCost(testWaypoint2);

		assertTrue("Cost = 9", result == 9);
	}

	// Test getCostBetween function
	@Test
	public void testGetCostBetween() {
		Waypoint testWaypoint = new Waypoint(2, 4, true, 0);
		Waypoint testWaypoint2 = new Waypoint(2, 2, true, 0);
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 2", result == 2);
	}

	@Test
	public void testGetCostBetween2() {
		Waypoint testWaypoint = new Waypoint(6, 15, true, 0);
		Waypoint testWaypoint2 = new Waypoint(15, 15, true, 0);
		double result = Waypoint.getCostBetween(testWaypoint, testWaypoint2);
		assertTrue("Cost = 9", result == 9);
	}

}
