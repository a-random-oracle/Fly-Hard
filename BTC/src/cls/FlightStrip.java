//    /|  /|  /|  /
//   / | / | / | /   In use by mwuk **BEWARE CHANGES**
//  /  |/  |/  |/
//
// V 0.9

package cls;

import java.awt.Color;

import scn.Game;

import btc.Main;
import cls.Player;
import lib.jog.graphics;
import lib.jog.input.EventHandler;          // <= May not be required
import lib.jog.window;

public class FlightStrip implements EventHandler {

    private boolean isVisible;

    private cls.Aircraft aircraft;
    
    private cls.FlightPlan flightPlan;

    private double positionY = 20;

    private double width;

    private double height;


    /**
     * Constructor for flight strips.
     * @param x - the x coord to draw at
     * @param y - tht y coord to draw at
     * @param w - the width of the strip
     * @param h - the height of the strip
     */
    public FlightStrip(double w, double h, Aircraft plane, FlightPlan plan) {
//        if (Game.getInstance() != null && Game.getInstance().getPlayer() != null && Game.getInstance().getPlayer().getFlightStrips() != null) {
//        	for (FlightStrip fs : Game.getInstance().getPlayer().getFlightStrips()) {
//        		if (fs.getY() == positionY) {
        			positionY += 80 * Game.getInstance().getPlayer().getFlightStrips().size();
//        		}
//        	}
//        }
    	width = w;
        height = h;
        aircraft = plane;
        flightPlan = plan;
        isVisible = true;
        

    }


    /**
     * Shows strip for selected aircraft (prototyping only).
     * <p>
     * This will potentially become a method to call externally
     * to trigger instantiation of a strip.
     * </p>
     * @param aircraft - the aircraft to display the strip of
     */
///    public void show(Aircraft aircraft) {
//    	if (aircraft != null) {
//    		aircraft = aircraft;
//    		isVisible = true;
//    	}
//    }

    public void hide() {
        //aircraft = null;
        isVisible = false;
    }
    

    public void draw(double xOffset) {
        	drawOutline(xOffset);
            drawFlightNumber(xOffset);
            drawAirline(xOffset);
            drawAltitude(xOffset);
            drawRoute(xOffset);
            drawStatus(xOffset);        
    }

    private void drawOutline(double xOffset) {
    	// TODO mouseover/click highlight (plane/strip sync)
        graphics.setColour(graphics.blue);
        graphics.rectangle(true, xOffset, positionY, width, height);
        graphics.setColour(graphics.white);
        graphics.rectangle(true, xOffset + 2, positionY + 2, 40, height - 4);

    }

    private void drawFlightNumber(double xOffset) {
    	graphics.setColour(graphics.black);
    	graphics.print(aircraft.getName().substring(0,2), (xOffset + 2), (positionY + 2));
    	graphics.print(aircraft.getName().substring(2,5), (xOffset + 2), (positionY + 30));
    }

    private void drawAirline(double xOffset) {
    	graphics.setColour(graphics.white);
        graphics.print(aircraft.getAirline(), (xOffset + 2 + 40), (positionY + 2));

    }

    private void drawAltitude(double xOffset) {
//        btc.Main.display.drawString((float)(xOffset + (width/2)), (float)(positionY + height), (new Integer(aircraft.getAltitude()).toString() + "ft"));
    	graphics.print(new Integer(aircraft.getAltitude()).toString() + "ft", (xOffset + (width/2)), ((positionY + height) - 24));
    }
    
    private void drawRoute(double xOffset) {
    	graphics.print(aircraft.getOrigin().substring(0,3) + " TO " + aircraft.getDestination().substring(0, 3), (xOffset + (width/2) - 20), (positionY + height - 36));
    }
    
    private void drawStatus(double xOffset) {
    	if (aircraft.isInDanger()) {
    		graphics.setColour(graphics.red);
    		graphics.rectangle(true, (xOffset + 42), (positionY + height - 14), 116, 12);
    		graphics.setColour(graphics.white);
            graphics.printCentred("SHIIIIIIIIT", (xOffset + 100), (positionY + height - 14), 1, 1);

    	} else {
    		graphics.setColour(graphics.green);
    		graphics.rectangle(true, (xOffset + 42), (positionY + height - 14), 116, 12);
    		graphics.setColour(graphics.white);
            graphics.printCentred("AWW YISS", (xOffset + 100), (positionY + height - 14), 1, 1);
    	}
    }
    
    public double getY() {
    	return this.positionY;
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
