package cls;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;

import lib.jog.graphics;
import lib.jog.input;

public class InputBox {
	
	private final static int[] validKeys = {
		input.KEY_0, input.KEY_1, input.KEY_2,
		input.KEY_3, input.KEY_4, input.KEY_5,
		input.KEY_6, input.KEY_7, input.KEY_8,
		input.KEY_9, input.KEY_A, input.KEY_B,
		input.KEY_C, input.KEY_D, input.KEY_E,
		input.KEY_F, input.KEY_G, input.KEY_H,
		input.KEY_I, input.KEY_J, input.KEY_K,
		input.KEY_L, input.KEY_M, input.KEY_N,
		input.KEY_O, input.KEY_P, input.KEY_Q,
		input.KEY_R, input.KEY_S, input.KEY_T,
		input.KEY_U, input.KEY_V, input.KEY_W,
		input.KEY_X, input.KEY_Y, input.KEY_Z,
		input.KEY_SPACE, input.KEY_RETURN, 
		input.KEY_BACKSPACE
	};

	private Color foreColour;

	private Color borderColour;

	private int x;

	private int y;

	private int width;

	private int height;

	private String text;

	/** Are we editing? */
	private boolean editing;

	/** Old text value */
	private String oldText;


	public InputBox(Color foreColour, Color borderColour,
			int x, int y, int width, int height) {
		this.foreColour = foreColour;
		this.borderColour = borderColour;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.editing = false;
		this.text = "";
		this.oldText = "";
	}

	public void activate() {
		editing = true;
		oldText = text;
	}

	public void deactivate() {
		editing = false;
	}

	public void update(double dt) {
		if (Mouse.isButtonDown(input.MOUSE_LEFT)) {
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

		while (Keyboard.next()) {
			if (editing && !Keyboard.isRepeatEvent()) {
				int key = pollKeys();
				
				switch (key) {
				case -1:
					// Do nothing
					break;
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
						text = oldText + Keyboard.getKeyName(key);
					}
					break;
				}
				
				oldText = text;
			}
		}
	}

	private int pollKeys() {
		for (int i = 0; i < validKeys.length; i++) {
			if (input.isKeyDown(validKeys[i])) {
				return validKeys[i];
			}
		}

		return -1;
	}
	
	public void draw() {
		graphics.setColour(borderColour);
		graphics.rectangle(true, x, y, width, height);
		graphics.setColour(foreColour);
		graphics.rectangle(true, x + 2, y + 2, width - 4, height - 4);
		graphics.setColour(0, 0, 0);
		
		if (editing && !(text.length() >= 12)) {
			graphics.printScaled(text + "_", x + 4, y + 4, 2, 1);
		} else {
			graphics.printScaled(text, x + 4, y + 4, 2, 1);
		}
	}
	
	public String getText() {
		return text;
	}

}
