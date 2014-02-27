package scn;

import btc.Main;
import lib.jog.window;
import lib.jog.audio.Sound;

public class HostSelect extends Scene {

	private final int HOST_BUTTON_W = 128;
	private final int HOST_BUTTON_H = 32;
	private final int HOST_BUTTON_X = window.width()/4 - (HOST_BUTTON_W / 2);
	private final int HOST_BUTTON_Y = 2*window.height()/3;

	private final int CONNECT_BUTTON_W = HOST_BUTTON_W;
	private final int CONNECT_BUTTON_H = HOST_BUTTON_H;
	private final int CONNECT_BUTTON_X = 3*window.width()/4 - (CONNECT_BUTTON_W / 2);
	private final int CONNECT_BUTTON_Y = HOST_BUTTON_Y;

	//To allow the difficulty selection to work with multiple potential game scenes, e.g. separate Demo and a Full Game
	private int scene;
	
	protected HostSelect(Main main, int scn) {
		super(main);
		this.scene = scn;
	}
	
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(double timeDifference) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playSound(Sound sound) {
		// TODO Auto-generated method stub
		
	}

}
