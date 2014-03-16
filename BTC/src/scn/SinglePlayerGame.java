package scn;

import java.util.ArrayList;
import java.util.Random;

import lib.ButtonText;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public class SinglePlayerGame extends Game {

	/** The player */
	private Player player;


	// Constructor ----------------------------------------------------------------------

	/**
	 * Constructor for SinglePlayerGame.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public SinglePlayerGame(Main main, DifficultySetting difficulty) {
		super(main, difficulty);
	}


	// Implemented methods --------------------------------------------------------------

	/**
	 * Initialise and begin music, initialise background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		super.start();

		// Set up airports
		Airport airport1 = Airport.create("Mosgrizzly Airport",
				(window.width() - (2 * xOffset)) / 4,
				window.height() / 2);

		Airport airport2 = Airport.create("Mosbear Airport",
				3 * (window.width() - (2 * xOffset)) / 4,
				window.height() / 2);

		Airport[] airports = new Airport[2];
		airports[0] = airport1;
		airports[1] = airport2;

		// Set up entry/exit points
		Waypoint topLeft = new Waypoint(8, 8,
				true, "North West Top Leftonia");
		Waypoint bottomLeft = new Waypoint(8, window.height() - (2 * yOffset) - 4,
				true, "100 Acre Woods");
		Waypoint topRight = new Waypoint(window.width() - (2 * xOffset) - 4, 8,
				true, "City of Rightson");
		Waypoint bottomRight = new Waypoint(window.width() - (2 * xOffset) - 4,
				window.height() - (2 * yOffset) - 4,
				true, "South Sea");

		locationWaypoints = new Waypoint[4 + airports.length];
		locationWaypoints[0] = topLeft;
		locationWaypoints[1] = bottomLeft;
		locationWaypoints[2] = topRight;
		locationWaypoints[3] = bottomRight;
		
		// Assign players to location waypoints
		locationWaypointMap.put(0, 0);
		locationWaypointMap.put(1, 0);
		locationWaypointMap.put(2, 0);
		locationWaypointMap.put(3, 0);

		// Add airports to list of location waypoints
		for (int i = 0; i < airports.length; i++) {
			locationWaypoints[4 + i] = airports[i];
			locationWaypointMap.put(4 + i, 0);
		}

		// Set up map waypoints
		// Create airspace waypoints
		Waypoint wp1 = new Waypoint(125, 175, false);
		Waypoint wp2 = new Waypoint(200, 635, false);
		Waypoint wp3 = new Waypoint(250, 400, false);
		Waypoint wp4 = new Waypoint(500, 200, false);
		Waypoint wp5 = new Waypoint(500, 655, false);
		Waypoint wp6 = new Waypoint(700, 100, false);
		Waypoint wp7 = new Waypoint(800, 750, false);
		Waypoint wp8 = new Waypoint(1000, 750, false);
		Waypoint wp9 = new Waypoint(1040, 150, false);
		Waypoint wp10 = new Waypoint(1050, 400, false);

		// Add in airspace waypoints
		Waypoint[] airspaceWaypoints = new Waypoint[10 + locationWaypoints.length];
		airspaceWaypoints[0] = wp1;
		airspaceWaypoints[1] = wp2;
		airspaceWaypoints[2] = wp3;
		airspaceWaypoints[3] = wp4;
		airspaceWaypoints[4] = wp5;
		airspaceWaypoints[5] = wp6;
		airspaceWaypoints[6] = wp7;
		airspaceWaypoints[7] = wp8;
		airspaceWaypoints[8] = wp9;
		airspaceWaypoints[9] = wp10;

		// Add in location waypoints
		for (int j = 0; j < locationWaypoints.length; j++) {
			airspaceWaypoints[10 + j] = locationWaypoints[j];
		}
		
		// Set up the player TODO
		player = new Player("Bob", true, "127.0.0.1", airports, airspaceWaypoints);
		getPlayers().add(this.player);

		// Create the manual control button
		manualControlButtons = new ButtonText[1];
		
		ButtonText.Action manual = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player);
			}
		};

		manualControlButtons[player.getID()] = new ButtonText(" Take Control", manual,
				(window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32, 8, 4);
		
		// Reset game attributes
		deselectAircraft(player);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(double timeDifference) {
		if (paused) return;
		
		timeElapsed += timeDifference;

		graphics.setColour(graphics.green_transp);

		// Update aircraft
		for (Aircraft aircraft : player.getAircraft()) {
			aircraft.update(timeDifference);
		}

		// Check if any aircraft in the airspace have collided
		checkCollisions(timeDifference);

		// Deselect and remove any aircraft which have completed their routes
		for (int i = player.getAircraft().size() - 1; i >= 0; i--) {
			if (player.getAircraft().get(i).isFinished()) {
				if (player.getAircraft().get(i).equals(player.getSelectedAircraft())) {
					deselectAircraft(player);
				}
				
				player.getAircraft().remove(i);
			}
		}

		// Update the airports
		for (Airport airport : player.getAirports()) {
			airport.update(player.getAircraft());
		}

		if (player.getSelectedAircraft() != null) {
			if (player.getSelectedAircraft().isManuallyControlled()) {
				// Handle directional control for a manually controlled aircraft
				if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A})) {
					// Turn left when 'Left' or 'A' key is pressed
					player.getSelectedAircraft().turnLeft(timeDifference);
				} else if (input.keyPressed(new int[]{input.KEY_RIGHT, input.KEY_D})) {
					// Turn right when 'Right' or 'D' key is pressed
					player.getSelectedAircraft().turnRight(timeDifference);
				}
			} else if (input.keyPressed(new int[]{
					input.KEY_LEFT, input.KEY_A, input.KEY_RIGHT, input.KEY_D})) {
				// If any of the directional keys is pressed, set selected aircraft
				// to manual control
				toggleManualControl(player);
			}

			// Handle altitude controls
			if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})
					&& (player.getSelectedAircraft().getPosition().getZ() > 28000)) {
				player.getSelectedAircraft().setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})
					&& (player.getSelectedAircraft().getPosition().getZ() < 30000)) {
				player.getSelectedAircraft().setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
		}
		
		// Deselect any aircraft which are outside the airspace
		// This ensures that players can't keep controlling aircraft
		// after they've left the airspace
		for (Aircraft airc : player.getAircraft()) {
			if (!(airc.isAtDestination())) {
				if (airc.isOutOfAirspaceBounds()) {
					deselectAircraft(airc, player);
				}
			}
		}

		// Update the counter used to determine when another flight should
		// enter the airspace
		// If the counter has reached 0, then spawn a new aircraft
		player.setFlightGenerationTimeElapsed(player.getFlightGenerationTimeElapsed()
				+ timeDifference);
		if (player.getFlightGenerationTimeElapsed()
				>= getFlightGenerationInterval(player)) {
			player.setFlightGenerationTimeElapsed(player.getFlightGenerationTimeElapsed()
					- getFlightGenerationInterval(player));
			
			if (player.getAircraft().size() < player.getMaxAircraft()) {
				generateFlight(player);
			}
		}

		// If there are no aircraft in the airspace, spawn a new aircraft
		if (player.getAircraft().size() == 0) generateFlight(player);

		// Update debug box
		// PLEASE DO NOT REMOVE - this is very useful for debugging
		out.update(timeDifference);
	}


	// Event handling -------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		if (paused) return;

		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y, player)) {
				// If an aircraft has been clicked, select it
				Aircraft clickedAircraft = findClickedAircraft(x, y, player);
				deselectAircraft(player);
				player.setSelectedAircraft(clickedAircraft);
			} else if (waypointInFlightplanClicked(x, y,
					player.getSelectedAircraft(), player)
					&& !player.getSelectedAircraft().isManuallyControlled()) {
				// If a waypoint in the currently selected aircraft's flight
				// plan has been clicked, save this waypoint to the
				// clicked waypoint attribute
				player.setSelectedWaypoint(findClickedWaypoint(x, y, player));
				if (player.getSelectedWaypoint() != null) {
					if (!player.getSelectedWaypoint().isEntryOrExit()) {
						player.setWaypointClicked(true); // Flag to mouseReleased
						player.setSelectedPathpoint(player.getSelectedAircraft()
								.getFlightPlan()
								.indexOfWaypoint(player.getSelectedWaypoint()));
					} else {
						// If the clicked waypoint is an entry/exit point, discard it
						// as we don't want the user to be able to move these points
						player.setSelectedWaypoint(null);
					}
				}
			}

			for (Airport airport : player.getAirports()) {
				if (player.getSelectedAircraft() != null
						&& airport.isArrivalsClicked(x, y)) {
					if ((player.getSelectedAircraft().isWaitingToLand)
							&& (player.getSelectedAircraft()
									.currentTarget.equals(airport.getLocation()))) {
						// If arrivals is clicked, and the selected aircraft
						// is waiting to land at that airport, cause the aircraft
						// to land
						airport.mousePressed(key, x, y);
						player.getSelectedAircraft().land();
						deselectAircraft(player);
					}
				} else if (airport.isDeparturesClicked(x, y)) {
					if (airport.aircraftHangar.size() > 0) {
						// If departures is clicked, and there is a flight waiting
						// to take off, let it take off
						airport.mousePressed(key, x, y);
						airport.signalTakeOff();
					}
				}
			}
		} else if (key == input.MOUSE_RIGHT) {
			if (aircraftClicked(x, y, player)) {
				deselectAircraft(player);
				player.setSelectedAircraft(findClickedAircraft(x, y, player));
			}

			if (player.getSelectedAircraft() != null) {
				if (compassClicked(x, y, player.getSelectedAircraft())) {
					player.setCompassClicked(true); // Flag to mouseReleased
					if (!player.getSelectedAircraft().isManuallyControlled()) {
						toggleManualControl(player);
					}
				} else {
					if (player.getSelectedAircraft().isManuallyControlled()) {
						toggleManualControl(player);
					} else {
						deselectAircraft(player);					
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		if (paused) return;

		for (Airport airport : player.getAirports()) {
			airport.mouseReleased(key, x, y);
		}

		if (key == input.MOUSE_LEFT) {
			if (manualOverridePressed(x, y, player)) {
				manualControlButtons[player.getID()].act();
			} else if (player.isWaypointClicked() && player.getSelectedAircraft() != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y, player);
				if (newWaypoint != null) {
					player.getSelectedAircraft().alterPath(player.getSelectedPathpoint(),
							newWaypoint);
				}
				
				player.setSelectedPathpoint(-1);
			}
			// Fine to set to null now as will have been dealt with
			player.setSelectedWaypoint(null);
		} else if (key == input.MOUSE_RIGHT) {
			if (player.isCompassClicked() && player.getSelectedAircraft() != null) {
				double dx = input.mouseX()
						- player.getSelectedAircraft().getPosition().getX()
						+ xOffset - 8;
				double dy = input.mouseY()
						- player.getSelectedAircraft().getPosition().getY()
						+ yOffset - 8;
				double newBearing = Math.atan2(dy, dx);
				player.getSelectedAircraft().setBearing(newBearing);
			}
		} else if (key == input.MOUSE_WHEEL_UP) {
			player.setControlAltitude(30000);
		} else if (key == input.MOUSE_WHEEL_DOWN){
			player.setControlAltitude(28000);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyPressed(int key) {
		if (paused) return;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyReleased(int key) {
		// Ensure p and escape still work when paused
		if (key == input.KEY_P) {
			paused = !paused;
		} else if (key == input.KEY_ESCAPE) {
			paused = false;
		}

		if (paused) return;

		switch (key) {
		case input.KEY_SPACE :
			toggleManualControl(player);
			break;

		case input.KEY_LCRTL :
			generateFlight(player);
			break;

		case input.KEY_ESCAPE :
			player.getAircraft().clear();
			for (Airport airport : player.getAirports()) airport.clear();
			main.closeScene();
			break;

		case input.KEY_F5 :
			Aircraft a1 = createAircraft();
			Aircraft a2 = createAircraft();
			gameOver(a1, a2);
			break;
		}
	}


	// Helper methods -------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Aircraft createAircraft() {
		// Origin and Destination
		String destinationName;
		String originName = "";
		Waypoint originPoint = null;
		Waypoint destinationPoint;
		Airport destinationAirport = null;

		// Chooses two waypoints randomly and then checks if they satisfy the rules
		// If not, it tries until it finds good ones

		ArrayList<Waypoint> availableOrigins = getAvailableEntryPoints();

		if (availableOrigins.isEmpty()) {
			int randomAirport = (new Random())
					.nextInt((player.getAirports().length - 1) + 1);
			
			if (player.getAirports()[randomAirport].aircraftHangar.size()
					== player.getAirports()[randomAirport].getHangarSize()) {
				return null;
			} else {
				originPoint = player.getAirports()[randomAirport]
						.getDeparturesCentre();
				originName = player.getAirports()[randomAirport]
						.name;
			}
			//destinationAirport = airports[randomAirport];
		} else {
			originPoint = availableOrigins.get(
					(new Random()).nextInt((availableOrigins.size() - 1) + 1));

			// If random point is an airport, use its departures location
			if (originPoint instanceof Airport) {
				//destinationAirport = (Airport) originPoint;
				originName = originPoint.name;
				originPoint = ((Airport) originPoint).getDeparturesCentre();
			} else {
				for (int i = 0; i < locationWaypoints.length; i++) {
					if (locationWaypoints[i].equals(originPoint)) {
						originName = locationWaypoints[i].getName();
						break;
					}
				}
			}
		}

		// Generate a destination
		// Keep trying until the random destination is not equal to the chosen origin
		// Also, if origin is an airport, prevent destination from being an airport
		int destination = 0;

		do {
			destination = (new Random()).nextInt((locationWaypoints.length - 1) + 1);
			destinationName = locationWaypoints[destination].getName();
			destinationPoint = locationWaypoints[destination];
		} while (destinationName.equals(originName) ||
				((getAirportFromName(originName) != null)
						&& (getAirportFromName(destinationName) != null)));
		
		// If destination is an airport, flag it
		if (destinationPoint instanceof Airport) {
			destinationAirport = (Airport) destinationPoint;
		}

		// Generate a unique, random flight name
		String name = "";
		boolean nameTaken = true;
		while (nameTaken) {
			name = "Flight " + (int)(900 * Math.random() + 100);
			nameTaken = false;
			for (Aircraft a : player.getAircraft()) {
				if (a.getName() == name) nameTaken = true;
			}
		}

		int speed = 32 + (int)(10 * Math.random());

		return new Aircraft(name, destinationName, originName,
				destinationPoint, originPoint, aircraftImage, speed,
				player.getWaypoints(), difficulty, destinationAirport);
	}


	// Deprecated -----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();
		player = new Player("Test Player", true, "127.0.0.1", null, null);
		getPlayers().add(this.player);
		player.setAircraft(new ArrayList<Aircraft>());
	}

}
