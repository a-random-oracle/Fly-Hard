package scn;

import net.NetworkManager;

import org.newdawn.slick.Color;

import btc.Main;
import cls.InputBox;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.audio.Sound;
import lib.ButtonText;

public class Lobby extends Scene {

	private final int CREATE_BUTTON_W = 128;
	private final int CREATE_BUTTON_H = 32;
	private final int CREATE_BUTTON_X = 500;
	private final int CREATE_BUTTON_Y = 200;
	
	private InputBox inputBox;
	
	private ButtonText[] buttons;

	// To allow the difficulty selection to work with multiple potential game
	// scenes, e.g. separate Demo and a Full Game
	//private int scene;
	public final static int CREATE_DEMO = 0;
	
	protected Lobby(int scn) {
		super();
		//this.scene = scn;
	}
	
	
	@Override
	public void start() {
		inputBox = new InputBox(Color.white, Color.red, 400, 500, 200, 30);
		
		buttons = new ButtonText[1];
		
		ButtonText.Action createGame = new ButtonText.Action() {
			@Override
			public void action() {
				NetworkManager.setPlayerName(inputBox.getText());
				Main.getNetworkManager().sendMessage("INIT");
			}
		};
		
		buttons[0] = new lib.ButtonText("Create Game", createGame, CREATE_BUTTON_X, CREATE_BUTTON_Y,
				CREATE_BUTTON_W, CREATE_BUTTON_H);
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
	public void update(double timeDifference) {
		
		inputBox.update(timeDifference);
	}

	@Override
	public void draw() {
		graphics.setColour(256,256,256); //WHITE

		inputBox.draw();

		//graphics.printCentred(chooseHost, window.width()/2 - 50,
				//window.height()/2 + 50, 1, 100);

		graphics.rectangle(false, CREATE_BUTTON_X, CREATE_BUTTON_Y,
				CREATE_BUTTON_W, CREATE_BUTTON_H);

		for (lib.ButtonText b : buttons) {
			b.draw();
		}
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

}
