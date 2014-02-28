package cls;

import java.util.ArrayList;

import btc.Main;

import scn.Demo;

import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.input.EventHandler;

public class Airport extends Waypoint implements EventHandler {
	/** The airport's x position (measured to the top-left of the image) */
	private static double xLocation;
	
	/** The airport's y position (measured to the top-left of the image) */
	private static double yLocation;		

	/** The distance between the left edge of the airport image, and the arrivals area */
	private final static double relativeArrivalsX = 91;
	
	/** The distance between the top edge of the airport image, and the arrivals area */
	private final static double relativeArrivalsY = 36;
	
	/** The absolute distance from the map edge to the left of the arrivals area */
	private static double arrivalsX;
	
	/** The absolute distance from the map edge to the top of the arrivals area */
	private static double arrivalsY;
	
	/** The width of the arrivals area */
	private static double arrivalsWidth = 102;
	
	/** The height of the arrivals area */
	private static double arrivalsHeight = 53;
	
	/** The distance between the left edge of the airport image, and the departures area */
	private final static double relativeDeparturesX = 2;
	
	/** The distance between the left edge of the airport image, and the departures area */
	private final static double relativeDeparturesY = 3;
	
	/** The absolute distance from the map edge to the left of the departures area */
	private static double departuresX;
	
	/** The absolute distance from the map edge to the top of the departures area */
	private static double departuresY;
	
	/** The width of the departures area */
	private static double departuresWidth = 52;
	
	/** The height of the departures area */
	private static double departuresHeight = 37;
	
	/** Whether the airport currently in use - i.e. whether an aircraft is either
	 * arriving or departing */
	public boolean isActive = false;
	
	/** Whether the arrivals area has been clicked */
	private boolean isArrivalsClicked = false;
	
	/** Whether the departures area has been clicked */
	private boolean isDeparturesClicked = false;
	
	/** The image used to represent the airport */
	private Image image;
	
	/** The scaling factor to apply to cause the airport to 'fit in' with the map size
	 * this is taken to be the lower (and hence smaller) of the height and width scales */
	private static double scale = Math.min(Main.getXScale(), Main.getYScale());
	
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
	 * @param name the airport's name
	 * @param x the x position to display the top-left corner of the airport at
	 * @param y the y position to display the top-left corner of the airport at
	 * @param image the image to use for the airport
	 */
	public Airport(String name, double x, double y, Image image) {
		super(((x + relativeArrivalsX + (arrivalsWidth/2))),
				((y + relativeArrivalsY + (arrivalsHeight/2))), true, name);
		
		// Set the airport image
		this.image = image;
		
		// Scale the x and y co-ords by the scaling factor
		xLocation = x * scale;
		yLocation = y * scale;
		
		// Scale the arrivals rectangle by the scaling factor
		arrivalsX = (x + relativeArrivalsX) * scale;
		arrivalsY = (y + relativeArrivalsY) * scale;
		arrivalsWidth *= scale;
		arrivalsHeight *= scale;
		
		// Scale the departures rectangle by the scaling factor
		departuresX = (x + relativeDeparturesX) * scale;
		departuresY = (y + relativeDeparturesY) * scale;
		departuresWidth *= scale;
		departuresHeight *= scale;
	}
	  
	/** 
	 * Decides whether to draw the radius around the airport by checking if
	 * any aircraft which are landing are close
	 * @param demo the class containing the list of aircraft to check
	 */
	public void update(ArrayList<Aircraft> aircraft) {
		aircraftWaitingToLand.clear();
		for (Aircraft a : aircraft) {
			if (a.currentTarget.equals(this.getLocation())) {
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
		graphics.drawScaled(image, xLocation, yLocation, scale);
		
		int greenFine = 128;
		int greenDanger = 0;
		int redFine = 0;
		int redDanger = 128;
		
		// Draw the hangar button if plane is waiting (departing flights)
		if (aircraftHangar.size() > 0) {
			// Colour fades from green (fine) to red (danger) over 5 seconds as plane is waiting
			int timeWaiting = (int)(Demo.getTime() - timeEntered.get(0));
			// Assume it hasn't been waiting
			int greenNow = greenFine; 
			int redNow = redFine;
			if (timeWaiting > 0) { // Prevent division by 0
				if (timeWaiting >= 5) { // Cap at 5 seconds
					greenNow = greenDanger;
					redNow = redDanger;
				} else {
					// Colour between fine and danger, scaled by timeWaiting
					greenNow = greenFine - (int)(Math.abs(greenFine-greenDanger) * (timeWaiting/5.0)); 
					redNow = (int)(Math.abs(redFine-redDanger) * (timeWaiting/5.0));
				}
			}

			// Draw border, draw as filled if clicked
			graphics.setColour(redNow, greenNow, 0, 256);
			graphics.rectangle(isDeparturesClicked, departuresX,
					departuresY, departuresWidth, departuresHeight);

			// Draw box
			graphics.setColour(redNow, greenNow, 0, 64);
			graphics.rectangle(true, departuresX + 1, departuresY + 1,
					departuresWidth - 1, departuresHeight - 1);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftHangar.size()),
					departuresX + 23, departuresY + 15);
		}
		
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraftWaitingToLand.size() > 0) {
			graphics.rectangle(isArrivalsClicked, arrivalsX, arrivalsY,
					arrivalsWidth, arrivalsHeight);
			
			// Draw box
			graphics.setColour(128, 128, 0, 64);
			graphics.rectangle(true, arrivalsX + 1, arrivalsY + 1,
					arrivalsWidth - 1, arrivalsHeight - 1);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftWaitingToLand.size()),
					arrivalsX + 50,
					arrivalsY + 26);
		}
	}

	/**
	 * Handles mouse click events.
	 * @param key the key which was pressed
	 * @param x the x position of the mouse
	 * @param y the y position of the mouse
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
	 * @param key the key which was pressed
	 * @param x the x position of the mouse
	 * @param y the y position of the mouse
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		isArrivalsClicked = false;
		isDeparturesClicked = false;
	}

	@Override
	public void keyPressed(int key) {}

	@Override
	public void keyReleased(int key) {}
	
	/**
	 * Checks whether the specified testX and testY coordinates are within a region.
	 * <p>
	 * The region to check starts at the point (x, y), with width 'width' and height 'height'.
	 * </p>
	 * @param testX the x position of the point to test
	 * @param testY the y position of the point to test
	 * @param x the x co-ord of the top-left of the region
	 * @param y the y co-ord of the top-left of the region
	 * @param width the width of the region
	 * @param height the height of the region
	 * @return <code>true</code> if the position is within the region specified,
	 * 			<code>false</code> otherwise
	 */
	public boolean isWithinRect(int testX, int testY, int x, int y, int width, int height) {
		return x <= testX && testX <= x + width && y <= testY && testY <= y + height;
	}
	
	/**
	 * Calculates whether a position is within the arrivals area.
	 * @param position the position to check
	 * @return <code>true</code> if the position is within the arrivals area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinArrivals(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)(arrivalsX) + Demo.airspaceViewOffsetX,
				(int)(arrivalsY) + Demo.airspaceViewOffsetY,
				(int)arrivalsWidth,
				(int)arrivalsHeight);
	}
	
	/**
	 * Calculates whether a position is within the arrivals area, with an optional offset.
	 * @param position the position to check
	 * @param applyOffset <code>true</code> if the airspace offset should be taken
	 * 						into consideration, otherwise <code>false</code>
	 * @return <code>true</code> if the position is within the arrivals area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinArrivals(Vector position, boolean applyOffset) {
		return (applyOffset ? isWithinArrivals(position) : isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)arrivalsX,
				(int)arrivalsY,
				(int)arrivalsWidth,
				(int)arrivalsHeight));
	}
	
	/**
	 * Calculates whether a position is within the departures area.
	 * @param position the point to be tested
	 * @return <code>true</code> if the position is within the departures area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)(departuresX) + Demo.airspaceViewOffsetX,
				(int)(departuresY) + Demo.airspaceViewOffsetY,
				(int)departuresWidth,
				(int)departuresHeight);
	}
	
	/**
	 * Gets the size of the airport's hangar - i.e. the number of aircraft it can store
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
	 * @param aircraft the aircraft to add to the hangar
	 */
	public void addToHangar(Aircraft aircraft) {
		if (aircraftHangar.size() < hangarSize) {
			aircraftHangar.add(aircraft);
			timeEntered.add(Demo.getTime());
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
	
	// Used for testing, to avoid the need to have a demo instance
	@Deprecated
	public void signalTakeOffTesting() {
		if (aircraftHangar.size() > 0) {
			aircraftHangar.remove(0);
			timeEntered.remove(0);
		}	
	}
	
}
