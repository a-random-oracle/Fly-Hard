package net;

import net.NetworkManager.State;
import btc.Main;
import scn.Game;
import scn.Game.DifficultySetting;
import scn.MultiPlayerGame;

/**
 * Handles instructions.
 * <p>
 * Instructions should be of the form:
 * 'COMMAND':'PARAMETERS;
 * </p>
 * <p>
 * Multiple instructions can be handled by passing
 * in a string of the form:
 * 'Instruction1';'Instruction2'
 * When multiple instructions are passed, they will be
 * handled sequentially, in the order they appear in the
 * string.
 * </p>
 */
public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";
	
	/** The messages to be delivered to the main thread */
	private static String messages = "";
	
	
	/**
	 * Handles instructions.
	 * <p>
	 * Takes a semicolon-delimited list of instructions and
	 * processes them sequentially.
	 * </p>
	 * @param instruction - the instruction(s) to handle
	 */
	public static void handleInstruction(String instruction) {
		if (instruction != null) {
			// Split the instruction string into individual instructions
			String[] instructionList = instruction.split(LIST_DELIM);

			// Check that there is at least one instruction
			if (instructionList != null) {
				if (instructionList.length > 0) {
					// Loop through the instructions, handling them
					// sequentially
					for (String instr : instructionList) {
						handleIndividualInstruction(instr);
					}
				}
			}
		}
	}
	
	/**
	 * Handles an instruction.
	 * <p>
	 * Breaks an instruction down into an instruction part
	 * and a parameter part, and passes these to the
	 * appropriate method (as specified in the instruction
	 * part.
	 * </p>
	 * @param instruction - the instruction to handle
	 */
	private static void handleIndividualInstruction(String instruction) {
		// Get the instruction
		String instr = instruction.split(DELIM)[0];
		
		// Return immediately if the instruction is invalid
		if (instr == null) return;
		
		// Check if the received data has parameters
		String parameters = null;
		if (instruction != null && instruction.contains(DELIM)) {
			parameters = instruction.substring(instruction.indexOf(DELIM) + 1);
		}
		
		// Switch to the appropriate method
		// If a connection to the server has not yet been established
		if (NetworkManager.getState() == State.CONNECTING) {
			switch (instr) {
			case "SERVER_FULL":
				handleServerFull();
				break;
			case "SET_ID":
				handleSetID(parameters);
				break;
			}
		}
		
		// Switch to the appropriate method
		// Provided that a connection to the server has been established
		if (NetworkManager.getState() == State.CONNECTED) {
			switch (instr) {
			case "SET_SEED":
				handleSetSeed(parameters);
				break;
			case "START_GAME":
				handleStartGame(parameters);
				break;
			case "END_GAME":
				handleEndGame();
				break;
			case "CLOSED_CONNECTION":
				handleClosedConnection();
				break;
			case "NULL":
				break;
			case "INVALID_REQUEST":
				handleInvalidRequest();
				break;
			}
		}
	}
	
	
	/**
	 * Handles a SERVER_FULL instruction.
	 * <p>
	 * SERVER_FULL instructions inform the client that they couldn't establish
	 * a connection to the server.
	 * </p>
	 * <p>
	 * The client will then make another attempt to connect to the server.
	 * </p>
	 */
	private static void handleServerFull() {
		NetworkManager.sendMessage("START");
	}
	
	/**
	 * Handles a SET_ID instruction.
	 * <p>
	 * SET_ID instructions set the ID which the NetworkManager passes
	 * to the server with each request.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleSetID(String parameters) {
		try {
			// Get the player ID to set from the response
			int idToSet = Integer.parseInt(parameters);
			
			// Set the current player's server-generated ID
			NetworkManager.setID(idToSet);

			NetworkManager.print("Player has ID: " + idToSet);
			
			// Set the network manager's state to CONNECTED
			NetworkManager.setState(State.CONNECTED);
		} catch (Exception e) {
			NetworkManager.print(e);
		}
	}
	
	/**
	 * Handles a SET_SEED instruction.
	 * <p>
	 * SET_SEED instructions set the random seed used by the game.
	 * </p>
	 * <p>
	 * This will cause random events to by synchronised across all
	 * players using the seed provided.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleSetSeed(String parameters) {
		// Get the player ID to set from the response
		int seedToSet = 0;
		try {
			seedToSet = Integer.parseInt(parameters);
		} catch (Exception e) {
			NetworkManager.print(e);
		}

		// Set the current player's random seed
		Main.setRandomSeed(seedToSet);

		NetworkManager.print("Using random seed: " + seedToSet);
	}
	
	/**
	 * Handles a START_GAME instruction.
	 * <p>
	 * START_GAME instructions cause a new instance of MultiPlayerGame
	 * to be created.
	 * </p>
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleStartGame(String parameters) {
		if (Thread.currentThread().getId() != NetworkManager.getNetworkThreadID()) {
			// Get the position to set from the response
			int playerPosition = -1;
			try {
				playerPosition = Integer.parseInt(parameters);
			} catch (Exception e) {
				NetworkManager.print(e);
			}

			// Start a new multiplayer game
			Main.setScene(MultiPlayerGame
					.createMultiPlayerGame(DifficultySetting.EASY,
							playerPosition));
		} else {
			// Obtain a lock on the message buffer
			synchronized (messages) {
				// Add a START_GAME instruction to the message buffer
				messages = "START_GAME:" + parameters;
			}
		}
	}
	
	/**
	 * Handles an END_GAME instruction.
	 * <p>
	 * END_GAME instructions cause the current game instance to end.
	 * </p>
	 */
	private static void handleEndGame() {
		if (Game.getInstance() != null) {
			// Obtain a lock on the game instance
			synchronized(Game.getInstance()) {
				Game.getInstance().setEnding(true);
			}
		}
	}
	
	/**
	 * Handles a CLOSED_CONNECTION instruction.
	 * <p>
	 * CLOSED_CONNECTION instructions cause the network manager's state
	 * to become CLOSED.
	 * </p>
	 */
	private static void handleClosedConnection() {
		NetworkManager.setState(State.CLOSED);
		Main.quit();
	}

	/**
	 * Handles an INVALID_REQUEST instruction.
	 * <p>
	 * INVALID_REQUEST instructions presently do nothing.
	 * </p>
	 */
	private static void handleInvalidRequest() {
		// TODO
	}
	
	
	/**
	 * Gets any messages which need to be processed by the main thread.
	 * <p>
	 * This operation is <b>destructive</b>, i.e. the message buffer will
	 * be cleared after it has been read.
	 * </p>
	 * @return the contents of the message buffer
	 */
	public static String getMessages() {
		// Obtain a lock on the message buffer
		synchronized (messages) {
			String messageBuffer = messages;
			messages = "";
			return messageBuffer;
		}
	}
	
}
