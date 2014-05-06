package scn;

import org.newdawn.slick.Color;
import java.io.File;

import scn.Game.DifficultySetting;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.graphics.Image;

import btc.Main;

public class DifficultySelect extends Scene {

	private final int yBorder = (window.height() - 440) / 2 - 20;
	private final int EASY_BUTTON_W = window.width() - (2 * window.height()/3) + 80;
	private final int EASY_BUTTON_H = 40;
	private final int EASY_BUTTON_X = window.height()/3 - 40;
	private final int EASY_BUTTON_Y = yBorder + 80;

	private final int MEDIUM_BUTTON_W = EASY_BUTTON_W;
	private final int MEDIUM_BUTTON_H = EASY_BUTTON_H;
	private final int MEDIUM_BUTTON_X = EASY_BUTTON_X;
	private final int MEDIUM_BUTTON_Y = EASY_BUTTON_Y + 80;

	private final int HARD_BUTTON_W = EASY_BUTTON_W;
	private final int HARD_BUTTON_H = EASY_BUTTON_H;
	private final int HARD_BUTTON_X = EASY_BUTTON_X;
	private final int HARD_BUTTON_Y = MEDIUM_BUTTON_Y + 80;

	/** Images for menu icons */
	public static final Image SINGLE_PLAYER =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "singleplayer_512.png");

	private lib.ButtonText[] buttons;


	// To allow the difficulty selection to work with multiple potential
	// game scenes, e.g. separate Demo and a Full Game
	private int scene;
	public final static int CREATE_DEMO = 0;

	/**
	 * Constructor
	 * @param main the main containing the scene
	 * @param scene the scene to create e.g. Demo
	 */
	protected DifficultySelect(int scene) {
		super();
		this.scene = scene;
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
	/**
	 * Initialises scene variables, buttons, text box.
	 */
	public void start() {
		buttons = new lib.ButtonText[3];
		lib.ButtonText.Action easy = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene) {
				case DifficultySelect.CREATE_DEMO:
					Main.setScene(SinglePlayerGame
							.createSinglePlayerGame(DifficultySetting.EASY));
					break;
				}
			}
		};
		buttons[0] = new lib.ButtonText("Easy", easy,
				EASY_BUTTON_X, EASY_BUTTON_Y, EASY_BUTTON_W, EASY_BUTTON_H, 40, -12);
		buttons[0].setInset(true);

		lib.ButtonText.Action medium = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					Main.setScene(SinglePlayerGame
							.createSinglePlayerGame(DifficultySetting.MEDIUM));
					break;
				}
			}
		};
		buttons[1] = new lib.ButtonText("Medium", medium,
				MEDIUM_BUTTON_X, MEDIUM_BUTTON_Y, MEDIUM_BUTTON_W, MEDIUM_BUTTON_H, 40, -12);
		buttons[1].setInset(true);

		lib.ButtonText.Action hard = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					Main.setScene(SinglePlayerGame
							.createSinglePlayerGame(DifficultySetting.HARD));
					break;
				}
			}
		};
		buttons[2] = new lib.ButtonText("Hard", hard,
				HARD_BUTTON_X, HARD_BUTTON_Y, HARD_BUTTON_W, HARD_BUTTON_H, 40, -12);
		buttons[2].setInset(true);

	}

	@Override
	/**
	 * Updates text box
	 */
	public void update(double timeDifference) {
	}

	/**
	 * Draws text box, buttons, and prints strings
	 */
	@Override
	public void draw() {
		graphics.setFont(Main.menuTitleFont);
		graphics.setColour(graphics.safetyOrange);
		graphics.rectangle(true, window.height()/3 - 40, yBorder - 2,
			(window.width() - (2 * window.height()/3 - 80)), 70);
		graphics.setColour(Color.black);
		graphics.drawScaled(SINGLE_PLAYER, window.height()/3 - 36, yBorder + 15, 0.0625);
		graphics.print("Single Player", window.height()/3,
				yBorder);
		graphics.setFont(Main.transSign);
		graphics.printRight("Solo", (window.width() - (window.height()/3) + 20), yBorder, 0, 0);
		graphics.printRight("Einzelspieler", (window.width() - (window.height()/3) + 20), yBorder + 20, 0, 0);
		graphics.printRight("Yksinpeli", (window.width() - (window.height()/3) + 20), yBorder + 40, 0, 0);
		graphics.setColour(Color.white);
		graphics.printRight("Facile", (window.width() - (window.height()/3) + 20), EASY_BUTTON_Y - 10, 0, 0);
		graphics.printRight("Leicht", (window.width() - (window.height()/3) + 20), EASY_BUTTON_Y + 5, 0, 0);
		graphics.printRight("Helppo", (window.width() - (window.height()/3) + 20), EASY_BUTTON_Y + 20, 0, 0);
		graphics.printRight("Moyen", (window.width() - (window.height()/3) + 20), MEDIUM_BUTTON_Y - 10, 0, 0);
		graphics.printRight("Mittlere", (window.width() - (window.height()/3) + 20), MEDIUM_BUTTON_Y + 5, 0, 0);
		graphics.printRight("Keskikokoinen", (window.width() - (window.height()/3) + 20), MEDIUM_BUTTON_Y + 20, 0, 0);
		graphics.printRight("Difficile", (window.width() - (window.height()/3) + 20), HARD_BUTTON_Y - 10, 0, 0);
		graphics.printRight("Schwer", (window.width() - (window.height()/3) + 20), HARD_BUTTON_Y + 5, 0, 0);
		graphics.printRight("Vaikea", (window.width() - (window.height()/3) + 20), HARD_BUTTON_Y + 20, 0, 0);

		for (lib.ButtonText b : buttons) {
			b.draw();
		}

	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {
	}

}
