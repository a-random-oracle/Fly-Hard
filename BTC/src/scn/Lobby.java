package scn;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import net.InstructionHandler;
import net.NetworkManager;

import org.newdawn.slick.Color;

import btc.Main;
import cls.InputBox;
import cls.Vector;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;
import lib.ButtonText;

public class Lobby extends Scene {

	/** The width of the create game button */
	private static final int CREATE_BUTTON_W = 200;

	/** The height of the create game button */
	private static final int CREATE_BUTTON_H = 32;

	/** Coordinates for the top left of the game selection table */
	private static final Vector tableTopLeft = new Vector(0.07, 0.30, 0, true);

	/** Coordinates for the top right of the game selection table */
	private static final Vector tableTopRight = new Vector(0.60, 0.30, 0, true);

	/** Coordinates for the bottom right of the game selection table */
	private static final Vector tableBottomRight = new Vector(0.60, 0.95, 0, true);
	
	/** Coordinates for the top left of the high scores table */
	private static final Vector scoresTopLeft = new Vector(0.65, 0.30, 0, true);

	/** Coordinates for the top right of the high scores table */
	private static final Vector scoresTopRight = new Vector(0.93, 0.30, 0, true);

	/** The coordinates of the input box */
	private static final Vector nameEntryBoxPos = new Vector((1d/3d), 0.15, 0, true);

	/** The height to draw table rows */
	private static final double rowHeight =
			(tableBottomRight.getY() - tableTopRight.getY()) / 15;
	
	/** The time since the list of available players was last updated */
	private double timeSincePlayerUpdate = 1;
	
	/** The time since the list of high scores was last updated */
	private double timeSinceScoreUpdate = 10;

	/** The time since the players waiting string was last updated */
	private double timeSinceWaitingUpdate = 0.5;

	/** The time since the list of instructions was last checked */
	private double timeSinceStartGameUpdate = 0.1;
	
	/** The map of available players */
	private LinkedHashMap<Integer, String> availablePlayers;
	
	/** The map of high scores */
	private TreeMap<Long, ArrayList<String>> highScores;
	
	/** The input box used for name entry */
	private static InputBox nameEntryBox = new InputBox(Color.white, Color.darkGray,
			(int) nameEntryBoxPos.getX() + Game.getXOffset(),
			(int) nameEntryBoxPos.getY() + Game.getYOffset(),
			200, 30, true);

	/** The button used to create a new game. The player then becomes a host. */
	private ButtonText createGameButton;

	/** The array of button which will cause the player to join a multiplayer game */
	private LinkedList<ButtonText> joinButtons = new LinkedList<ButtonText>();

	/** Whether the player has created a game */
	private boolean waitingForOpponent = false;

	/** The dynamic string of dots to display after text */
	private String waitingForOpponentDots = "";
	
	private int yBorder = (window.height() - 440) / 2 - 20;

	/** Declaring location of Multiplayer icon/graphics */
	public static final Image MULTIPLAYER =
			graphics.newImage("gfx" + File.separator + "pup"
					+ File.separator + "multiplayer_512.png");


	/**
	 * Constructs a new lobby.
	 * <p>
	 * The lobby is the waiting are in which players who wish to
	 * play against other players can select who they wish to
	 * play against, or to become the host for a game.
	 * </p>
	 */
	protected Lobby() {
		super();
	}


	@Override
	public void start() {
		// Reset the network manager
		NetworkManager.resetConnectionProperties();
		
		// Implement the action that occurs upon clicking the create game button
		ButtonText.Action createGame = new ButtonText.Action() {
			@Override
			public void action() {
				// Set the player as a host
				NetworkManager.setName(nameEntryBox.getText());
				NetworkManager.setHost(true);
				setWaitingForOpponent(true);
			}
		};

		createGameButton = new ButtonText("Create Game", Main.menuMainFont, createGame,
				(int) (3 * window.width()/7),
				(int) (nameEntryBoxPos.getY() + Game.getYOffset() + 3),
				CREATE_BUTTON_W, CREATE_BUTTON_H, 0, 0, 2);
	}

	@Override
	public void update(double timeDifference) {
		// Increment the time before the next data send
		timeSincePlayerUpdate += timeDifference;
		timeSinceScoreUpdate += timeDifference;
		timeSinceWaitingUpdate += timeDifference;
		timeSinceStartGameUpdate += timeDifference;

		// Update the map of available players approximately every second
		if (timeSincePlayerUpdate > 1) {
			// Update the list of players
			updateAvailablePlayers();

			// Reset the time
			timeSincePlayerUpdate = 0;
		}
		
		// Update the list of high scores approximately every 10 seconds
		if (timeSinceScoreUpdate > 2) {
			// Update the list of players
			updateHighScores();

			// Reset the time
			timeSinceScoreUpdate = 0;
		}

		// Update dots on strings
		if (timeSinceWaitingUpdate > 0.25) {
			if (waitingForOpponentDots.length() == 3) {
				waitingForOpponentDots = "";
			} else {
				waitingForOpponentDots += ".";
			}

			// Reset the time
			timeSinceWaitingUpdate = 0;
		}

		// Check for instructions approximately every tenth of a second
		if (timeSinceStartGameUpdate > 0.1) {
			// Process queued instructions
			String waitingInstructions = InstructionHandler.getMessages();

			if (waitingInstructions != null) {
				if (waitingInstructions.contains("START_GAME")) {
					setWaitingForOpponent(false);
					NetworkManager.setHost(false);
					InstructionHandler.handleInstruction(waitingInstructions);
				}
			}

			// Reset the time
			timeSinceStartGameUpdate = 0;
		}
		
		// Update the name entry box
		nameEntryBox.update(timeDifference);
	}

	/**
	 * Checks the server to get any updates to the map of available players.
	 */
	private void updateAvailablePlayers() {
		// Clear the map of available players
		availablePlayers = new LinkedHashMap<Integer, String>();
				
		// Get the available opponents from the server
		String openConnectionsString = NetworkManager
				.postMessage("GET_OPEN_CONNECTIONS");
		
		if (openConnectionsString != null) {
			String[] openConnections = openConnectionsString.split("#");

			// Format the open connections into a hashmap
			String[] currentEntry;
			for (int i = 0; i < openConnections.length; i++) {
				if (!openConnections[i].equals("INVALID_CLIENT")
						&& !openConnections[i].equals("INVALID_REQUEST")
						&& !openConnections[i].contains("NO_CONNECTIONS")) {
					currentEntry = openConnections[i].split("=");

					if (currentEntry != null && currentEntry[0] != null) {
						try {
							int id = Integer.valueOf(currentEntry[0]);

							switch (currentEntry.length) {
							case 1:
								availablePlayers.put(id, "ANON");
								break;
							case 2:
								availablePlayers.put(id, currentEntry[1]);
								break;
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}

			// Get the available client IDs
			Integer[] clientIDs = getAvailablePlayerIDs();

			// Clear the array of available games
			joinButtons.clear();

			for (int i = 0; i < availablePlayers.size(); i++) {
				ButtonText.Action currentAction =
						createPlayerButtonAction(clientIDs[i]);
				
				// Create a new join button for the available connection
				ButtonText joinButton = new ButtonText("Join Game", Main.menuMainFont,
						currentAction,
						(int) (tableTopRight.getX() - 5 + Game.getXOffset()),
						(int) (tableTopLeft.getY()
								+ Game.getYOffset() + ((i + 0.33) * rowHeight) + 4),
						(int) ((tableTopRight.getX() - tableTopLeft.getX())
								* (1d/8d)),
						(int) (rowHeight * (3d/4d)), 0, 0);
				
				// Set the join button's availability
				// The buttons should be disabled if the player is waiting
				// for an opponent
				joinButton.setAvailability(!waitingForOpponent);
				
				// Add the join button to the list of join buttons
				joinButtons.add(joinButton);
			}
		}
	}
	
	/**
	 * Checks the server to get any updates to the map of high scores.
	 */
	private void updateHighScores() {
		// Clear the list of high scores
		highScores = new TreeMap<Long, ArrayList<String>>();

		// Get the collapsed list of high scores from the server
		String collapsedHighScores = NetworkManager
				.postMessage("GET_HIGH_SCORES");

		if (collapsedHighScores != null) {
			String[] highScoresList = collapsedHighScores.split("#");

			// Format the open connections into a hashmap
			String[] currentEntry;
			for (int i = 0; i < highScoresList.length; i++) {
				if (!highScoresList[i].equals("INVALID_CLIENT")
						&& !highScoresList[i].equals("INVALID_REQUEST")
						&& !highScoresList[i].contains("NO_HIGH_SCORES")) {
					currentEntry = highScoresList[i].split("=");

					if (currentEntry != null && currentEntry.length == 2
							&& currentEntry[0] != null
							&& currentEntry[1] != null) {
						try {
							String name = currentEntry[0];
							long score = Long.valueOf(currentEntry[1]);

							if (!highScores.containsKey(score)) {
								// If the score is not already in the list
								// of high scores, create a new list of names
								// with that score, add in the specified name,
								// and add the list and score to the hash map
								ArrayList<String> newNameList = new ArrayList<String>();
								newNameList.add(name);
								highScores.put(score, newNameList);
							} else {
								// Otherwise, add the name to the list
								// of names with the score
								highScores.get(score).add(name);
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public void draw() {
		
		graphics.setColour(graphics.safetyOrange);
		graphics.setFont(Main.menuTitleFont);
		graphics.rectangle(true, window.height()/3 - 40, 10, (window.width() - (2 * window.height()/3)) + 80, 70);
		graphics.setColour(Color.black);
		graphics.print("Multiplayer", window.height()/3, 12);
//		graphics.setColour(graphics.safetyOrange);
		graphics.drawScaled(MULTIPLAYER, window.height()/3 - 36, 25, 0.0625);

		
		graphics.setFont(Main.menuMainFont);
		graphics.setColour(Color.white);
		// Draw the name entry label
		graphics.printRight("Enter Name: ", (nameEntryBoxPos.getX()
				- (nameEntryBox.getWidth() / 2) + Game.getXOffset()),
				(nameEntryBoxPos.getY() + Game.getYOffset() + 3), 2, 0);
		
		// Draw the name entry input box
		nameEntryBox.draw();
		
		// Draw the create game button
		createGameButton.draw();

		// If the player is waiting for an opponent, print the waiting for
		// opponent string (with cycling dots)
		if (waitingForOpponent) {
			graphics.setColour(255, 255, 255);
			
			graphics.printCentred(waitingForOpponentDots.replace(".", " ")
					+ " Waiting for an opponent to join " + waitingForOpponentDots,
					nameEntryBoxPos.getX(),
					(nameEntryBoxPos.getY() + Game.getYOffset() + 30),
					2, 0);
		}

		// Draw the available games table
		drawTable();
	}

	/**
	 * Draws the table of available games.
	 */
	public void drawTable() {
		// Draw the headings in white
		graphics.setColour(Color.white);

		// Draw the host name column label
		graphics.print("Host Name", tableTopLeft.getX() + Game.getXOffset() + 5,
				tableTopLeft.getY() + Game.getYOffset() - 30, 1);

		// Draw the description column label
		graphics.printCentred("Status", tableTopLeft.getX() + Game.getXOffset(),
				tableTopLeft.getY() + Game.getYOffset() - 30,
				1, (tableTopRight.getX() - tableTopLeft.getX()));
		
		// Draw the action column label
		graphics.printRight("Action", tableTopRight.getX() + Game.getXOffset() - 5,
				tableTopLeft.getY() + Game.getYOffset() - 30, 1, 0);
		
		// Draw the table in safety orange
		graphics.setColour(graphics.safetyOrange);
		
		// Draw the table border
		graphics.rectangle(false,
				(tableTopLeft.getX() + Game.getXOffset()),
				(tableTopLeft.getY() + Game.getYOffset()),
				(tableTopRight.getX() - tableTopLeft.getX()),
				(tableBottomRight.getY() - tableTopRight.getY()));
		
		

		if (availablePlayers != null && availablePlayers.size() > 0) {
			// Draw vertical lines below each player's row
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.line((tableTopLeft.getX() + Game.getXOffset()),
						(tableTopLeft.getY() + Game.getYOffset()
								+ ((i + 1) * rowHeight)),
						(tableTopRight.getX() + Game.getXOffset()),
						(tableTopLeft.getY() + Game.getYOffset()
								+ ((i + 1) * rowHeight)));
			}

			graphics.setColour(Color.white);
			
			Integer[] playerIDs = getAvailablePlayerIDs();

			// Draw the player's names
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.print(availablePlayers.get(playerIDs[i]),
						(tableTopLeft.getX() + Game.getXOffset()) + 5,
						(tableTopLeft.getY() + Game.getYOffset()
								+ ((i + 0.33) * rowHeight)) - 10);
			}

			// Draw the player's descriptions
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.printCentred(waitingForOpponentDots.replace(".",  " ")
						+ " Waiting for an opponent " + waitingForOpponentDots,
						tableTopLeft.getX() + Game.getXOffset(),
						(tableTopLeft.getY() + Game.getYOffset()
								+ ((i + 0.33) * rowHeight) - 10),
						1, (tableTopRight.getX() - tableTopLeft.getX()));
			}
			
			// Draw the play game buttons
			for (int i = 0; i < joinButtons.size(); i++) {
				joinButtons.get(i).drawRight();
			}
		}
		
		// Draw the title in safety orange
		graphics.setColour(graphics.safetyOrange);
		
		// Draw the title in super flightstrip font
		graphics.setFont(Main.flightstripFontSuper);
		
		// Draw the high scores table heading
		graphics.printCentred("High Scores",
				(scoresTopLeft.getX() + scoresTopRight.getX())/2,
				window.height()/7
				+ ((scoresTopLeft.getY() - nameEntryBoxPos.getY()) / 2)
				+ Game.getYOffset(),
				2, 0);
		
		// Draw the headings in white
		graphics.setColour(Color.white);
		graphics.setFont(Main.menuMainFont);
		
		// Draw the rank column label
		graphics.print("Rank", scoresTopLeft.getX() + Game.getXOffset() + 5,
				scoresTopLeft.getY() + Game.getYOffset() - 30, 1);

		// Draw the name column label
		graphics.print("Name", scoresTopLeft.getX() + Game.getXOffset() + 50,
				scoresTopLeft.getY() + Game.getYOffset() - 30, 1);

		// Draw the score column label
		graphics.printRight("Score", scoresTopRight.getX() + Game.getXOffset() - 5,
				scoresTopLeft.getY() + Game.getYOffset() - 30, 1, 0);
		
		// Draw the table in safety orange
		graphics.setColour(graphics.safetyOrange);
		
		// Draw the high scores table border
		graphics.rectangle(false,
				(scoresTopLeft.getX() + Game.getXOffset()),
				(scoresTopLeft.getY() + Game.getYOffset()),
				(scoresTopRight.getX() - scoresTopLeft.getX()),
				(tableBottomRight.getY() - scoresTopRight.getY()));

		if (highScores != null && highScores.size() > 0) {
			int i = 0;
			for (Long score : highScores.descendingKeySet()) {
				for (String name : highScores.get(score)) {
					
					graphics.setColour(Color.white);
					// Draw the numbers
					graphics.printRight(String.valueOf(i + 1),
							(scoresTopLeft.getX() + Game.getXOffset()) + 35,
							(scoresTopLeft.getY() + Game.getYOffset()
									+ ((i + 0.33) * rowHeight)) - 10, 1, 0);
					
					// Draw the player's names
					graphics.print(name,
							(scoresTopLeft.getX() + Game.getXOffset()) + 50,
							(scoresTopLeft.getY() + Game.getYOffset()
									+ ((i + 0.33) * rowHeight)) - 10);

					// Draw the scores
					graphics.printRight(String.valueOf(score),
							scoresTopRight.getX() + Game.getXOffset() - 5,
							(scoresTopLeft.getY() + Game.getYOffset()
									+ ((i + 0.33) * rowHeight)) - 10, 1, 0);

					graphics.setColour(graphics.safetyOrange);
					// Draw vertical lines below each score
					graphics.line((scoresTopLeft.getX() + Game.getXOffset()),
							(scoresTopLeft.getY() + Game.getYOffset()
									+ ((i + 1) * rowHeight)),
									(scoresTopRight.getX() + Game.getXOffset()),
									(scoresTopLeft.getY() + Game.getYOffset()
											+ ((i + 1) * rowHeight)));

					i++;
				}
			}
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	/**
	 * Causes a button to act if the mouse is released over it.
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		// Allow the name entry box to access mouse events
		nameEntryBox.mouseReleased(key, x, y);
					
		if (key == input.MOUSE_LEFT) {
			// Cause the create game button to fire when clicked
			if (createGameButton.isMouseOver(x, y)) {
				if (nameEntryBox.isEmpty()) {
					nameEntryBox.alert(200);
				} else{
					createGameButton.act();
				}
			}

			// Cause the join game buttons to fire when clicked
			if (joinButtons != null) {
				for (ButtonText b : joinButtons) {
					if (b.isMouseOverRight(x, y)) {
						if (nameEntryBox.isEmpty()) {
							nameEntryBox.alert(200);
						} else{
							b.act();;
						}
					}
				}
			}
		}
	}

	@Override
	public void keyPressed(int key) {
		// Pass key input to the name entry box
		nameEntryBox.keyPressed(key);
	}

	/**
	 * Quits back to title scene on escape button.
	 */
	@Override
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			Main.closeScene();
		}
	}

	@Override
	public void close() {
		setWaitingForOpponent(false);
		
		NetworkManager.setHost(false);
		NetworkManager.postMessage("LEAVING_LOBBY");
	}

	@Override
	public void playSound(Sound sound) {}

	/**
	 * Toggles whether the player is waiting for an opponent.
	 * <p>
	 * This toggles:
	 * <ul>
	 * <li>The waiting for opponent attribute</li>
	 * <li>The availability of the name entry box</li>
	 * <li>The availability of the create game button</li>
	 * </ul>
	 * </p>
	 * @param isWaiting - <code>true</code> if the player is
	 * 						waiting for an opponent, otherwise
	 * 						<code>false</code>
	 */
	private void setWaitingForOpponent(boolean isWaiting) {
		if (isWaiting) {
			// Set waiting for opponent
			waitingForOpponent = true;

			// Disable the name entry box
			nameEntryBox.setEnabled(false);

			// Disable the create game button
			createGameButton.setAvailability(false);
			
			// Disable the join game buttons
			for (ButtonText joinButton : joinButtons) {
				joinButton.setAvailability(false);
			}
		} else {
			// Clear waiting for opponent
			waitingForOpponent = false;

			// Enable the name entry box
			nameEntryBox.setEnabled(true);

			// Enable the create game button
			createGameButton.setAvailability(true);
			
			// Enable the join game buttons
			for (ButtonText joinButton : joinButtons) {
				joinButton.setAvailability(true);
			}
		}
	}
	
	/**
	 * Selects a game to play.
	 * @param clientID - the ID of the client to connect to
	 */
	private void selectGame(int clientID) {
		// Send a JOIN instruction to the server, passing the ID
		// of the game to connect to as a parameter
		NetworkManager.setName(nameEntryBox.getText());
		NetworkManager.setHost(false);
		NetworkManager.postMessage("JOIN:" + clientID);
	}

	/**
	 * Creates a new button action to connect to a player.
	 * @param id - the ID of the player which this button
	 * 			will connect to
	 * @return a button action which will cause the player to open
	 * 			a connection to the client with the specified ID
	 */
	private ButtonText.Action createPlayerButtonAction(final int id) {
		return new ButtonText.Action() {
			@Override
			public void action() {
				selectGame(id);
				System.out.println("Selected client: " + id);
			}
		};
	}

	/**
	 * Gets a list of all available player IDs from the
	 * available players map.
	 * @return a list of the IDs of all available players
	 */
	private Integer[] getAvailablePlayerIDs() {
		if (availablePlayers != null && availablePlayers.keySet() != null) {
			return availablePlayers.keySet()
					.toArray(new Integer[availablePlayers.size()]);
		} else {
			return null;
		}
	}

}
