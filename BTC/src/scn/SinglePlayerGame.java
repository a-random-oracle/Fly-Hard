package scn;

import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.input;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public class SinglePlayerGame extends Game {

	/** The current player */
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
		
		// Assign location waypoints to the player
		locationWaypointMap.put(0, 0);
		locationWaypointMap.put(1, 0);
		locationWaypointMap.put(2, 0);
		locationWaypointMap.put(3, 0);
		locationWaypointMap.put(4, 0);
		locationWaypointMap.put(5, 0);
		
		// Generate list of waypoints to pass to the player
		Waypoint[] playersWaypoints = new Waypoint[airspaceWaypoints.length
		                                           + locationWaypoints.length];
		
		for (int i = 0; i < airspaceWaypoints.length; i++) {
			playersWaypoints[i] = airspaceWaypoints[i];
		}
		
		// Add in location waypoints
		for (int i = 0; i < locationWaypoints.length; i++) {
			playersWaypoints[airspaceWaypoints.length + i]
					= locationWaypoints[i];
		}
		
		// Set up the player
		player = new Player("Bob1", true, "127.0.0.1",
				airports, playersWaypoints);
		getPlayers().add(player);

		// Create the manual control button
		manualControlButtons = new ButtonText[players.size()];
		
		ButtonText.Action manual = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player);
			}
		};

		manualControlButtons[player.getID()]
				= new ButtonText(" Take Control", manual,
						(window.width() - 128 - (2 * xOffset)) / 2,
						32, 128, 32, 8, 4);

		// Reset game attributes for each player
		deselectAircraft(player);
	}


	// Event handling -------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		if (paused) return;
		
		Player player = players.get(mouseSide % players.size());

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
		
		Player player = players.get(mouseSide % players.size());

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
		
		Player player = players.get(mouseSide % players.size());

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
			Aircraft a1 = createAircraft(player);
			Aircraft a2 = createAircraft(player);
			gameOver(a1, a2);
			break;
		}
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
