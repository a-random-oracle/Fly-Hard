package net;

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
	
	
	/**
	 * Handles instructions.
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
		
		// Otherwise, switch to the appropriate method
		switch (instr) {
		case "SET_ID":
			handleSetID(parameters);
			break;
		case "WAIT":
			handleWait();
			break;
		case "PROCEED":
			handleProceed();
			break;
		case "SET_SEED":
			handleSetSeed(parameters);
			break;
		case "START_GAME":
			handleStartGame(parameters);
			break;
		case "TRANSFER":
			handleTransfer(parameters);
			break;
		case "REMOVE":
			handleRemove(parameters);
			break;
		case "END_GAME":
			handleEndGame();
			break;
		case "NULL":
			break;
		case "INVALID_REQUEST":
			handleInvalidRequest();
			break;
		}
	}
	
	
	/**
	 * Handles a SET_ID instruction.
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleSetID(String parameters) {
		// Get the player ID to set from the response
		int IDToSet = -1;
		try {
			IDToSet = Integer.parseInt(parameters);
		} catch (Exception e) {
			NetworkManager.print(e);
		}

		// Set the current player's server-generated ID
		NetworkManager.setID(IDToSet);

		NetworkManager.print("Player has ID: " + NetworkManager.getID());
	}
	
	/**
	 * Handles a WAIT instruction.
	 */
	private static void handleWait() {
		NetworkManager.print("Waiting.");
		
		try {
			// Wait, then poll server to check for an opponent
			Thread.sleep(100);
			NetworkManager.postMessage("CHECK_FOR_OPPONENT");
		} catch (InterruptedException e) {
			NetworkManager.print(e);
		}
	}
	
	/**
	 * Handles a PROCEED instruction.
	 */
	private static void handleProceed() {
		NetworkManager.print("Resuming.");
	}
	
	/**
	 * Handles a SET_SEED instruction.
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
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleStartGame(String parameters) {
		// Get the position to set from the response
		int playerPosition = -1;
		try {
			playerPosition = Integer.parseInt(parameters);
		} catch (Exception e) {
			NetworkManager.print(e);
		}

		// Start a new multiplayer game
		Main.setScene(MultiPlayerGame
				.createMultiPlayerGame(DifficultySetting.EASY, playerPosition));
	}
	
	/**
	 * Handles a TRANSFER instruction.
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleTransfer(String parameters) {
		Game.getInstance().getCurrentPlayer().getAircraft().add(
				Game.getInstance().getAircraftFromName(parameters));
		
		NetworkManager.postMessage("SEND:REMOVE:" + parameters);
	}
	
	/**
	 * Handles a REMOVE instruction.
	 * @param parameters - the parameters accompanying the instruction
	 */
	private static void handleRemove(String parameters) {
		// Obtain a lock on the aircraft array
		synchronized (Game.getInstance().getCurrentPlayer().getAircraft()) {
			Game.getInstance().getCurrentPlayer().getAircraft().remove(
					Game.getInstance().getAircraftFromName(parameters));
		}
	}
	
	/**
	 * Handles an END_GAME instruction.
	 */
	private static void handleEndGame() {
		// Obtain a lock on the game instance
		synchronized(Game.getInstance()) {
			Game.getInstance().setEnding(true);
		}
	}

	/**
	 * Handles an INVALID_REQUEST instruction.
	 */
	private static void handleInvalidRequest() {
		// TODO
	}
	
}
