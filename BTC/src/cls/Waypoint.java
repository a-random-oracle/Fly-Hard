package cls;

import java.io.Serializable;

import lib.jog.graphics;

public class Waypoint implements Serializable {
	
	// TODO last updated: 2014.03.15 14:30
	private static final long serialVersionUID = 1413476488063120300L;

	/** Leniency to allow mouse input to be accepted in a small area around the waypoint  */
	public static final int MOUSE_LENIENCY = 32;
	
	/** The radius of the waypoint image */
	public static final int WAYPOINT_ICON_RADIUS = 8;
	
	/** The vector position of the waypoint */
	private Vector waypointLocation;
	
	/** The name to associate with this waypoint */
	public String name;
	
	/** Marks whether the waypoint is an entry point, exit point or airport */
	private boolean entryOrExit;
	
	/**
	 * Constructor for waypoints
	 * @param x the x coordinate of the waypoint
	 * @param y the y coordinate of the waypoint
	 * @param entryOrExit whether the waypoint is a point where planes may
	 * 			enter and leave the airspace
	 * @param player the player who should have control of this waypoint
	 */
	public Waypoint(double x, double y,
			boolean entryOrExit, String name) {
		this.waypointLocation = new Vector(x, y, 0);
		this.entryOrExit = entryOrExit;
		this.name = name;
		
		// Scale points to fit on screen
		// Entry and exit points are scaled automatically
		if (!entryOrExit) {
			waypointLocation = waypointLocation.remapPosition();
		}
	}
	
	public Waypoint(double x, double y, boolean entryOrExit) {
		this.waypointLocation = new Vector(x, y, 0);
		this.entryOrExit = entryOrExit;
		this.name = "";
		
		// Scale points to fit on screen
		// Entry and exit points are scaled automatically
		if (!entryOrExit) {
			waypointLocation = waypointLocation.remapPosition();
		}
	}
	
	/**
	 * Gets the waypoint's vector location.
	 * @return the waypoint's location
	 */
	public Vector getLocation() {
		return waypointLocation ;
	}
	
	/**
	 * Gets whether the waypoint is an entry point, exit point or airport.
	 * @return <code>true</code> if the waypoint is an entry point, exit point
	 * 			or airport, otherwise <code>false</code>
	 */
	public boolean isEntryOrExit() {
		return this.entryOrExit;
	}
	
	/**
	 * Gets the name associated with this waypoint.
	 * <p>
	 * This will typically be the null string ("") unless the waypoint
	 * is an entry point, exit point or airport.
	 * @return the waypoint's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Draws the waypoint.
	 */
	public void draw() {
		draw(waypointLocation.getX(), waypointLocation.getY());
	}
	
	/**
	 * Draws the waypoint.
	 * @param x the x location to draw the waypoint at
	 * @param y the y location to draw the waypoint at
	 */
	public void draw(double x, double y) {
		if (this.isEntryOrExit()) 
			graphics.setColour(64, 128, 0, 192);
		else
			graphics.setColour(graphics.red_transp);
		
		graphics.circle(false, x-WAYPOINT_ICON_RADIUS/2 + 2,
				y-WAYPOINT_ICON_RADIUS/2 + 2, WAYPOINT_ICON_RADIUS);
		graphics.circle(true, x-WAYPOINT_ICON_RADIUS/2 + 2,
				y-WAYPOINT_ICON_RADIUS/2 + 2, WAYPOINT_ICON_RADIUS - 2);
	}
	
	/**
	 * Checks if the mouse is over the waypoint, within MOUSE_LENIANCY
	 * @param mx the mouse's x location
	 * @param my the mouse's y location
	 * @return whether the mouse is considered over the waypoint.
	 */
	public boolean isMouseOver(int x, int y) {
		double dx = waypointLocation.getX() - x;
		double dy = waypointLocation.getY() - y;
		return dx*dx + dy*dy < MOUSE_LENIENCY*MOUSE_LENIENCY;
	}
	
	/**
	 * Gets the cost of travelling between this waypoint and another - Used for pathfinding
	 * @param fromPoint The point to consider cost from, to this waypoint
	 * @return the distance(cost) between the two waypoints
	 */
	public double getCost(Waypoint fromPoint) {
		return waypointLocation .sub(fromPoint.getLocation()).magnitude();
	}
	
	/**
	 * Gets the cost between two waypoints
	 * @param source the source waypoint
	 * @param target the target waypoint
	 * @return the cost between source and target
	 */
	public static double getCostBetween(Waypoint source, Waypoint target) {
		return target.getCost(source);
	}
	
}
