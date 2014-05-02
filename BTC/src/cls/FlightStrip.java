//    /|  /|  /|  /
//   / | / | / | /   In use by mwuk **BEWARE CHANGES**
//  /  |/  |/  |/
//
// V 0.9

package cls;

import java.io.Serializable;

import btc.Main;
import scn.Game;
import lib.jog.graphics;

public class FlightStrip implements Serializable {

	/** Serialisation ID */
	private static final long serialVersionUID = -7542014798949722639L;
	
	/** The default width */
	private static final int STANDARD_WIDTH = 160;
	
	/** The default height */
	private static final int STANDARD_HEIGHT = 60;
	
	/** The default separation */
	private static final int SEPARATION = 10;

	/** Whether the flight strip should be drawn or not */
	private boolean isVisible;

	/** The aircraft which the flight strip is linked to */
    private Aircraft aircraft;

    /** The flight strip's vertical position */
    private double positionY = 0;

    /** The flight strip's width */
    private double width;

    /** The flight strip's height */
    private double height;


    /**
     * Constructor for flight strips.
     * @param aircraft - the linked aircraft
     */
    public FlightStrip(Aircraft aircraft) {
    	this.positionY = getNextSlot();
    	this.width = STANDARD_WIDTH;
    	this.height = STANDARD_HEIGHT;
    	this.aircraft = aircraft;
    	this.isVisible = true;
    }
    
    /**
     * Constructor for flight strips.
     * @param width - the width of the strip
     * @param height - the height of the strip
     * @param aircraft - the linked aircraft
     */
    public FlightStrip(double width, double height, Aircraft aircraft) {
    	// Get the next available slot
    	for (FlightStrip fs : Game.getInstance().getPlayer().getFlightStrips()) {
    		if (fs.isVisible) {
    			this.positionY += fs.width;
    		}
    	}
    	
    	this.width = width;
    	this.height = height;
    	this.aircraft = aircraft;
    	this.isVisible = true;
    }
    
    /**
     * Constructor for flight strips.
     * @param flightStrip - the flight strip to copy
     */
    private FlightStrip(FlightStrip flightStrip) {
    	isVisible = flightStrip.isVisible;
        aircraft = (flightStrip.aircraft != null)
        		? flightStrip.aircraft.clone() : null;
        positionY = flightStrip.positionY;
        width = flightStrip.width;
        height = flightStrip.height;
    }
    
    
    /**
     * Gets the aircraft connected to the flight strip.
     * @return the aircraft connected to the flight strip
     */
    public Aircraft getAircraft() {
    	return aircraft;
    }
    
    /**
     * Gets the top of the next available slot for a flight strip.
     * @return the top of the next available slot for a flight strip
     */
    private double getNextSlot() {
    	double nextSlot = 0;
    	
    	if (Game.getInstance() != null
    			&& Game.getInstance().getPlayer() != null
    			&& Game.getInstance().getPlayer().getFlightStrips() != null) {
    		for (FlightStrip fs : Game.getInstance()
    				.getPlayer().getFlightStrips()) {
    			if (fs.isVisible) {
    				nextSlot += fs.height + SEPARATION;
    			}
    		}
    	}
    	
    	return nextSlot;
    }
    
    /**
     * Shows strip for selected aircraft (prototyping only).
     * <p>
     * This will potentially become a method to call externally
     * to trigger instantiation of a strip.
     * </p>
     */
	public void show() {
		isVisible = true;
	}

	/**
	 * Stops the flight strip from being drawn.
	 */
    public void hide() {
        isVisible = false;
    }
    
    /**
     * Updates the flight strip.
     */
    public void update(double dt) {
    	this.positionY = getNextSlot();
    }
    
    /**
     * Draws the flight strip.
     * @param xOffset - the horizontal offset from the window's left edge
     * @param yOffset - the vertical offset from the window's top edge
     */
    public void draw(double xOffset, double yOffset) {
    	if (isVisible) {
    		graphics.setFont(Main.flightstripFont);
    		drawOutline(xOffset, yOffset);
    		drawFlightNumber(xOffset, yOffset);
    		drawAirline(xOffset, yOffset);
    		drawAltitude(xOffset, yOffset);
    		drawRoute(xOffset, yOffset);
    		drawStatus(xOffset, yOffset);
    		graphics.setFont(Main.standardFont);
    	}
    }

    private void drawOutline(double xOffset, double yOffset) {
    	// TODO mouseover/click highlight (plane/strip sync)
        graphics.setColour(graphics.blue);
        graphics.rectangle(true, xOffset, yOffset + positionY, width, height);
        graphics.setColour(graphics.white);
        graphics.rectangle(true, xOffset + 2, yOffset + positionY + 2, 40, height - 4);
    }

    private void drawFlightNumber(double xOffset, double yOffset) {
    	graphics.setColour(graphics.black);
    	graphics.print(aircraft.getName().substring(0,2), (xOffset + 2), (yOffset + positionY + 2));
    	graphics.print(aircraft.getName().substring(2,5), (xOffset + 2), (yOffset + positionY + 30));
    }

    private void drawAirline(double xOffset, double yOffset) {
    	graphics.setColour(graphics.white);
        graphics.print(aircraft.getAirline(), (xOffset + 2 + 40), (yOffset + positionY + 2));

    }

    private void drawAltitude(double xOffset, double yOffset) {
//        btc.Main.display.drawString((float)(xOffset + (width/2)), (float)(yOffset + positionY + height), (new Integer(aircraft.getAltitude()).toString() + "ft"));
    	graphics.print(String.format("%,d", (int) aircraft.getPosition().getZ()) + "ft", (xOffset + (width/2)), ((yOffset + positionY + height) - 24));
    }
    
    private void drawRoute(double xOffset, double yOffset) {
    	graphics.print(aircraft.getFlightPlan().getOriginName().substring(0, 3) + " TO " + aircraft.getFlightPlan().getDestinationName().substring(0, 3), (xOffset + (width/2) - 20), (yOffset + positionY + height - 36));
    }
    
    private void drawStatus(double xOffset, double yOffset) {
    	if (aircraft.isInDanger()) {
    		graphics.setColour(graphics.red);
    		graphics.rectangle(true, (xOffset + 42), (yOffset + positionY + height - 14), 116, 12);
    		graphics.setColour(graphics.white);
            graphics.printCentred("SHIIIIIIIIT", (xOffset + 100), (yOffset + positionY + height - 14), 1, 1);

    	} else {
    		graphics.setColour(graphics.green);
    		graphics.rectangle(true, (xOffset + 42), (yOffset + positionY + height - 14), 116, 12);
    		graphics.setColour(graphics.white);
            graphics.printCentred("AWW YISS", (xOffset + 100), (yOffset + positionY + height - 14), 1, 1);
    	}
    }
    
    
    public void mousePressed(int key, int x, int y) {}
    
    public void mouseReleased(int key, int mx, int my) {}
    
    
    /**
	 * Clones the flight strip.
	 */
	public FlightStrip clone() {
		return new FlightStrip(this);
	}

}
