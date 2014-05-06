package cls;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import cls.Powerup.PowerupEffect;

import btc.Main;
import scn.Game;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.graphics.Image;
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
	
	/** THe alpha of the fog effect rendered on an airport */
	private double fogRender = 0;
	
	private static final Image FOG =
			graphics.newImage("gfx" + File.separator + "apt"
					+ File.separator + "fog.png");
	
	/**
	 * Constructs an airport.
	 * <p>
	 * Sets up the airport image, and scales the location attributes.
	 * </p>
	 * @param name - the airport's name
	 * @param x - the position at which the centre of the airport
	 * 				should be located
	 * @param y - the position at which the centre of the airport
	 * 				should be located
	 */
	public Airport(String name, double x, double y) {
		super(x, y, true, name, true);
	}
	
	/**
	 * Constructs an airport.
	 * @param airport - the airport to copy
	 * @param waypoint - the superclass object
	 */
	@SuppressWarnings("unchecked")
	private Airport(Airport airport, Waypoint waypoint) {
		super(waypoint);
		isActive = airport.isActive;
		isArrivalsClicked = airport.isArrivalsClicked;
		isDeparturesClicked = airport.isDeparturesClicked;
		aircraftWaitingToLand = (airport.aircraftWaitingToLand != null)
				? (ArrayList<Aircraft>)
						airport.aircraftWaitingToLand.clone() : null;
		aircraftHangar = (airport.aircraftHangar != null)
				? (ArrayList<Aircraft>)
						airport.aircraftHangar.clone() : null;
		timeEntered = (airport.timeEntered != null)
				? (ArrayList<Double>)
						airport.timeEntered.clone() : null;
		hangarSize = airport.hangarSize;
	}
	
	
	/** 
	 * Updates the aircraft at the airport.
	 * @param aircraft - the list of aircraft to check
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
			int timeWaiting = (int)(Game.getInstance().getTime() - timeEntered.get(0));
			
			for(int i = 0; i < aircraftHangar.size(); i++ ) {
				int planeTimeWaiting = (int)(Game.getInstance().getTime() - timeEntered.get(0));
				aircraftHangar.get(i).setTimeWaiting(planeTimeWaiting);
			}
			
			// Assume it hasn't been waiting
			int greenNow = greenFine; 
			int redNow = redFine;
			
			if (timeWaiting > 0) { // Prevent division by 0
				if (timeWaiting >= 5) { // Cap at 5 seconds
					greenNow = greenDanger;
					redNow = redDanger;
					
					//Decrement the score of an aircraft that has stayed in the airport too long
					//Only decrement score once
					for(int i = 0; i < aircraftHangar.size(); i++ ) {
						Aircraft currentAircraft = aircraftHangar.get(i);
						if(currentAircraft.isAirportPenaltyApplied() == false && currentAircraft.getTimeWaiting() >= 5) {
							currentAircraft.decrementScoreLarge();
							currentAircraft.setAirportPenaltyApplied(true);
						}
					}
					
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
		
		boolean dec = true;
		for (Powerup pp : Game.getInstance().getPlayerFromAirport(this).getPowerups()) {
			if (pp.isActive() && pp.getEffect() == PowerupEffect.FOG) {
				fogRender = fogRender + ( 1 - fogRender ) * 0.1;
				dec = false;
				break;
			}
		}
		if (dec) fogRender -= fogRender * 0.04;
		graphics.setColour( 255, 255, 255, fogRender * 255 );
		if (fogRender>0.001) graphics.drawScaled( FOG, getLocationX(), getLocationY()-getMinScale()*FOG.height()/3, getMinScale());
	}

	/**
	 * Handles mouse click events.
	 * @param key - the key which was pressed
	 * @param x - the x position of the mouse
	 * @param y - the y position of the mouse
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
	 * @param key - the key which was pressed
	 * @param x - the x position of the mouse
	 * @param y - the y position of the mouse
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
	 * @param x - the x position of the mouse
	 * @param y - the y position of the mouse
	 * @return <code>true</code> if the arrivals area has been clicked,
	 * 			<code>false</code> otherwise
	 */
	public boolean isArrivalsClicked(int x, int y) {
		return isWithinArrivals(new Vector(x, y, 0)) && !isActive;
	}
	
	/**
	 * Determines whether the departures area has just been clicked.
	 * @param x - the x position of the mouse
	 * @param y - the y position of the mouse
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
	 * @param testX - the x position of the point to test
	 * @param testY - the y position of the point to test
	 * @param x - the x co-ord of the top-left of the region
	 * @param y - the y co-ord of the top-left of the region
	 * @param width - the width of the region
	 * @param height - the height of the region
	 * @return <code>true</code> if the position is within the region specified,
	 * 			<code>false</code> otherwise
	 */
	public boolean isWithinRect(int testX, int testY,
			int x, int y, int width, int height) {
		return x <= testX
				&& testX <= x + width
				&& y <= testY
				&& testY <= y + height;
	}
	
	/**
	 * Calculates whether a position is within the arrivals area.
	 * @param position - the position to check
	 * @return <code>true</code> if the position is within the arrivals area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinArrivals(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)getArrivalsX() + Game.getXOffset(),
				(int)getArrivalsY() + Game.getYOffset(),
				(int)getArrivalsWidth(),
				(int)getArrivalsHeight());
	}
	
	/**
	 * Calculates whether a position is within the arrivals area, with an optional offset.
	 * @param position - the position to check
	 * @param applyOffset - <code>true</code> if the airspace offset should be taken
	 * 							into consideration, otherwise <code>false</code>
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
	 * @param position - the point to be tested
	 * @return <code>true</code> if the position is within the departures area,
	 * 			otherwise <code>false</code>
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect(
				(int)position.getX(),
				(int)position.getY(),
				(int)getDeparturesX() + Game.getXOffset(),
				(int)getDeparturesY() + Game.getYOffset(),
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
	 * @param aircraft - the aircraft to add to the hangar
	 */
	public void addToHangar(Aircraft aircraft) {
		if (aircraftHangar.size() < hangarSize) {
			aircraftHangar.add(aircraft);
			
			if (Game.getInstance() != null) {
				timeEntered.add(Game.getInstance().getTime());
			}
		}
	}
	
	/**
	 * Causes the next aircraft in the hangar to take off.
	 */
	public void signalTakeOff() {
		if (!aircraftHangar.isEmpty() && !isActive) {
			Aircraft aircraft = aircraftHangar.remove(0);
			timeEntered.remove(0);
			aircraft.takeOff();
			
			// Increment the player's number of planes taken off
			Player player = Game.getInstance().getPlayerFromAirport(this);
			player.setPlanesTakenOff(player.getPlanesTakenOff() + 1);
			System.out.println("planes taken off: " + player.getPlanesTakenOff());
		}	
	}
	
	/**
	 * Calculates the longest amount of time any aircraft has been waiting.
	 * @param currentTime - the current game time
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
		double xScale = (double)(window.width() - (2 * Game.getXOffset()))
				/ (double)Main.TARGET_WIDTH;
		double yScale = (double)(window.height() - (2 * Game.getYOffset()))
				/ (double)Main.TARGET_HEIGHT;
		
		return Math.min(xScale, yScale);
	}
	
	/**
	 * Gets the airport's x position.
	 * @return the airport's x position
	 */
	private double getLocationX() {
		return getLocation().getX()
				- ((RELATIVE_ARRIVALS_X + (RELATIVE_ARRIVALS_WIDTH / 2))
						* getMinScale());
	}
	
	/**
	 * Gets the airport's y position.
	 * @return the airport's y position
	 */
	private double getLocationY() {
		return getLocation().getY()
				- ((RELATIVE_ARRIVALS_Y + (RELATIVE_ARRIVALS_HEIGHT / 2))
						* getMinScale());
	}
	
	/**
	 * Gets the airport's arrivals area's x position.
	 * @return the airport's arrivals area's x position
	 */
	private double getArrivalsX() {
		return getLocationX() + (RELATIVE_ARRIVALS_X * getMinScale());
	}
	
	/**
	 * Gets the airport's arrivals area's y position.
	 * @return the airport's arrivals area's y position
	 */
	private double getArrivalsY() {
		return getLocationY() + (RELATIVE_ARRIVALS_Y * getMinScale());
	}
	
	/**
	 * Gets the airport's arrivals area's width.
	 * @return the airport's arrivals area's width
	 */
	private double getArrivalsWidth() {
		return RELATIVE_ARRIVALS_WIDTH * getMinScale();
	}
	
	/**
	 * Gets the airport's arrivals area's height.
	 * @return the airport's arrivals area's height
	 */
	private double getArrivalsHeight() {
		return RELATIVE_ARRIVALS_HEIGHT * getMinScale();
	}
	
	/**
	 * Gets the airport's arrivals area's height.
	 * @return the airport's arrivals area's height
	 */
	private double getDeparturesX() {
		return getLocationX() + (RELATIVE_DEPARTURES_X * getMinScale());
	}
	
	/**
	 * Gets the airport's departures area's y position.
	 * @return the airport's departures area's y position
	 */
	private double getDeparturesY() {
		return getLocationY() + (RELATIVE_DEPARTURES_Y * getMinScale());
	}
	
	/**
	 * Gets the airport's departures area's width.
	 * @return the airport's departures area's width
	 */
	private double getDeparturesWidth() {
		return RELATIVE_DEPARTURES_WIDTH * getMinScale();
	}
	
	/**
	 * Gets the airport's departures area's height.
	 * @return the airport's departures area's height
	 */
	private double getDeparturesHeight() {
		return RELATIVE_DEPARTURES_HEIGHT * getMinScale();
	}
	
	/**
	 * Sets the airport as active.
	 */
	public void setActive() {
		this.isActive = true;
	}
	
	/**
	 * Sets the airport as not active.
	 */
	public void setInactive() {
		this.isActive = false;
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
	
	/**
	 * Clones the airport.
	 */
	public Airport clone() {
		return new Airport(this, super.clone());
	}
	
}
