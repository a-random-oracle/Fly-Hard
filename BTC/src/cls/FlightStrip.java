//    /|  /|  /|  /
//   / | / | / | /   In use by mwuk **BEWARE CHANGES**
//  /  |/  |/  |/
//
// V 0.1.5

package cls;

import java.awt.Color;

import btc.Main;
import lib.jog.graphics;
import lib.jog.input.EventHandler;          // <= May not be required

public class FlightStrip implements EventHandler {

    private boolean isVisible;

    private cls.Aircraft aircraft;
    
    private cls.FlightPlan flightPlan;

    private double positionX;

    private double positionY;

    private double width;

    private double height;


    /**
     * Constructor for flight strips.
     * @param x - the x coord to draw at
     * @param y - tht y coord to draw at
     * @param w - the width of the strip
     * @param h - the height of the strip
     */
    public FlightStrip(double x, double y, double w, double h, Aircraft plane, FlightPlan plan) {
        positionX = 0 + (int)(Math.random() * (x/2));
        positionY = 0 + (int)(Math.random() * (y/2));
        width = w;
        height = h;
        aircraft = plane;
        flightPlan = plan;
        isVisible = true;
        draw();

    }


    /**
     * Shows strip for selected aircraft (prototyping only).
     * <p>
     * This will potentially become a method to call externally
     * to trigger instantiation of a strip.
     * </p>
     * @param aircraft - the aircraft to display the strip of
     */
    public void show(Aircraft aircraft) {
//    	if (aircraft != null) {
//    		aircraft = aircraft;
//    		isVisible = true;
//    	}
    }

    public void hide() {
        //aircraft = null;
        isVisible = false;
    }

    public void draw() {
        	drawOutline();
        if (isVisible) {
            drawFlightNumber();
            drawAirline();
            drawAltitude();
            drawRoute();
            drawStatus();
        }
        
    }

    private void drawOutline() {
        graphics.setColour(graphics.blue);
        graphics.rectangle(true, positionX, positionY, width, height);
        graphics.setColour(graphics.white);
        graphics.rectangle(true, positionX + 2, positionY + 2, 40, height - 4);

    }

    private void drawFlightNumber() {
//    	btc.Main.display.drawString((float)(positionX + width/2), (float)(positionY + (height/2)), (aircraft.getName().substring(0,2)), org.newdawn.slick.Color.red);
//    	graphics.print(aircraft.getName(),(positionX + (width/2)), (positionY + (height/2)));
    	graphics.setColour(graphics.black);
    	graphics.print(aircraft.getName().substring(0,2), (positionX + 2), (positionY + 2));
    	graphics.print(aircraft.getName().substring(2,5), (positionX + 2), (positionY + 30));
    }

    private void drawAirline() {
    	graphics.setColour(graphics.white);
        graphics.print(aircraft.getAirline(), (positionX + 2 + 40), (positionY + 2));

    }

    private void drawAltitude() {
//        btc.Main.display.drawString((float)(positionX + (width/2)), (float)(positionY + height), (new Integer(aircraft.getAltitude()).toString() + "ft"));
    	graphics.print(new Integer(aircraft.getAltitude()).toString() + "ft", (positionX + (width/2)), ((positionY + height) - 24));
    }
    
    private void drawRoute() {
    	graphics.print(aircraft.getOrigin().substring(0,3) + " to " + aircraft.getDestination().substring(0, 3), (positionX + (width/2) - 20), (positionY + height - 36));
    }
    
    private void drawStatus() {
    	if (aircraft.isInDanger()) {
    		graphics.setColour(graphics.red);
    		graphics.rectangle(true, (positionX + 42), (positionY + height - 14), 116, 12);
    	} else {
    		graphics.setColour(graphics.green);
    		graphics.rectangle(true, (positionX + 42), (positionY + height - 14), 116, 12);
    	}
    }

    @Override
    public void mousePressed(int key, int x, int y) {}

    @Override
    public void mouseReleased(int key, int mx, int my) {}

    @Override
    public void keyPressed(int key) {}

    @Override
    public void keyReleased(int key) {}

}
