package lib;

import lib.jog.graphics;
import lib.jog.input;
import btc.Main;

public class ButtonText {

	public interface Action {
		public void action();
	}

	private int x, y, width, height, ox, oy, size;
	private String text;
	private org.newdawn.slick.Color colourDefault, colourHover, colourUnavailable;
	private Action action;
	private boolean available;

	private float hover = 0;
	private boolean inset = false;

	public ButtonText(String text, Action action, int x, int y, int w, int h, int ox, int oy) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = ox;
		this.oy = oy;
		this.size = 1;
		colourDefault = graphics.safetyOrange;
		colourHover = new org.newdawn.slick.Color(256, 256, 256);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public ButtonText(String text, Action action, int x, int y, int w, int h) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		this.size = 1;
		colourDefault = graphics.safetyOrange;
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public ButtonText(String text, Action action, int x, int y, int w, int h, int ox, int oy, int size) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = ox;
		this.oy = oy;
		this.size = size;
		colourDefault = graphics.safetyOrange;
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public ButtonText(String text, Action action, int x, int y, int w, int h, int size) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		this.size = size;
		colourDefault = graphics.safetyOrange;
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public boolean isMouseOver(int mx, int my) {
		return (mx >= x && mx <= x + width && my >= y && my <= y + height);
	}

	public boolean isMouseOver() {
		return isMouseOver(input.mouseX(), input.mouseY());
	}

	public boolean isMouseOverRight(int mx, int my) {
		return (mx >= (x - width) && mx <= x && my >= y && my <= y + height);
	}

	public boolean isMouseOverRight() {
		return isMouseOverRight(input.mouseX(), input.mouseY());
	}

	/**
	 * Sets the string of text used
	 * @param newText - The string to be used
	 */
	public void setText(String newText) {
		text = newText;
	}

	/**
	 * Allows the button to have an animating effect on MouseOver
	 * @param anim - Boolean if button should animated
	 */
	public void setInset(boolean anim) {
		inset = anim;
	}

	/**
	 * Sets the button text to available - Changing the color to the one specified in ButtonText()
	 * @param available - value of the availability, either True or False
	 */
	public void setAvailability(boolean available) {
		this.available = available;
	}

	public void act() {
		if (!available) return;
		action.action();
	}

	/**
	 * Draws the button text which reacts to mouse interactions
	 */
	public void draw() {
		if (!available) {
			graphics.setColour(colourUnavailable);
		}
		else if (isMouseOver()) {
			graphics.setColour(colourHover);
			hover = hover + ( 1 - hover ) / 5;
		} else {
			graphics.setColour(colourDefault);
			hover -= hover / 5;
		}
		graphics.setColour( new org.newdawn.slick.Color(
			colourDefault.r * ( 1 - hover ) + colourHover.r * hover,
			colourDefault.g * ( 1 - hover ) + colourHover.g * hover,
			colourDefault.b * ( 1 - hover ) + colourHover.b * hover

		) );
		graphics.setFont(Main.menuTitle);
		graphics.print(text, x + ox + (inset ? hover * 10 : 0), y + oy, size);
	}

	/**
	 * Draws the button text which reacts to mouse interactions
	 * <p>
	 * Text will be drawn right-aligned
	 * </p>
	 */
	public void drawRight() {
		if (!available) {
			graphics.setColour(colourUnavailable);
		}
		else if (isMouseOverRight()) {
			graphics.setColour(colourHover);
		} else {
			graphics.setColour(colourDefault);
		}
		graphics.setFont(Main.engSign);
		graphics.printRight(text, x + ox, y + oy - 12, size, 0);
	}

}
