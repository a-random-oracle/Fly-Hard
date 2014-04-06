package net;

import scn.Game;

public abstract class InstructionHandler {
	
	/** The instruction list delimiter */
	public static final String LIST_DELIM = ";";
	
	/** The instruction delimiter */
	public static final String DELIM = ":";

	/** The list of valid instructions */
	public static final String[] VALID_INSTRUCTIONS =
			new String[] {"SETID", "WAIT", "END", "INVALID_REQUEST"};
	
	
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
		// Return immediately if the instruction is invalid
		if (!isValidInstruction(instruction)) return;
		
		// Otherwise, switch to the appropriate method
		switch (instruction) {
		case "SETID":
			handleSetID(instruction);
			break;
		case "WAIT":
			handleWait();
			break;
		case "END":
			handleEnd();
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
		int playerIDToSet = Integer.parseInt(instruction.split(DELIM)[1]);

		// Set the current player
		Game.getInstance().setCurrentPlayer(
				Game.getInstance().getPlayers().get(playerIDToSet));

		System.out.println("Playing as: " + Game.getInstance()
				.getCurrentPlayer().getName());
	}
	
	/**
	 * Handles a WAIT instruction.
	 */
	private static void handleWait() {
		Game.getInstance().setPaused(true);
	}
	
	/**
	 * Handles an END instruction.
	 */
	private static void handleEnd() {
		// TODO
	}
	
	/**
	 * Handles an INVALID_REQUEST instruction.
	 */
	private static void handleInvalidRequest() {
		// Do nothing
	}
	
	
	// Helper methods -------------------------------------------------------------------
	
	/**
	 * Checks if an instruction is in the list of valid instructions.
	 * @param instruction
	 * 			the instruction to check
	 * @return <code>true</code if the instruction is valid,
	 * 			otherwise <code>false</code>
	 */
	private static boolean isValidInstruction(String instruction) {
		for (String validInstruction : VALID_INSTRUCTIONS) {
			if (validInstruction.equals(instruction)) {
				return true;
			}
		}
		
		// Instruction hasn't been found, so it's not valid
		return false;
	}
	
}
