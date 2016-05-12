package cls;

import java.io.Serializable;
import java.util.ArrayList;

public class FlightPlan implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = 1991043602981895063L;

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
	
	/** The airport which the aircraft following this path originated at */
	private Airport originAirport;
	
	/** The airport which the aircraft following this path is heading towards */
	private Airport destinationAirport;
	
	
	/**
	 * Constructs a flight plan.
	 * @param route - the waypoints to use
	 * @param originName - the name of the origin location
	 * @param destinationName - the name of the destination location
	 * @param originPoint - the point at which the flight plan starts
	 * @param destinationPoint - the point at which the flight plan ends
	 * @param originAirport - the airport which the aircraft originated at
	 * @param destinationAirport - the airport which the aircraft is travelling to
	 */
	public FlightPlan(Waypoint[] route, String originName, String destinationName,
			Waypoint originPoint, Waypoint destinationPoint,
			Airport originAirport, Airport destinationAirport) {
		this.route = findGreedyRoute(originPoint, destinationPoint, route);
		this.originName = originName;
		this.origin = originPoint.getLocation();
		this.destinationName = destinationName;
		this.destination = destinationPoint.getLocation();
		this.originAirport = originAirport;
		this.destinationAirport= destinationAirport;
	}
	
	/**
	 * Constructs a flight plan.
	 * @param flightPlan - the flight plan to copy
	 */
	private FlightPlan(FlightPlan flightPlan) {
		route = (flightPlan.route != null)
				? flightPlan.route.clone() : null;
		originName = flightPlan.originName;
		origin = (flightPlan.origin != null)
				? flightPlan.origin.clone() : null;
		destinationName = flightPlan.destinationName;
		destination = (flightPlan.destination != null)
				? flightPlan.destination.clone() : null;
		originAirport = (flightPlan.originAirport != null)
				? flightPlan.originAirport.clone() : null;
		destinationAirport = (flightPlan.destinationAirport != null)
				? flightPlan.destinationAirport.clone() : null;
	}
	
	
	/**
	 * Gets the route which the flight plan describes.
	 * @return the route which the flight plan describes
	 */
	public Waypoint[] getRoute() {
		return route;
	}
	
	/**
	 * Gets the name of the location at the beginning of the flight plan.
	 * @return the name of the location at the beginning of the flight plan.
	 */
	public String getOriginName() {
		return originName;
	}
	
	/**
	 * Gets the location at the beginning of the flight plan.
	 * @return the location at the beginning of the flight plan.
	 */
	public Vector getOrigin() {
		return origin;
	}
	
	/**
	 * Gets the name of the location at the end of the flight plan.
	 * @return the name of the location at the end of the flight plan.
	 */
	public String getDestinationName() {
		return destinationName;
	}
	
	/**
	 * Gets the location at the end of the flight plan.
	 * @return the location at the end of the flight plan.
	 */
	public Vector getDestination() {
		return destination;
	}
	
	/**
	 * Gets the airport at the beginning of the flight plan.
	 * @return the airport at the beginning of the flight plan.
	 */
	public Airport getOriginAirport() {
		return originAirport;
	}
	
	/**
	 * Gets the airport at the end of the flight plan.
	 * @return the airport at the end of the flight plan.
	 */
	public Airport getDestinationAirport() {
		return destinationAirport;
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
	 * <p>
	 * Waypoint costs are considered according to distance from current aircraft location.
	 * </p>
	 * <p>
	 * Costs are further weighted by distance from waypoint to destination.
	 * </p>
	 * @param origin - the waypoint from which to begin
	 * @param destination - the waypoint at which to end
	 * @param waypoints - the waypoints to be used
	 * @return a sensible route between the origin and the destination
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
	
	/**
	 * Gets the position of a waypoint in the flight plan.
	 * @param waypoint - the waypoint to get the position of
	 * @return the position of the specified waypoint
	 */
	public int indexOfWaypoint(Waypoint waypoint) {
		int index = -1;
		for (int i = 0; i < getRoute().length; i++) {
			if (getRoute()[i] == waypoint) index = i;
		}
		return index;
	}
	
	
	/**
	 * Clones the flight plan.
	 */
	public FlightPlan clone() {
		return new FlightPlan(this);
	}
	
}