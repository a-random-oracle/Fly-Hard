package scn;

import java.io.File;
import java.util.ArrayList;

import btc.Main;
import net.NetworkManager;
import lib.ButtonText;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.graphics.Image;
import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Powerup;
import cls.Waypoint;

public class MultiPlayerGame extends Game {
	
	/** The image used for the fog powerup effect */
	public static final Image FOG_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "fog9a.png");
	
	/** The image used for the speed up powerup effect */
	public static final Image SPEED_UP_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "speed3a.png");
	
	/** The image used for the slow down powerup effect */
	public static final Image SLOW_DOWN_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "slow2a.png");
	
	/** The image used for the transfer powerup effect */
	public static final Image TRANSFER_IMAGE =
			graphics.newImage("gfx/pUp" + File.separator + "transfer1a.png");
	
	/** The y-coordinate at which the middle zone borders begin */
	private static int yStart = window.height() - Y_OFFSET;

	/** The y-coordinate at which the middle zone borders end */
	private static int yEnd = Y_OFFSET;

	/** The x-coordinate at which the left middle zone border is located */
	public static int leftEntryX = (int) (window.width() * (3d/7d));

	/** The x-coordinate at which the right middle zone border is located */
	public static int rightEntryX = window.width() - leftEntryX;
	
	private static Waypoint[] powerupPoints;

	/** The player's position: 0 = left-hand side, 1 = right-hand side */
	private int playerPosition;

	/** The opposing player */
	private Player opposingPlayer;

	/** Time since new powerup generated */ 
	private double powerupGenerationTimeElapsed;

	/** Interval between powerup spawn */ 
	private double powerUpInterval;

	/** The list of aircraft which are currently being transferred */
	private ArrayList<Aircraft> aircraftUnderTransfer;


	/**
	 * Creates a new instance of a multiplayer game.
	 * <p>
	 * If an instance of Game already exists, this will print
	 * an error message and return the current instance.
	 * </p>
	 * @param difficulty - the difficulty the scene is to be initialised with
	 * @param playerPosition - the side of the screen the player will control
	 * @return the multiplayer game instance
	 */
	public static MultiPlayerGame createMultiPlayerGame(
			DifficultySetting difficulty, int playerPosition) {
		if (instance == null) {
			return new MultiPlayerGame(difficulty, playerPosition);
		} else {
			Exception e = new Exception("Attempting to create a " +
					"second instance of Game");
			e.printStackTrace();
			return (MultiPlayerGame) instance;
		}
	}

	/**
	 * Constructs a multiplayer game.
	 * @param difficulty - the difficulty the scene is to be initialised with
	 * @param playerPosition - the side of the screen the player will control
	 */
	private MultiPlayerGame(DifficultySetting difficulty, int playerPosition) {
		super(difficulty);
		instance = this;

		this.playerPosition = playerPosition;

		// Define other waypoints
		powerupPoints = new Waypoint[] {
				new Waypoint(0.5, 0.30833, false, true),
				new Waypoint(0.5, 0.56667, false, true),
				new Waypoint(0.5, 0.86146, false, true)
		};
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

		// Set up the game
		setUpGame(playerPosition);

		// Create the manual control buttons
		ButtonText.Action manual = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl(player);
			}
		};

		manualControlButton = new ButtonText(" Take Control", manual,
				(window.width() - 128 - (2 * X_OFFSET)) / 2,
				32, 128, 32, 8, 4);

		aircraftUnderTransfer = new ArrayList<Aircraft>();
		powerupGenerationTimeElapsed = 0;
		powerUpInterval = 5;
	}

	/**
	 * Sets up the game.
	 * <p>
	 * This creates the waypoints and airports to assign to the
	 * players, and then creates the players.
	 * </p>
	 */
	private void setUpGame(int playerPosition) {
		// Generate the lists of waypoints to pass to the players
		Waypoint[] player0Waypoints = new Waypoint[5 + 3];
		Waypoint[] player1Waypoints = new Waypoint[5 + 3];

		player0Waypoints[0] = airspaceWaypoints[0];
		player0Waypoints[1] = airspaceWaypoints[1];
		player0Waypoints[2] = airspaceWaypoints[2];
		player0Waypoints[3] = airspaceWaypoints[3];
		player0Waypoints[4] = airspaceWaypoints[4];

		player1Waypoints[0] = airspaceWaypoints[5];
		player1Waypoints[1] = airspaceWaypoints[6];
		player1Waypoints[2] = airspaceWaypoints[7];
		player1Waypoints[3] = airspaceWaypoints[8];
		player1Waypoints[4] = airspaceWaypoints[9];

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

		// Set up the players
		if (playerPosition == 0) {
			player = new Player(0, player0Airports, player0Waypoints);
			opposingPlayer = new Player(1, player1Airports, player1Waypoints);
		} else if (playerPosition == 1) {
			player = new Player(1, player1Airports, player1Waypoints);
			opposingPlayer = new Player(0, player0Airports, player0Waypoints);
		}
	}

	@Override
	public void update(double timeDifference) {
		// Update powerups
		powerupGenerationTimeElapsed += timeDifference;

		if (this.powerupGenerationTimeElapsed > this.powerUpInterval) {
			powerupGenerationTimeElapsed = 0;

			// Only one player is responsible for generating powerups
			if (playerPosition == 1) {
				// Check if there are any powerups on the map
				boolean powerupExists = false;
				for (Waypoint powerupPoint : powerupPoints) {
					if (powerupPoint.getPowerup() != null) {
						powerupExists = true;
					}
				}

				// If there are currently no powerups, generate one
				if (!powerupExists) {
					Waypoint randomWaypoint = powerupPoints[Main.getRandom().nextInt(powerupPoints.length)];
					
					// Generate a new powerup on the selected waypoint
					randomWaypoint.setPowerup(new Powerup());

					// Send the waypoint to the other player
					NetworkManager.sendData(-1, randomWaypoint);
				}
			}
		}
		
		// Check if any powerups have been taken
		checkPowerups(); //TODO

		// Send and receive data
		updateData();

		// Send current player's data to the server
		NetworkManager.sendData(System.currentTimeMillis(), player);

		super.update(timeDifference);

		if (paused) return;

		// Update the opposing player
		updatePlayer(timeDifference, opposingPlayer);
		
		// Fire any powerups attached to the player
		for (Powerup powerup : player.getPowerups()) {
			powerup.activateEffect();
		}

		// Deselect any aircraft which are inside the airspace of the other player
		// This ensures that players can't keep controlling aircraft
		// after they've entered another player's airspace
		returnToAirspace();
	}
	
	/**
	 * Sends and receives player and powerup data.
	 */
	private void updateData() {
		// Get data from the server
		Object data = NetworkManager.receiveData();

		if (data != null) {
			if (data instanceof Player) {
				// Set the opposing player's data
				opposingPlayer = (Player) data;
				
				// Check if any powerups have been claimed
				for (int i = 0; i > powerupPoints.length; i++) {
					if (powerupPoints[i] != null
							&& powerupPoints[i].getPowerup() != null
							&& opposingPlayer.getPowerups().contains(
									powerupPoints[i].getPowerup())) {
						powerupPoints[i].setPowerup(null);
					}
				}

				// Check if any aircraft under transfer are in the list
				if (aircraftUnderTransfer.size() > 0) {
					for (int i = aircraftUnderTransfer.size() - 1; i == 0; i--) {
						if (opposingPlayer.getAircraft()
								.contains(aircraftUnderTransfer.get(i))) {
							aircraftUnderTransfer.remove(i);
						} else {
							// If not, add them in
							opposingPlayer.getAircraft().add(
									aircraftUnderTransfer.get(i));
						}
					}
				}
			} else if (data instanceof Powerup) {
				// Add the powerup to the player
				player.addPowerup((Powerup) data);
			} else if (data instanceof Waypoint) {
				// Update the waypoint
				Waypoint updatedWaypoint = (Waypoint) data;
				
				// Loop through the middle waypoints to find the one to update
				for (Waypoint waypoint : powerupPoints) {
					if (waypoint.equals(updatedWaypoint)) {
						waypoint.setPowerup(updatedWaypoint.getPowerup());
					}
				}
			} else if (data.getClass().isArray()) {
				// Set both players' data
				Player[] playerArray = (Player[]) data;

				if (playerArray.length == 2) {
					player = playerArray[1];
					opposingPlayer = playerArray[0];
				}
				
				// Check if any powerups have been claimed
				for (int i = 0; i > powerupPoints.length; i++) {
					if (powerupPoints[i] != null
							&& powerupPoints[i].getPowerup() != null
							&& opposingPlayer.getPowerups().contains(
									powerupPoints[i].getPowerup())) {
						powerupPoints[i].setPowerup(null);
					}
				}
			}
		}
	}
	
	@Override
	public void draw() {
		super.draw();
		
		// Draw the middle zone
		drawMiddleZone();
	}

	@Override
	protected void drawMapFeatures() {
		drawAirports(player);
		drawAirports(opposingPlayer);

		drawWaypoints(player);
		drawWaypoints(opposingPlayer);

		drawAircraft(player);
		drawAircraft(opposingPlayer);

		drawSelectedAircraft();

		drawManualControlButton(player);
		
		drawPowerupPoints();
	}

	/**
	 * Draws the middle zone.
	 * <p>
	 * The middle zone is shared by both players.
	 * </p>
	 * <p>
	 * Players are forced to take manual control when in this zone
	 * and are not permitted to fly into the other player's flight area.
	 * </p>
	 * <p>
	 * It is in this zone that the power-ups spawn.
	 * </p>
	 */
	protected void drawMiddleZone() {
		graphics.setColour(graphics.green);

		// Draw the two lines
		graphics.line(leftEntryX, yStart, leftEntryX, yEnd);
		graphics.line(rightEntryX, yStart, rightEntryX, yEnd);
	}
	
	/**
	 * Draws the middle waypoints.
	 */
	private void drawPowerupPoints() {
		// Draw the power-up points
		for (Waypoint waypoint : powerupPoints) {
			// Draw the waypoint
			waypoint.draw(graphics.blue_transp);
			
			// If the waypoint has a powerup attached, draw the powerup
			if (waypoint.getPowerup() != null) {
				waypoint.getPowerup().draw(waypoint.getLocation().getX(),
						waypoint.getLocation().getY());
			}
		}
	}

	public void keyReleased(int key) {
		super.keyReleased(key);

		switch (key) {
		case input.KEY_T:
			if (player.getSelectedAircraft() != null) {
				aircraftUnderTransfer.add(player.getSelectedAircraft());
				opposingPlayer.getAircraft().add(player.getSelectedAircraft());
				player.getAircraft().remove(player.getSelectedAircraft());

				NetworkManager.sendData(-1, new Player[] {player, opposingPlayer});

				deselectAircraft(player);
			}
		}
	}

	/**
	 * Removes control of an aircraft from the player when 
	 * their aircraft goes into the other player's airspace.
	 */
	public void returnToAirspace() {
		for (Aircraft airc : player.getAircraft()) {
			if (!airc.isAtDestination()) {
				if (airc.isOutOfPlayersAirspace()) {
					deselectAircraft(airc, player);
				}
			}
		}
	}

	/**
	 * Checks if either player has run out of lives.
	 * @return <code>true</code> if either player has run out of lives,
	 * 			otherwise <code>false</code>
	 */
	public boolean checkLives() {
		if (player.getLives() == 0 || opposingPlayer.getLives() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if an aircraft has flown over a waypoint which is holding a powerup.
	 * <p>
	 * If so, the powerup is added to the appropriate player and removed from
	 * its waypoint.
	 * </p>
	 * */
	private void checkPowerups() {
		// Loop through each middle waypoint
		for (Waypoint waypoint : powerupPoints) {
			for (Aircraft aircraft : player.getAircraft()) {
				// If the aircraft is at the waypoint, and if that waypoint has
				// a powerup
				if (aircraft.isAt(waypoint.getLocation())
						&& waypoint.getPowerup() != null) {
					// Add the waypoint to the appropriate player
					waypoint.getPowerup().addToPlayer();

					// And remove the powerup from the waypoint
					waypoint.setPowerup(null);
				}
			}

			// Loop through each opposing aircraft
			for (Aircraft aircraft : opposingPlayer.getAircraft()) {
				// If the aircraft is at the waypoint, and if that waypoint has
				// a powerup
				if (aircraft.isAt(waypoint.getLocation())
						&& waypoint.getPowerup() != null) {
					// Just remove the powerup from the waypoint
					waypoint.setPowerup(null);
				}
			}
		}
	}

	@Override
	public void checkCollisions(double timeDifference) {
		for (Aircraft plane : player.getAircraft()) {
			int collisionState = plane.updateCollisions(timeDifference,
					getAllAircraft());
			if (collisionState >= 0) {
				int lives = player.getLives();
				player.setLives(lives--);
				return;
			}
		}
	}

	@Override
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		if (checkLives()) {
			player.getAircraft().clear();
			opposingPlayer.getAircraft().clear();

			for (Airport airport : player.getAirports()) {
				airport.clear();
			}

			for (Airport airport : opposingPlayer.getAirports()) {
				airport.clear();
			}

			super.gameOver(plane1, plane2);
		}
	}

	/**
	 * Gets a player from an aircraft.
	 * @param aircraft - the aircraft to get the controlling player of
	 * @return the player controlling the specified aircraft
	 */
	@Override
	public Player getPlayerFromAircraft(Aircraft aircraft) {
		for (Aircraft a : player.getAircraft()) {
			if (a.equals(aircraft)) {
				return player;
			}
		}

		for (Aircraft a : opposingPlayer.getAircraft()) {
			if (a.equals(aircraft)) {
				return opposingPlayer;
			}
		}

		return null;
	}

	/**
	 * Gets a player from an airport.
	 * @param airport - the airport to get the controlling player of
	 * @return the player controlling the specified airport
	 */
	@Override
	public Player getPlayerFromAirport(Airport airport) {
		for (int i = 0; i < player.getAirports().length; i++) {
			if (player.getAirports()[i].equals(airport)) {
				return player;
			}
		}

		for (int i = 0; i < opposingPlayer.getAirports().length; i++) {
			if (opposingPlayer.getAirports()[i].equals(airport)) {
				return opposingPlayer;
			}
		}

		return null;
	}

	/**
	 * Returns whether a given name is an airport or not.
	 * @param name - the name to test
	 * @return <code>true</code> if the name matches an airport name,
	 * 			otherwise <code>false</code>
	 */
	@Override
	public Airport getAirportFromName(String name) {
		for (Airport airport : getAllAirports()) {
			// If a match is found, return true
			if (airport.name.equals(name)) return airport;
		}

		// Otherwise
		return null;
	}

	/**
	 * Gets a list of all airports in the airspace.
	 * @return a list of all the airports in the airspace
	 */
	@Override
	public Airport[] getAllAirports() {
		int count = 0;

		// Count the number of airports in the airspace
		count += player.getAirports().length;
		count += opposingPlayer.getAirports().length;

		// Initialise a new array to store all the airports
		Airport[] allAirports = new Airport[count];

		// Loop through each player, adding their airports to the list
		int index = 0;

		for (Airport airport : player.getAirports()) {
			allAirports[index] = airport;
			index++;
		}

		for (Airport airport : opposingPlayer.getAirports()) {
			allAirports[index] = airport;
			index++;
		}

		return allAirports;
	}

	/**
	 * Gets a list of all aircraft in the airspace.
	 * @return a list of all the aircraft in the airspace
	 */
	@Override
	public ArrayList<Aircraft> getAllAircraft() {
		ArrayList<Aircraft> allAircraft = new ArrayList<Aircraft>();

		allAircraft.addAll(player.getAircraft());
		allAircraft.addAll(opposingPlayer.getAircraft());

		return allAircraft;
	}
	
	public ArrayList<Aircraft> getAircraftUnderTransfer() {
		return aircraftUnderTransfer;
	}
	
	public Player getOpposingPlayer() {
		return opposingPlayer;
	}


	// Close ----------------------------------------------------------------------------

	@Override
	public void close() {
		super.close();

		// Send a message to the opponent to let
		// them know we're closing
		NetworkManager.postMessage("END_GAME");
	}


	// Deprecated -----------------------------------------------------------------------

	@Deprecated
	@Override
	public void initializeAircraftArray() {
		super.start();

		player = new Player(0, null, null);

		opposingPlayer = new Player(1, null, null);

		player.setAircraft(new ArrayList<Aircraft>());
		opposingPlayer.setAircraft(new ArrayList<Aircraft>());
	}

}