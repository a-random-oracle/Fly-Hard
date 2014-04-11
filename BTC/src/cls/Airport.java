package cls;

import java.io.Serializable;
import java.util.ArrayList;

import btc.Main;
import scn.Game;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.input.EventHandler;

public class Airport extends Waypoint implements EventHandler, Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = -2239129660591866487L;

	/** The distance between the left edge of the airport image, and the arrivals area */
	private static final double RELATIVE_ARRIVALS_X = 91;
	
	/** The distance between the top edge of the airport image, and the arrivals area */
	private static final double RELATIVE_ARRIVALS_Y = 36;
	
	/** The relative width of the arrivals area */
	private static final double RELATIVE_ARRIVALS_WIDTH = 102;
	
	/** The relative height of the arrivals area */
	private static final double RELATIVE_ARRIVALS_HEIGHT = 53;
	
	/** The distance between the left edge of the airport image, and the departures area */
	private static final double RELATIVE_DEPARTURES_X = 2;
	
	/** The distance between the left edge of the airport image, and the departures area */
	private static final double RELATIVE_DEPARTURES_Y = 3;
	
	/** The relative width of the departures area */
	private static final double RELATIVE_DEPARTURES_WIDTH = 52;
	
	/** The relative height of the departures area */
	private static final double RELATIVE_DEPARTURES_HEIGHT = 37;

	/** The airport's position (measured to the top-left of the image) */
	private Vector location;
	
	/** The airport's arrivals area's position (measured to the top-left of the image) */
	private Vector arrivals;
	
	/** The airport's departures area's position (measured to the top-left of the image) */
	private Vector departures;
	
	/** Whether the airport currently in use - i.e. whether an aircraft is either
	 * arriving or departing */
	public boolean isActive = false;
	
	/** Whether the arrivals area has been clicked */
	private boolean isArrivalsClicked = false;
	
	/** Whether the departures area has been clicked */
	private boolean isDeparturesClicked = false;
	
	/** A list of aircraft waiting to land at the airport */
	public ArrayList<Aircraft> aircraftWaitingToLand = new ArrayList<Aircraft>();
	
	/** A list of aircraft at the airport */
	public ArrayList<Aircraft> aircraftHangar = new ArrayList<Aircraft>();
	
	/** Time entered is directly related to the aircraft hangar and stores the time each aircraft entered the
	 * hangar this is used to determine score multiplier decrease if aircraft is in the hangar for too long */
	public ArrayList<Double> timeEntered = new ArrayList<Double>();
	
	/** The maximum number of aircraft the airport can hold */
	private int hangarSize = 3;
	
	/**
	 * Constructs an airport.
	 * <p>
	 * Sets up the airport image, and scales the location attributes.
	 * </p>
	 * @param name
	 * 			the airport's name
	 * @param x
	 * 			the position at which the centre of the airport
	 * 			should be located
	 * @param y
	 * 			the position at which the centre of the airport
	 * 			should be located
	 */
	public Airport(String name, double x, double y) {
		super(x, y, true, name, true);
		
		double distToX = RELATIVE_ARRIVALS_X + (RELATIVE_ARRIVALS_WIDTH / 2);
		double distToY = RELATIVE_ARRIVALS_Y + (RELATIVE_ARRIVALS_HEIGHT / 2);
		
		location = new Vector(getLocation().getX() - (distToX * getMinScale()),
				getLocation().getY() - (distToY * getMinScale()), 0);
		
		// Scale the arrivals rectangle by the scaling factor
		arrivals = new Vector(location.getX() + (RELATIVE_ARRIVALS_X * getMinScale()),
				location.getY() + (RELATIVE_ARRIVALS_Y * getMinScale()), 0);
		
		// Scale the departures rectangle by the scaling factor
		departures = new Vector(location.getX() + (RELATIVE_DEPARTURES_X * getMinScale()),
				location.getY() + (RELATIVE_DEPARTURES_Y * getMinScale()), 0);
	}
	  
	/** 
	 * Updates the aircraft at the airport.
	 * @param aircraft
	 * 			the list of aircraft to check
	 */
	public void update(ArrayList<Aircraft> aircraft) {
		aircraftWaitingToLand.clear();
		for (Aircraft a : aircraft) {
			if (a.currentTarget.equals(getLocation())) {
				aircraftWaitingToLand.add(a);
			}
		}
	}
	
	/**
	 * Draws the airport.
	 * <p>
	 * This includes the airport image, and the rectangles displayed when
	 * arrivals or departures is active.
	 * </p>
	 */
	@Override
	public void draw() {
		// Draw the airport image, applying the scale factor
		graphics.drawScaled(Game.airportImage,
				getLocationX(), getLocationY(), getMinScale());
		
		int greenFine = 128;
		int greenDanger = 0;
		int redFine = 0;
		int redDanger = 128;
		
		// Draw the hangar button if plane is waiting (departing flights)
		if (aircraftHangar.size() > 0) {
			// Colour fades from green (fine) to red (danger)
			// over 5 seconds as plane is waiting
			int timeWaiting = (int)(Game.getInstance().getTime()
					- timeEntered.get(0));
			
			// Assume it hasn't been waiting
			int greenNow = greenFine; 
			int redNow = redFine;
			
			if (timeWaiting > 0) { // Prevent division by 0
				if (timeWaiting >= 5) { // Cap at 5 seconds
					greenNow = greenDanger;
					redNow = redDanger;
				} else {
					// Colour between fine and danger, scaled by timeWaiting
					greenNow = greenFine - (int)(Math.abs(greenFine-greenDanger)
							* (timeWaiting/5.0)); 
					redNow = (int)(Math.abs(redFine-redDanger)
							* (timeWaiting/5.0));
				}
			}

			// Draw border, draw as filled if clicked
			graphics.setColour(redNow, greenNow, 0, 256);
			graphics.rectangle(isDeparturesClicked,
					getDeparturesX(), getDeparturesY(),
					getDeparturesWidth(), getDeparturesHeight());

			// Draw box
			graphics.setColour(redNow, greenNow, 0, 64);
			graphics.rectangle(true, getDeparturesX() + 1, getDeparturesY() + 1,
					getDeparturesWidth() - 1, getDeparturesHeight() - 1);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftHangar.size()),
					getDeparturesX() + (getDeparturesWidth() / 2),
					getDeparturesY() + (getDeparturesHeight() / 2));
		}
		
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraftWaitingToLand.size() > 0) {
			graphics.rectangle(isArrivalsClicked,
					getArrivalsX(), getArrivalsY(),
					getArrivalsWidth(), getArrivalsHeight());
			
			// Draw box
			graphics.setColour(128, 128, 0, 64);
			graphics.rectangle(true, getArrivalsX() + 1, getArrivalsY() + 1,
					getArrivalsWidth() - 1, getArrivalsHeight() - 1);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftWaitingToLand.size()),
					getArrivalsX() + (getArrivalsWidth() / 2),
					getArrivalsY() + (getArrivalsHeight() / 2));
		}
	}

	/**
	 * Handles mouse click events.
	 * @param key
	 * 			the key which was pressed
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		if (key == input.MOUSE_LEFT) { 
			if (isWithinArrivals(new Vector(x, y, 0))) {
				isArrivalsClicked = true;
			} else if (isWithinDepartures(new Vector(x, y, 0))) {
				isDeparturesClicked = true;
			}
		}
	}

	/**
	 * Handles mouse release events.
	 * @param key
	 * 			the key which was pressed
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		isArrivalsClicked = false;
		isDeparturesClicked = false;
	}

	/**
	 * Handles key press events.
	 */
	@Override
	public void keyPressed(int key) {}

	/**
	 * Handles key release events.
	 */
	@Override
	public void keyReleased(int key) {}
	
	/**
	 * Determines whether the arrivals area has just been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return <code>true</code> if the arrivals area has been clicked,
	 * 			<code>false</code> otherwise
	 */
	public boolean isArrivalsClicked(int x, int y) {
		return isWithinArrivals(new Vector(x, y, 0)) && !isActive;
	}
	
	/**
	 * Determines whether the departures area has just been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @return <code>true</code> if the departures area has been clicked,
	 * 			<code>false</code> otherwise
	 */
	public boolean isDeparturesClicked(int x, int y) {
		return isWithinDepartures(new Vector(x, y, 0)) && !isActive;
	}
	
	/**
	 * Checks whether the specified testX and testY coordinates are
	 * within a region.
	 * <p>
	 * The region to check starts at the point (x, y), with width
	 * 'width' and height 'height'.
	 * </p>
	 * @param testX
	 * 			the x position of the point to test
	 * @param testY
	 * 			the y position of the point to test
	 * @param x
	 * 			the x co-ord of the top-left of the region
	 * @param y
	 * 			the y co-ord of the top-left of the region
	 * @param width
	 * 			the width of the region
	 * @param height
	 * 			the height of the region
	 * @return <code>true</code> if the position is within the region specified,
	 * 			<code>false</code> otherwise
	 */
	public boolean isWithinRect(int testX, int testY, int x, int y, int width, int height) {
		return x <= testX && testX <= x + width && y <= testY && testY <= y + height;
	}
	
	/**
	 * Calculates whether a position is within the arrivals area.
	 * @param position
	 * 			the position to check
	 * @return <code>true</code> if the position is within the arrivals area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinArrivals(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)getArrivalsX() + Game.X_OFFSET,
				(int)getArrivalsY() + Game.Y_OFFSET,
				(int)getArrivalsWidth(),
				(int)getArrivalsHeight());
	}
	
	/**
	 * Calculates whether a position is within the arrivals area, with an optional offset.
	 * @param position
	 * 			the position to check
	 * @param applyOffset
	 * 			<code>true</code> if the airspace offset should be taken
	 * 			into consideration, otherwise <code>false</code>
	 * @return <code>true</code> if the position is within the arrivals area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinArrivals(Vector position, boolean applyOffset) {
		return (applyOffset ? isWithinArrivals(position) : isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)getArrivalsX(),
				(int)getArrivalsY(),
				(int)getArrivalsWidth(),
				(int)getArrivalsHeight()));
	}
	
	/**
	 * Calculates whether a position is within the departures area.
	 * @param position
	 * 			the point to be tested
	 * @return <code>true</code> if the position is within the departures area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)getDeparturesX() + Game.X_OFFSET,
				(int)getDeparturesY() + Game.Y_OFFSET,
				(int)getDeparturesWidth(),
				(int)getDeparturesHeight());
	}
	
	/**
	 * Gets the size of the airport's hangar.
	 * <p>
	 * i.e. the number of aircraft it can store.
	 * </p>
	 * @return the size of the airport's hangar
	 */
	public int getHangarSize() {
		return hangarSize;
	}
	
	/**
	 * Adds an aircraft to the back of the hangar.
	 * <p>
	 * Also records the time in the timeEntered list.
	 * </p>
	 * <p>
	 * The aircraft will only be added if the current size is less than the maximum
	 * specified by hangarSize.
	 * </p>
	 * @param aircraft
	 * 			the aircraft to add to the hangar
	 */
	public void addToHangar(Aircraft aircraft) {
		if (aircraftHangar.size() < hangarSize) {
			aircraftHangar.add(aircraft);
			timeEntered.add(Game.getInstance().getTime());
		}
	}
	
	/**
	 * Causes the next aircraft in the hangar to take off.
	 */
	public void signalTakeOff() {
		if (!aircraftHangar.isEmpty()) {
			Aircraft aircraft = aircraftHangar.remove(0);
			timeEntered.remove(0);
			aircraft.takeOff();
		}	
	}
	
	/**
	 * Calculates the longest amount of time any aircraft has been waiting.
	 * @param currentTime
	 * 			the current game time
	 * @return the longest amount of time an aircraft has been waiting for
	 */
	public double getLongestTimeInHangar(double currentTime) {
		return aircraftHangar.isEmpty() ? 0 : currentTime-timeEntered.get(0);
	}

	/**
	 * Clears the airport.
	 * <p>
	 * Clears the hangar, and resets the time and activation attributes.
	 * </p>
	 */
	public void clear() {
		aircraftHangar.clear();
		timeEntered.clear();
		isActive = false;
	}
	
	/**
	 * Gets the centre of the departures area.
	 * @return a waypoint at the centre of the departures area
	 */
	public Waypoint getDeparturesCentre() {
		return new Waypoint(getDeparturesX() + (getDeparturesWidth() / 2),
				getDeparturesY() + (getDeparturesHeight() / 2), true, false);
	}
	
	/**
	 * Gets the minimum of the x and y scales, considering x and y offsets.
	 * @return the minimum scale
	 */
	public static double getMinScale() {
		double xScale = (double)(window.width() - (2 * Game.X_OFFSET))
				/ (double)Main.TARGET_WIDTH;
		double yScale = (double)(window.height() - (2 * Game.Y_OFFSET))
				/ (double)Main.TARGET_HEIGHT;
		
		return Math.min(xScale, yScale);
	}
	
	private double getLocationX() {
		return location.getX() * getMinScale();
	}
	
	private double getLocationY() {
		return location.getY() * getMinScale();
	}
	
	private double getArrivalsX() {
		return arrivals.getX() * getMinScale();
	}
	
	private double getArrivalsY() {
		return arrivals.getY() * getMinScale();
	}
	
	private double getArrivalsWidth() {
		return RELATIVE_ARRIVALS_WIDTH * getMinScale();
	}
	
	private double getArrivalsHeight() {
		return RELATIVE_ARRIVALS_HEIGHT * getMinScale();
	}
	
	private double getDeparturesX() {
		return departures.getX() * getMinScale();
	}
	
	private double getDeparturesY() {
		return departures.getY() * getMinScale();
	}
	
	private double getDeparturesWidth() {
		return RELATIVE_DEPARTURES_WIDTH * getMinScale();
	}
	
	private double getDeparturesHeight() {
		return RELATIVE_DEPARTURES_HEIGHT * getMinScale();
	}
	
	/**
	 * Instructs an aircraft to take off.
	 * <p>
	 * Used for testing, to avoid the need to have a demo instance.
	 * </p>
	 */
	@Deprecated
	public void signalTakeOffTesting() {
		if (aircraftHangar.size() > 0) {
			aircraftHangar.remove(0);
			timeEntered.remove(0);
		}	
	}
	
}
