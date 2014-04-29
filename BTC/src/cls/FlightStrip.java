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

    private cls.Aircraft currentAircraft;

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
    public FlightStrip(double x, double y, double w, double h) {
        positionX = x;
        positionY = y;
        width = w;
        height = h;
        hide();
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
    	if (aircraft != null) {
    		currentAircraft = aircraft;
    		isVisible = true;
    	}
    }

    public void hide() {
        currentAircraft = null;
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
//    	btc.Main.display.drawString((float)(positionX + width/2), (float)(positionY + (height/2)), (currentAircraft.getName().substring(0,2)), org.newdawn.slick.Color.red);
//    	graphics.print(currentAircraft.getName(),(positionX + (width/2)), (positionY + (height/2)));
    	graphics.setColour(graphics.black);
    	graphics.print(currentAircraft.getName().substring(0,2), (positionX + 2), (positionY + 2));
    	graphics.print(currentAircraft.getName().substring(2,5), (positionX + 2), (positionY + 30));
    }

    private void drawAirline() {
    	graphics.setColour(graphics.white);
        graphics.print(currentAircraft.getAirline(), (positionX + 2 + 40), (positionY + 2));
    }

    private void drawAltitude() {
//        btc.Main.display.drawString((float)(positionX + (width/2)), (float)(positionY + height), (new Integer(currentAircraft.getAltitude()).toString() + "ft"));
    	graphics.print(new Integer(currentAircraft.getAltitude()).toString() + "ft", (positionX + (width/2)), ((positionY + height) - 24));
    }
    
    private void drawRoute() {
    	graphics.print(currentAircraft.getOrigin().substring(0,3) + " to " + currentAircraft.getDestination().substring(0, 3), (positionX + (width/2) - 20), (positionY + height - 36));
    }
    
    private void drawStatus() {
    	if (currentAircraft.isInDanger()) {
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
