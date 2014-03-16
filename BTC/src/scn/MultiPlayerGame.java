package scn;

import lib.ButtonText;
import lib.jog.input;
import lib.jog.window;
import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;
import btc.Main;

public class MultiPlayerGame extends Game {
	
	/** The unique instance of this class */
	private static MultiPlayerGame instance = null;
	
	/** The first player */
	Player player0;
	
	/** The second player */
	Player player1;
	
	/**
	 * Creates a new instance of a multi-player game.
	 * <p>
	 * If an instance of MultiPlayerGame already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 * @return the multi-player game instance
	 */
	public static MultiPlayerGame createMultiPlayerGame(Main main,
			DifficultySetting difficulty) {
		if (instance == null) {
			return new MultiPlayerGame(main, difficulty);
		} else {
			Exception e = new Exception("Attempting to create a " +
					"second instance of MultiPlayerGame");
			e.printStackTrace();
			return instance;
		}
	}

	/**
	 * Constructs a multi-player game.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	private MultiPlayerGame(Main main, DifficultySetting difficulty) {
		super(main, difficulty);
	}
	
	@Override
	public void start() {
		super.start();
		
		// Assign location waypoints to the player
		locationWaypointMap.put(0, 0);
		locationWaypointMap.put(1, 0);
		locationWaypointMap.put(2, 1);
		locationWaypointMap.put(3, 1);
		locationWaypointMap.put(4, 0);
		locationWaypointMap.put(5, 1);
		
		// Generate the lists of waypoints to pass to the players
		Waypoint[] player0Waypoints = new Waypoint[5 + 3];
		Waypoint[] player1Waypoints = new Waypoint[5 + 3];
		
		player0Waypoints[0] = airspaceWaypoints[0];
		player0Waypoints[1] = airspaceWaypoints[1];
		player0Waypoints[2] = airspaceWaypoints[2];
		player0Waypoints[3] = airspaceWaypoints[3];
		player0Waypoints[4] = airspaceWaypoints[4];
		
		player0Waypoints[0] = airspaceWaypoints[5];
		player0Waypoints[1] = airspaceWaypoints[6];
		player0Waypoints[2] = airspaceWaypoints[7];
		player0Waypoints[3] = airspaceWaypoints[8];
		player0Waypoints[4] = airspaceWaypoints[9];
		
		// Add in location waypoints
		player0Waypoints[5] = locationWaypoints[0];
		player0Waypoints[6] = locationWaypoints[1];
		player0Waypoints[7] = locationWaypoints[4];
		
		player1Waypoints[5] = locationWaypoints[2];
		player1Waypoints[6] = locationWaypoints[3];
		player1Waypoints[7] = locationWaypoints[5];
		
		// Add airports to lists
		Airport[] player0Airports = new Airport[1];
		Airport[] player1Airports = new Airport[1];
		
		player0Airports[0] = airports[0];
		player1Airports[0] = airports[1];
		
		// Set up the player
		player0 = new Player("Bob1", true, "127.0.0.1",
				player0Airports, player0Waypoints);
		player1 = new Player("Bob2", false, "127.0.0.1",
				player1Airports, player1Waypoints);
		getPlayers().add(player0);
		getPlayers().add(player1);

		// Create the manual control button
		manualControlButtons = new ButtonText[players.size()];
		
		ButtonText.Action manual0 = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player0);
			}
		};
		
		ButtonText.Action manual1 = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player1);
			}
		};

		manualControlButtons[player0.getID()]
				= new ButtonText(" Take Control", manual0,
						(window.width() - 128 - (2 * xOffset)) / 3,
						32, 128, 32, 8, 4);
		
		manualControlButtons[player1.getID()]
				= new ButtonText(" Take Control", manual1,
						2 * (window.width() - 128 - (2 * xOffset)) / 3,
						32, 128, 32, 8, 4);

		// Reset game attributes for each player
		deselectAircraft(player0);
		deselectAircraft(player1);
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
		public void initializeAircraftArray() {}

	}
