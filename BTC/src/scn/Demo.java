package scn;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import lib.ButtonText;
import lib.jog.audio;
import lib.jog.audio.Music;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.window;

import cls.Aircraft;
import cls.Airport;
import cls.OrdersBox;
import cls.Waypoint;

import btc.Main;

public class Demo extends Scene {
	
	/** The maximum number of aircraft allowed in the airspace simultaneously */
	private static int maxAircraft = 5;
	
	// Due to the way the airspace elements are drawn (graphics.setviewport) these variables are needed to manually
	// adjust mouse listeners and elements drawn outside the airspace so that they align with the airspace elements.
	// These variables can be used to adjust the size of the airspace view.
	
	/** The distance between the left edge of the screen and the map area */
	public static int xOffset = 196;
	
	/** The distance between the top edge of the screen and the map area */
	public static int yOffset = 48;
	
	public static OrdersBox out = new OrdersBox(window.width() - xOffset + 20,
			yOffset, xOffset - 40, window.height() - (2 * yOffset), 30);
	
	// Static constants for difficulty settings
	// Difficulty of demo scene determined by difficulty selection scene
	
	/** The easiest difficulty setting */
	public final static int DIFFICULTY_EASY = 0;
	
	/** The medium difficulty setting */
	public final static int DIFFICULTY_MEDIUM = 1;
	
	/** The hardest difficulty setting */
	public final static int DIFFICULTY_HARD = 2;
	
	/** The current difficulty setting */
	public static int difficulty;
	
	/** Time since the scene began */
	private static double timeElapsed;
	
	/** The currently selected aircraft */
	private Aircraft selectedAircraft;
	
	/** The currently selected waypoint */
	private Waypoint clickedWaypoint;
	
	/** Selected path point, in an aircraft's route, used for altering the route */
	private int selectedPathpoint;
	
	/** A list of aircraft present in the airspace */
	public static ArrayList<Aircraft> aircraftInAirspace;
	
	/** A list of aircraft which have recently left the airspace */
	public ArrayList<Aircraft> recentlyDepartedAircraft;
	
	/** The image to use for aircraft */
	private Image aircraftImage;
	
	/** A button to start and end manual control of an aircraft */
	private ButtonText manualOverrideButton;
	
	/** Tracks if manual heading compass of a manually controlled aircraft has been clicked */
	private boolean compassClicked;
	
	/** Tracks if waypoint of a manually controlled aircraft has been clicked */
	private boolean waypointClicked;
	
	/** The time elapsed since the last flight was generated */
	private double flightGenerationTimeElapsed = 6;
	
	/** The current control altitude of the ATCO - initially 30,000 */
	private int highlightedAltitude = 30000;
	
	/** Music to play during the game scene */
	private Music music;
	
	/** The background to draw in the airspace */
	private Image background;
	
	/** Array of the airports in the airspace */
	public static Airport[] airports;
	
	/** The set of waypoints in the airspace which are entry/exit points */
	public static Waypoint[] locationWaypoints;

	/** All waypoints in the airspace, including locationWaypoints */
	public static Waypoint[] airspaceWaypoints;
	
	/** Is the game paused */
	private boolean paused = false;
	
	/**
	 * Constructor for Demo.
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Demo(Main main, int difficulty) {
		super(main);
		Demo.difficulty = difficulty;
	}

	/**
	 * This constructor should only be used for unit testing. Its purpose is to allow an instance
	 * of demo class to be created without an instance of Main class (effectively launching the game).
	 * @param difficulty the difficulty level to run the game at
	 */	
	@Deprecated
	public Demo(int difficulty) {
		Demo.difficulty = difficulty;
	}
	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		// Load in graphics
		background = graphics.newImage("gfx" + File.separator + "background_base.png");
		aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
		
		// Load in music
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		
		// Start the music
		//music.play(); TODO <- add this back in for release

		// Set up airports
		airports = new Airport[2];

		Airport airport1 = Airport.create("Mosgrizzly Airport",
				(window.width() - (2 * xOffset)) / 4,
				window.height() / 2);

		Airport airport2 = Airport.create("Mosbear Airport",
				3 * (window.width() - (2 * xOffset)) / 4,
				window.height() / 2);

		airports[0] = airport1;
		airports[1] = airport2;

		// Set up entry/exit points
		locationWaypoints = new Waypoint[4 + airports.length];

		Waypoint topLeft = new Waypoint(8, 8,
				true, "North West Top Leftonia");
		Waypoint bottomLeft = new Waypoint(8, window.height() - (2 * yOffset) - 4,
				true, "100 Acre Woods");
		Waypoint topRight = new Waypoint(window.width() - (2 * xOffset) - 4, 8,
				true, "City of Rightson");
		Waypoint bottomRight = new Waypoint(window.width() - (2 * xOffset) - 4,
				window.height() - (2 * yOffset) - 4,
				true, "South Sea");

		locationWaypoints[0] = topLeft;
		locationWaypoints[1] = bottomLeft;
		locationWaypoints[2] = topRight;
		locationWaypoints[3] = bottomRight;

		// Add airports to list of location waypoints
		for (int i = 0; i < airports.length; i++) {
			locationWaypoints[4 + i] = airports[i];
		}

		// Set up map waypoints
		airspaceWaypoints = new Waypoint[10 + locationWaypoints.length];

		// Create airspace waypoints
		Waypoint wp1 = new Waypoint(125, 70, false);
		Waypoint wp2 = new Waypoint(700, 100, false);
		Waypoint wp3 = new Waypoint(1040, 80, false);
		Waypoint wp4 = new Waypoint(500, 200, false);
		Waypoint wp5 = new Waypoint(1050, 400, false);
		Waypoint wp6 = new Waypoint(250, 400, false);
		Waypoint wp7 = new Waypoint(200, 635, false);
		Waypoint wp8 = new Waypoint(500, 655, false);
		Waypoint wp9 = new Waypoint(800, 750, false);
		Waypoint wp10 = new Waypoint(1000, 750, false);

		// Add in airspace waypoints
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
		
		// Set up game components
		aircraftInAirspace = new ArrayList<Aircraft>();
		recentlyDepartedAircraft = new ArrayList<Aircraft>();

		// Create the manual control button
		ButtonText.Action manual = new ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl();
			}
		};
		
		manualOverrideButton = new ButtonText(" Take Control", manual,
				(window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32, 8, 4);
		
		// Reset game attributes
		timeElapsed = 0;
		compassClicked = false;
		selectedAircraft = null;
		clickedWaypoint = null;
		selectedPathpoint = -1;
		deselectAircraft();
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
		
		timeElapsed += timeDifference;
		
		graphics.setColour(graphics.green_transp);
		
		// Update aircraft
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.update(timeDifference);
			if (aircraft.isFinished()) {
				recentlyDepartedAircraft.add(aircraft);
			}
		}
		
		// Check if any aircraft in the airspace have collided
		checkCollisions(timeDifference);
		
		// Deselect and remove any aircraft which have completed their routes
		for (int i = aircraftInAirspace.size() - 1; i >= 0; i--) {
			if (aircraftInAirspace.get(i).isFinished()) {
				if (aircraftInAirspace.get(i) == selectedAircraft) {
					deselectAircraft();
				}
				aircraftInAirspace.remove(i);
			}
		}

		// Update the airports
		for (Airport airport : airports) airport.update(aircraftInAirspace);
		
		if (selectedAircraft != null) {
			if (selectedAircraft.isManuallyControlled()) {
				// Handle directional control for a manually controlled aircraft
				if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A})) {
					// Turn left when 'Left' or 'A' key is pressed
					selectedAircraft.turnLeft(timeDifference);
				} else if (input.keyPressed(new int[]{input.KEY_RIGHT, input.KEY_D})) {
					// Turn right when 'Right' or 'D' key is pressed
					selectedAircraft.turnRight(timeDifference);
				}
			} else if (input.keyPressed(new int[]{
					input.KEY_LEFT, input.KEY_A, input.KEY_RIGHT, input.KEY_D})) {
				// If any of the directional keys is pressed, set selected aircraft
				// to manual control
				toggleManualControl();
			}
			
			// Handle altitude controls
			if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})
					&& (selectedAircraft.getPosition().getZ() > 28000)) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})
					&& (selectedAircraft.getPosition().getZ() < 30000)) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
			
			// If the aircraft under manual control is out of bounds, deselect it
			// This ensures that players can't keep controlling aircraft
			// after they've left the airspace
			if (!(selectedAircraft.isAtDestination())) {
				if (selectedAircraft.isOutOfAirspaceBounds()) {
					deselectAircraft();
				}
			}	
		}
		
		// Update the counter used to determine when another flight should enter the airspace
		// If the counter has reached 0, then spawn a new aircraft
		flightGenerationTimeElapsed += timeDifference;
		if(flightGenerationTimeElapsed >= getFlightGenerationInterval()) {
			flightGenerationTimeElapsed -= getFlightGenerationInterval();
			if (aircraftInAirspace.size() < maxAircraft) {
				generateFlight();
			}
		}
		
		// If there are no aircraft in the airspace, spawn a new aircraft
		if (aircraftInAirspace.size() == 0) generateFlight();
		
		// Update debug box
		out.update(timeDifference);
	}
	
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints.
	 */
	@Override
	public void draw() {
		// Draw the rectangle surrounding the map area
		graphics.setColour(graphics.green);
		graphics.rectangle(false, xOffset, yOffset,
				window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));
		
		// Set the viewport - this is the boundary used when drawing objects
		graphics.setViewport(xOffset, yOffset,
				window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));// - 176);
		
		// Draw the map background
		graphics.setColour(255, 255, 255, 48);
		graphics.drawScaled(background, 0, 0, Math.max(Main.getXScale(), Main.getYScale()));
		
		// Draw all the airports
		for (Airport airport : airports) {
			graphics.setColour(255, 255, 255, 64);
			airport.draw();
		}
		
		// Draw the waypoints etc.
		drawMap();
		
		// Reset the viewport
		graphics.setViewport();
		
		// Draw the compass around the selected aircraft,
		// but only if it is being manually controlled
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.drawCompass();
		}
		
		// Draw extra information such as the number of flights in the airspace
		graphics.setColour(graphics.green);
		drawAdditional();
		
		// Draw debug box
		out.draw();
	}
	
	/**
	 * Draw waypoints, and route of a selected aircraft between waypoints.
	 * <p>
	 * Also prints waypoint names next to waypoints.
	 * </p>
	 */
	private void drawMap() {
		// Draw all waypoints, except airport waypoints
		for (Waypoint waypoint : airspaceWaypoints) {
			if (!(waypoint instanceof Airport)) {
				waypoint.draw();
			}
		}
		
		graphics.setColour(255, 255, 255);
		
		// Draw all aircraft, and show their routes if the mouse is hovering above them
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.draw(highlightedAltitude);
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
		
		if (selectedAircraft != null) {
			// Draw the selected aircraft's flight path
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			// Display the manual control button
			graphics.setColour(graphics.black);
			graphics.rectangle(true, (window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false, (window.width() - 128 - (2 * xOffset)) / 2, 32, 128, 32);
			manualOverrideButton.draw();
		}
		
		if (clickedWaypoint != null && selectedAircraft.isManuallyControlled() == false) {
			selectedAircraft.drawModifiedPath(selectedPathpoint,
					input.mouseX() - xOffset,
					input.mouseY() - yOffset);
		}
		
		// Draw entry/exit points
		graphics.setViewport();
		graphics.setColour(graphics.green);
		graphics.print(locationWaypoints[0].getName(),
				locationWaypoints[0].getLocation().getX() + xOffset + 9,
				locationWaypoints[0].getLocation().getY() + yOffset - 6);
		graphics.print(locationWaypoints[1].getName(),
				locationWaypoints[1].getLocation().getX() + xOffset + 9,
				locationWaypoints[1].getLocation().getY() + yOffset - 6);
		graphics.print(locationWaypoints[2].getName(),
				locationWaypoints[2].getLocation().getX() + xOffset - 141,
				locationWaypoints[2].getLocation().getY() + yOffset - 6);
		graphics.print(locationWaypoints[3].getName(),
				locationWaypoints[3].getLocation().getX() + xOffset- 91,
				locationWaypoints[3].getLocation().getY() + yOffset - 6);
		graphics.print(locationWaypoints[4].getName(),
				locationWaypoints[4].getLocation().getX() + xOffset - 20,
				locationWaypoints[4].getLocation().getY() + yOffset + 25);
	}
	
	/**
	 * Causes a selected aircraft to call methods to toggle manual control.
	 */
	private void toggleManualControl() {
		if (selectedAircraft == null) return;
		
		selectedAircraft.toggleManualControl();
		manualOverrideButton.setText(
				(selectedAircraft.isManuallyControlled() ? "Remove" : " Take") + " Control");
	}
	
	/**
	 * Causes an aircraft to call methods to handle deselection.
	 */
	private void deselectAircraft() {
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.toggleManualControl();
			manualOverrideButton.setText(" Take Control");
		}
		
		selectedAircraft = null;
		clickedWaypoint = null; 
		selectedPathpoint = -1;
	}
	
	/**
	 * Cause all planes in airspace to update collisions
	 * Catch and handle a resultant game over state
	 * @param timeDifference delta time since last collision check
	 */
	private void checkCollisions(double timeDifference) {
		for (Aircraft plane : aircraftInAirspace) {
			int collisionState = plane.updateCollisions(timeDifference, aircraftInAirspace);
			if (collisionState >= 0) {
				gameOver(plane, aircraftInAirspace.get(collisionState));
				return;
			}
		}
	}
	
	/**
	 * Handle a game over caused by two planes colliding
	 * Create a gameOver scene and make it the current scene
	 * @param plane1 the first plane involved in the collision
	 * @param plane2 the second plane in the collision
	 */
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		aircraftInAirspace.clear();
		for (Airport airport : airports) airport.clear();
		//playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg")); //TODO <- add back in for release
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, 0)); //TODO <- pass score
	}
	
	private boolean compassClicked() {
		if (selectedAircraft != null) {
			double dx = selectedAircraft.getPosition().getX() - input.mouseX() + xOffset;
			double dy = selectedAircraft.getPosition().getY() - input.mouseY() + yOffset;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		return false;
	}
	
	private boolean aircraftClicked(int x, int y) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(x - xOffset, y - yOffset)) {
				return true;
			}
		}
		return false;
	}
	
	private Aircraft findClickedAircraft(int x, int y) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(x - xOffset, y - yOffset)) {
				return a;
			}
		}
		return null;
	}
	
	private boolean waypointInFlightplanClicked(int x, int y, Aircraft a) {
		if (a != null) {
			for (Waypoint w : airspaceWaypoints) {
				if (w.isMouseOver(x - xOffset, y - yOffset) && a.getFlightPlan().indexOfWaypoint(w) > -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Waypoint findClickedWaypoint(int x, int y) {
		for (Waypoint w : airspaceWaypoints) {
			if (w.isMouseOver(x - xOffset, y - yOffset)) {
				return w;
			}
		}
		return null;
	}

	/**
	 * Handle mouse input
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		if (paused) return;
		
		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y)) {
				Aircraft clickedAircraft = findClickedAircraft(x, y);
				deselectAircraft();
				selectedAircraft = clickedAircraft;
			} else if (waypointInFlightplanClicked(x, y, selectedAircraft) && !selectedAircraft.isManuallyControlled()) {
				clickedWaypoint = findClickedWaypoint(x, y);
				if (clickedWaypoint != null) {
					if (!clickedWaypoint.isEntryOrExit()) {
						waypointClicked = true; // Flag to mouseReleased
						selectedPathpoint = selectedAircraft.getFlightPlan().indexOfWaypoint(clickedWaypoint);
					} else {
						// If the clicked waypoint is an entry/exit point, discard it
						// as we don't want the user to be able to move these points
						clickedWaypoint = null;
					}
				}
			}
			
			for (Airport airport : airports) {
				if (selectedAircraft != null && airport.isArrivalsClicked(x, y)) {
					if ((selectedAircraft.isWaitingToLand)
							&& (selectedAircraft.currentTarget.equals(airport.getLocation()))) {
						airport.mousePressed(key, x, y);
						selectedAircraft.land();
						deselectAircraft();
					}
				} else if (airport.isDeparturesClicked(x, y)) {
					if (airport.aircraftHangar.size() > 0) {
						airport.mousePressed(key, x, y);
						airport.signalTakeOff();
					}
				}
			}
		} else if (key == input.MOUSE_RIGHT) {
			if (aircraftClicked(x, y)) {
				selectedAircraft = findClickedAircraft(x, y);
			}
			if (selectedAircraft != null) {
				if (compassClicked()) {
					compassClicked = true; // Flag to mouseReleased
					if (!selectedAircraft.isManuallyControlled())
						toggleManualControl();
				} else {
					if (selectedAircraft.isManuallyControlled()) {
						toggleManualControl();
					} else {
						deselectAircraft();					
					}
				}
			}
		}
	}
	
	private boolean manualOverridePressed(int x, int y) {
		return manualOverrideButton.isMouseOver(x - xOffset, y - yOffset);
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		if (paused) return;
		
		for (Airport airport : airports) {
			airport.mouseReleased(key, x, y);
		}
		
		if (key == input.MOUSE_LEFT) {
			if (manualOverridePressed(x, y)) {
				manualOverrideButton.act();
			} else if (waypointClicked && selectedAircraft != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y);
				if (newWaypoint != null)  selectedAircraft.alterPath(selectedPathpoint, newWaypoint);
				selectedPathpoint = -1;
			}
			clickedWaypoint = null; // Fine to set to null now as will have been dealt with
		} else if (key == input.MOUSE_RIGHT) {
			if (compassClicked && selectedAircraft != null) {
				double dx = input.mouseX() - selectedAircraft.getPosition().getX() + xOffset - 8;
				double dy = input.mouseY() - selectedAircraft.getPosition().getY() + yOffset - 8;
				double newBearing = Math.atan2(dy, dx);
				selectedAircraft.setBearing(newBearing);
			}
		} else if (key == input.MOUSE_WHEEL_UP) {
			highlightedAltitude = 30000;
		} else if (key == input.MOUSE_WHEEL_DOWN){
			highlightedAltitude = 28000;
		}
	}

	@Override
	public void keyPressed(int key) {
		if (paused) return;
	}

	/**
	 * Handle keyboard input
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
				toggleManualControl();
				break;
			
			case input.KEY_LCRTL :
				generateFlight();
				break;
			
			case input.KEY_ESCAPE :
				aircraftInAirspace.clear();
				for (Airport airport : airports) airport.clear();
				main.closeScene();
				break;
			
			case input.KEY_F5 :
				Aircraft a1 = createAircraft();
				Aircraft a2 = createAircraft();
				gameOver(a1, a2);
				break;
		}
	}
	
	/**
	 * Draw a readout of the time the game has been played for, and number of planes in the sky.
	 */
	private void drawAdditional() {
		int hours = (int)(timeElapsed / (60 * 60));
		int minutes = (int)(timeElapsed / 60);
		minutes %= 60;
		double seconds = timeElapsed % 60;
		DecimalFormat df = new DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds); 
		graphics.print(timePlayed, window.width() - Demo.xOffset - (timePlayed.length() * 8 + 32), 32);
		int planes = aircraftInAirspace.size();
		graphics.print(String.valueOf("Highlighted altitude: " + Integer.toString(highlightedAltitude)) , 32, 15);
		graphics.print(String.valueOf(aircraftInAirspace.size())
				+ " plane" + (planes == 1 ? "" : "s") + " in the sky.", 32, 32);
	}
	
	/**
	 * Creates a new aircraft object and introduces it to the airspace. 
	 */
	private void generateFlight() {
		Aircraft aircraft = createAircraft();
		
		if (aircraft != null) {
			// If the aircraft starts at an airport, add it to that airport
			for (Airport airport : airports) {
				if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
					airport.addToHangar(aircraft);
					return;
				}
			}
			
			// Otherwise, add the aircraft to the airspace
			aircraftInAirspace.add(aircraft);
		}
	}
	
	/**
	 * Sets the airport to busy, adds the aircraft passed to the airspace,
	 * where it begins its flight plan starting at the airport.
	 * @param aircraft the aircraft to take off
	 */
	public static void takeOffSequence(Aircraft aircraft) {
		aircraftInAirspace.add(aircraft);
		
		for (Airport airport : airports) {
			if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
				airport.isActive = false;
				return;
			}
		}
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane.
	 * <p>
	 * Specifically, returns points where no plane is currently going to exit the
	 * airspace there, also it is not too close to any plane.
	 * </p>
	 */	
	private ArrayList<Waypoint> getAvailableEntryPoints() {
		ArrayList<Waypoint> availableEntryPoints = new ArrayList<Waypoint>();
		
		for (Waypoint entryPoint : locationWaypoints) {
			
			boolean isAvailable = true;
			// Prevents spawning a plane at a waypoint if:
			//   any plane is currently going towards it
			//   or any plane is less than 250 from it

			for (Aircraft aircraft : aircraftInAirspace) {
				// Check if any plane is currently going towards the exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close from the chosen originPoint
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
	 * Handle nitty gritty of aircraft creating
	 * including randomisation of entry, exit, altitude, etc.
	 * @return the created aircraft object
	 */
	private Aircraft createAircraft() {
		// Origin and Destination
		String destinationName;
		String originName = "";
		Waypoint originPoint = null;
		Waypoint destinationPoint;
		
		// Chooses two waypoints randomly and then checks if they satisfy the rules
		// If not, it tries until it finds good ones
	
		ArrayList<Waypoint> availableOrigins = getAvailableEntryPoints();
		
		if (availableOrigins.isEmpty()) {
			int randomAirport = (new Random()).nextInt((airports.length - 1) + 1);
			if (airports[randomAirport].aircraftHangar.size()
					== airports[randomAirport].getHangarSize()) {
				return null;
			} else {
				originPoint = airports[randomAirport].getDeparturesCentre();
				originName = airports[randomAirport].name;
			}
		} else {
			originPoint = availableOrigins.get(
					(new Random()).nextInt((availableOrigins.size() - 1) + 1));
			
			// If random point is an airport, use its departures location
			if (originPoint instanceof Airport) {
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
		
		// Work out destination
		// Keep trying until the random destination is not equal to the chosen origin
		int destination = 0;
		do {
			destination = (new Random()).nextInt((locationWaypoints.length - 1) + 1);
			destinationName = locationWaypoints[destination].getName();
			destinationPoint = locationWaypoints[destination];
		} while (locationWaypoints[destination].getName() == originName);
		
		// Generate a unique, random flight name
		String name = "";
		boolean nameTaken = true;
		while (nameTaken) {
			name = "Flight " + (int)(900 * Math.random() + 100);
			nameTaken = false;
			for (Aircraft a : aircraftInAirspace) {
				if (a.getName() == name) nameTaken = true;
			}
		}
		
		int speed = 32 + (int)(10 * Math.random());
		
		return new Aircraft(name, destinationName, originName, destinationPoint, originPoint,
				aircraftImage, speed, airspaceWaypoints, difficulty);
	}
	
	@Override
	public void playSound(audio.Sound sound) {
		sound.stop();
		sound.play();
	}
	
	/**
	 * Cleanly exit by stopping the scene's music
	 */
	@Override
	public void close() {
		music.stop();
	}

	/**
	 * Gets how long the game has been played for.
	 * @return the length of time the game has been running for
	 */
	public static double getTime() {
		return timeElapsed;
	}
	
	/**
	 * Getter for aircraft list.
	 * @return the arrayList of aircraft in the airspace
	 */
	public ArrayList<Aircraft> aircraftList() {
		return aircraftInAirspace;
	}
	
	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class).
	 * Its purpose is to initialize array where aircraft are stored. 
	 */	
	@Deprecated
	public void initializeAircraftArray() {
		aircraftInAirspace = new ArrayList<Aircraft>();
	}
	
	/**
	 * The interval in seconds to generate flights after.
	 */
	private int getFlightGenerationInterval() {
		switch (difficulty) {
		case 1:
			// Planes move 2x faster on medium so this makes them spawn
			// 2 times as often to keep the ratio
			return (30 / (maxAircraft * 2));
		case 2:
			// Planes move 3x faster on hard so this makes them spawn
			// 3 times as often to keep the ratio 
			return (30 / (maxAircraft * 3) );
		default:
			return (30 / maxAircraft);
		}	
	}
	
}
