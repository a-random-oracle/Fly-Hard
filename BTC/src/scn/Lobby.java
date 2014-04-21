package scn;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import net.InstructionHandler;
import net.NetworkManager;

import org.newdawn.slick.Color;

import btc.Main;
import cls.InputBox;
import cls.Vector;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.audio.Sound;
import lib.ButtonText;

public class Lobby extends Scene {

	private static final int CREATE_BUTTON_W = 200;

	private static final int CREATE_BUTTON_H = 32;

	/** Coordinates for the top left of the game selection table */
	private static final Vector tableTopLeft = new Vector(0.05, 0.2, 0, true);

	/** Coordinates for the top right of the game selection table */
	private static final Vector tableTopRight = new Vector(0.95, 0.2, 0, true);

	/** Coordinates for the bottom right of the game selection table */
	private static final Vector tableBottomRight = new Vector(0.95, 0.95, 0, true);

	/** The coordinates of the input box */
	private static final Vector nameEntryBoxPos = new Vector(0.5, 0.02, 0, true);

	/** The height to draw table rows */
	private static final double rowHeight =
			(tableBottomRight.getY() - tableTopRight.getY()) / 15;
	
	/** The time since the list of available players was last updated */
	private double timeSincePlayerUpdate = 1;

	/** The time since the players waiting string was last updated */
	private double timeSinceWaitingUpdate = 0.5;

	/** The time since the list of instructions was last checked */
	private double timeSinceStartGameUpdate = 0.1;
	
	/** The map of available players */
	private LinkedHashMap<Integer, String> availablePlayers;
	
	/** The input box used for name entry */
	private InputBox nameEntryBox;

	/** The button used to create a new game. The player then becomes a host. */
	private ButtonText createGameButton;

	/** The array of button which will cause the player to join a multiplayer game */
	private LinkedList<ButtonText> joinButtons = new LinkedList<ButtonText>();

	/** has the player created a game? If so, they are waiting for an opponent*/
	private boolean isWaitingForOpponent = false;

	private String waitingForOpponentDots = "";


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
		// Create an input box for name entry and position it above
		// the game selection table
		nameEntryBox = new InputBox(Color.white, Color.darkGray,
				(int) nameEntryBoxPos.getX() + Game.X_OFFSET,
				(int) nameEntryBoxPos.getY() + Game.Y_OFFSET,
				200, 23, true);

		// Implement the action that occurs upon clicking the create game button
		ButtonText.Action createGame = new ButtonText.Action() {
			@Override
			public void action() {
				isWaitingForOpponent = true;
				nameEntryBox.setEnabled(false);
				createGameButton.setAvailability(false);
				NetworkManager.postMessage("SET_NAME:" + nameEntryBox.getText()
						+ ";SET_HOST");
			}
		};

		createGameButton = new ButtonText("Create Game", createGame,
				(int) (nameEntryBoxPos.getX()
						+ (nameEntryBox.getWidth() / 2) + Game.X_OFFSET + 50),
				(int) (nameEntryBoxPos.getY() + Game.Y_OFFSET + 3),
				CREATE_BUTTON_W, CREATE_BUTTON_H, 0, 0, 2);
	}

	@Override
	public void update(double timeDifference) {
		// Increment the time before the next data send
		timeSincePlayerUpdate += timeDifference;
		timeSinceWaitingUpdate += timeDifference;
		timeSinceStartGameUpdate += timeDifference;

		// Update the map of available players approximately every second
		if (timeSincePlayerUpdate > 1) {
			// Update the list of players
			updateAvailablePlayers();

			// Reset the time
			timeSincePlayerUpdate = 0;
		}

		// Update dots on strings
		if (timeSinceWaitingUpdate > 0.5) {
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
				InstructionHandler.handleInstruction(waitingInstructions);
			}

			// Reset the time
			timeSinceStartGameUpdate = 0;
		}

		// Activate the create button only when a name has been entered
		// into the input box
		/*if (!nameEntryBox.isEmpty()) {
			createGameButton.setAvailability(true);
		} else {
			createGameButton.setAvailability(false);
		}*/ //XXX
		
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
				if (!openConnections[i].equals("NO_CONNECTIONS")) {
					currentEntry = openConnections[i].split(":");

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

				joinButtons.add(new ButtonText("Join Game",
						currentAction,
						(int) (tableTopRight.getX() - 5 + Game.X_OFFSET),
						(int) (tableTopLeft.getY()
								+ Game.Y_OFFSET + ((i + 0.33) * rowHeight)),
						(int) ((tableTopRight.getX() - tableTopLeft.getX())
								* (1d/8d)),
						(int) (rowHeight * (3d/4d)), 0, 0));
			}

			// If the player is waiting for an opponent, disable the join game
			// buttons
			if (isWaitingForOpponent) {
				for (ButtonText joinButton : joinButtons) {
					joinButton.setAvailability(false);
				}
			}
		}
	}

	@Override
	public void draw() {
		// Draw the name entry label
		graphics.printRight("Enter Name: ", (nameEntryBoxPos.getX()
				- (nameEntryBox.getWidth() / 2) + Game.X_OFFSET),
				(nameEntryBoxPos.getY() + Game.Y_OFFSET + 3), 2, 0);
		
		// Draw the name entry input box
		nameEntryBox.draw();
		
		// Draw the create game button
		createGameButton.draw();

		// If the player is waiting for an opponent, print the waiting for
		// opponent string (with cycling dots)
		if (isWaitingForOpponent) {
			graphics.setColour(255, 255, 255);
			
			graphics.printCentred(waitingForOpponentDots.replace(".", " ")
					+ " Waiting for an opponent to join " + waitingForOpponentDots,
					tableTopLeft.getX() + Game.X_OFFSET,
					nameEntryBoxPos.getY()
					+ ((tableTopLeft.getY() - nameEntryBoxPos.getY()) / 2)
					+ Game.Y_OFFSET,
					2, (tableTopRight.getX() - tableTopLeft.getX()));
		}

		// Draw the available games table
		drawTable();
	}

	public void drawTable() {
		// Draw the table in white
		graphics.setColour(255, 255, 255);

		// Draw the host name column label
		graphics.print("Host Name", tableTopLeft.getX() + Game.X_OFFSET + 5,
				tableTopLeft.getY() + Game.Y_OFFSET - 15, 1);

		// Draw the description column label
		graphics.printCentred("Status", tableTopLeft.getX() + Game.X_OFFSET,
				tableTopLeft.getY() + Game.Y_OFFSET - 15,
				1, (tableTopRight.getX() - tableTopLeft.getX()));
		
		// Draw the action column label
		graphics.printRight("Action", tableTopRight.getX() + Game.X_OFFSET - 5,
				tableTopLeft.getY() + Game.Y_OFFSET - 15, 1, 0);

		// Draw the table outside border
		graphics.rectangle(false,
				(tableTopLeft.getX() + Game.X_OFFSET),
				(tableTopLeft.getY() + Game.Y_OFFSET),
				(tableTopRight.getX() - tableTopLeft.getX()),
				(tableBottomRight.getY() - tableTopRight.getY()));

		if (availablePlayers != null && availablePlayers.size() > 0) {
			// Draw vertical lines below each player's row
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.line((tableTopLeft.getX() + Game.X_OFFSET),
						(tableTopLeft.getY() + Game.Y_OFFSET
								+ ((i + 1) * rowHeight)),
						(tableTopRight.getX() + Game.X_OFFSET),
						(tableTopLeft.getY() + Game.Y_OFFSET
								+ ((i + 1) * rowHeight)));
			}

			Integer[] playerIDs = getAvailablePlayerIDs();

			// Draw the player's names
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.print(availablePlayers.get(playerIDs[i]),
						(tableTopLeft.getX() + Game.X_OFFSET) + 5,
						(tableTopLeft.getY() + Game.Y_OFFSET
								+ ((i + 0.33) * rowHeight)));
			}

			// Draw the player's descriptions
			for (int i = 0; i < joinButtons.size(); i++) {
				graphics.printCentred(waitingForOpponentDots.replace(".",  " ")
						+ " Waiting for an opponent " + waitingForOpponentDots,
						tableTopLeft.getX() + Game.X_OFFSET,
						(tableTopLeft.getY() + Game.Y_OFFSET
								+ ((i + 0.33) * rowHeight)),
						1, (tableTopRight.getX() - tableTopLeft.getX()));
			}

			// Draw the play game buttons
			for (int i = 0; i < joinButtons.size(); i++) {
				joinButtons.get(i).drawRight();
			}
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	/**
	 * Causes a button to act if mouse released over it.
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
	public void close() {}

	@Override
	public void playSound(Sound sound) {}

	/**
	 * Selects a game to play.
	 * @param clientID - the ID of the client to connect to
	 */
	private void selectGame(int clientID) {
		// Send a JOIN instruction to the server, passing the ID
		// of the game to connect to as a parameter
		NetworkManager.postMessage("SET_NAME:" + nameEntryBox.getText()
				+ ";JOIN:" + clientID);
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
