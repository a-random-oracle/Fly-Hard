package cls;

import java.io.File;
import java.util.ArrayList;

import btc.Main;

import scn.Demo;

import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.input.EventHandler;

public class Airport extends Waypoint implements EventHandler {
	// Put the airport in the middle of the airspace
	private static double xLocation;
	private static double yLocation;		

	// All location values are absolute and based on the current version of the airport image.
	private static double xArrivalsLocation;
	private static double yArrivalsLocation;
	private static double arrivalsWidth;
	private static double arrivalsHeight;
	
	private static double xDeparturesLocation;
	private static double yDeparturesLocation;
	private static double departuresWidth;
	private static double departuresHeight;
	
	public boolean isActive = false; // True if there is an aircraft Landing/Taking off
	private boolean isArrivalsClicked = false;
	private boolean isDeparturesClicked = false;
		
	private Image airport;
	
	public ArrayList<Aircraft> aircraftWaitingToLand = new ArrayList<Aircraft>();
	
	/**
	 * Time entered is directly related to the aircraft hangar and stores the time each aircraft entered the hangar
	 * this is used to determine score multiplier decrease if aircraft is in the hangar for too long
	 */
	public ArrayList<Aircraft> aircraftHangar = new ArrayList<Aircraft>();
	public ArrayList<Double> timeEntered = new ArrayList<Double>();
	private int hangarSize = 3;
	
	public Airport(String name, double x, double y) {
		super(x, y, true, name);
		
		xLocation = x * Main.getScale();
		yLocation = y * Main.getScale();
		
		xArrivalsLocation = (x + 90) * Main.getScale();
		yArrivalsLocation = (y + 83) * Main.getScale();
		arrivalsWidth = 105 * Main.getScale();
		arrivalsHeight = 52 * Main.getScale();
		
		xDeparturesLocation = (x + 2) * Main.getScale();
		yDeparturesLocation = (y + 50) * Main.getScale();
		departuresWidth = 50 * Main.getScale();
		departuresHeight = 36 * Main.getScale();
	}
	
	public void loadImage() {
		airport = graphics.newImage("gfx" + File.separator + "Airport.png");
	}
	
	@Override
	public void draw() { 
		// Draw the airport image
		graphics.draw(airport, xLocation-airport.width()/2, yLocation-airport.height()/2);
		
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
			graphics.rectangle(isDeparturesClicked, xDeparturesLocation-airport.width()/2,
					yDeparturesLocation-airport.height()/2, departuresWidth, departuresHeight);

			// Draw box
			graphics.setColour(redNow, greenNow, 0, 64);
			graphics.rectangle(true, xDeparturesLocation-airport.width()/2 + 1,
					yDeparturesLocation-airport.height()/2 + 1, departuresWidth - 2,
					departuresHeight - 2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftHangar.size()),
					xDeparturesLocation-airport.width()/2 + 23,
					yDeparturesLocation-airport.height()/2 + 15);
		}
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraftWaitingToLand.size() > 0) {
			// Draw border, draw as filled if clicked
			graphics.rectangle(isArrivalsClicked, xArrivalsLocation-airport.width()/2,
					yArrivalsLocation-airport.height()/2, arrivalsWidth, arrivalsHeight);
			graphics.setColour(128, 128, 0, 64);			
			// Draw box
			graphics.rectangle(true, xArrivalsLocation-airport.width()/2 + 1,
					yArrivalsLocation-airport.height()/2 + 1, arrivalsWidth -2,
					arrivalsHeight -2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftWaitingToLand.size()),
					xArrivalsLocation-airport.width()/2 + 50,
					yArrivalsLocation-airport.height()/2 + 26);
		}		
	}
	
	public double getLongestTimeInHangar(double currentTime) {
		return aircraftHangar.isEmpty() ? 0 : currentTime-timeEntered.get(0);
	}
	
	/**
	 *  Arrivals is the portion of the airport image which is used to issue the land command
	 * @param position is the point to be tested
	 * @return true if point is within the rectangle that defines the arrivals portion of the airport
	 */
	public boolean isWithinArrivals(Vector position) {
		return isWithinRect((int)position.getX(), (int)position.getY(),
				(int)(xArrivalsLocation-airport.width()/2) + Demo.airspaceViewOffsetX,
				(int)(yArrivalsLocation-airport.height()/2) + Demo.airspaceViewOffsetY,
				(int)arrivalsWidth, (int)arrivalsHeight);
	}
	
	// Used for calculating if an aircraft is within the airspace for landing - offset should not be applied
	public boolean isWithinArrivals(Vector position, boolean applyOffset) {
		return (applyOffset ? isWithinArrivals(position) : isWithinRect((int)position.getX(),
				(int)position.getY(),(int)(xArrivalsLocation-airport.width()/2),
				(int)(yArrivalsLocation-airport.height()/2), (int)arrivalsWidth, (int)arrivalsHeight));
	}
	
	/**
	 * Departures is the portion of the airport image which is used to issue the take off command
	 * @param position is the point to be tested
	 * @return true if point is within the rectangle that defines the departures portion of the airport
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect((int)position.getX(), (int)position.getY(),
				(int)(xDeparturesLocation-airport.width()/2) + Demo.airspaceViewOffsetX,
				(int)(yDeparturesLocation-airport.height()/2) + Demo.airspaceViewOffsetY,
				(int)departuresWidth, (int)departuresHeight);
	}
	
	public boolean isWithinRect(int testX, int testY, int x, int y, int width, int height) {
		return x <= testX && testX <= x + width && y <= testY && testY <= y + height;
	}
	
	/**
	 * Adds aircraft to the back of the hangar and records the time in the timeEntered list
	 * will only add the aircraft if the current size is less than the maximum denoted by hangarSize
	 * @param aircraft
	 */
	public void addToHangar(Aircraft aircraft) {
		if (aircraftHangar.size() < hangarSize) {
			aircraftHangar.add(aircraft);
			timeEntered.add(Demo.getTime());
		}
	}
	
	public void signalTakeOff() {
		if (!aircraftHangar.isEmpty()) {
			Aircraft aircraft = aircraftHangar.remove(0);
			timeEntered.remove(0);
			aircraft.takeOff();
		}	
	}
	  
	/** 
	 * Decides whether to draw the radius around the airport by checking if any aircraft which are landing are close
	 * @param demo For getting aircraft list
	 */
	public void update(Demo demo) {
		aircraftWaitingToLand.clear();
		for (Aircraft a : demo.aircraftList()) {
			if (a.currentTarget.equals(this.getLocation())) {
				aircraftWaitingToLand.add(a);
			}
		}
	}
	
	public int getHangarSize() {
		return hangarSize;
	}

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


	@Override
	public void mouseReleased(int key, int x, int y) {
		isArrivalsClicked = false;
		isDeparturesClicked = false;
	}

	@Override
	public void keyPressed(int key) {
				
	}

	@Override
	public void keyReleased(int key) {
		
	}
	
	// Used in testing avoiding the need to have a demo instance
	@Deprecated
	public void signalTakeOffTesting() {
		if (aircraftHangar.size() > 0) {
			aircraftHangar.remove(0);
			timeEntered.remove(0);
		}	
	}

	public void clear() {
		aircraftHangar.clear();
		timeEntered.clear();
		isActive = false;
	}
}
