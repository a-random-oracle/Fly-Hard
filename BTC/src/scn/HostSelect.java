package scn;

import btc.Main;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;

import lib.ButtonText;

public class HostSelect extends Scene {

	private final int HOST_BUTTON_W = 128;
	private final int HOST_BUTTON_H = 32;
	private final int HOST_BUTTON_X = window.width()/4 - (HOST_BUTTON_W / 2);
	private final int HOST_BUTTON_Y = 2*window.height()/3;

	private final int CONNECT_BUTTON_W = HOST_BUTTON_W;
	private final int CONNECT_BUTTON_H = HOST_BUTTON_H;
	private final int CONNECT_BUTTON_X = 3*window.width()/4 - (CONNECT_BUTTON_W / 2);
	private final int CONNECT_BUTTON_Y = HOST_BUTTON_Y;
	
	private ButtonText[] buttons;

	// To allow the difficulty selection to work with multiple potential game
	// scenes, e.g. separate Demo and a Full Game
	//private int scene;
	public final static int CREATE_DEMO = 0;
	
	protected HostSelect(int scn) {
		super();
		//this.scene = scn;
	}
	
	
	@Override
	public void start() {
		buttons = new ButtonText[2];
		
		buttons[0] = new lib.ButtonText("HOST", null, HOST_BUTTON_X, HOST_BUTTON_Y,
				HOST_BUTTON_W, HOST_BUTTON_H);
		
		buttons[1] = new lib.ButtonText("CONNECT", null, CONNECT_BUTTON_X, CONNECT_BUTTON_Y,
				CONNECT_BUTTON_W, CONNECT_BUTTON_H);
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	/**
	 * Causes a button to act if mouse released over it
	 */
	public void mouseReleased(int key, int x, int y) {
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

	@Override
	public void keyPressed(int key) {}

	@Override
	/**
	 * Quits back to title scene on escape button
	 */
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			Main.closeScene();
		}
	}

	@Override
	public void update(double timeDifference) {}

	@Override
	public void draw() {
		graphics.setColour(0,128,0); //GREEN
		
		//String chooseHost = " ";
		
		graphics.print("You have chosen to play multiplayer", window.width() / 6, window.height() -920, 3);
		graphics.print("Choose to host a game or join a host", window.width() / 6, window.height() - 850, 3);


		//graphics.printCentred(chooseHost, window.width()/2 - 50,
				//window.height()/2 + 50, 1, 100);

		graphics.rectangle(false, HOST_BUTTON_X, HOST_BUTTON_Y,
				HOST_BUTTON_W, HOST_BUTTON_H);
		graphics.rectangle(false, CONNECT_BUTTON_X, CONNECT_BUTTON_Y,
				CONNECT_BUTTON_W, CONNECT_BUTTON_H);

		for (lib.ButtonText b : buttons) {
			b.draw();
		}
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

}
