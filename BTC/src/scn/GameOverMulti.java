package scn;

import btc.Main;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.window;
import cls.Player;

public class GameOverMulti extends Scene {

	/** The player who hosted the game */
	private Player hostPlayer;

	/** The player who joined the game */
	private Player opposingPlayer;

	/** The value corresponding to the key which has most recently been pressed */
	private int keyPressed;

	public GameOverMulti(Player hostPlayer, Player opposingPlayer) {
		this.hostPlayer = hostPlayer;
		this.opposingPlayer = opposingPlayer;
	}

	@Override
	public void start() {

	}

	public void update(double timeDifference) {

	}

	public void draw() {
		// draw a line separating  both player's stats
		graphics.line(window.width() / 2, 100, window.width() / 2, window.height() - 100);
		// draw both player's statistics from the finished game
		drawHostPlayerStats();
		drawOpposingPlayerStats();
	}

	public void drawHostPlayerStats() {
		graphics.printCentred(hostPlayer.getName(), 20, 10, 3, 100);
		graphics.printCentred("Planes cleared from airspace : " 
				+ hostPlayer.getPlanesCleared(), 20, 50, 2, 200);
		graphics.printCentred("Planes landed : "
				+ hostPlayer.getPlanesLanded(), 20, 100, 2, 200);
		graphics.printCentred("Planes taken off : "
				+ hostPlayer.getPlanesTakenOff(), 20, 150, 2, 200);
		graphics.printCentred("Planes collided : "
				+ hostPlayer.getPlanesCollided(), 20, 200, 2, 200);
	}

	public void drawOpposingPlayerStats() {
		graphics.printCentred(opposingPlayer.getName(), 500, 10, 3, 100);
		graphics.printCentred("Planes cleared from airspace : " 
				+ opposingPlayer.getPlanesCleared(), 500, 50, 2, 200);
		graphics.printCentred("Planes landed : "
				+ opposingPlayer.getPlanesLanded(), 500, 100, 2, 200);
		graphics.printCentred("Planes taken off : "
				+ opposingPlayer.getPlanesTakenOff(), 500, 150, 2, 200);
		graphics.printCentred("Planes collided : "
				+ opposingPlayer.getPlanesCollided(), 500, 200, 2, 200);
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	@Override
	public void keyPressed(int key) {
		keyPressed = key;
	}

	@Override
	public void keyReleased(int key) {
		if (key == keyPressed) {
			Main.closeScene();
			Main.setScene(new Lobby());
		}
	}

	@Override
	public void close() {
	}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
