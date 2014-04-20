package cls;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import lib.jog.graphics;
import lib.jog.input;

/**
 * <code>InputBox</code> represents a blank field in the window.
 * It acts as a blank box in which text can be input.
 *
 */
public class InputBox {
	
	/**
	 * <code>validKeys</code> are the legal keys
	 *  permitted for entry into the input box.
	 */
	private static ArrayList<Integer> validKeys;

	/** The foreground colour of the input box */
	private Color foreColour;

	/** The border colour of the input box */
	private Color borderColour;

	/** The x-origin of the input box */
	private int x;

	/** The y-origin of the input box */
	private int y;

	/** The width in pixels of the input box */
	private int width;

	/** The height in pixels of the input box */
	private int height;

	/** The text typed into the input box */
	private String text;

	/** Whether the input box is currently being edited */
	private boolean editing;
	
	/** Whether the user can still select or enter text into the input box */
	private boolean enabled;


	/**
	 * The default constructor.
	 * @param foreColour - the colour of the foreground
	 * @param borderColour - the colour of the background
	 * @param x - the x-origin of the input box
	 * @param y - the y-origin of the input box
	 * @param width - the width in pixels of the input box
	 * @param height - the height in pixels of the input box
	 */
	public InputBox(Color foreColour, Color borderColour,
			int x, int y, int width, int height) {
		this.foreColour = foreColour;
		this.borderColour = borderColour;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.editing = false;
		this.enabled = true;
		this.text = "";
		
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
	 * Draw the input box and any text.
	 */
	public void draw() {
		drawCentred(x + (width / 2));
	}
	
	/**
	 * Render/draw the input box and the text, if any.
	 * <p>
	 * This will ensure that the input box is centred.
	 * </p>
	 * @param x - the x co-ordinate to centre around
	 */
	public void drawCentred(double xPos) {
		graphics.setColour(borderColour);
		graphics.rectangle(true, (xPos - (width / 2)), y, width, height);
		graphics.setColour(foreColour);
		graphics.rectangle(true, (xPos - (width / 2)) + 2, y + 2,
				width - 4, height - 4);
		
		graphics.setColour(0, 0, 0);
		
		if (editing && !(text.length() >= 12)) {
			graphics.printScaled(text + "_",
					(xPos - (width / 2)) + 4, y + 4, 2, 1);
		} else {
			graphics.printScaled(text,
					(xPos - (width / 2)) + 4, y + 4, 2, 1);
		}
		
		if (!enabled) {
			graphics.setColour(0, 0, 0, 128);
			graphics.rectangle(true, (xPos - (width / 2)) + 2, y + 2,
					width - 4, height - 4);
		}
	}
	
	public void mouseReleased(int button, int x, int y) {
		if (editing) {
			if (!input.isMouseInRect(x, y, width, height)) {
				deactivate();
			}
		} else {
			if (input.isMouseInRect(x, y, width, height)) {
				activate();
			}
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
	 * <p>Called upon clicking on the input box.</p>
	 */
	public void activate() {
		if (enabled) {
			editing = true;
		}
	}

	/**
	 * Removes control from the input box.
	 * Text may not be entered.
	 * <p>Called upon the following conditions:</p>
	 * <p> - The mouse has been clicked at 
	 * 			a position outside of the input box</p>
	 * <p> - The return key was pressed while entering text</p>
	 */
	public void deactivate() {
		editing = false;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		if (!enabled) {
			deactivate();
		}
	}
	
	/**
	 * <code>isEmpty</code> determines if the player
	 * has actually entered a name into the input box.
	 * @return - true if the input box contains > 0 characters
	 * </p>
	 * 			- false for the "" string
	 */
	public boolean isEmpty() {
		if (text == null || "".equals(text)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Allows access to the text entered
	 * @return The text within the input box
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Gets the input box's x position.
	 * @return the input box's x position
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Gets the input box's y position.
	 * @return the input box's y position
	 */
	public double getY() {
		return y;
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
