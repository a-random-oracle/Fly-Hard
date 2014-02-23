package cls;

import java.io.File;

import scn.Demo;

import lib.jog.graphics;
import lib.jog.input;
import lib.jog.input.EventHandler;
import lib.jog.window;

public class Airport extends Waypoint implements EventHandler {
	// Put the airport in the middle of the airspace
	private static double xLocation = window.width()/2;
	private static double yLocation = window.height()/2;		

	// All location values are absolute and based on the current version of the airport image.
	private static double xArrivalsLocation = xLocation + 90;
	private static double yArrivalsLocation = yLocation + 83;
	private static double arrivalsWidth = 105;
	private static double arrivalsHeight = 52;
	
	private static double xDeparturesLocation = xLocation + 2;
	private static double yDeparturesLocation = yLocation + 50;
	private static double departuresWidth = 50;
	private static double departuresHeight = 36;
	
	public boolean isActive = false; // True if there is an aircraft Landing/Taking off
	private boolean isArrivalsClicked = false;
	private boolean isDeparturesClicked = false;
		
	private graphics.Image airport;
	
	public java.util.ArrayList<Aircraft> aircraftWaitingToLand = new java.util.ArrayList<Aircraft>();
	/**
	 * Time entered is directly related to the aircraft hangar and stores the time each aircraft entered the hangar
	 * this is used to determine score multiplier decrease if aircraft is in the hangar for too long
	 */
	public java.util.ArrayList<Aircraft> aircraftHangar = new java.util.ArrayList<Aircraft>();
	public java.util.ArrayList<Double> timeEntered = new java.util.ArrayList<Double>();
	private int hangarSize = 3;
	
	public Airport(String name) {
		super(xLocation, yLocation, true, name);
	}
	
	public void loadImage() {
		airport = graphics.newImage("gfx" + File.separator + "Airport.png");
	}
	
	@Override
	public void draw() { 
		// Draw the airport image
		graphics.draw(airport, xLocation-airport.width()/2, yLocation-airport.height()/2);
		
		int green_fine = 128;
		int green_danger = 0;
		int red_fine = 0;
		int red_danger = 128;
		
		// Draw the hangar button if plane is waiting (departing flights)
		if (aircraftHangar.size() > 0) {
			// Colour fades from green (fine) to red (danger) over 5 seconds as plane is waiting
			int time_waiting = (int)(Demo.getTime() - timeEntered.get(0));
			// Assume it hasn't been waiting
			int green_now = green_fine; 
			int red_now = red_fine;
			if (time_waiting > 0) { // Prevent division by 0
				if (time_waiting >= 5) { // Cap at 5 seconds
					green_now = green_danger;
					red_now = red_danger;
				} else {
					// Colour between fine and danger, scaled by time_waiting
					green_now = green_fine - (int)(Math.abs(green_fine-green_danger) * (time_waiting/5.0)); 
					red_now = (int)(Math.abs(red_fine-red_danger) * (time_waiting/5.0));
				}
			}

			// Draw border, draw as filled if clicked
			graphics.setColour(red_now, green_now, 0, 256);
			graphics.rectangle(isDeparturesClicked, xDeparturesLocation-airport.width()/2, yDeparturesLocation-airport.height()/2, departuresWidth, departuresHeight);

			// Draw box
			graphics.setColour(red_now, green_now, 0, 64);
			graphics.rectangle(true, xDeparturesLocation-airport.width()/2 + 1, yDeparturesLocation-airport.height()/2 + 1, departuresWidth - 2, departuresHeight - 2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftHangar.size()), xDeparturesLocation-airport.width()/2 + 23, yDeparturesLocation-airport.height()/2 + 15);
		}
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraftWaitingToLand.size() > 0) {
			// Draw border, draw as filled if clicked
			graphics.rectangle(isArrivalsClicked, xArrivalsLocation-airport.width()/2, yArrivalsLocation-airport.height()/2, arrivalsWidth, arrivalsHeight);
			graphics.setColour(128, 128, 0, 64);			
			// Draw box
			graphics.rectangle(true, xArrivalsLocation-airport.width()/2 + 1, yArrivalsLocation-airport.height()/2 + 1, arrivalsWidth -2, arrivalsHeight -2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraftWaitingToLand.size()), xArrivalsLocation-airport.width()/2 + 50, yArrivalsLocation-airport.height()/2 + 26);
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
		return isWithinRect((int)position.getX(), (int)position.getY(),(int)(xArrivalsLocation-airport.width()/2) + Demo.airspace_view_offset_x, (int)(yArrivalsLocation-airport.height()/2) + Demo.airspace_view_offset_y, (int)arrivalsWidth, (int)arrivalsHeight);
	}
	
	// Used for calculating if an aircraft is within the airspace for landing - offset should not be applied
	public boolean isWithinArrivals(Vector position, boolean apply_offset) {
		return (apply_offset ? isWithinArrivals(position) : isWithinRect((int)position.getX(), (int)position.getY(),(int)(xArrivalsLocation-airport.width()/2), (int)(yArrivalsLocation-airport.height()/2), (int)arrivalsWidth, (int)arrivalsHeight));
	}
	
	/**
	 * Departures is the portion of the airport image which is used to issue the take off command
	 * @param position is the point to be tested
	 * @return true if point is within the rectangle that defines the departures portion of the airport
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect((int)position.getX(), (int)position.getY(), (int)(xDeparturesLocation-airport.width()/2) + Demo.airspace_view_offset_x, (int)(yDeparturesLocation-airport.height()/2) + Demo.airspace_view_offset_y, (int)departuresWidth, (int)departuresHeight);
	}
	
	public boolean isWithinRect(int test_x, int test_y, int x, int y, int width, int height) {
		return x <= test_x && test_x <= x + width && y <= test_y && test_y <= y + height;
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
			if (a.current_target.equals(this.getLocation())) {
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
