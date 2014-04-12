package net;

import btc.Main;
import cls.Player;
import scn.Game;
import scn.MultiPlayerGame;

public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";

	/** The list of valid instructions */
	public static final String[] VALID_INSTRUCTIONS = new String[] {
		"SETID",
		"SETPOS",
		"SETSEED",
		"WAIT",
		"PROCEED",
		"TRANSFER",
		"REMOVE",
		"END",
		"NULL",
		"INVALID_REQUEST"};
	
	/** Whether to output data to the standard output */
	private static boolean verbose = true;
	
	
	/**
	 * Handles instructions.
	 * @param instruction
	 * 			the instruction(s) to handle
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
	 * @param instruction
	 * 			the instruction to handle
	 */
	private static void handleIndividualInstruction(String instruction) {
		// Get the instruction
		String instr = getInstruction(instruction);
		
		// Return immediately if the instruction is invalid
		if (instr == null) return;
		
		// Check if the received data has parameters
		String parameters = null;
		if (instruction != null && instruction.contains(DELIM)) {
			parameters = instruction.substring(instruction.indexOf(DELIM) + 1);
		}
		
		// Otherwise, switch to the appropriate method
		switch (instr) {
		case "SETID":
			handleSetID(parameters);
			break;
		case "SETPOS":
			handleSetPos(parameters);
			break;
		case "SETSEED":
			handleSetSeed(parameters);
			break;
		case "WAIT":
			handleWait();
			break;
		case "PROCEED":
			handleProceed();
			break;
		case "TRANSFER":
			handleTransfer(parameters);
			break;
		case "REMOVE":
			handleRemove(parameters);
			break;
		case "END":
			handleEnd();
			break;
		case "NULL":
			break;
		case "INVALID_REQUEST":
			handleInvalidRequest();
			break;
		}
	}
	
	
	// Instruction handling -------------------------------------------------------------
	
	/**
	 * Handles a SETID instruction.
	 * @param parameters
	 * 			the parameters accompanying the instruction
	 */
	private static void handleSetID(String parameters) {
		// Get the player ID to set from the response
		int IDToSet = -1;
		try {
			IDToSet = Integer.parseInt(parameters);
		} catch (Exception e) {
			print(e);
		}

		// Set the current player's server-generated ID
		NetworkManager.setID(IDToSet);

		print("Player has ID: " + NetworkManager.getID());
	}
	
	/**
	 * Handles a SETPOS instruction.
	 * @param parameters
	 * 			the parameters accompanying the instruction
	 */
	private static void handleSetPos(String parameters) {
		// Get the position to set from the response
		int playerPosition = -1;
		try {
			playerPosition = Integer.parseInt(parameters);
		} catch (Exception e) {
			print(e);
		}

		// Set the current player
		switch (playerPosition) {
		case 0:
			// Player with ID = 0 should already be set as the current player
			break;
		case 1:
			// Swap the player with the opposing player
			Player tempPlayer = Game.getInstance().getCurrentPlayer();
			((MultiPlayerGame) Game.getInstance()).setCurrentPlayer(
					((MultiPlayerGame) Game.getInstance()).getOpposingPlayer());
			((MultiPlayerGame) Game.getInstance()).setOpposingPlayer(tempPlayer);
			break;
		}

		print("Playing as: " + Game.getInstance().getCurrentPlayer()
				.getName());
	}
	
	/**
	 * Handles a SETSEED instruction.
	 * @param parameters
	 * 			the parameters accompanying the instruction
	 */
	private static void handleSetSeed(String parameters) {
		// Get the player ID to set from the response
		int seedToSet = 0;
		try {
			seedToSet = Integer.parseInt(parameters);
		} catch (Exception e) {
			print(e);
		}

		// Set the current player's random seed
		Main.setRandomSeed(seedToSet);

		print("Using random seed: " + seedToSet);
	}
	
	/**
	 * Handles a WAIT instruction.
	 */
	private static void handleWait() {
		print("Waiting.");
		
		try {
			// Wait, then poll server to check for an opponent
			Thread.sleep(10);
			handleInstruction(NetworkManager.postMessage("CHECK_FOR_OPPONENT"));
		} catch (InterruptedException e) {
			print(e);
		}
	}
	
	/**
	 * Handles a PROCEED instruction.
	 */
	private static void handleProceed() {
		print("Resuming.");
	}
	
	/**
	 * Handles a TRANSFER instruction.
	 * @param parameters
	 * 			the parameters accompanying the instruction
	 */
	private static void handleTransfer(String parameters) {
		Game.getInstance().getCurrentPlayer().getAircraft().add(
				Game.getInstance().getAircraftFromName(parameters));
		
		NetworkManager.postMessage("SEND:REMOVE:" + parameters);
	}
	
	/**
	 * Handles a REMOVE instruction.
	 * @param parameters
	 * 			the parameters accompanying the instruction
	 */
	private static void handleRemove(String parameters) {
		// Obtain a lock on the aircraft array
		synchronized (Game.getInstance().getCurrentPlayer().getAircraft()) {
			Game.getInstance().getCurrentPlayer().getAircraft().remove(
					Game.getInstance().getAircraftFromName(parameters));
		}
	}
	
	/**
	 * Handles an END instruction.
	 */
	private static void handleEnd() {
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
	
	
	// Helper methods -------------------------------------------------------------------
	
	/**
	 * Checks if an instruction is in the list of valid instructions,
	 * and if so returns the instruction.
	 * @param instruction
	 * 			the instruction to check
	 * @return the instruction
	 */
	private static String getInstruction(String instruction) {
		// Try to split out the instruction's parameters
		String[] instructionList = instruction.split(DELIM);
		
		// Check either the instruction given (if there are no
		// parameters), or the instruction part of the instruction
		// (if there are parameters)
		String instructionToCheck = null;
		if (instructionList.length > 0) {
			instructionToCheck = instructionList[0];
			
			// Loop through the valid instructions, and try to match
			// these to the specified instruction
			for (String validInstruction : VALID_INSTRUCTIONS) {
				if (validInstruction.equals(instructionToCheck)) {
					return instructionToCheck;
				}
			}
		}
		
		// The instruction hasn't been found, so it's not valid
		return null;
	}
	
	
	// Printing -------------------------------------------------------------------------
	
	/**
	 * Prints strings to the standard output.
	 * <p>
	 * If {@link #verbose} is set to <code>true</code>, this will
	 * function in the same was as {@link System.out#println()}.
	 * </p>
	 * <p>
	 * Otherwise this will do nothing.
	 * </p>
	 * @param string
	 * 			the string to output
	 */
	public static void print(String string) {
		if (verbose) System.out.println(string);
	}
	
	/**
	 * Prints error messages to the standard output.
	 * <p>
	 * Uses {@link #print(String)} to print stack traces.
	 * </p>
	 * @param e
	 * 			the exception to output
	 */
	public static void print(Exception e) {
		print(e.toString());
		
		for (int i = 0; i < e.getStackTrace().length; i++) {
			print("at " + e.getStackTrace()[i].toString());
		}
	}
	
}
