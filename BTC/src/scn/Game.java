package scn;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import lib.ButtonText;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Music;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;

import cls.Aircraft;
import cls.Airport;
import cls.OrdersBox;
import cls.Player;
import cls.Waypoint;

import btc.Main;

public abstract class Game extends Scene {
	
	/** The unique instance of this class */
	protected static Game instance = null;
	
	/** The list of players currently playing the game */
	protected ArrayList<Player> players;
	
	/** The current player */
	protected Player player;
	
	// Due to the way the airspace elements are drawn (graphics.setviewport)
	// these variables are needed to manually adjust mouse listeners and elements
	// drawn outside the airspace so that they align with the airspace elements.
	// These variables can be used to adjust the size of the airspace view.
	
	/** The distance between the left edge of the screen and the map area */
	protected static int xOffset;
	
	/** The distance between the top edge of the screen and the map area */
	protected static int yOffset;
	
	// PLEASE DO NOT REMOVE - this is very useful for debugging
	protected static OrdersBox out;
	
	/** Difficulty settings: easy, medium and hard */
	public enum DifficultySetting {EASY, MEDIUM, HARD}
	
	/** The default maximum number of aircraft */
	public static final int DEFAULT_MAX_AIRCRAFT = 2;
	
	/** The current difficulty setting */
	protected DifficultySetting difficulty;
	
	/** The time since the scene began */
	protected static double timeElapsed;
	
	/** The image to use for aircraft */
	protected Image aircraftImage;
	
	/** The music to play during the game scene */
	protected Music music;
	
	/** The background to draw in the airspace */
	protected Image background;
	
	/** The airports in the airspace */
	protected static Airport[] airports;
	
	/** The set of waypoints in the airspace which are entry/exit points */
	protected static Waypoint[] locationWaypoints;
	
	/** The waypoints through which aircraft must travel to reach their destination */
	protected static Waypoint[] airspaceWaypoints;
	
	/** The location waypoints under each players' control */
	protected Hashtable<Integer, Integer> locationWaypointMap;
	
	/** The manual control buttons */
	protected ButtonText[] manualControlButtons;
	
	/** Is the game paused */
	protected boolean paused;
	
	
	// Constructors ---------------------------------------------------------------------
	
	/**
	 * Constructor for Demo.
	 * @param main
	 * 			the main containing the scene
	 * @param difficulty
	 * 			the difficulty the scene is to be initialised with
	 */
	public Game(Main main, DifficultySetting difficulty) {
		super(main);
		
		this.difficulty = difficulty;
		
		// Set screen offsets
		xOffset = 196;
		yOffset = 48;
		
		// Define airports
		airports = new Airport[] {
				Airport.create("Mosgrizzly Airport",
						(window.width() - (2 * xOffset)) / 4,
						window.height() / 2),
				Airport.create("Mosbear Airport",
						3 * (window.width() - (2 * xOffset)) / 4,
						window.height() / 2)
		};
		
		// Define entry and exit points
		locationWaypoints = new Waypoint[] {
				new Waypoint(8, 8,
						true, "North West Top Leftonia"),
				new Waypoint(8, window.height() - (2 * yOffset) - 4,
						true, "100 Acre Woods"),
				new Waypoint(window.width() - (2 * xOffset) - 4, 8,
						true, "City of Rightson"),
				new Waypoint(window.width() - (2 * xOffset) - 4,
						window.height() - (2 * yOffset) - 4,
						true, "South Sea"),
				airports[0],
				airports[1]
		};
		
		// Define other waypointsinstance = this;
		airspaceWaypoints = new Waypoint[] {
				new Waypoint(125, 175, false),
				new Waypoint(200, 635, false),
				new Waypoint(250, 400, false),
				new Waypoint(500, 200, false),
				new Waypoint(500, 655, false),
				new Waypoint(700, 100, false),
				new Waypoint(800, 750, false),
				new Waypoint(1000, 750, false),
				new Waypoint(1040, 150, false),
				new Waypoint(1050, 400, false)
		};
	}
	
	
	// Implemented methods --------------------------------------------------------------
	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		// Set up players
		players = new ArrayList<Player>();
		
		// Set up waypoints
		locationWaypointMap = new Hashtable<Integer, Integer>();
		
		// Set up variables
		out = new OrdersBox(window.width() - xOffset + 20,
				yOffset, xOffset - 40, window.height() - (2 * yOffset), 30);
		paused = false;

		if (!Main.testing) {
			// Load in graphics
			background = graphics.newImage("gfx" + File.separator + "background_base.png");
			aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");

			// Load in music
			music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");

			// Start the music
			//music.play(); TODO <- add this back in for release
		}
		
		// Reset game attributes
		timeElapsed = 0;
	}
	
	/**
	 * Update all objects within the scene, e.g. aircraft.
	 * <p>
	 * Also runs collision detection and generates a new flight if flight
	 * generation interval has been exceeded.
	 * </p>
	 */
	@Override
	public void update(double timeDifference) {
		if (paused) return;
		
		// Update the time the game has run for
		timeElapsed += timeDifference;
		
		// Get which side of the screen the mouse is on
		int mouseSide = (input.mouseX() < (window.width() / 2)) ? 0 : 1;
		
		// Update which player is controlling inputs
		player = players.get(mouseSide % players.size());
		
		// Check if any aircraft in the airspace have collided
		checkCollisions(timeDifference);

		for (Player player : players) {
			// Update aircraft
			for (Aircraft aircraft : player.getAircraft()) {
				aircraft.update(timeDifference);
			}

			// Deselect and remove any aircraft which have completed their routes
			for (int i = player.getAircraft().size() - 1; i >= 0; i--) {
				if (player.getAircraft().get(i).isFinished()) {
					if (player.getAircraft().get(i).equals(player
							.getSelectedAircraft())) {
						deselectAircraft(player);
					}

					player.getAircraft().remove(i);
				}
			}

			// Update the airports
			for (Airport airport : player.getAirports()) {
				airport.update(player.getAircraft());
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
			player.setFlightGenerationTimeElapsed(player
					.getFlightGenerationTimeElapsed() + timeDifference);
			if (player.getFlightGenerationTimeElapsed()
					>= getFlightGenerationInterval(player)) {
				player.setFlightGenerationTimeElapsed(player
						.getFlightGenerationTimeElapsed()
						- getFlightGenerationInterval(player));
				
				if (player.getAircraft().size() < player.getMaxAircraft()) {
					generateFlight(player);
				}
			}

			// If there are no aircraft in the airspace, spawn a new aircraft
			if (player.getAircraft().size() == 0) generateFlight(player);
		}
		
		if (player.getSelectedAircraft() != null) {
			if (player.getSelectedAircraft().isManuallyControlled()) {
				// Handle directional control for a manually
				// controlled aircraft
				if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A})) {
					// Turn left when 'Left' or 'A' key is pressed
					player.getSelectedAircraft().turnLeft(timeDifference);
				} else if (input.keyPressed(new int[]{input.KEY_RIGHT,
						input.KEY_D})) {
					// Turn right when 'Right' or 'D' key is pressed
					player.getSelectedAircraft().turnRight(timeDifference);
				}
			} else if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A,
					input.KEY_RIGHT, input.KEY_D})) {
				// If any of the directional keys is pressed, set
				// selected aircraft to manual control
				toggleManualControl(player);
			}

			// Handle altitude controls
			if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})
					&& (player.getSelectedAircraft()
							.getPosition().getZ() > 28000)) {
				player.getSelectedAircraft()
						.setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})
					&& (player.getSelectedAircraft()
							.getPosition().getZ() < 30000)) {
				player.getSelectedAircraft()
						.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
		}

		// Update debug box
		// PLEASE DO NOT REMOVE - this is very useful for debugging
		out.update(timeDifference);
	}
	
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints.
	 */
	@Override
	public void draw() {
		// Draw the rectangle surrounding the map area
		graphics.setColour(graphics.green);
		graphics.rectangle(false, xOffset, yOffset, window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));

		// Set the viewport - this is the boundary used when drawing objects
		graphics.setViewport(xOffset, yOffset, window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));

		// Draw the map background
		graphics.setColour(255, 255, 255, 48);
		graphics.drawScaled(background, 0, 0,
				Math.max(Main.getXScale(), Main.getYScale()));

		// Draw individual map features
		for (Player player : players) {
			drawAirports(player);
			drawWaypoints(player);
			drawManualControlButton(player);
			drawAircraft(player);
		}
		
		// Reset the viewport - these statistics can appear outside the game
		// area
		graphics.setViewport();
		drawAdditional(getAllAircraft().size());

		// Draw debug box
		// PLEASE DO NOT REMOVE - this is very useful for debugging
		out.draw();
	}
	
	/**
	 * Draws aircraft.
	 * <p>
	 * Calls the aircraft.draw() method for each aircraft.
	 * </p>
	 * <p>
	 * Also draws flight paths, and the manual control compass.
	 * </p>
	 */
	protected void drawAircraft(Player player) {
		graphics.setColour(255, 255, 255);

		// Draw all aircraft, and show their routes if the mouse is hovering
		// above them
		for (Aircraft aircraft : player.getAircraft()) {
			aircraft.draw(player.getAircraftColour(), player.getControlAltitude());
			
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
		
		// Handle the selected aircraft
		if (player.getSelectedAircraft() != null) {
			// Draw the compass around the selected aircraft, but only if it is
			// being manually controlled
			if (player.getSelectedAircraft().isManuallyControlled()) {
				player.getSelectedAircraft().drawCompass();
			}

			// If the selected aircraft's flight path is being manipulated,
			// draw the manipulated path
			if (player.getSelectedWaypoint() != null
					&& !player.getSelectedAircraft().isManuallyControlled()) {
				player.getSelectedAircraft().drawModifiedPath(
						player.getSelectedPathpoint(),
						input.mouseX() - xOffset,
						input.mouseY() - yOffset);
			}

			// Draw the selected aircraft's flight path
			player.getSelectedAircraft().drawFlightPath(true);
			graphics.setColour(graphics.green);
		}
	}

	/**
	 * Draws waypoints.
	 * <p>
	 * Calls the waypoint.draw() method for each waypoint, excluding airport
	 * waypoints.
	 * </p>
	 * <p>
	 * Also prints the names of the entry/exit points.
	 * </p>
	 */
	protected void drawWaypoints(Player player) {
		// Draw all waypoints, except airport waypoints
		for (Waypoint waypoint : player.getWaypoints()) {
			if (!(waypoint instanceof Airport)) {
				waypoint.draw();
			}
		}
		
		// Draw entry/exit points
		graphics.setColour(graphics.green);
		
		graphics.print(locationWaypoints[0].getName(),
				locationWaypoints[0].getLocation().getX() + 9,
				locationWaypoints[0].getLocation().getY() - 6);
		graphics.print(locationWaypoints[1].getName(),
				locationWaypoints[1].getLocation().getX() + 9,
				locationWaypoints[1].getLocation().getY() - 6);
		graphics.print(locationWaypoints[2].getName(),
				locationWaypoints[2].getLocation().getX() - 141,
				locationWaypoints[2].getLocation().getY() - 6);
		graphics.print(locationWaypoints[3].getName(),
				locationWaypoints[3].getLocation().getX() - 91,
				locationWaypoints[3].getLocation().getY() - 6);
	}

	/**
	 * Draws airports.
	 * <p>
	 * Calls the airport.draw() method for each airport.
	 * </p>
	 * <p>
	 * Also prints the names of the airports.
	 * </p>
	 */
	protected void drawAirports(Player player) {
		// Draw the airports
		for (Airport airport : player.getAirports()) {
			graphics.setColour(255, 255, 255, 64);
			airport.draw();
		}
		
		// Draw the airport names
		graphics.setColour(graphics.green);
		
		graphics.print(locationWaypoints[4].getName(),
				locationWaypoints[4].getLocation().getX() - 20,
				locationWaypoints[4].getLocation().getY() + 25);
		graphics.print(locationWaypoints[5].getName(),
				locationWaypoints[5].getLocation().getX() - 20,
				locationWaypoints[5].getLocation().getY() + 25);
	}

	/**
	 * Draws the manual control button.
	 */
	protected void drawManualControlButton(Player player) {
		if (player.getSelectedAircraft() != null) {
			graphics.setColour(graphics.green);
			// Display the manual control button
			graphics.setColour(graphics.black);
			graphics.rectangle(true,
					(player.getID() + 1) * (window.width()
							- 128 - (2 * xOffset)) / (players.size() + 1),
							32, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false,
					(player.getID() + 1) * (window.width()
							- 128 - (2 * xOffset)) / (players.size() + 1),
							32, 128, 32);
			manualControlButtons[player.getID()].draw();
		}
	}

	/**
	 * Draws a readout of the time the game has been played for, and number of planes
	 * in the sky.
	 */
	protected void drawAdditional(int aircraftCount) {
		graphics.setColour(graphics.green);
		
		// Get the time the game has been played for
		int hours = (int)(timeElapsed / (60 * 60));
		int minutes = (int)(timeElapsed / 60);
		minutes %= 60;
		double seconds = timeElapsed % 60;

		// Display this in the form 'hh:mm:ss'
		DecimalFormat df = new DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes)
				+ df.format(seconds);

		// Print this to the screen
		graphics.print(timePlayed, window.width() - xOffset
				- (timePlayed.length() * 8 + 32), 32);

		// Print the highlighted altitude to the screen TODO <- check with Mark
		//graphics.print(String.valueOf("Highlighted altitude: " + Integer
		//		.toString(highlightedAltitude)) , 32 + xOffset, 15);

		// Print the number of aircraft in the airspace to the screen
		graphics.print(String.valueOf(aircraftCount)
				+ " aircraft in the airspace.", 32 + xOffset, 32);
		
		// Print the current player
		graphics.printCentred("Currently playing as player: " + player.getName(),
				(((double) window.width() - (2 * xOffset)) / 2), 32d, 1, 300);
	}
	
	/**
	 * Plays the music attached to the game.
	 */
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}
	
	
	// Event handling -------------------------------------------------------------------

	/**
	 * Handles mouse click events.
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
	 * Handles mouse release events.
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
				double dx = (input.mouseX() - xOffset)
						- player.getSelectedAircraft().getPosition().getX()
						- 8;
				double dy = (input.mouseY() - yOffset)
						- player.getSelectedAircraft().getPosition().getY()
						- 8;
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
	 * Handles key press events.
	 */
	@Override
	public void keyPressed(int key) {
		if (paused) return;
	}

	/**
	 * Handles key release events.
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
			Aircraft a1 = createAircraft(player);
			Aircraft a2 = createAircraft(player);
			gameOver(a1, a2);
			break;
		}
	}
	
	
	// Game ending ----------------------------------------------------------------------
	
	/**
	 * Check if any aircraft in the airspace have collided.
	 * @param timeDifference
	 * 			the time since the last collision check
	 */
	protected void checkCollisions(double timeDifference) {
		for (Aircraft plane : getAllAircraft()) {
			int collisionState = plane.updateCollisions(timeDifference,
					getAllAircraft());
			if (collisionState >= 0) {
				gameOver(plane, getAllAircraft().get(collisionState));
				return;
			}
		}
	}
	
	/**
	 * Handle a game over caused by two planes colliding.
	 * Create a GameOver scene and make it the current scene.
	 * @param plane1
	 * 			the first plane involved in the collision
	 * @param plane2
	 * 			the second plane in the collision
	 */
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		for (Player player : players) {
			player.getAircraft().clear();
			
			for (Airport airport : player.getAirports()) {
				airport.clear();
			}
		}
		
		// TODO <- add back in for release
		//playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, 0)); //TODO <- pass score
	}
	
	/**
	 * Cleanly exit by stopping the scene's music.
	 */
	@Override
	public void close() {
		music.stop();
		instance = null;
	}
	
	
	// Helper methods -------------------------------------------------------------------
	
	/**
	 * The interval in seconds to generate flights after.
	 */
	protected int getFlightGenerationInterval(Player player) {
		switch (difficulty) {
		case MEDIUM:
			// Planes move 2x faster on medium so this makes them spawn
			// 2 times as often to keep the ratio
			return (30 / (player.getMaxAircraft() * 2));
		case HARD:
			// Planes move 3x faster on hard so this makes them spawn
			// 3 times as often to keep the ratio 
			return (30 / (player.getMaxAircraft() * 3) );
		default:
			return (30 / player.getMaxAircraft());
		}	
	}
	
	/**
	 * Creates a new aircraft object and introduces it to the airspace. 
	 */
	protected void generateFlight(Player player) {
		Aircraft aircraft = createAircraft(player);

		if (aircraft != null && player != null) {
			// If the aircraft starts at an airport, add it to that airport
			for (Airport airport : player.getAirports()) {
				if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
					airport.addToHangar(aircraft);
					return;
				}
			}

			// Otherwise, add the aircraft to the airspace
			player.getAircraft().add(aircraft);
		}
	}
	
	/**
	 * Handle aircraft creation.
	 * @return the created aircraft object
	 */
	protected Aircraft createAircraft(Player player) {
		out.addOrder(player.getName());
		
		String destinationName;
		String originName = "";
		Waypoint originPoint = null;
		Waypoint destinationPoint;
		Airport originAirport = null;
		Airport destinationAirport = null;
		
		// Get a list of this player's location waypoints
		Waypoint[] playersLocationWaypoints = getPlayersLocationWaypoints(player);

		// Get a list of location waypoints where a crash would not be immediate
		ArrayList<Waypoint> availableOrigins = getAvailableEntryPoints(player);

		if (availableOrigins.isEmpty()) {
			int randomAirport = (new Random())
					.nextInt((player.getAirports().length - 1) + 1);
			
			if (player.getAirports()[randomAirport].aircraftHangar.size()
					== player.getAirports()[randomAirport].getHangarSize()) {
				return null;
			} else {
				originAirport = player.getAirports()[randomAirport];
				originPoint = player.getAirports()[randomAirport]
						.getDeparturesCentre();
				originName = player.getAirports()[randomAirport].name;
			}
		} else {
			originPoint = availableOrigins.get(
					(new Random()).nextInt((availableOrigins.size() - 1) + 1));

			// If random point is an airport, use its departures location
			if (originPoint instanceof Airport) {
				originAirport = ((Airport) originPoint);
				originName = originPoint.name;
				originPoint = ((Airport) originPoint).getDeparturesCentre();
			} else {
				for (int i = 0; i < playersLocationWaypoints.length; i++) {
					if (playersLocationWaypoints[i].equals(originPoint)) {
						originName = playersLocationWaypoints[i].getName();
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
			destination = (new Random())
					.nextInt((playersLocationWaypoints.length - 1) + 1);
			destinationName = playersLocationWaypoints[destination].getName();
			destinationPoint = playersLocationWaypoints[destination];
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
			
			// Check the generated name against every other flight name
			for (Aircraft a : getAllAircraft()) {
				if (a.getName() == name) nameTaken = true;
			}
		}
		
		// Generate a random speed, centred around 37
		int speed = 32 + (int)(10 * Math.random());
		
		return new Aircraft(name, destinationName, originName,
				destinationPoint, originPoint, aircraftImage, speed,
				player.getWaypoints(), difficulty, originAirport,
				destinationAirport);
	}
	
	/**
	 * Causes deselection of the selected aircraft.
	 * @param player
	 * 			the player to reset the selected plane attribute for
	 */
	protected void deselectAircraft(Player player) {
		deselectAircraft(player.getSelectedAircraft(), player);
	}
	
	/**
	 * Causes deselection of the specified aircraft.
	 * @param aircraft
	 * 			the aircraft to deselect
	 * @param player
	 * 			the player to reset the selected plane attribute for
	 */
	protected void deselectAircraft(Aircraft aircraft, Player player) {
		if (aircraft != null && aircraft.isManuallyControlled()) {
			aircraft.toggleManualControl();
			manualControlButtons[player.getID()].setText(" Take Control");
		}
		
		if (aircraft != null && aircraft.equals(player.getSelectedAircraft())) {
			player.setSelectedAircraft(null);
		}
		
		player.setSelectedWaypoint(null); 
		player.setSelectedPathpoint(-1);
	}
	
	/**
	 * Causes a player's selected aircraft to call methods to toggle manual control.
	 */
	protected void toggleManualControl(Player player) {
		Aircraft selectedAircraft = player.getSelectedAircraft();
		
		if (selectedAircraft == null) return;

		selectedAircraft.toggleManualControl();
		manualControlButtons[player.getID()].setText(
				(selectedAircraft.isManuallyControlled() ?
						"Remove" : " Take") + " Control");
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane.
	 * <p>
	 * Specifically, returns points where no plane is currently going to exit the
	 * airspace there, also it is not too close to any plane.
	 * </p>
	 * @param player
	 * 			the player whose entry points should be checked
	 * @return a list of available entry points
	 */	
	public ArrayList<Waypoint> getAvailableEntryPoints(Player player) {
		ArrayList<Waypoint> availableEntryPoints = new ArrayList<Waypoint>();
		
		// Only check location waypoints which are under the players' control
		Waypoint[] playersLocationWaypoints = getPlayersLocationWaypoints(player);

		for (Waypoint entryPoint : playersLocationWaypoints) {
			boolean isAvailable = true;
			// Prevents spawning a plane at a waypoint if:
			//   - any plane is currently going towards it
			//   - or any plane is less than 250 from it

			for (Aircraft aircraft : getAllAircraft()) {
				// Check if any plane is currently going towards the
				// exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close
				// from the chosen originPoint
				if (aircraft.currentTarget.equals(entryPoint.getLocation())
						|| aircraft.isCloseToEntry(entryPoint.getLocation())) {
					isAvailable = false;
				}	
			}

			if (isAvailable) {
				availableEntryPoints.add(entryPoint);
			}	
		}
		return availableEntryPoints;
	}
	
	/**
	 * Returns whether a given name is an airport or not.
	 * @param name
	 * 			the name to test
	 * @return <code>true</code> if the name matches an airport name,
	 * 			otherwise <code>false</code>
	 */
	public Airport getAirportFromName(String name) {
		for (Airport airport : getAllAirports()) {
			// If a match is found, return true
			if (airport.name.equals(name)) return airport;
		}

		// Otherwise
		return null;
	}
	
	
	// Click event helpers --------------------------------------------------------------
	
	/**
	 * Gets whether the manual control compass has been clicked or not.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param aircraft
	 * 			the aircraft whose compass region should be checked
	 * @return <code>true</code> if the compass has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected boolean compassClicked(int x, int y, Aircraft aircraft) {
		if (aircraft != null) {
			double dx = aircraft.getPosition().getX() - x + xOffset;
			double dy = aircraft.getPosition().getY() - y + yOffset;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		return false;
	}
	
	/**
	 * Gets whether an aircraft has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose aircraft should be checked
	 * @return <code>true</code> if an aircraft has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected boolean aircraftClicked(int x, int y, Player player) {
		return (findClickedAircraft(x, y, player) != null);
	}
	
	/**
	 * Gets the aircraft which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose aircraft should be checked
	 * @return if an aircraft was clicked, returns the corresponding aircraft object,
	 * 			otherwise returns null
	 */
	protected Aircraft findClickedAircraft(int x, int y, Player player) {
		for (Aircraft a : player.getAircraft()) {
			if (a.isMouseOver(x - xOffset, y - yOffset)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Gets whether a waypoint in an aircraft's flight plan has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose waypoints should be searched
	 * @return <code>true</code> if a waypoint in an aircraft's flight plan
	 * 			has been clicked, otherwise <code>false</code>
	 */
	protected boolean waypointInFlightplanClicked(int x, int y,
			Aircraft aircraft, Player player) {
		Waypoint clickedWaypoint = findClickedWaypoint(x, y, player);
		return (clickedWaypoint != null) && (aircraft != null)
				&& (aircraft.getFlightPlan().indexOfWaypoint(clickedWaypoint) > -1);
	}
	
	/**
	 * Gets the waypoint which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose waypoints should be searched
	 * @return if a waypoint was clicked, returns the corresponding waypoint object,
	 * 			otherwise returns null
	 */
	protected Waypoint findClickedWaypoint(int x, int y, Player player) {
		for (Waypoint w : player.getWaypoints()) {
			if (w.isMouseOver(x - xOffset, y - yOffset)) {
				return w;
			}
		}
		return null;
	}
	
	/**
	 * Gets whether the manual control button has been clicked.
	 * @param x
	 * 			the mouse's x position
	 * @param y
	 * 			the mouse's y position
	 * @param player
	 * 			the player to check this for
	 * @return <code>true</code> if the manual control button has been
	 * 			pressed, otherwise <code>false</code>
	 */
	protected boolean manualOverridePressed(int x, int y, Player player) {
		return manualControlButtons[player.getID()]
				.isMouseOver(x - xOffset, y - yOffset);
	}
	
	
	// Accessors ------------------------------------------------------------------------
	
	/**
	 * Gets the current instance of the game.
	 * @return the current game
	 */
	public static Game getInstance() {
		return instance;
	}
	
	/**
	 * Gets the horizontal offset of the game region.
	 * @return the horizontal offset of the game region
	 */
	public static int getXOffset() {
		return xOffset;
	}
	
	/**
	 * Gets the vertical offset of the game region.
	 * @return the vertical offset of the game region
	 */
	public static int getYOffset() {
		return yOffset;
	}
	
	/**
	 * Gets the list of players.
	 * @return the list of players
	 */
	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	public Player getHost() {
		for (Player player : instance.getPlayers()) {
			if (player.isHosting()) {
				return player;
			}
		}
		return null;
	}
	
	/**
	 * Gets a list of all aircraft in the airspace.
	 * @return a list of all the aircraft in the airspace
	 */
	public ArrayList<Aircraft> getAllAircraft() {
		ArrayList<Aircraft> allAircraft = new ArrayList<Aircraft>();
		
		for (Player player : players) {
			allAircraft.addAll(player.getAircraft());
		}
		
		return allAircraft;
	}
	
	/**
	 * Gets a list of all airports in the airspace.
	 * @return a list of all the airports in the airspace
	 */
	public Airport[] getAllAirports() {
		int count = 0;
		
		// Count the number of airports in the airspace
		for (Player player : players) count += player.getAirports().length;
		
		// Initialise a new array to store all the airports
		Airport[] allAirports = new Airport[count];
		
		// Loop through each player, adding their airports to the list
		int index = 0;
		Airport[] curAirports;
		for (Player player : players) {
			curAirports = player.getAirports();
			
			for (Airport airport : curAirports) {
				allAirports[index] = airport;
				index++;
			}
		}
		
		return allAirports;
	}
	
	/**
	 * Gets all the location waypoints assigned to a particular player.
	 * @param player
	 * 			the player to get waypoints for
	 * @return a list of the selected player's location waypoints
	 */
	public Waypoint[] getPlayersLocationWaypoints(Player player) {
		return getPlayersLocationWaypoints(player.getID());
	}
	
	/**
	 * Gets all the location waypoints assigned to a particular player's ID.
	 * @param playerID
	 * 			the ID of the player to get waypoints for
	 * @return a list of the selected player's location waypoints
	 */
	public Waypoint[] getPlayersLocationWaypoints(int playerID) {
		int count = 0;
		int index = 0;
		
		// Scan through location waypoint map to get the number
		// of location waypoints assigned to this player
		for (int i = 0; i < locationWaypointMap.entrySet().size(); i++) {
			if (locationWaypointMap.get(i) == playerID) count++;
		}
		
		// Create a new array to hold the waypoints
		Waypoint[] playersLocationWaypoints = new Waypoint[count];
		
		// Loop through each location waypoint, and check which player it's
		// assigned to; if the assigned player is the current player, add it to
		// the list to check
		for (int i = 0; i < locationWaypointMap.entrySet().size(); i++) {
			if (locationWaypointMap.get(i) == playerID) {
				playersLocationWaypoints[index] = locationWaypoints[i];
				index++;
			}
		}
		
		return playersLocationWaypoints;
	}
	
	/**
	 * Gets how long the game has been played for.
	 * @return the length of time the game has been running for
	 */
	public static double getTime() {
		return timeElapsed;
	}
	
	/**
	 * Generates a new unique player ID.
	 * <p>
	 * This will be one higher than the highest ID currently
	 * in use.
	 * </p>
	 * @return a new unique player ID
	 */
	public int getNewPlayerID() {
		// Start player ID's at 0
		int maxPlayerID = -1;
		
		// Search through list of players to find highest ID
		// currently in use
		for (Player player : players) {
			out.addOrder(player.getName() + " = " + player.getID());
			if (player.getID() > maxPlayerID) maxPlayerID = player.getID();
		}
		
		// Return an ID one higher than the highest ID currently
		// in use
		return maxPlayerID + 1;
	}
	
	/**
	 * Finds which player is associated with a given airport.
	 * @return the player associated with the supplied airport
	 */
	public Player getPlayerFromAirport(Airport airport) {
		for (Player player : players) {
			Airport[] playersAirports = player.getAirports();
			
			for (Airport a : playersAirports) {
				if (a.equals(airport)) {
					return player;
				}
			}
		}
		
		return null;
	}
	
	
	// Deprecated -----------------------------------------------------------------------
	
	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class).
	 * Its purpose is to initialise the list of aircraft.
	 */	
	@Deprecated
	public abstract void initializeAircraftArray();
	
}
