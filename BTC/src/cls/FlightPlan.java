package cls;

import java.util.ArrayList;

public class FlightPlan {	
	private Waypoint[] route;
	private String originName;
	private String destinationName;
	private Vector destination;
	
	FlightPlan(Waypoint[] route, String originName, String destinationName, Waypoint originPoint, Waypoint destinationPoint) {
		this.route = findGreedyRoute(originPoint, destinationPoint, route);
		this.originName = originName;
		this.destinationName = destinationName;
		this.destination = destinationPoint.getLocation();
	}
	
	public Waypoint[] getRoute() {
		return route;
	}
	
	public Vector getDestination() {
		return destination;
	}
	
	public String getDestinationName() {
		return destinationName;
	}
	
	public String getOriginName() {
		return originName;
	}
	
	/**
	 * Edits the plane's path by changing the waypoint it will go to at a certain stage in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		route[routeStage] = newWaypoint;
	}
	
	/**
	 * Calculates optimal distance for a plane - Used for scoring
	 * @return total distance a plane needs to pass based on its flight plan to get to its exit point
	 */
	public int getTotalDistance() {
		int dist = 0;

		for (int i = 0; i < getRoute().length - 1; i++) {
			dist += Waypoint.getCostBetween(getRoute()[i], getRoute()[i + 1]);
		}

		return dist;
	}
	
	/**
	 * Creates a sensible route from an origin to a destination from an array of
	 * waypoints. Waypoint costs are considered according to distance from
	 * current aircraft location Costs are further weighted by distance from
	 * waypoint to destination.
	 * @param origin the waypoint from which to begin.
	 * @param destination the waypoint at which to end.
	 * @param waypoints the waypoints to be used.
	 * @return a sensible route between the origin and the destination, using a sensible amount of waypoint.
	 */
	private Waypoint[] findGreedyRoute(Waypoint origin, Waypoint destination, Waypoint[] waypoints) {
		// To hold the route as we generate it.
		ArrayList<Waypoint> selectedWaypoints = new ArrayList<Waypoint>();
		// Initialise the origin as the first point in the route.
		// SelectedWaypoints.add(origin);
		// To track our position as we generate the route. Initialise to the start of the route
		Waypoint currentPos = origin;

		// To track the closest next waypoint
		double cost = Double.MAX_VALUE;
		Waypoint cheapest = null;
		// To track if the route is complete
		boolean atDestination = false;

		while (!atDestination) {
			for (Waypoint point : waypoints) {
				boolean skip = false;

				for (Waypoint routePoints : selectedWaypoints) {
					// Check we have not already selected the waypoint
					// If we have, skip evaluating the point
					// This protects the aircraft from getting stuck looping between points
					if (routePoints.getLocation().equals(point.getLocation())) {
						skip = true; // Flag to skip
						break; // No need to check rest of list, already found a match.
					}
				}
				// Do not consider the waypoint we are currently at or the origin
				// Do not consider offscreen waypoints which are not the destination
				// Also skip if flagged as a previously selected waypoint
				if (skip | point.getLocation().equals(currentPos.getLocation()) | point.getLocation().equals(origin.getLocation())
						| (point.isEntryOrExit() && !(point.getLocation().equals(destination.getLocation())))) {
					skip = false; // Reset flag
					continue;
				} else {
					/*
					 * Get cost of visiting waypoint 
					 * Compare cost vs current cheapest 
					 * If smaller, replace
					 */
					if (point.getCost(currentPos) + 0.5 * Waypoint.getCostBetween(point, destination) < cost) {
						// Cheaper route found, update
						cheapest = point;
						cost = point.getCost(currentPos) + 0.5 * Waypoint.getCostBetween(point, destination);
					}
				}

			} // End for - evaluated all waypoints
			// The cheapest waypoint must have been found
			assert cheapest != null : "The cheapest waypoint was not found";

			if (cheapest.getLocation().equals(destination.getLocation())) {
				/*
				 * Route has reached destination, break out of while loop
				 */
				atDestination = true;
			}
			// Update the selected route
			// Consider further points in route from the position of the selected point
			selectedWaypoints.add(cheapest);
			currentPos = cheapest;
			// Resaturate cost for next loop
			cost = Double.MAX_VALUE;

		} // End while
		// Create a Waypoint[] to hold the new route
		Waypoint[] route = new Waypoint[selectedWaypoints.size()];
		// Fill route with the selected waypoints
		for (int i = 0; i < selectedWaypoints.size(); i++) {
			route[i] = selectedWaypoints.get(i);
		}
		return route;
	}
	
	public int indexOfWaypoint(Waypoint waypoint) {
		int index = -1;
		for (int i = 0; i < getRoute().length; i++) {
			if (getRoute()[i] == waypoint) index = i;
		}
		return index;
	}
}