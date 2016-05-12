package cls;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import lib.jog.graphics;
import lib.jog.input;

/**
 * Represents an input field in the window.
 * <p>
 * To use the input box, the following methods must be
 * linked to the environment the input box is displayed in:
 * <ul>
 * <li>{@link #update(double)}</li>
 * <li>{@link #draw()}</li>
 * <li>{@link #mouseReleased(int, int, int)}</li>
 * <li>{@link #keyPressed(int)}</li>
 * </ul>
 * </p>
 */
public class InputBox {
	
	/**
	 * The keys permitted for entry into the input box.
	 */
	private static ArrayList<Integer> validKeys;

	/** The foreground colour of the input box */
	private Color foregroundColour;
	
	/** The background colour of the input box */
	private Color backgroundColour;

	/** The border colour of the input box */
	private Color borderColour;

	/** The x position of the input box */
	private int x;

	/** The y position of the input box */
	private int y;

	/** The width of the input box */
	private int width;

	/** The height of the input box */
	private int height;
	
	/** Whether the input box should be centred */
	private boolean centred;

	/** The text in the input box */
	private String text;

	/** Whether the input box is currently being edited */
	private boolean editing;
	
	/** Whether the user can still select or enter text into the input box */
	private boolean enabled;
	
	/** Whether the input box is displaying an alert */
	private boolean alerting;
	
	/** The length of time in milliseconds that the input box should be alerting for */
	private double alertDuration;
	
	/** The length of time in milliseconds that the input box has been alerting for */
	private double alertTime;


	/**
	 * Constructs an input box.
	 * <p>
	 * The foreground (text) colour will be set to black.
	 * </p>
	 * @param foregroundColour - the colour of the text
	 * @param backgroundColour - the colour of the text
	 * @param borderColour - the colour of the background
	 * @param x - the x position of the input box
	 * @param y - the y position of the input box
	 * @param width - the width of the input box
	 * @param height - the height of the input box
	 * @param centred - should the input box be centred
	 */
	public InputBox(Color backgroundColour, Color borderColour,
			int x, int y, int width, int height, boolean centred) {
		this.foregroundColour = Color.black;
		this.backgroundColour = backgroundColour;
		this.borderColour = borderColour;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.centred = centred;

		this.editing = false;
		this.enabled = true;
		this.text = "";
		
		setValidKeys();
	}
	
	/**
	 * Constructs an input box.
	 * <p>
	 * The foreground (text) colour can be specified</p>
	 * @param foregroundColour - the colour of the text
	 * @param backgroundColour - the colour of the text
	 * @param borderColour - the colour of the background
	 * @param x - the x position of the input box
	 * @param y - the y position of the input box
	 * @param width - the width of the input box
	 * @param height - the height of the input box
	 * @param centred - should the input box be centred
	 */
	public InputBox(Color foregroundColour, Color backgroundColour,
			Color borderColour, int x, int y, int width, int height,
			boolean centred) {
		this.foregroundColour = foregroundColour;
		this.backgroundColour = backgroundColour;
		this.borderColour = borderColour;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.centred = centred;

		this.editing = false;
		this.enabled = true;
		this.text = "";
		
		setValidKeys();
	}
	
	
	/**
	 * Sets the list of valid keys.
	 * <p>
	 * Adds the following to the list of valid keys:
	 * <ul>
	 * <li>The numeric keys</li>
	 * <li>The alphabet keys</li>
	 * <li>The space key</li>
	 * <li>The enter key</li>
	 * <li>The backspace key</li>
	 * </ul>
	 * </p>
	 */
	private void setValidKeys() {
		InputBox.validKeys = new ArrayList<Integer>();
		
		// Add the numeric keys to the list of valid entry keys
		InputBox.validKeys.add(input.KEY_0);
		InputBox.validKeys.add(input.KEY_1);
		InputBox.validKeys.add(input.KEY_2);
		InputBox.validKeys.add(input.KEY_3);
		InputBox.validKeys.add(input.KEY_4);
		InputBox.validKeys.add(input.KEY_5);
		InputBox.validKeys.add(input.KEY_6);
		InputBox.validKeys.add(input.KEY_7);
		InputBox.validKeys.add(input.KEY_8);
		InputBox.validKeys.add(input.KEY_9);
		
		// Add the num pad keys to the list of valid entry keys
		InputBox.validKeys.add(input.KEY_NUM_0);
		InputBox.validKeys.add(input.KEY_NUM_1);
		InputBox.validKeys.add(input.KEY_NUM_2);
		InputBox.validKeys.add(input.KEY_NUM_3);
		InputBox.validKeys.add(input.KEY_NUM_4);
		InputBox.validKeys.add(input.KEY_NUM_5);
		InputBox.validKeys.add(input.KEY_NUM_6);
		InputBox.validKeys.add(input.KEY_NUM_7);
		InputBox.validKeys.add(input.KEY_NUM_8);
		InputBox.validKeys.add(input.KEY_NUM_9);
		
		// Add the alphabet to the list of valid entry keys
		InputBox.validKeys.add(input.KEY_A);
		InputBox.validKeys.add(input.KEY_B);
		InputBox.validKeys.add(input.KEY_C);
		InputBox.validKeys.add(input.KEY_D);
		InputBox.validKeys.add(input.KEY_E);
		InputBox.validKeys.add(input.KEY_F);
		InputBox.validKeys.add(input.KEY_G);
		InputBox.validKeys.add(input.KEY_H);
		InputBox.validKeys.add(input.KEY_I);
		InputBox.validKeys.add(input.KEY_J);
		InputBox.validKeys.add(input.KEY_K);
		InputBox.validKeys.add(input.KEY_L);
		InputBox.validKeys.add(input.KEY_M);
		InputBox.validKeys.add(input.KEY_N);
		InputBox.validKeys.add(input.KEY_O);
		InputBox.validKeys.add(input.KEY_P);
		InputBox.validKeys.add(input.KEY_Q);
		InputBox.validKeys.add(input.KEY_R);
		InputBox.validKeys.add(input.KEY_S);
		InputBox.validKeys.add(input.KEY_T);
		InputBox.validKeys.add(input.KEY_U);
		InputBox.validKeys.add(input.KEY_V);
		InputBox.validKeys.add(input.KEY_W);
		InputBox.validKeys.add(input.KEY_X);
		InputBox.validKeys.add(input.KEY_Y);
		InputBox.validKeys.add(input.KEY_Z);
	
		// Add special keys to the list of valid entry keys
		InputBox.validKeys.add(input.KEY_SPACE);
		InputBox.validKeys.add(input.KEY_RETURN);
		InputBox.validKeys.add(input.KEY_BACKSPACE);
	}
	
	/**
	 * Updates the input box.
	 */
	public void update(double timeDelta) {
		if (alerting) {
			// Update the time the alert has been showing for
			alertTime += timeDelta;
			
			// If the alert duration is up, stop the input box from alerting
			// Also reset the alert duration andthe alert time
			if (alertTime >= alertDuration) {
				alerting = false;
				alertDuration = 0;
				alertTime = 0;
			}
		}
	}
	
	/**
	 * Draws the input box and any text.
	 */
	public void draw() {
		if (centred) {
			drawCentred(x);
		} else {
			drawCentred(x + (width / 2));
		}
	}
	
	/**
	 * Draws the input box and any text.
	 * <p>
	 * This will ensure that the input box is centred.
	 * </p>
	 * @param x - the x position to centre around
	 */
	private void drawCentred(double xPos) {
		// Draw the outside border
		graphics.setColour(borderColour);
		graphics.rectangle(true, (xPos - (width / 2)), y, width, height);
		
		// Draw the background
		graphics.setColour(backgroundColour);
		graphics.rectangle(true, (xPos - (width / 2)) + 2, y + 2,
				width - 4, height - 4);
		
		// Draw the text
		graphics.setColour(foregroundColour);
		
		// Add an underscore to the end of the text if further text can still
		// be added
		if (editing && !(text.length() >= 12)) {
			graphics.printScaled(text + "_",
					(xPos - (width / 2)) + 4, y + 4, 2, 1);
		} else {
			graphics.printScaled(text,
					(xPos - (width / 2)) + 4, y + 4, 2, 1);
		}
		
		if (!enabled) {
			// Grey out the input box if it is disabled
			graphics.setColour(0, 0, 0, 128);
			graphics.rectangle(true, (xPos - (width / 2)) + 2, y + 2,
					width - 4, height - 4);
		} else if (alerting) {
			// Colour the input box red to alert the user
			graphics.setColour(255, 0, 0, 255);
			graphics.rectangle(true, (xPos - (width / 2)) + 2, y + 2,
					width - 4, height - 4);
		}
	}
	
	/**
	 * Handles mouse events.
	 * @param button - the mouse button
	 * @param x - the x position of the event
	 * @param y - the y position of the event
	 */
	public void mouseReleased(int button, int x, int y) {
		if (button == input.MOUSE_LEFT) {
			if (centred) {
				if (input.isMouseInRect(this.x - (width / 2), this.y,
						width, height)) {
					activate();
				} else {
					deactivate();
				}
			} else {
				if (input.isMouseInRect(this.x, this.y, width, height)) {
					activate();
				} else {
					deactivate();
				}
			}
		} else if (button == input.MOUSE_RIGHT) {
			deactivate();
		}
	}
	
	/**
	 * Handles keyboard inputs.
	 * @param key - the key which was pressed
	 */
	public void keyPressed(int key) {
		if (enabled && validKeys.contains(key)) {
			switch (key) {
			case input.KEY_RETURN:
				deactivate();
				break;
			case input.KEY_BACKSPACE:
				if (text.length() > 0) {
					text = text.substring(0, text.length() - 1);
				}
				break;
			case input.KEY_SPACE:
				if (!(text.length() >= 12)) {
					text += " ";
				}
				break;
			case input.KEY_NUM_0:
				text = text + "0";
				break;
			case input.KEY_NUM_1:
				text = text + "1";
				break;
			case input.KEY_NUM_2:
				text = text + "2";
				break;
			case input.KEY_NUM_3:
				text = text + "3";
				break;
			case input.KEY_NUM_4:
				text = text + "4";
				break;
			case input.KEY_NUM_5:
				text = text + "5";
				break;
			case input.KEY_NUM_6:
				text = text + "6";
				break;
			case input.KEY_NUM_7:
				text = text + "7";
				break;
			case input.KEY_NUM_8:
				text = text + "8";
				break;
			case input.KEY_NUM_9:
				text = text + "9";
				break;
			default:
				if (!(text.length() >= 12)) {
					text = text + Keyboard.getKeyName(key);
				}
				break;
			}
		}
	}
	
	/**
	 * Allows text to be entered into the input box.
	 * <p>
	 * Called upon clicking on the input box.
	 * </p>
	 */
	public void activate() {
		if (enabled) {
			editing = true;
		}
	}

	/**
	 * Removes control from the input box. Text may not be entered.
	 * <p>
	 * Called upon the following conditions:
	 * <ul>
	 * <li>The mouse has been clicked at 
	 * 			a position outside of the input box</li>
	 * <li>The return key was pressed while entering text</li>
	 * </ul>
	 * </p>
	 */
	public void deactivate() {
		editing = false;
	}
	
	/**
	 * Sets whether the input box can be used.
	 * @param enabled - should the input box be enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		if (!enabled) {
			deactivate();
		}
	}
	
	/**
	 * Causes the input box to flash red.
	 * @param alertDuration - the length of time in milliseconds to flash for
	 */
	public void alert(double alertDuration) {
		this.alerting = true;
		this.alertDuration = alertDuration / 1000;
	}
	
	/**
	 * Determines if the input box contains any text.
	 * @return <code>true</code> if the input box contains text,
	 * 			otherwise <code>false</code>
	 */
	public boolean isEmpty() {
		if (text == null || "".equals(text)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Allows access to the text entered.
	 * @return the text held by the input box
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the input box's width.
	 * @return the input box's width
	 */
	public double getWidth() {
		return width;
	}
	
	/**
	 * Gets the input box's height.
	 * @return the input box's height
	 */
	public double getHeight() {
		return height;
	}

}
