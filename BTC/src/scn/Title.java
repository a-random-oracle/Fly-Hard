package scn;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.newdawn.slick.Color;

import lib.jog.audio;
import lib.jog.audio.Sound;
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
		graphics.setFont(Main.engSign);
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
				window.height()/3 , 100,
				window.width(), 40, 0, -12);

		// Multi player Button
		lib.ButtonText.Action multiplayer = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.setScene(new Lobby());
			}
		};
		buttons[1] = new lib.ButtonText("Multiplayer", multiplayer,
				window.height(), window.height()/2 + 96,
				window.width(), 40, 0, -12);

		// Credits Button
		lib.ButtonText.Action credits = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.setScene(new Credits());
			}
		};
		buttons[2] = new lib.ButtonText("Credits", credits,
				window.height(), window.height()/2 + 126,
				window.width(), 40, 0, -12);

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
				window.height(), window.height()/2 + 156,
				window.width() - window.height(), 24, 8, 6);
		
		// Exit Button
		lib.ButtonText.Action exit = new lib.ButtonText.Action() {
			@Override
			public void action() {
				Main.quit();
			}
		};
		buttons[4] = new lib.ButtonText("Exit", exit,
				window.height(), window.height()/2 + 186,
				window.width() - window.height(), 24, 8, 6);

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
		//drawRadar();
		drawMenu();
	}
	
	/**
	 * Draws the radar arc and title string.
	 */
	private void drawRadar() {
		// Radar
		// set of circles for radar 'screen'
		graphics.setColour(graphics.safetyOrange);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/2 - 32, 100);
		graphics.setColour(0, 128, 0, 32);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/3, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/4 - 16, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/9, 100);
		graphics.circle(false, window.height()/2, window.height()/2, 2, 100);
		graphics.setColour(graphics.safetyOrange);
		// sweep of radar
		double radarAngle = (angle * 4) % (2 * Math.PI);
		int w = (int)( Math.cos(radarAngle) * (window.height()/2 - 32) );
		int h = (int)( Math.sin(radarAngle) * (window.height()/2 - 32) );
		graphics.line(window.height()/2, window.height()/2, window.height()/2 + w, window.height()/2 + h);
		graphics.setColour(0, 128, 0, 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -8 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -7 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -6 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -5 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -4 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -3 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -2 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -1 * Math.PI / 8);
		// Title
		String title = "Fly-Hard";
		int titleLength = title.length();
		// fades title string's characters over time
		// characters brighten when the sweep passes over them
		double a = radarAngle + (Math.PI * 6 / 7);
		for (int i = 0; i < titleLength; i++) {
			a -= Math.PI / 32;
			double opacity = a %= (2 * Math.PI);
			opacity *= 256 / (2 * Math.PI);
			opacity = 256 - opacity;
			opacity %= 256;
			graphics.setColour(0, 128, 0, opacity);
			int xPos = (window.height() / 2) - ((titleLength * 14) / 2);
			int yPos = (window.height() / 3);
			graphics.print(title.substring(i, i+1), xPos + i * 14, yPos, 1.8);
		}
		
		String subtitle = "#goahardorgoahome";
		int subtitleLength = subtitle.length();
		a = radarAngle + (Math.PI * 2 / 3);
		for (int i = 0; i < subtitleLength; i++) {
			a -= Math.PI / 32;
			double opacity = a %= (2 * Math.PI);
			opacity *= 256 / (2 * Math.PI);
			opacity = 256 - opacity;
			opacity %= 256;
			graphics.setColour(graphics.safetyOrange);
			graphics.setFont(Main.menuTitle);
			int xPos = (window.height() / 2) - ((subtitleLength * 14) / 2);
			int yPos = (window.height() / 3) + 20;
			graphics.print(subtitle.substring(i, i+1), xPos + i * 14, yPos, 1.8);
		}
	}

	/**
	 * Draws menu boxes, boxes around buttons, and strings.
	 */
	private void drawMenu() {
		// Draw Extras e.g. Date, Time, Credits
		graphics.setColour(graphics.safetyOrange);
//		graphics.line(window.height(), 16, window.height(), window.height() - 16);
//		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
//		java.text.DateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
//		java.util.Date date = new java.util.Date();
//		graphics.setFont(Main.mainFont);
//		graphics.print(dateFormat.format(date), window.height() + 8, 20);
//		graphics.print(timeFormat.format(date), window.height() + 8, 36);
//		graphics.line(window.height(), 48, window.width() - 16, 48);
//		graphics.print("Created by:   Team FLR", window.height() + 8, 56);
//		graphics.print("Extended by:  Team MQV", window.height() + 8, 68);
//		graphics.print("Perfected by: Team GOA", window.height() + 8, 80);

		graphics.setFont(Main.menuTitle);
		graphics.print("Fly Hard", 260, 40);
		graphics.setFont(Main.transSign);
		graphics.setColour(Color.white);
//		graphics.printRight("Avion Héros", window.width() - 200, 40, 100, 20);
		graphics.printRight("Avion Hèros", window.width() - 200, 40, 200, 20);
		graphics.printRight("Flugzeug Flugzeug Revolution", window.width() - 200, 60, 200, 20);
		graphics.printRight("'n Verhaal Van Twee Richards", window.width() - 200, 80, 200, 20);
		// Draw Buttons
		for (lib.ButtonText b : buttons) b.draw();
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
