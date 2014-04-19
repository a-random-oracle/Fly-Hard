package scn;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.InstructionHandler;
import net.NetworkManager;

import org.newdawn.slick.Color;

import btc.Main;
import cls.InputBox;
import cls.Vector;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.audio.Sound;
import lib.jog.window;
import lib.ButtonText;

public class Lobby extends Scene {
	
	/** The time since the list of available players was last updated */
	private double timeSinceUpdate;
	
	private final int CREATE_BUTTON_W = 200;
	private final int CREATE_BUTTON_H = 32;
	
	private InputBox inputBox;
	
	private ButtonText CreateGameButton;
	
	private ArrayList<ButtonText> availableGames = new ArrayList<ButtonText>();
	
	/** The map of available players */
	private LinkedHashMap<Integer, String> availablePlayers;
	
	private Vector topLeft = new Vector(0.05, 0.2, 0, true);
	
	private Vector bottomLeft = new Vector(0.05, 0.8, 0, true);
	
	private Vector topRight = new Vector(0.95, 0.2, 0, true);
	
	private Vector bottomRight = new Vector(0.95, 0.8, 0, true);
	
	private Vector createButtonLeft = new Vector(topLeft.getRelativeX() + 0.60, topLeft.getRelativeY(), 0, true);
	
	private int rowHeight = (int) (40 * Main.getYScale());
	
	
	protected Lobby() {
		super();
	}
	
	
	@Override
	public void start() {
		// Instantiate the input box and position it correctly above the game selection box
		inputBox = new InputBox(Color.white, Color.red,
				(int)topLeft.getX() + (Game.X_OFFSET * 2), (int)topLeft.getY(), 200, 30);
		
		ButtonText.Action createGame = new ButtonText.Action() {
			@Override
			public void action() {
				NetworkManager.postMessage("INIT:" + inputBox.getText());
			}
		};
		
		CreateGameButton = new ButtonText("Create Game", createGame, (int)createButtonLeft.getX(), (int)createButtonLeft.getY(),
				CREATE_BUTTON_W, CREATE_BUTTON_H, 0, 0, 2);
	}

	@Override
	public void update(double timeDifference) {
		// Increment the time before the next data send
		timeSinceUpdate += timeDifference;

		// Update the map of available players approximately every two seconds
		if (timeSinceUpdate > 2) {
			// Update the list of players
			updateAvailablePlayers();
			
			// Process queued instructions
			String waitingInstructions = InstructionHandler.getMessages();
			
			if (waitingInstructions != null) {
				InstructionHandler.handleInstruction(waitingInstructions);
			}

			// Reset the time
			timeSinceUpdate = 0;
		}
			
		/*
		 * Activate the create button only when a name has
		 * been entered into the input box
		 */
		if (!inputBox.isEmpty()) {
			CreateGameButton.setAvailability(true);
		} else {
			CreateGameButton.setAvailability(false);
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
									+ ((topRight.getX() - topLeft.getX()) * (3d/4d))),
							(int) (topLeft.getY() + Game.Y_OFFSET + (i * rowHeight)),
							(int) ((topRight.getX() - topLeft.getX()) * (1d/4d)),
							(int) (rowHeight * (3d/4d))));
		}
	}

	@Override
	public void draw() {
		graphics.print("Enter Name: ", topLeft.getX() + Game.X_OFFSET, topLeft.getY(), 2);
		
		drawTable();
		
		inputBox.draw();

		CreateGameButton.draw();
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
		}
		
		if (availableGames != null) {
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
		if (CreateGameButton.isMouseOver(x, y)); {
			CreateGameButton.act();
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
	public void keyPressed(int key) {}

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
		return availablePlayers.keySet()
				.toArray(new Integer[availablePlayers.size()]);
	}

}
