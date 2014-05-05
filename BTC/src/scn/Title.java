package scn;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.newdawn.slick.Color;

import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

public class Title extends Scene {
	/** The path to the user manual */
	private final static String HELP_FILE_PATH = System.getProperty("user.dir") + "/user_manual.pdf";

	/** The 'beep' played as the radar makes a sweep */
	private audio.Sound beep;

	/** A List of buttons, to hold declared buttons in the scene */
	private lib.ButtonText[] buttons;

	/** Holds the angle to draw the radar sweep at */
	private double angle;

	/** The base image to provide powerup colours */
	public static final Image SINGLE_PLAYER =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "singleplayer_512.png");

	public static final Image MULTIPLAYER =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "multiplayer_512.png");

	public static final Image CREDITS =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "credits_512.png");

	public static final Image HELP =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "help_512.png");

	public static final Image EXIT =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "exit_512.png");
	/** Integer offset to centre the main menu */
	private int yBorder = (window.height() - 440) / 2 - 20;
	/**
	 * Constructor for the Title Scene.
	 * @param main
	 * 			the main holding the scene
	 */
	public Title() {
		super();
	}

	/**
	 * Initialises all objects, such as buttons and sound effects.
	 * <p>
	 * Only runs at the start of the scene.
	 * </p>
	 */
	@Override
	public void start() {
		graphics.setFont(Main.engSignFont);
		graphics.setColour(graphics.safetyOrange);
		beep = audio.newSoundEffect("sfx" + File.separator + "beep.ogg");
		beep.setVolume(0.2f);

		buttons = new lib.ButtonText[5];

		// Single player Button
		lib.ButtonText.Action demo = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.setScene(new DifficultySelect(DifficultySelect.CREATE_DEMO));
			}
		};
		buttons[0] = new lib.ButtonText("Single Player", demo,
				window.height()/3 - 40, yBorder + 80,
				window.width() - (2 * window.height()/3) + 80, 40, 40, -12);
		buttons[0].setInset(true);

		// Multi player Button
		lib.ButtonText.Action multiplayer = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.setScene(new Lobby());
			}
		};
		buttons[1] = new lib.ButtonText("Multiplayer", multiplayer,
				window.height()/3 - 40, yBorder + 160,
				window.width() - (2 * window.height()/3) + 80, 40, 40, -12);
		buttons[1].setInset(true);

		// Credits Button
		lib.ButtonText.Action credits = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.setScene(new Credits());
			}
		};

		buttons[2] = new lib.ButtonText("Credits", credits,
				window.height()/3 - 40, yBorder + 240,
				window.width() - (2 * window.height()/3) + 80, 40, 40, -12);
		buttons[2].setInset(true);

		// Help Button
		lib.ButtonText.Action help = new lib.ButtonText.Action() {
			@Override
			public void action() {
				try {
					Desktop.getDesktop().open(new File(HELP_FILE_PATH));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		buttons[3] = new lib.ButtonText("Information", help,
				window.height()/3 - 40, yBorder + 320,
				window.width() - (2 * window.height()/3) + 80, 40, 40, -12);
		buttons[3].setInset(true);

		// Exit Button
		lib.ButtonText.Action exit = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.quit();
			}
		};
		buttons[4] = new lib.ButtonText("Exit", exit,
				window.height()/3 - 40, yBorder + 400,
				window.width() - (2 * window.height()/3) + 80, 40, 40, -12);
		buttons[4].setInset(true);

		angle = 0;
	}

	/**
	 * Updates all objects in the title scene.
	 * @param timeDifference
	 * 			the time since the last update
	 */
	@Override
	public void update(double timeDifference) {
		angle += timeDifference * (3d / 4d); //increase the angle of the radar sweep

		//Check the angle of the radar sweep;
		//If approaching the BTC title string, play the beep
		double beepTimer = (angle * 4) + (Math.PI * 4 / 5);
		beepTimer %= (2 * Math.PI);
		if ( beepTimer <= 0.1 ) {
			//playSound(beep); <- Driving me absolutely crazy so it can go for now.
		}
	}

	/**
	 * Handles drawing of the scene.
	 * <p>
	 * Calls {@link #drawRadar()} and {@link #drawMenu()} to
	 * draw elements of the scene.
	 * </p>
	 */
	@Override
	public void draw() {
		drawMenu();
	}

	/**
	 * Draws menu boxes, boxes around buttons, and strings.
	 */
	private void drawMenu() {
		graphics.setColour(graphics.safetyOrange);
		graphics.setFont(Main.menuTitleFont);
		graphics.rectangle(true, window.height()/3 - 40, yBorder - 2, (window.width() - (2 * window.height()/3) + 80), 70);
		graphics.setColour(Color.black);
		graphics.print("Fly Hard", window.height()/3, yBorder);
		graphics.setFont(Main.transSign);
		graphics.setColour(Color.white);
		graphics.printRight("Héros de l'avion", (window.width() - (window.height()/3) + 20), yBorder, 0, 0);
		graphics.printRight("Flugzeug Flugzeug Revolution", (window.width() - (window.height()/3) + 20), yBorder + 20, 0, 0);
		graphics.printRight("Tarina ja Kaksi Richards", (window.width() - (window.height()/3) + 20), yBorder + 40, 0, 0);
		graphics.printRight("Solo", (window.width() - (window.height()/3) + 20) - 2, yBorder + 70, 0, 0);
		graphics.printRight("Einzelspieler", (window.width() - (window.height()/3) + 20) - 4, yBorder + 85, 0, 0);
		graphics.printRight("Yksinpeli", (window.width() - (window.height()/3) + 20) - 4, yBorder + 100, 0, 0);
		graphics.printRight("Multijouer", (window.width() - (window.height()/3) + 20) - 4, yBorder + 150, 0, 0);
		graphics.printRight("Mehrspieler", (window.width() - (window.height()/3) + 20) - 4, yBorder + 165, 0, 0);
		graphics.printRight("Moninpeli", (window.width() - (window.height()/3) + 20) - 4, yBorder + 180, 0, 0);
		graphics.printRight("Cr\u00E9dits", (window.width() - (window.height()/3) + 20) - 4, yBorder + 230, 0, 0);
		graphics.printRight("Credits", (window.width() - (window.height()/3) + 20) - 4, yBorder + 245, 0, 0);
		graphics.printRight("Ov", (window.width() - (window.height()/3) + 20) - 4, yBorder + 260, 0, 0);
		graphics.printRight("Aider", (window.width() - (window.height()/3) + 20) - 4, yBorder + 310, 0, 0);
		graphics.printRight("Hilfe", (window.width() - (window.height()/3) + 20) - 4, yBorder + 325, 0, 0);
		graphics.printRight("Auttaa", (window.width() - (window.height()/3) + 20) - 4, yBorder + 340, 0, 0);
		graphics.printRight("Sortie", (window.width() - (window.height()/3) + 20) - 4, yBorder + 390, 0, 0);
		graphics.printRight("Ausfahrt", (window.width() - (window.height()/3) + 20) - 4, yBorder + 405, 0, 0);
		graphics.printRight("Postuminen", (window.width() - (window.height()/3) + 20) - 4, yBorder + 420, 0, 0);
		graphics.setColour(graphics.safetyOrange);
		graphics.drawScaled(SINGLE_PLAYER, window.height()/3 - 40, yBorder + 80, 0.0625);
		graphics.drawScaled(MULTIPLAYER, window.height()/3 - 40, yBorder + 160, 0.0625);
		graphics.drawScaled(CREDITS, window.height()/3 - 40, yBorder + 240, 0.0625);
		graphics.drawScaled(HELP, window.height()/3 - 40, yBorder + 320, 0.0625);
		graphics.drawScaled(EXIT, window.height()/3 - 40, yBorder + 400, 0.0625);

		// Draw Buttons
		for (lib.ButtonText b : buttons) b.draw();
		graphics.setFont(Main.mainFont);
//		graphics.setColour(graphics.safetyOrange);
//		graphics.line(window.height(), window.height()/2 + 60, window.width() - 16, window.height()/2 + 60);
//		graphics.line(window.height(), window.height()/2 + 90, window.width() - 16, window.height()/2 + 90);
//		graphics.line(window.height(), window.height()/2 + 120, window.width() - 16, window.height()/2 + 120);
//		graphics.line(window.height(), window.height()/2 + 150, window.width() - 16, window.height()/2 + 150);
//		graphics.line(window.height(), window.height()/2 + 180, window.width() - 16, window.height()/2 + 180);
//		graphics.line(window.height(), window.height()/2 + 210, window.width() - 16, window.height()/2 + 210);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(int key, int x, int y) {}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Causes a button to act if clicked by any mouse key.
	 * </p>
	 */
	@Override
	public void mouseReleased(int key, int mx, int my) {
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(mx, my)) {
				b.act();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyPressed(int key) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyReleased(int key) {
		// Exit if ESC key pressed
		// Added for testing TODO - remove this for release
		if (key == input.KEY_ESCAPE) {
			Main.quit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
