package scn;

import java.io.File;

import lib.RandomNumber;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.window;
import cls.Aircraft;
import cls.Airport;
import cls.AirportControlBox;
import cls.Vector;
import cls.Waypoint;
import btc.Main;

public class Demo extends Scene {
	
	// Position of things drawn to window   
	private final int PLANE_INFO_X = 16;
	private final int PLANE_INFO_Y = window.height() - 120;
	private final int PLANE_INFO_W = window.width()/4 - 16;
	private final int PLANE_INFO_H = 112;
	
	private final int ALTIMETER_X = PLANE_INFO_X + PLANE_INFO_W + 8;
	private final int ALTIMETER_Y = window.height() - 120;
	private final int ALTIMETER_W = 244;
	private final int ALTIMETER_H = 112;
	
	private final int AIRPORT_CONTROL_X = ALTIMETER_X + ALTIMETER_W + 8;
	private final int AIRPORT_CONTROL_Y = window.height() - 120;
	private final int AIRPORT_CONTROL_W = 244;
	private final int AIRPORT_CONTROL_H = 112;
	
	private final int ORDERSBOX_X = AIRPORT_CONTROL_X + AIRPORT_CONTROL_W + 8;
	private final static int ORDERSBOX_Y = window.height() - 120;
	private final int ORDERSBOX_W = window.width() - (ORDERSBOX_X + 16);
	private final static int ORDERSBOX_H = 112;
	
	// Static CONSTANTS for difficulty settings
	// Difficulty of demo scene determined by difficulty selection scene
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_MEDIUM = 1;
	public final static int DIFFICULTY_HARD = 2;
	public static int difficulty = DIFFICULTY_EASY;
	
	// Necessary for testing
	
	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class).
	 * Its purpose is to initialize array where aircraft are stored. 
	 */	
	@Deprecated
	public void initializeAircraftArray() {
		aircraftInAirspace = new java.util.ArrayList<Aircraft>();
	}
	
	// Additional constructor for testing purposes
	 
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
	 * The score throughout the game
	 */
	private cls.Score score; 
	
	/**
	 * Flag to dtermine if aircraft warning message is to be displayed
	 */
	private boolean shownAircraftWaitingMessage = false;
	
	/**
	 * Orders box to print orders from ACTO to aircraft to
	 */
	private cls.OrdersBox ordersBox;
	
	/**
	 * Time since the scene began
	 * Could be used for score
	 */
	private static double timeElapsed;
	
	/**
	 * The currently selected aircraft
	 */
	private Aircraft selectedAircraft;
	
	/**
	 * The currently selected waypoint
	 */
	private Waypoint clickedWaypoint;
	
	/**
	 * Selected path point, in an aircraft's route, used for altering the route
	 */
	private int selectedPathpoint;
	
	/**
	 * A list of aircraft present in the airspace
	 */
	public static java.util.ArrayList<Aircraft> aircraftInAirspace;
	
	public java.util.ArrayList<Aircraft> recentlyDepartedAircraft;
	
	/**
	 * An image to be used for airports
	 */
	private Image airportImage;
	
	/**
	 * An image to be used for aircraft
	 * Expand to list of images for multiple aircraft appearances
	 */
	private graphics.Image aircraftImage;
	
	/**
	 * A button to start and end manual control of an aircraft
	 */
	private lib.ButtonText manualOverrideButton;
	
	/**
	 * Tracks if manual heading compass of a manually controlled aircraft has been clicked
	 */
	private boolean compassClicked;
	
	/**
	 * Tracks if waypoint of a manually controlled aircraft has been clicked
	 */
	private boolean waypointClicked;
	
	/**
	 * An altimeter to display aircraft altitidue, heading, etc.
	 */
	private cls.Altimeter altimeter;
	
	private cls.AirportControlBox airportControlBox;
	
	/**
	 * The time elapsed since the last flight was generated
	 */
	private double flightGenerationTimeElapsed = 6;
	
	/**
	 * The current control altitude of the ACTO - initially 30,000
	 */
	private int highlightedAltitude = 30000;
	
	/**
	 * Music to play during the game scene
	 */
	private audio.Music music;
	
	/**
	 * The background to draw in the airspace.
	 */
	private graphics.Image background;
	
	/**
	 * Demo's instance of the airport class
	 */
	public static Airport airport;
	
	/**
	 * This method provides maximum number of planes using value of multiplier
	 * @return maximum number of planes
	 */
	private int getMaxAircraft() {
		if (score.getMultiplier() == 1) 
			return 3;
		else if (score.getMultiplier() == 3) 
			return 5;
		else
			return score.getMultiplier();
	}
	
	/**
	 * The interval in seconds to generate flights after
	 */
	private int getFlightGenerationInterval() {
		if (difficulty == 1)
			return (30 / (getMaxAircraft() * 2)); // Planes move 2x faster on medium so this makes them spawn 2 times as often to keep the ratio
		if (difficulty == 2)
			return (30 / (getMaxAircraft() * 3) ); // Planes move 3x faster on hard so this makes them spawn 3 times as often to keep the ratio 
		return (30 / getMaxAircraft());
	}
	
	/**
	 * The set of waypoints in the airspace which are origins / destinations
	 */
	public static Waypoint[] locationWaypoints = new Waypoint[] {
		/* A set of Waypoints which are origin / destination points */
		new Waypoint(8, 8, true, "North West Top Leftonia"), //top left
		new Waypoint(8, window.height() - ORDERSBOX_H - 72, true, "100 Acre Woods"), //bottom left
		new Waypoint(window.width() - 40, 8, true, "City of Rightson"), // top right
		new Waypoint(window.width() - 40, window.height() - ORDERSBOX_H - 72, true, "South Sea"), //bottom right
		null
	};

	/**
	 * All waypoints in the airspace, INCLUDING locationWaypoints.
	 */
	public static Waypoint[] airspaceWaypoints = new Waypoint[] {		
		/* All waypoints in the airspace, including location Way Points*/
	
		// Airspace waypoints
		new Waypoint(125, 70, false),   // 0
		new Waypoint(700, 100, false),  // 1
		new Waypoint(1040, 80, false),  // 2
		new Waypoint(500, 200, false),  // 3
		new Waypoint(1050, 400, false), // 4
		new Waypoint(250, 400, false),  // 5
		new Waypoint(200, 635, false),  // 6
		new Waypoint(500, 655, false),  // 7
		new Waypoint(800, 750, false),  // 8
		new Waypoint(1000, 750, false), // 9
		// Destination/origin waypoints - present in this list for pathfinding.
		locationWaypoints[0],           // 10
		locationWaypoints[1],           // 11
		locationWaypoints[2],           // 12
		locationWaypoints[3],           // 13
		null							// 14
	};
	
	/**
	 * Constructor
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Demo(Main main, int difficulty) {
		super(main);
		Demo.difficulty = difficulty;
	}
	
	@Override
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty
	 */
	public void start() {
		background = graphics.newImage("gfx" + File.separator + "background_base.png");
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		//music.play();
		ordersBox = new cls.OrdersBox(ORDERSBOX_X, ORDERSBOX_Y, ORDERSBOX_W, ORDERSBOX_H, 6);
		aircraftInAirspace = new java.util.ArrayList<Aircraft>();
		recentlyDepartedAircraft = new java.util.ArrayList<Aircraft>();
		
		aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
		airportImage = graphics.newImage("gfx" + File.separator + "Airport.png");
		
		airport = new Airport("Mosbear Airport", window.width()/2, window.height()/2, airportImage);
		locationWaypoints[locationWaypoints.length-1] = airport;
		airspaceWaypoints[airspaceWaypoints.length-1] = airport;
		
		lib.ButtonText.Action manual = new lib.ButtonText.Action() {
			@Override
			public void action() {
				// _selectedAircraft.manuallyControl();
				toggleManualControl();
			}
		};
		
		score = new cls.Score();
		
		manualOverrideButton = new lib.ButtonText("Take Control", manual, (window.width() - 128) / 2, 32, 128, 64, 8, 4);
		timeElapsed = 0;
		compassClicked = false;
		selectedAircraft = null;
		clickedWaypoint = null;
		selectedPathpoint = -1;
		
		manualOverrideButton = new lib.ButtonText(" Take Control", manual, (window.width() - 128) / 2, 32, 128, 64, 8, 4);
		altimeter = new cls.Altimeter(ALTIMETER_X, ALTIMETER_Y, ALTIMETER_W, ALTIMETER_H, ordersBox);
		airportControlBox = new AirportControlBox(AIRPORT_CONTROL_X, AIRPORT_CONTROL_Y, AIRPORT_CONTROL_W, AIRPORT_CONTROL_H, airport);
		deselectAircraft();
	}
	
	/**
	 * Getter for aircraft list
	 * @return the arrayList of aircraft in the airspace
	 */
	public java.util.ArrayList<Aircraft> aircraftList() {
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
	 * Causes an aircraft to call methods to handle deselection
	 */
	private void deselectAircraft() {
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.toggleManualControl();
			manualOverrideButton.setText(" Take Control");
		}
		selectedAircraft = null;
		clickedWaypoint = null; 
		selectedPathpoint = -1;
		altimeter.hide();
	}
	
	/**
	 * Update all objects within the scene, ie aircraft, orders box altimeter.
	 * Cause collision detection to occur
	 * Generate a new flight if flight generation interval has been exceeded.
	 */
	@Override
	public void update(double timeDifference) {
		timeElapsed += timeDifference;
		score.update();
		graphics.setColour(graphics.green_transp);
		if (airport.getLongestTimeInHangar(timeElapsed) > 5) {
			score.increaseMeterFill(-1);
			if (!shownAircraftWaitingMessage) {
				ordersBox.addOrder(">>> Plane waiting to take off, multiplier decreasing");
				shownAircraftWaitingMessage = true;
			}
		} else {
			shownAircraftWaitingMessage = false;
		}
		
		ordersBox.update(timeDifference);
		for (Aircraft aircraft : aircraftInAirspace) {
			aircraft.update(timeDifference);
			if (aircraft.isFinished()) {
				aircraft.setAdditionToMultiplier(score.getMultiplierLevel());
				score.increaseMeterFill(aircraft.getAdditionToMultiplier());
				aircraft.setScore(score.calculateAircraftScore(aircraft));
				score.increaseTotalScore(score.getMultiplier() * aircraft.getScore());
				aircraft.setDepartureTime(System.currentTimeMillis());
				recentlyDepartedAircraft.add(aircraft);
		
				if (aircraft.getAdditionToMultiplier() < 0)
					ordersBox.addOrder("<<< The plane has breached separation rules on its path, your multiplier may be reduced ");
				
				switch (RandomNumber.randInclusiveInt(0, 2)){
				case 0:
					ordersBox.addOrder("<<< Thank you Comrade");
					break;
				case 1:
					ordersBox.addOrder("<<< Well done Comrade");
					break;
				case 2:
					ordersBox.addOrder("<<< Many thanks Comrade");
					break;
				}
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
		airport.update(this);
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
			
			if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})) {
				selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
				
			if (selectedAircraft.isOutOfAirspaceBounds()) {
				ordersBox.addOrder(">>> " + selectedAircraft.getName() + " out of bounds, returning to route");
				deselectAircraft();
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
	 * Cause all planes in airspace to update collisions
	 * Catch and handle a resultant game over state
	 * @param time_difference delta time since last collision check
	 */
	private void checkCollisions(double timeDifference) {
		for (Aircraft plane : aircraftInAirspace) {
			int collisionState = plane.updateCollisions(timeDifference, aircraftList(), score);
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
		airport.clear();
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, score.getTotalScore()));
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
			double dx = selectedAircraft.getPosition().getX() - input.mouseX() + airspaceViewOffsetX;
			double dy = selectedAircraft.getPosition().getY() - input.mouseY() + airspaceViewOffsetY;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		return false;
	}
	
	private boolean aircraftClicked(int x, int y) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(x - airspaceViewOffsetX, y - airspaceViewOffsetY)) {
				return true;
			}
		}
		return false;
	}
	
	private Aircraft findClickedAircraft(int x, int y) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(x - airspaceViewOffsetX, y - airspaceViewOffsetY)) {
				return a;
			}
		}
		return null;
	}
	
	private boolean waypointInFlightplanClicked(int x, int y, Aircraft a) {
		if (a != null) {
			for (Waypoint w : airspaceWaypoints) {
				if (w.isMouseOver(x - airspaceViewOffsetX, y - airspaceViewOffsetY) && a.getFlightPlan().indexOfWaypoint(w) > -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Waypoint findClickedWaypoint(int x, int y) {
		for (Waypoint w : airspaceWaypoints) {
			if (w.isMouseOver(x - airspaceViewOffsetX, y - airspaceViewOffsetY)) {
				return w;
			}
		}
		return null;
	}
	
	private boolean isArrivalsClicked(int x, int y) {
		return airport.isWithinArrivals(new Vector(x,y,0)) && !airport.isActive;
	}
	
	private boolean isDeparturesClicked(int x, int y) {
		return airport.isWithinDepartures(new Vector(x,y,0)) && !airport.isActive;
	}

	/**
	 * Handle mouse input
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		airportControlBox.mousePressed(key, x, y);
		altimeter.mousePressed(key, x, y);
		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y)) {
				Aircraft clickedAircraft = findClickedAircraft(x, y);
				deselectAircraft();
				selectedAircraft = clickedAircraft;
				altimeter.show(selectedAircraft);
				
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
			
			if (isArrivalsClicked(x, y) && selectedAircraft != null) {
				if (selectedAircraft.isWaitingToLand && selectedAircraft.currentTarget.equals(airport.getLocation())) {
					airport.mousePressed(key, x, y);
					selectedAircraft.land();
					deselectAircraft();
				}
			} else if (isDeparturesClicked(x, y)) {
				if (airport.aircraftHangar.size() > 0) {
					airport.mousePressed(key, x, y);
					airport.signalTakeOff();
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
		return manualOverrideButton.isMouseOver(x - airspaceViewOffsetX, y - airspaceViewOffsetY);
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		airport.mouseReleased(key, x, y);
		airportControlBox.mouseReleased(key, x, y);
		altimeter.mouseReleased(key, x, y);
		
		if (key == input.MOUSE_LEFT) {
			if (manualOverridePressed(x, y)) {
				manualOverrideButton.act();
			} else if (waypointClicked && selectedAircraft != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y);
				if (newWaypoint instanceof Airport) {
					ordersBox.addOrder(">>> " + selectedAircraft.getName() + " please avoid cruising over Mosbear Airport, find a new course.");
					ordersBox.addOrder("<<< Roger that. Going around.");
				} else if (newWaypoint != null) {
					selectedAircraft.alterPath(selectedPathpoint, newWaypoint);
					ordersBox.addOrder(">>> " + selectedAircraft.getName() + " please alter your course.");
					ordersBox.addOrder("<<< Roger that. Altering course now.");
				}
				selectedPathpoint = -1;
			}
			clickedWaypoint = null; // Fine to set to null now as will have been dealt with
		} else if (key == input.MOUSE_RIGHT) {
			if (compassClicked && selectedAircraft != null) {
				double dx = input.mouseX() - selectedAircraft.getPosition().getX() + airspaceViewOffsetX;
				double dy = input.mouseY() - selectedAircraft.getPosition().getY() + airspaceViewOffsetY;
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
	}

	@Override
	/**
	 * handle keyboard input
	 */
	public void keyReleased(int key) {
		switch (key) {
		
			case input.KEY_SPACE :
				toggleManualControl();
			break;
			
			case input.KEY_LCRTL :
				generateFlight();
			break;
			
			case input.KEY_ESCAPE :
				aircraftInAirspace.clear();
				airport.clear();
				main.closeScene();
			break;
			
			case input.KEY_F5 :
				Aircraft a1 = createAircraft();
				Aircraft a2 = createAircraft();
				gameOver(a1, a2);
			break;
		}
	}
	
	// Due to the way the airspace elements are drawn (graphics.setviewport) these variables are needed to manually adjust mouse listeners and elements
	// drawn outside the airspace so that they align with the airspace elements. These variables can be used to adjust the size of the airspace view.
	public static int airspaceViewOffsetX = 16;
	public static int airspaceViewOffsetY = 48;
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints
	 */
	@Override
	public void draw() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, airspaceViewOffsetX, airspaceViewOffsetY,
				window.width() - 32, window.height() - 176);
		graphics.setViewport(airspaceViewOffsetX, airspaceViewOffsetY,
				window.width() - 32, window.height() - 176);
		graphics.setColour(255, 255, 255, 48);
		graphics.drawScaled(background, 0, 0, Math.max(Main.getXScale(), Main.getYScale()));
		graphics.setColour(255, 255, 255, 48);
		airport.draw();
		drawMap();	
		graphics.setViewport();
		
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.drawCompass();
		}
		
		score.draw();
		ordersBox.draw();
		altimeter.draw();
		airportControlBox.draw();
		drawPlaneInfo();
		
		graphics.setColour(graphics.green);
		drawAdditional();
		drawPlaneScoreLabels();
	}
	
	/**
	 * draw waypoints, and route of a selected aircraft between waypoints
	 * print waypoint names next to waypoints
	 */
	private void drawMap() {
		for (Waypoint waypoint : airspaceWaypoints) {
			if (!waypoint.equals(airport)) { // Skip the airport
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
			graphics.rectangle(true, (window.width() - 128) / 2, 16, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false, (window.width() - 128) / 2, 16, 128, 32);
			manualOverrideButton.draw();
			
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			
		}
		
		if (clickedWaypoint != null && selectedAircraft.isManuallyControlled() == false) {
			selectedAircraft.drawModifiedPath(selectedPathpoint, input.mouseX() - airspaceViewOffsetX, input.mouseY() - airspaceViewOffsetY);
		}
		
		graphics.setViewport();
		graphics.setColour(graphics.green);
		graphics.print(locationWaypoints[0].getName(), locationWaypoints[0].getLocation().getX() + airspaceViewOffsetX + 9, locationWaypoints[0].getLocation().getY() + airspaceViewOffsetY - 6);
		graphics.print(locationWaypoints[1].getName(), locationWaypoints[1].getLocation().getX() + airspaceViewOffsetX + 9, locationWaypoints[1].getLocation().getY() + airspaceViewOffsetY - 6);
		graphics.print(locationWaypoints[2].getName(), locationWaypoints[2].getLocation().getX() + airspaceViewOffsetX - 141, locationWaypoints[2].getLocation().getY() + airspaceViewOffsetY - 6);
		graphics.print(locationWaypoints[3].getName(), locationWaypoints[3].getLocation().getX() + airspaceViewOffsetX- 91, locationWaypoints[3].getLocation().getY() + airspaceViewOffsetY - 6);
		graphics.print(locationWaypoints[4].getName(), locationWaypoints[4].getLocation().getX() + airspaceViewOffsetX - 20, locationWaypoints[4].getLocation().getY() + airspaceViewOffsetY + 25);

	}
	
	/**
	 * draw the info of a selected plane in the scene GUI
	 */
	private void drawPlaneInfo() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, PLANE_INFO_X, PLANE_INFO_Y, PLANE_INFO_W, PLANE_INFO_H);
		if (selectedAircraft != null) {
			graphics.setViewport(PLANE_INFO_X, PLANE_INFO_Y, PLANE_INFO_W, PLANE_INFO_H);
			graphics.printCentred(selectedAircraft.getName(), 0, 5, 2, PLANE_INFO_W);
			// Altitude
			String altitude = String.format("%.0f", selectedAircraft.getPosition().getZ()) + "+";
			graphics.print("Altitude:", 10, 40);
			graphics.print(altitude, PLANE_INFO_W - 10 - altitude.length()*8, 40);
			// Speed
			String speed = String.format("%.2f", selectedAircraft.getSpeed() * 1.687810) + "=";
			graphics.print("Speed:", 10, 55);
			graphics.print(speed, PLANE_INFO_W - 10 - speed.length()*8, 55);
			// Origin
			graphics.print("Origin:", 10, 70);
			graphics.print(selectedAircraft.getFlightPlan().getOriginName(), PLANE_INFO_W - 10 - selectedAircraft.getFlightPlan().getOriginName().length()*8, 70);
			// Destination
			graphics.print("Destination:", 10, 85);
			graphics.print(selectedAircraft.getFlightPlan().getDestinationName(), PLANE_INFO_W - 10 - selectedAircraft.getFlightPlan().getDestinationName().length()*8, 85);
			graphics.setViewport();
		}
	}
	
	/**
	 * Draws points scored for a plane when it successfully leaves the airspace. The points the
	 * plane scored are displayed just above the plane.
	 */
	private void drawPlaneScoreLabels() {
		Aircraft aircraftToRemove = null;
		int displayedFor = 2000; // How long the label will be displayed for
		if (recentlyDepartedAircraft.size() != 0) {
			for (Aircraft plane : recentlyDepartedAircraft) {
				if (plane != null) {
					double currentTime = System.currentTimeMillis(); // Current (system) time
					double departureTime = plane.getTimeOfDeparture(); // Time when the plane successfully left airspace 
					double leftAirspaceFor = currentTime - departureTime; // How long since the plane left airspace
					if (leftAirspaceFor > displayedFor) {
						aircraftToRemove = plane;
					}
					else {
						int scoreTextAlpha =  (int)((displayedFor - leftAirspaceFor)/displayedFor * 255); // Transparency of the label, 255 is opaque
						String planeScoreValue = String.valueOf(plane.getScore() * score.getMultiplier());
						// Drawing the score
						int scoreTextX = (int) plane.getFlightPlan().getRoute()[plane.getFlightPlan().getRoute().length -1].getLocation().getX();
						int scoreTextY = (int) plane.getFlightPlan().getRoute()[plane.getFlightPlan().getRoute().length -1].getLocation().getY();
						graphics.setColour(255, 255, 255, scoreTextAlpha);
						if (scoreTextX < 40) scoreTextX += 50;
						if (scoreTextY < 40) scoreTextY += 50;
						if (scoreTextX > 1000) scoreTextX -= 50;
						if (scoreTextY > 1000) scoreTextY -= 50;
						graphics.print(planeScoreValue, scoreTextX, scoreTextY, 2);
					}
				}
			} 
			if (aircraftToRemove != null)
				recentlyDepartedAircraft.remove(aircraftToRemove);
		}
		
	}
		
	
	/**
	 * draw a readout of the time the game has been played for & aircraft in the sky.
	 */
	private void drawAdditional() {
		int hours = (int)(timeElapsed / (60 * 60));
		int minutes = (int)(timeElapsed / 60);
		minutes %= 60;
		double seconds = timeElapsed % 60;
		java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds); 
		graphics.print(timePlayed, window.width() - (timePlayed.length() * 8 + 32), 32);
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
			if (a.getFlightPlan().getOriginName().equals(airport.name)) {
				ordersBox.addOrder("<<< " + a.getName() + " is awaiting take off from " + a.getFlightPlan().getOriginName() + " heading towards " + a.getFlightPlan().getDestinationName() + ".");
				airport.addToHangar(a);
			} else {
				ordersBox.addOrder("<<< " + a.getName() + " incoming from " + a.getFlightPlan().getOriginName() + " heading towards " + a.getFlightPlan().getDestinationName() + ".");
				aircraftInAirspace.add(a);
			}
		}
	}
	
	/**
	 * Sets the airport to busy, adds the aircraft passed to the airspace, where it begins its flight plan starting at the airport
	 * @param aircraft
	 */
	public static void takeOffSequence(Aircraft aircraft) {
		aircraftInAirspace.add(aircraft);
		// Space to implement some animation features?
		airport.isActive = false;
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane (no plane is currently going to exit the airspace there,
	 * also it is not too close to any plane). 
	 * @param aircraft
	 */	
	private java.util.ArrayList<Waypoint> getAvailableEntryPoints() {
		java.util.ArrayList<Waypoint> available_entry_points = new java.util.ArrayList<Waypoint>();
		
		for (Waypoint entry_point : locationWaypoints) {
			
			boolean isAvailable = true;
			/**
			 * prevents spawning a plane in waypoint both:
			 * if any plane is currently going towards it 
			 * if any plane is less than 250 from it
			 */
			
			for (Aircraft aircraft : aircraftInAirspace) {
				// Check if any plane is currently going towards the exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close from the chosen originPoint
				if (aircraft.currentTarget.equals(entry_point.getLocation()) || aircraft.isCloseToEntry(entry_point.getLocation())) {
					isAvailable = false;
				}	
			}
			
			if (isAvailable) {
				available_entry_points.add(entry_point);
			}	
		}
		return available_entry_points;
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
		Waypoint originPoint;
		Waypoint destinationPoint;
	
		/**
		 * Chooses two waypoints randomly and then checks if they satisfy the rules, if not, it tries until it finds good ones. 
		 **/
	
		java.util.ArrayList<Waypoint> available_origins = getAvailableEntryPoints();
		
		if (available_origins.isEmpty()) {
			if (airport.aircraftHangar.size() == airport.getHangarSize()) {
				return null;
			} else {
				originPoint = airport;
				originName = airport.name;
			}
		} else {
			originPoint = available_origins.get(RandomNumber.randInclusiveInt(0, available_origins.size()-1));
			for (int i = 0; i < locationWaypoints.length; i++) {
				if (locationWaypoints[i].equals(originPoint)) {
					originName = locationWaypoints[i].getName();
					break;
				}
			}
		}
		
		// Work out destination
		int destination = RandomNumber.randInclusiveInt(0, locationWaypoints.length - 1);
		destinationName = locationWaypoints[destination].getName();
		destinationPoint = locationWaypoints[destination];
		
		while (locationWaypoints[destination].getName() == originName) {
			destination = RandomNumber.randInclusiveInt(0, locationWaypoints.length - 1);
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
		
		return new Aircraft(name, destinationName, originName, destinationPoint, originPoint, aircraftImage, 32 + (int)(10 * Math.random()), airspaceWaypoints, difficulty);
	}
	
	@Override
	public void playSound(audio.Sound sound) {
		sound.stop();
		sound.play();
	}
	
	@Override
	/**
	 * cleanly exit by stopping the scene's music
	 */
	public void close() {
		music.stop();
	}

	public static double getTime() {
		return timeElapsed;
	}
}
