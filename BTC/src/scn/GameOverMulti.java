package scn;

import btc.Main;
import lib.SpriteAnimation;
import lib.TextBox;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;
import cls.Aircraft;
import cls.Player;
import cls.Vector;

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
		
	}

	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
