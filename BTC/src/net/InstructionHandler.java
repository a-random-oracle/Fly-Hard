package net;

import com.google.gson.Gson;

import btc.Main;
import lib.jog.input;
import scn.Game;
import cls.Player;

public class InstructionHandler {
	
	/**
	 * Gets the player an instruction is associated with.
	 * @param instruction
	 * 			the instruction to parse
	 * @return the playerID of the player performing the action
	 * 			encoded in the instruction
	 */
	public static int getPlayerIDFromInstruction(String instruction) {
		// Split the instruction into its constituent parts
		String[] instructionArray = instruction.split("::");
		
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
	 * Processes an instruction.
	 * <p>
	 * Instructions should be of the form:
	 * PlayerID::Operation::Action::SubAction::Param1::Param2
	 * <b>or</b>
	 * PlayerID::PLAYER::Player
	 * </p>
	 * <p>
	 * Where:
	 * </p>
	 * <ul>
	 * <li>PlayerID  = the ID of the player to perform the action for</li>
	 * <li>Operation = one of the OP codes listed below
	 * <li>Action    = one of "L" (left mouse button), "R" (right mouse)
	 * 					button, "M" (mouse wheel) or "K" (key)</li>
	 * <li>SubAction = one of "P" (press), "R" (release) or "S" (scroll)</li>
	 * <li>Param1    = the first parameter for the event</li>
	 * <li>Param2    = the second parameter for the event</li>
	 * <li>Player    = the JSON representation of a player</li>
	 * </ul>
	 * <br>
	 * <p>
	 * Valid OP codes:
	 * </p>
	 * <ul>
	 * <li>OP        = input operation</li>
	 * <li>SEED      = load a new random seed</li>
	 * <li>RESET     = notes that the server has been reset</li>
	 * <li>NOOP      = null operation</li>
	 * <li>INIT      = initialises the current player</li>
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
	public static void processInstruction(String instruction) {
		// Split the instruction into its constituent parts
		String[] instructionArray = instruction.split("::");
		
		if (instructionArray.length == 6) {
			// Treat the instruction as valid
			
			// Get the first parameter
			int param1 = -1;
			try {
				param1 = Integer.parseInt(instructionArray[4]);
			} catch (NumberFormatException e) {
				// Invalid first parameter
				e.printStackTrace();
				return;
			}
			
			// Get the second parameter
			int param2 = -1;
			try {
				param2 = Integer.parseInt(instructionArray[5]);
			} catch (NumberFormatException e) {
				// Invalid second parameter
				e.printStackTrace();
				return;
			}
			
			switch (instructionArray[1]) {
			case "OP":
				processInputEvent(instructionArray[2],
						instructionArray[3], param1, param2);
				break;
			case "SEED":
				processSeedEvent(instructionArray[2],
						instructionArray[3], param1, param2);
				break;
			case "RESET":
				processResetEvent(instructionArray[2],
						instructionArray[3], param1, param2);
				break;
			case "INIT":
				int playerID = getPlayerIDFromInstruction(instruction);
				Game.getInstance().setPlayer(playerID,
						Game.getInstance().getPlayers().get(playerID));
				break;
			case "NOOP":
				// Do nothing
				break;
			}
		} else if (instructionArray.length == 3) {
			switch (instructionArray[1]) {
			case "PLAYER":
				Game.getInstance().setPlayer(
						getPlayerIDFromInstruction(instruction),
						(new Gson()).fromJson(instructionArray[2], Player.class));
				break;
			}
		} else {
			// Invalid length
			Exception e = new Exception("The instruction has an invalid length. "
					+ "Length is: " + instructionArray.length
					+ " (expected either 3 or 6).");
			e.printStackTrace();
			return;
		}
	}
	
	private static void processInputEvent(String action, String subAction,
			int param1, int param2) {
		switch (action) {
		case "L":
			// Left mouse button
			processLeftMouseEvent(subAction, param1, param2);
			break;
		case "R":
			// Right mouse button
			processRightMouseEvent(subAction, param1, param2);
			break;
		case "M":
			// Middle mouse button
			processMiddleMouseEvent(subAction, param1, param2);
			break;
		case "K":
			// Key action
			processKeyEvent(subAction, param1);
			break;
		}
	}

	private static void processSeedEvent(String action, String subAction,
			int param1, int param2) {
		Main.setRandomSeed(param1);
	}

	private static void processResetEvent(String action, String subAction,
			int param1, int param2) {
		// Cause the scent to close
		Game.getInstance().close();
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
