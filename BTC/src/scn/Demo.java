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
import cls.Waypoint;

import btc.Main;

public class Demo extends Scene {
	
	// Due to the way the airspace elements are drawn (graphics.setviewport) these variables are needed to manually
	// adjust mouse listeners and elements drawn outside the airspace so that they align with the airspace elements.
	// These variables can be used to adjust the size of the airspace view.
	public static int xOffset = 196;
	public static int yOffset = 48;
	
	// Static constants for difficulty settings
	// Difficulty of demo scene determined by difficulty selection scene
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_MEDIUM = 1;
	public final static int DIFFICULTY_HARD = 2;
	public static int difficulty = DIFFICULTY_EASY;
	
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
	
	public ArrayList<Aircraft> recentlyDepartedAircraft;
	
	/** An image to be used for aircraft */
	private Image aircraftImage;
	
	/** A button to start and end manual control of an aircraft */
	private ButtonText manualOverrideButton;
	
	/** Tracks if manual heading compass of a manually controlled aircraft has been clicked */
	private boolean compassClicked;
	
	/** Tracks if waypoint of a manually controlled aircraft has been clicked */
	private boolean waypointClicked;
	
	/** The time elapsed since the last flight was generated */
	private double flightGenerationTimeElapsed = 6;
	
	/** The current control altitude of the ACTO - initially 30,000 */
	private int highlightedAltitude = 30000;
	
	/** Music to play during the game scene */
	private Music music;
	
	/** The background to draw in the airspace. */
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
	 * Constructor
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Demo(Main main, int difficulty) {
		super(main);
		Demo.difficulty = difficulty;
	}

	/**
	 * This constructor should only be used for unit testing. Its purpose is to allow an instance
	 * of demo class to be created without an instance of Main class (effectively launching the game)
	 * @param difficulty
	 */	
	@Deprecated
	public Demo(int difficulty) {
		Demo.difficulty = difficulty;
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
	 * This method provides maximum number of planes using value of multiplier
	 * @return maximum number of planes
	 */
	private int getMaxAircraft() {
		return 4; //TODO
	}
	
	/**
	 * The interval in seconds to generate flights after.
	 */
	private int getFlightGenerationInterval() {
		switch (difficulty) {
		case 1:
			// Planes move 2x faster on medium so this makes them spawn
			// 2 times as often to keep the ratio
			return (30 / (getMaxAircraft() * 2));
		case 2:
			// Planes move 3x faster on hard so this makes them spawn
			// 3 times as often to keep the ratio 
			return (30 / (getMaxAircraft() * 3) );
		default:
			return (30 / getMaxAircraft());
		}	
	}
	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty
	 */
	@Override
	public void start() {
		// Load in graphics
		background = graphics.newImage("gfx" + File.separator + "background_base.png");
		aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
		
		// Load in music
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		
		// Start the music
		//music.play(); TODO <-Add this back in for release

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
		Waypoint bottomLeft = new Waypoint(8, window.height() - (2 * xOffset) - 4,
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
		//ordersBox = new cls.OrdersBox(ORDERSBOX_X, ORDERSBOX_Y, ORDERSBOX_W, ORDERSBOX_H, 6);
		aircraftInAirspace = new ArrayList<Aircraft>();
		recentlyDepartedAircraft = new ArrayList<Aircraft>();

		lib.ButtonText.Action manual = new lib.ButtonText.Action() {
			@Override
			public void action() {
				toggleManualControl();
			}
		};
		
		manualOverrideButton = new lib.ButtonText("Take Control", manual,
				(window.width() -128 - 2*xOffset) / 2, 32, 128, 32, 8, 4);
		timeElapsed = 0;
		compassClicked = false;
		selectedAircraft = null;
		clickedWaypoint = null;
		selectedPathpoint = -1;
		
		manualOverrideButton = new lib.ButtonText(" Take Control", manual,
				(window.width() - 128 - 2*xOffset) / 2, 32, 128, 32, 8, 4);
		deselectAircraft();
	}
	
	/**
	 * Getter for aircraft list.
	 * @return the arrayList of aircraft in the airspace
	 */
	public ArrayList<Aircraft> aircraftList() {
		return aircraftInAirspace;
	}
	
	/**
	 * Causes a selected aircraft to call methods to toggle manual control
	 */
	private void toggleManualControl() {
		if (selectedAircraft == null) return;
		selectedAircraft.toggleManualControl();
		manualOverrideButton.setText( (selectedAircraft.isManuallyControlled() ? "Remove" : " Take") + " Control");
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
	 * Update all objects within the scene, ie aircraft, orders box altimeter.
	 * Cause collision detection to occur
	 * Generate a new flight if flight generation interval has been exceeded.
	 */
	@Override
	public void update(double timeDifference) {
		if (paused) return;
		
		timeElapsed += timeDifference;
		graphics.setColour(graphics.green_transp);
		
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.update(timeDifference);
			if (aircraft.isFinished()) {
				aircraft.setDepartureTime(System.currentTimeMillis());
				recentlyDepartedAircraft.add(aircraft);
			}
		}
		checkCollisions(timeDifference);
		for (int i = aircraftInAirspace.size()-1; i >=0; i --) {
			if (aircraftInAirspace.get(i).isFinished()) {
				if (aircraftInAirspace.get(i) == selectedAircraft) {
					deselectAircraft();
				}
				aircraftInAirspace.remove(i);
			}
		}
		
		for (Airport airport : airports) {
			airport.update(aircraftInAirspace);
		}
		
		if (selectedAircraft != null) {
			if (selectedAircraft.isManuallyControlled()) {
				if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A})) {
					selectedAircraft.turnLeft(timeDifference);
				} else if (input.keyPressed(new int[]{input.KEY_RIGHT, input.KEY_D})) {
					selectedAircraft.turnRight(timeDifference);
				}
			} else if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A, input.KEY_RIGHT, input.KEY_D})) {
				toggleManualControl();
			}
			
			if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})&& selectedAircraft.getPosition().getZ() > 28000) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})&&selectedAircraft.getPosition().getZ() < 30000) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
				
			if (!(selectedAircraft.isAtDestination())) {
				if (selectedAircraft.isOutOfAirspaceBounds()) {
					deselectAircraft();
				}
			}	
		}
		
		flightGenerationTimeElapsed += timeDifference;
		if(flightGenerationTimeElapsed >= getFlightGenerationInterval()) {
			flightGenerationTimeElapsed -= getFlightGenerationInterval();
			if (aircraftInAirspace.size() < getMaxAircraft()) {
				generateFlight();
			}
		}
		if (aircraftInAirspace.size() == 0)
			generateFlight();
	}
	
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints
	 */
	@Override
	public void draw() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, xOffset, yOffset,
				window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));// - 176);
		graphics.setViewport(xOffset, yOffset,
				window.width() - (2 * xOffset),
				window.height() - (2 * yOffset));// - 176);
		graphics.setColour(255, 255, 255, 48);
		graphics.drawScaled(background, 0, 0, Math.max(Main.getXScale(), Main.getYScale()));
		graphics.setColour(255, 255, 255, 48);
		
		for (Airport airport : airports) {
			airport.draw();
		}
		
		drawMap();	
		graphics.setViewport();
		
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.drawCompass();
		}
		
		//score.draw();
		//ordersBox.draw();
		//altimeter.draw();
		//airportControlBox.draw();
		//drawPlaneInfo();
		
		graphics.setColour(graphics.green);
		drawAdditional();
		
		if (selectedAircraft != null) {//HERE
			graphics.line(input.mouseX(),
					input.mouseY(),
					selectedAircraft.getPosition().getX() + Demo.xOffset - 16/2,
					selectedAircraft.getPosition().getY() + Demo.yOffset - 16/2);
			
			graphics.line(selectedAircraft.getPosition().getX() + Demo.xOffset - 16/2,
					selectedAircraft.getPosition().getY() + Demo.yOffset - 16/2,
					selectedAircraft.getPosition().getX() + Demo.xOffset - 16/2 + (Math.cos(selectedAircraft.getBearing()) * 100),
					selectedAircraft.getPosition().getY() + Demo.yOffset - 16/2 + (Math.sin(selectedAircraft.getBearing()) * 100));
		}
	}
	
	/**
	 * Cause all planes in airspace to update collisions
	 * Catch and handle a resultant game over state
	 * @param timeDifference delta time since last collision check
	 */
	private void checkCollisions(double timeDifference) {
		for (Aircraft plane : aircraftInAirspace) {
			int collisionState = plane.updateCollisions(timeDifference, aircraftList());
			if (collisionState >= 0) {
				gameOver(plane, aircraftList().get(collisionState));
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
		
		for (Airport airport : airports) {
			airport.clear();
		}
		
		// playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, 0)); //TODO
	}
	
	/**
	 * Causes the scene to pause execution for the specified number of seconds
	 * @param seconds the number of seconds to wait.
	 */
	@Deprecated
	public void wait(int seconds){
		long startTime, endTime;
		startTime = System.currentTimeMillis();
		endTime = startTime + (seconds * 1000);
		
		while (startTime < endTime){
			startTime = System.currentTimeMillis();
		}
		
		return;
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
		//airportControlBox.mousePressed(key, x, y);
		//altimeter.mousePressed(key, x, y);
		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y)) {
				Aircraft clickedAircraft = findClickedAircraft(x, y);
				deselectAircraft();
				selectedAircraft = clickedAircraft;
				//altimeter.show(selectedAircraft);
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
				if (airport.isArrivalsClicked(x, y) && selectedAircraft != null) {
					if (selectedAircraft.isWaitingToLand && selectedAircraft.currentTarget.equals(airport.getLocation())) {
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
		for (Airport airport : airports) {
			airport.mouseReleased(key, x, y);
		}
		
		//airportControlBox.mouseReleased(key, x, y);
		//altimeter.mouseReleased(key, x, y);
		
		if (key == input.MOUSE_LEFT) {
			if (manualOverridePressed(x, y)) {
				manualOverrideButton.act();
			} else if (waypointClicked && selectedAircraft != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y);
				if (newWaypoint instanceof Airport) {
					//ordersBox.addOrder(">>> " + selectedAircraft.getName() + " please avoid cruising over Mosbear Airport, find a new course.");
					//ordersBox.addOrder("<<< Roger that. Going around.");
				} else if (newWaypoint != null) {
					selectedAircraft.alterPath(selectedPathpoint, newWaypoint);
					//ordersBox.addOrder(">>> " + selectedAircraft.getName() + " please alter your course.");
					//ordersBox.addOrder("<<< Roger that. Altering course now.");
				}
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
	public void keyPressed(int key) {}

	/**
	 * Handle keyboard input
	 */
	@Override
	public void keyReleased(int key) {
		switch (key) {
			case input.KEY_P :
				paused = !paused;
				break;
		
			case input.KEY_SPACE :
				toggleManualControl();
				break;
			
			case input.KEY_LCRTL :
				generateFlight();
				break;
			
			case input.KEY_ESCAPE :
				aircraftInAirspace.clear();
				
				for (Airport airport : airports) {
					airport.clear();
				}
				
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
	 * Draw waypoints, and route of a selected aircraft between waypoints.
	 * <p>
	 * Also prints waypoint names next to waypoints.
	 * </p>
	 */
	private void drawMap() {
		for (Waypoint waypoint : airspaceWaypoints) {
			if (!(waypoint instanceof Airport)) { // Skip the airport
				waypoint.draw();
			}
		}
		graphics.setColour(255, 255, 255);
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.draw(highlightedAltitude);
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
		
		if (selectedAircraft != null) {
			// Flight Path
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			// Override Button
			graphics.setColour(graphics.black);
			graphics.rectangle(true, (window.width() - 128 - 2*xOffset) / 2, 32, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false, (window.width() - 128 - 2*xOffset) / 2, 32, 128, 32);
			manualOverrideButton.draw();
			
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			
		}
		
		if (clickedWaypoint != null && selectedAircraft.isManuallyControlled() == false) {
			selectedAircraft.drawModifiedPath(selectedPathpoint, input.mouseX() - xOffset, input.mouseY() - yOffset);
		}
		
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
		graphics.print(String.valueOf(aircraftInAirspace.size()) + " plane" + (planes == 1 ? "" : "s") + " in the sky.", 32, 32);
	}
	
	/**
	 * Creates a new aircraft object and introduces it to the airspace. 
	 */
	private void generateFlight() {
		Aircraft a = createAircraft();
		if (a != null) {
			for (Airport airport : airports) {
				if (a.getFlightPlan().getOriginName().equals(airport.name)) {
					airport.addToHangar(a);
				} else {
					aircraftInAirspace.add(a);
				}
			}
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
			airport.isActive = false;
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
			/**
			 * prevents spawning a plane in waypoint both:
			 * if any plane is currently going towards it 
			 * if any plane is less than 250 from it
			 */
			
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
			for (Airport airport : airports) {
				if (airport.aircraftHangar.size() == airport.getHangarSize()) {
					return null;
				} else {
					originPoint = airport;
					originName = airport.name;
				}
			}
		} else {
			originPoint = availableOrigins.get((new Random())
					.nextInt((availableOrigins.size() - 1) + 1));
			for (int i = 0; i < locationWaypoints.length; i++) {
				if (locationWaypoints[i].equals(originPoint)) {
					originName = locationWaypoints[i].getName();
					break;
				}
			}
		}
		
		// Work out destination
		int destination = (new Random()).nextInt((locationWaypoints.length - 1) + 1);
		destinationName = locationWaypoints[destination].getName();
		destinationPoint = locationWaypoints[destination];
		
		while (locationWaypoints[destination].getName() == originName) {
			destination = (new Random()).nextInt((locationWaypoints.length - 1) + 1);
			destinationName = locationWaypoints[destination].getName();
			destinationPoint = locationWaypoints[destination];
		}
			
		
		// Name
		String name = "";
		boolean nameTaken = true;
		while (nameTaken) {
			name = "Flight " + (int)(900 * Math.random() + 100);
			nameTaken = false;
			for (Aircraft a : aircraftInAirspace) {
				if (a.getName() == name) nameTaken = true;
			}
		}
		
		return new Aircraft(name, destinationName, originName, destinationPoint, originPoint,
				aircraftImage, 32 + (int)(10 * Math.random()), airspaceWaypoints, difficulty);
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

	public static double getTime() {
		return timeElapsed;
	}
	
}
