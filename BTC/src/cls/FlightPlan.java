package cls;

import java.io.Serializable;
import java.util.ArrayList;

public class FlightPlan implements Serializable {
	
	// TODO last updated: 2014.03.13 00:05
	private static final long serialVersionUID = 4863826794317542260L;

	/** The waypoints the aircraft following this path will pass through */
	private Waypoint[] route;
	
	/** The name of the location the aircraft following this path originated at */
	private String originName;
	
	/** The position which the aircraft following this path originated at */
	private Vector origin;
	
	/** The name of the location the aircraft following this path will exit at */
	private String destinationName;
	
	/** The position which the aircraft following this path will exit at */
	private Vector destination;
	
	/** The airport which the aircraft following this path is heading towards */
	private Airport airport;
	
	FlightPlan(Waypoint[] route, String originName, String destinationName,
			Waypoint originPoint, Waypoint destinationPoint, Airport airport) {
		this.route = findGreedyRoute(originPoint, destinationPoint, route);
		this.originName = originName;
		this.origin = originPoint.getLocation();
		this.destinationName = destinationName;
		this.destination = destinationPoint.getLocation();
		this.airport = airport;
	}
	
	public Waypoint[] getRoute() {
		return route;
	}
	
	public String getOriginName() {
		return originName;
	}
	
	public Vector getOrigin() {
		return origin;
	}
	
	public String getDestinationName() {
		return destinationName;
	}
	
	public Vector getDestination() {
		return destination;
	}
	
	public Airport getAirport() {
		return airport;
	}
	
	/**
	 * Edits the plane's path by changing the waypoint it will go to at a certain stage in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		if (!newWaypoint.isEntryOrExit()) { 
			route[routeStage] = newWaypoint;
		}
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
	 * Creates a sensible route from an origin to a destination from an array of waypoints.
	 * 
	 * Waypoint costs are considered according to distance from current aircraft location
	 * Costs are further weighted by distance from waypoint to destination
	 * @param origin the waypoint from which to begin
	 * @param destination the waypoint at which to end
	 * @param waypoints the waypoints to be used
	 * @return a sensible route between the origin and the destination, using a sensible
	 * 			amount of waypoint
	 */
	public Waypoint[] findGreedyRoute(Waypoint origin, Waypoint destination,
			Waypoint[] waypoints) {
		Waypoint[] allWaypoints;
		
		// Is destination in waypoints list
		boolean destInWaypoints = false;
		
		for (int i = 0; i < waypoints.length; i++) {
			if (waypoints[i] == destination) {
				destInWaypoints = true;
				break;
			}
		}
		
		// If dest not in waypoint list, add it in
		if (!destInWaypoints) {
			int waypointCount = 0;
			allWaypoints = new Waypoint[waypoints.length + 1];
			
			for (Waypoint waypoint : waypoints) {
				allWaypoints[waypointCount] = waypoint;
				waypointCount++;
			}

			allWaypoints[waypointCount] = destination;
		} else {
			allWaypoints = waypoints.clone();
		}
		
		// Create an array to hold the route as we generate it
		ArrayList<Waypoint> selectedWaypoints = new ArrayList<Waypoint>();
		
		// Create a waypoint which will track our position as we generate the route
		// Initialise this to the start of the route
		Waypoint currentPos = origin;

		// Set the cost to a high value (as per using a greedy algorithm)
		double cost = 99999999999999.0;
		
		// Create a waypoint which will track the closest waypoint
		Waypoint cheapest = null;
		
		// Set a flag so we can tell if the route is complete
		boolean atDestination = false;
		
		while (!atDestination) {
			// For each possible waypoint
			for (Waypoint point : allWaypoints) {
				boolean skip = false;

				for (Waypoint routePoints : selectedWaypoints) {
					// Check we have not already selected the waypoint
					// If we have, skip evaluating the point
					// This protects the aircraft from getting stuck looping between points
					if (routePoints.getLocation().equals(point.getLocation())) {
						skip = true; //flag to skip
						break; // no need to check rest of list, already found a match.
					}
				}
				
				// Do not consider the waypoint we are currently at or the origin
				// Do not consider offscreen waypoints which are not the destination
				// Also skip if flagged as a previously selected waypoint
				if (skip == true
						|| point.getLocation().equals(currentPos.getLocation())
						|| point.getLocation().equals(origin.getLocation())
						|| ((point.isEntryOrExit())
								&& (!point.getLocation().equals(destination.getLocation())))) {
					skip = false;
					continue;
				} else {
					// Get the cost of visiting waypoint
					// Compare cost this cost to the current cheapest
					// If smaller, then this is the new cheapest waypoint
					if (point.getCost(currentPos) + 0.5
							* Waypoint.getCostBetween(point, destination) < cost) {
						// Cheaper route found, so update
						cheapest = point;
						cost = point.getCost(currentPos) + 0.5
								* Waypoint.getCostBetween(point, destination);
					}
				}
			}

			// The cheapest waypoint must have been found
			assert cheapest != null : "The cheapest waypoint was not found";

			// If the cheapest waypoint is the destination, then we have sucessfully
			// generated a route to the aircraft's destination, so break out of loop
			if (cheapest.getLocation().equals(destination.getLocation())) {
				// route has reached destination 
				// break out of while loop
				atDestination = true;
			}

			// Update the selected route
			// Consider further points in route from the position of the selected point
			selectedWaypoints.add(cheapest);
			currentPos = cheapest;
			
			// Resaturate cost for next loop
			cost = 99999999999.0;
		}
		
		// Create an array to hold the new route
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