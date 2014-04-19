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
	
	/** The time since the list of available players was last updated */
	private double timeSincePlayerUpdate = 1;
	
	/** The time since the list of available players was last updated */
	private double timeSinceStartGameUpdate = 0.1;
	
	private final int CREATE_BUTTON_W = 200;
	
	private final int CREATE_BUTTON_H = 32;
	
	private InputBox inputBox;
	
	private ButtonText createGameButton;
	
	private LinkedList<ButtonText> availableGames = new LinkedList<ButtonText>();
	
	/** The map of available players */
	private LinkedHashMap<Integer, String> availablePlayers;
	
	/** Coordinates for the top left of the game selection box */
	private Vector topLeft = new Vector(0.05, 0.2, 0, true);
	
	private Vector topRight = new Vector(0.95, 0.2, 0, true);
	
	private Vector bottomRight = new Vector(0.95, 0.8, 0, true);
	
	/** The coordinates of the "Enter name here: " string */
	private Vector stringCoords = new Vector(0.05, 0.2, 0, true);
	
	/** The coordinates of the input box */
	private Vector inputBoxCoords = new Vector(0.43, 0.2, 0, true);
	
	/** The coordinates of the create game button */
	private Vector createButtonCoords = new Vector(0.65, 0.2, 0, true);
	
	private int rowHeight = (int) (40 * Main.getYScale());
	
	
	protected Lobby() {
		super();
	}
	
	
	@Override
	public void start() {
		// Instantiate the input box and position it correctly above the game selection box
		inputBox = new InputBox(Color.white, Color.darkGray,
				(int)inputBoxCoords.getX(), (int)inputBoxCoords.getY(), 200, 23);
		
		//Implement the action that occurs upon clicking the createGame button
		ButtonText.Action createGame = new ButtonText.Action() {
			@Override
			public void action() {
				NetworkManager.postMessage("INIT:" + inputBox.getText());
			}
		};
		
		createGameButton = new ButtonText("Create Game", createGame,
				(int)createButtonCoords.getX(), (int)createButtonCoords.getY(),
				CREATE_BUTTON_W, CREATE_BUTTON_H, 0, 0, 2);
	}

	@Override
	public void update(double timeDifference) {
		// Increment the time before the next data send
		timeSincePlayerUpdate += timeDifference;
		timeSinceStartGameUpdate += timeDifference;

		// Update the map of available players approximately every second
		if (timeSincePlayerUpdate > 1) {
			// Update the list of players
			updateAvailablePlayers();

			// Reset the time
			timeSincePlayerUpdate = 0;
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
			
		/*
		 * Activate the create button only when a name has
		 * been entered into the input box
		 */
		if (!inputBox.isEmpty()) {
			createGameButton.setAvailability(true);
		} else {
			createGameButton.setAvailability(false);
		}
		inputBox.update(timeDifference);
	}
	
	/**
	 * Checks the server to get any updates to the map of available players.
	 */
	private void updateAvailablePlayers() {
		// Get the available opponents from the server
		String[] openConnections = NetworkManager
				.postMessage("GET_OPEN_CONNECTIONS").split(";");
		
		// Clear the map of available players
		availablePlayers = new LinkedHashMap<Integer, String>();
		
		// Format the open connections into a hashmap
		String[] currentEntry;
		for (int i = 0; i < openConnections.length; i++) {
			if (!openConnections[i].equals("NO CONNECTIONS")) {
				currentEntry = openConnections[i].split(":");

				switch (currentEntry.length) {
				case 1:
					availablePlayers.put(Integer.valueOf(currentEntry[0]),
							"ANON");
					break;
				case 2:
					availablePlayers.put(Integer.valueOf(currentEntry[0]),
							currentEntry[1]);
					break;
				}
			}
		}
		
		// Get the available client IDs
		Integer[] clientIDs = getAvailablePlayerIDs();
		
		// Clear the array of available games
		availableGames.clear();
		
		for (int i = 0; i < availablePlayers.size(); i++) {
			ButtonText.Action currentAction =
					createPlayerButtonAction(clientIDs[i]);
			
			availableGames.add(new ButtonText("Join Game",
							currentAction,
							(int) (topLeft.getX() + Game.X_OFFSET
									+ ((topRight.getX() - topLeft.getX()) * (7d/8d))),
							(int) (topLeft.getY() + Game.Y_OFFSET + ((i + 0.33) * rowHeight)),
							(int) ((topRight.getX() - topLeft.getX()) * (7d/8d)),
							(int) (rowHeight * (7d/8d)), 0, 0));
		}
	}

	@Override
	public void draw() {
		graphics.print("Enter Name: ",
				stringCoords.getX() + Game.X_OFFSET,
				stringCoords.getY(), 2);

		drawTable();
		
		inputBox.draw();

		createGameButton.draw();
	}
	
	public void drawTable() {
		graphics.setColour(255, 255, 255);
		
		graphics.rectangle(false,
				(topLeft.getX() + Game.X_OFFSET),
				(topLeft.getY() + Game.Y_OFFSET),
				(topRight.getX() - topLeft.getX()),
				(bottomRight.getY() - topRight.getY()));
		
		if (availableGames != null) {
			for (int i = 0; i < availableGames.size(); i++) {
				graphics.line((topLeft.getX() + Game.X_OFFSET),
						(topLeft.getY() + Game.Y_OFFSET + ((i + 1) * rowHeight)),
						(topRight.getX() + Game.X_OFFSET),
						(topRight.getY() + Game.Y_OFFSET + ((i + 1) * rowHeight)));
			}
			
			Integer[] playerIDs = getAvailablePlayerIDs();
			
			for (int i = 0; i < availableGames.size(); i++) {
				graphics.printCentred(availablePlayers.get(playerIDs[i]),
						(topLeft.getX() + Game.X_OFFSET),
						(topLeft.getY() + Game.Y_OFFSET + ((i + 0.33) * rowHeight)),
						1, ((topRight.getX() + Game.X_OFFSET) * (1d/10d)));
			}
			
			for (int i = 0; i < availableGames.size(); i++) {
				availableGames.get(i).draw();
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
		if (createGameButton.isMouseOver(x, y)); {
			createGameButton.act();
		}
		
		if (availableGames != null) {
			for (ButtonText b : availableGames) {
				if (b.isMouseOver(x, y)) {
					b.act();
				}
			}
		}
	}

	@Override
	public void keyPressed(int key) {
		inputBox.keyPressed(key);
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
