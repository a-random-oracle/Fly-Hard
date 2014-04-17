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
	
	protected Lobby() {
		super();
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
		
		buttons[0] = new ButtonText("Create Game", createGame, CREATE_BUTTON_X, CREATE_BUTTON_Y,
				CREATE_BUTTON_W, CREATE_BUTTON_H);
		
		// Get the available opponents from the server
		String[] openConnections = NetworkManager
				.postMessage("GET_OPEN_CONNECTIONS").split(";");
		
		// Get the names of the available opponents
		String[] availablePlayers = new String[openConnections.length];
		for (int i = 0; i < openConnections.length; i++) {
			if (openConnections[i].split(":").length > 1) {
				availablePlayers[i] = openConnections[i].split(":")[1];
			} else {
				availablePlayers[i] = "DEFAULT PLAYER NAME";
			}
			
			System.out.println("Found player: " + availablePlayers[i]);
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	/**
	 * Causes a button to act if mouse released over it
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		for (ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

	@Override
	public void keyPressed(int key) {}

	/**
	 * Quits back to title scene on escape button
	 */
	@Override
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

		graphics.rectangle(false, CREATE_BUTTON_X, CREATE_BUTTON_Y,
				CREATE_BUTTON_W, CREATE_BUTTON_H);

		for (ButtonText b : buttons) {
			b.draw();
		}
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

}
