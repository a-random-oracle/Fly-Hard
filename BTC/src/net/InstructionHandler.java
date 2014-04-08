package net;

import btc.Main;

import scn.Game;

public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";

	/** The list of valid instructions */
	public static final String[] VALID_INSTRUCTIONS = new String[] {
		"SETID",
		"SETPOS",
		"WAIT",
		"PROCEED",
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
		
		// Otherwise, switch to the appropriate method
		switch (instr) {
		case "SETID":
			handleSetID(instruction);
			break;
		case "SETPOS":
			handleSetPos(instruction);
			break;
		case "WAIT":
			handleWait();
			break;
		case "PROCEED":
			handleProceed();
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
	 * @param instruction
	 * 			the full SETID instruction
	 */
	private static void handleSetID(String instruction) {
		// Get the player ID to set from the response
		int IDToSet = -1;
		try {
			IDToSet = Integer.parseInt(instruction.split(DELIM)[1]);
		} catch (Exception e) {
			print(e);
		}

		// Set the current player's server-generated ID
		NetworkManager.setID(IDToSet);

		print("Player has ID: " + NetworkManager.getID());
	}
	
	/**
	 * Handles a SETPOS instruction.
	 * @param instruction
	 * 			the full SETPOS instruction
	 */
	private static void handleSetPos(String instruction) {
		// Get the position to set from the response
		int playerPositionToSet = -1;
		try {
			playerPositionToSet = Integer.parseInt(instruction.split(DELIM)[1]);
		} catch (Exception e) {
			print(e);
		}

		// Set the current player
		Game.getInstance().setCurrentPlayer(
				Game.getInstance().getPlayers().get(playerPositionToSet));

		print("Playing as: " + Game.getInstance().getCurrentPlayer()
				.getName());
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
	 * Handles an END instruction.
	 */
	private static void handleEnd() {
		// Close
		Main.closeScene();
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
