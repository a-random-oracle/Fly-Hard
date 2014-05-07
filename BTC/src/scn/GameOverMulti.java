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
	
	/** Integer offset to centre vertically */
	private int yBorder = (window.height() - 440) / 2 - 20;

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
				returnToLobby, (window.width()/2 + window.height()/3) - 40, window.height() - (yBorder/2) + 9,
				80, 18, -1, -8);
//		exitButtons[0].setInset(true);
		
		//Instantiate and position the "return to title" button
		exitButtons[1] = new ButtonText("Return to Title", Main.flightstripFontMid,
				returnToTitle, (window.width()/2 + window.height()/3)/2 - 40, window.height() - (yBorder/2) + 9,
				80, 18, -1, -8);
//		exitButtons[1].setInset(true);
	}

	public void update(double timeDifference) {

	}

	public void draw() {
		// draw a line separating  both player's stats
		graphics.setColour(graphics.safetyOrange);
		graphics.line(window.width() / 2, yBorder, window.width() / 2, window.height() - yBorder);
		graphics.rectangle(true, window.height()/3 - 40, yBorder - 2,
				(window.width() - (2 * window.height()/3 - 80)), 70);
		graphics.setColour(Color.black);
		graphics.setFont(Main.menuTitleFont);
		if (hostPlayer.getPlanesCollided() < opposingPlayer.getPlanesCollided()) {
			graphics.print("You Win!", window.height()/3,  yBorder);
		} else if (hostPlayer.getPlanesCollided() > opposingPlayer.getPlanesCollided()) {
			graphics.print("You Lose!", window.height()/3, yBorder);
		} else {
			graphics.print("Game Over", window.height()/3, yBorder);
		}
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
		graphics.printCentred(hostPlayer.getName(), (window.width()/2 + window.height()/3)/2, yBorder + 80, 0, 0);
		graphics.setFont(Main.menuMainFont);
		graphics.setColour(Color.white);
		graphics.printCentred("(you)", (window.width()/2 + window.height()/3)/2, yBorder + 125, 0, 0);
		graphics.printCentred("Planes cleared from airspace : " 
				+ hostPlayer.getPlanesCleared(), (window.width()/2 + window.height()/3)/2, yBorder + 160, 0, 0);
		graphics.printCentred("Planes landed : "
				+ hostPlayer.getPlanesLanded(), (window.width()/2 + window.height()/3)/2, yBorder + 200, 0, 0);
		graphics.printCentred("Planes taken off : "
				+ hostPlayer.getPlanesTakenOff(), (window.width()/2 + window.height()/3)/2, yBorder + 240, 0, 0);
		graphics.printCentred("Planes collided : "
				+ hostPlayer.getPlanesCollided(), (window.width()/2 + window.height()/3)/2, yBorder + 280, 0, 0);
		graphics.printRight("Final score :", (window.width()/2 + window.height()/3)/2 + 20, yBorder + 320, 0, 0);
		graphics.setColour(graphics.safetyOrange);
		graphics.print(Integer.toString(hostPlayer.getScore()), (int)((window.width()/2 + window.height()/3)/2 + 24), (int)(yBorder + 320));

		
	}

	public void drawOpposingPlayerStats() {
		graphics.setFont(Main.menuTitleFont);
		graphics.setColour(graphics.safetyOrange);
		graphics.printCentred(opposingPlayer.getName(), (window.width()/2 + window.height()/3), yBorder  +80, 0, 0);
		graphics.setFont(Main.menuMainFont);
		graphics.setColour(Color.white);
		graphics.printCentred("Planes cleared from airspace : " 
				+ opposingPlayer.getPlanesCleared(), (window.width()/2 + window.height()/3), yBorder + 160, 0, 0);
		graphics.printCentred("Planes landed : "
				+ opposingPlayer.getPlanesLanded(), (window.width()/2 + window.height()/3), yBorder + 200, 0, 0);
		graphics.printCentred("Planes taken off : "
				+ opposingPlayer.getPlanesTakenOff(), (window.width()/2 + window.height()/3), yBorder + 240, 0, 0);
		graphics.printCentred("Planes collided : "
				+ opposingPlayer.getPlanesCollided(), (window.width()/2 + window.height()/3), yBorder + 280, 0, 0);
		graphics.setColour(Color.white);
		graphics.printRight("Final score :", (window.width()/2 + window.height()/3) + 20, yBorder + 320, 0, 0);
		graphics.setColour(graphics.safetyOrange);
		graphics.print(Integer.toString(opposingPlayer.getScore()), (int)((window.width()/2 + window.height()/3) + 24), (int)(yBorder + 320));
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
