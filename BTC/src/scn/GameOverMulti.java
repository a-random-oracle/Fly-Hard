package scn;

import org.newdawn.slick.Color;

import btc.Main;
import lib.ButtonText;
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
	
	private ButtonText[] exitButtons;

	public GameOverMulti(Player hostPlayer, Player opposingPlayer) {
		this.hostPlayer = hostPlayer;
		this.opposingPlayer = opposingPlayer;
	}

	@Override
	public void start() {
		exitButtons = new ButtonText[2];
		
		//Action to return to the lobby scene
		ButtonText.Action returnToLobby = new ButtonText.Action() {
			
			@Override
			public void action() {
				Main.setScene(new Lobby());
			}
		};
		//Action to return to the title scene
		ButtonText.Action returnToTitle = new ButtonText.Action() {
			
			@Override
			public void action() {
				Main.setScene(new Title());
			}
		};
		//Instantiate and position the "return to lobby" button
		exitButtons[0] = new ButtonText("Play Again", Main.flightstripFontMid,
				returnToLobby, window.width()/2 - 100, window.height() - 50,
				100, 60, 100, 10);
		exitButtons[0].setInset(true);
		
		//Instantiate and position the "return to title" button
		exitButtons[1] = new ButtonText("Return to Title", Main.flightstripFontMid,
				returnToTitle, window.width()/2 + 80, window.height() - 50,
				100, 60, 100, 10);
		exitButtons[1].setInset(true);
	}

	public void update(double timeDifference) {

	}

	public void draw() {
		// draw a line separating  both player's stats
		graphics.line(window.width() / 2, 20, window.width() / 2, window.height() - 20);
		// draw both player's statistics from the finished game
		drawHostPlayerStats();
		drawOpposingPlayerStats();
		
		//Draw the buttons
		for (ButtonText b : exitButtons) {
			b.draw();
		}
	}

	public void drawHostPlayerStats() {
		graphics.setFont(Main.menuTitleFont);
		graphics.setColour(graphics.safetyOrange);
		graphics.printCentred(hostPlayer.getName(), window.width()/4, 10, 0, 0);
		graphics.setFont(Main.menuMainFont);
		graphics.setColour(Color.white);
		graphics.printCentred("Planes cleared from airspace : " 
				+ hostPlayer.getPlanesCleared(), window.width()/4, 80, 0, 0);
		graphics.printCentred("Planes landed : "
				+ hostPlayer.getPlanesLanded(), window.width()/4, 130, 0, 0);
		graphics.printCentred("Planes taken off : "
				+ hostPlayer.getPlanesTakenOff(), window.width()/4, 180, 0, 0);
		graphics.printCentred("Planes collided : "
				+ hostPlayer.getPlanesCollided(), window.width()/4, 230, 0, 0);
	}

	public void drawOpposingPlayerStats() {
		graphics.setFont(Main.menuTitleFont);
		graphics.setColour(graphics.safetyOrange);
		graphics.printCentred(opposingPlayer.getName(), 3 * window.width()/4, 10, 0, 0);
		graphics.setFont(Main.menuMainFont);
		graphics.setColour(Color.white);
		graphics.printCentred("Planes cleared from airspace : " 
				+ opposingPlayer.getPlanesCleared(), 3 * window.width()/4, 80, 0, 0);
		graphics.printCentred("Planes landed : "
				+ opposingPlayer.getPlanesLanded(), 3 * window.width()/4, 130, 0, 0);
		graphics.printCentred("Planes taken off : "
				+ opposingPlayer.getPlanesTakenOff(), 3 * window.width()/4, 180, 0, 0);
		graphics.printCentred("Planes collided : "
				+ opposingPlayer.getPlanesCollided(), 3 * window.width()/4, 230, 0, 0);
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {
		for (ButtonText b : exitButtons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

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
