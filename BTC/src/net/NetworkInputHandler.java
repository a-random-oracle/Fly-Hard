package net;

import lib.jog.input;
import scn.Game;

public class NetworkInputHandler {
	
	/**
	 * Gets the player an instruction is associated with.
	 * @param instruction
	 * 			the instruction to parse
	 * @return the playerID of the player performing the action
	 * 			encoded in the instruction
	 */
	public static int getPlayerIDFromInstruction(String instruction) {
		// Split the instruction into its constituent parts
		String[] instructionArray = instruction.split(":");
		
		int playerID = -1;
		try {
			playerID = Integer.parseInt(instructionArray[0]);
		} catch (NumberFormatException e) {
			// Invalid playerID
			e.printStackTrace();
		}
		
		return playerID;
	}
	
	/**
	 * Enacts an instruction.
	 * <p>
	 * Instructions should be of the form:
	 * PlayerID:Action:SubAction:Param1:Param2
	 * </p>
	 * <p>
	 * Where:
	 * </p>
	 * <ul>
	 * <li>PlayerID  = the ID of the player to perform the action for</li>
	 * <li>Action    = one of "L" (left mouse button), "R" (right mouse)
	 * 					button, "M" (mouse wheel) or "K" (key)</li>
	 * <li>SubAction = one of "P" (press), "R" (release) or "S" (scroll)</li>
	 * <li>Param1    = the first parameter for the event</li>
	 * <li>Param2    = the second parameter for the event</li>
	 * </ul>
	 * <p>
	 * For click events, the parameters represent the x and y position
	 * of the event respectively.
	 * </p>
	 * <p>
	 * For scroll events, the first parameter represents the scroll amount,
	 * and the second parameter should be '0'.
	 * </p>
	 * <p>
	 * For key events, the first parameter represents the key's key code,
	 * and the second parameter should be '0'.
	 * </p>
	 * @param instruction
	 * 			the string representing the instruction to perform
	 */
	public static void processInputInstruction(String instruction) {
		// Split the instruction into its constituent parts
		String[] instructionArray = instruction.split(":");
		
		if (instructionArray.length != 5) {
			// Invalid length
			Exception e = new Exception("The instruction has an invalid length. "
					+ "Length is: " + instructionArray.length
					+ " (expected 5).");
			e.printStackTrace();
			return;
		} else {
			// Treat the instruction as valid
			
			// Get the first parameter
			int param1 = -1;
			try {
				param1 = Integer.parseInt(instructionArray[3]);
			} catch (NumberFormatException e) {
				// Invalid first parameter
				e.printStackTrace();
				return;
			}
			
			// Get the second parameter
			int param2 = -1;
			try {
				param2 = Integer.parseInt(instructionArray[4]);
			} catch (NumberFormatException e) {
				// Invalid second parameter
				e.printStackTrace();
				return;
			}
			
			switch (instructionArray[1]) {
			case "L":
				// Left mouse button
				processLeftMouseEvent(instructionArray[2], param1, param2);
				break;
			case "R":
				// Right mouse button
				processRightMouseEvent(instructionArray[2], param1, param2);
				break;
			case "M":
				// Middle mouse button
				processMiddleMouseEvent(instructionArray[2], param1, param2);
				break;
			case "K":
				// Key action
				processKeyEvent(instructionArray[2], param1);
				break;
			}
		}
	}
	
	/**
	 * Processes a left mouse button event.
	 * @param subAction
	 * 			either "P" (press) or "R" (release)
	 * @param param1
	 * 			the x co-ordinate of the event location
	 * @param param2
	 * 			the y co-ordinate of the event location
	 */
	private static void processLeftMouseEvent(String subAction,
			int param1, int param2) {
		switch (subAction) {
		case "P":
			// Left mouse button pressed
			Game.getInstance().mousePressed(input.MOUSE_LEFT,
					param1, param2);
			break;
		case "R":
			// Left mouse button released
			Game.getInstance().mouseReleased(input.MOUSE_LEFT,
					param1, param2);
			break;
		}
	}
	
	/**
	 * Processes a right mouse button event.
	 * @param subAction
	 * 			either "P" (press) or "R" (release)
	 * @param param1
	 * 			the x co-ordinate of the event location
	 * @param param2
	 * 			the y co-ordinate of the event location
	 */
	private static void processRightMouseEvent(String subAction,
			int param1, int param2) {
		switch (subAction) {
		case "P":
			// Right mouse button pressed
			Game.getInstance().mousePressed(input.MOUSE_RIGHT,
					param1, param2);
			break;
		case "R":
			// Right mouse button released
			Game.getInstance().mouseReleased(input.MOUSE_RIGHT,
					param1, param2);
			break;
		}
	}
	
	/**
	 * Processes a middle mouse button event.
	 * @param subAction
	 * 			one of "P" (press), "R" (release) or
	 * 			"S" (scroll)
	 * @param param1
	 * 			the x co-ordinate of the event location
	 * @param param2
	 * 			the y co-ordinate of the event location
	 */
	private static void processMiddleMouseEvent(String subAction,
			int param1, int param2) {
		switch (subAction) {
		case "P":
			// Middle mouse button pressed
			Game.getInstance().mousePressed(input.MOUSE_MIDDLE,
					param1, param2);
			break;
		case "R":
			// Middle mouse button released
			Game.getInstance().mouseReleased(input.MOUSE_MIDDLE,
					param1, param2);
			break;
		case "S":
			// Middle mouse button scrolled
			//TODO
			break;
		}
	}
	
	/**
	 * Processes a key event.
	 * @param subAction
	 * 			either "P" (key press) or "R" (key release)
	 * @param param1
	 * 			the key
	 */
	private static void processKeyEvent(String subAction, int param1) {
		switch (subAction) {
		case "P":
			// Key pressed
			Game.getInstance().keyPressed(param1);
			break;
		case "R":
			// Key released
			Game.getInstance().keyReleased(param1);
			break;
		}
	}
	
}
