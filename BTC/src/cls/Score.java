package cls;

import java.util.Arrays;

import lib.jog.graphics;

public class Score {
	
	
	private final int MAX_SCORE = 9999999;
	private final int MAX_DIGITS_IN_SCORE = (int)Math.log10(MAX_SCORE) + 1;
	
	private int current_digits_in_score;
	
	/**
	 * Records the total score the user has achieved at a given time.
	 */	
	private int total_score = 0;
	private int target_score = 0;
	
	/**
	 * Getter for total score in case it is needed outside the Demo class.
	 * @return totalScore
	 */	
	public int getTotalScore() {
		if (total_score > MAX_SCORE) total_score = MAX_SCORE;
		return total_score;
	}
	
	public int getTargetScore() {
		return target_score;
	}
	
	/**
	 * Allows for increasing total score outside of Demo class.
	 * @param scoreDifference
	 */	
	public void increaseTotalScore(int amount) {
		if (amount > 0)
			target_score += amount;
	}
	
	/**
	 * Takes an aircraft and calculates it's score.
	 * Score per plane is based on a base score (which varies with difficulty) for the plane,
	 * and how efficient the player has been in navigating the aircraft to it's destination.
	 * A minimum of the base score is always awarded with a bonus of up to base_score/3.
	 */
	public int calculateAircraftScore(Aircraft aircraft) {
		double efficiency = efficiencyFactor(aircraft);
		int base_score = aircraft.getBaseScore();
		int bonus = (int)((base_score/3) * efficiency);
		int aircraft_score = base_score + bonus;
		return aircraft_score;
	}
	
	/**
	 * calculates how optimal the player was, by taking the ratio of the time to traverse the shortest path to the actual time taken.
	 * @param optimalTime - Ideal time, not really possible to achieve.  
	 * @param timeTaken - Total time a plane spent in the airspace. 
	 * @return the extent to which the player achieved optimal time.
	 */
	private double efficiencyFactor(Aircraft aircraft) {
		double optimalTime = aircraft.getOptimalTime();
		double timeTaken = System.currentTimeMillis()/1000 - aircraft.getTimeOfCreation();
		double efficiency = optimalTime/timeTaken;
		return efficiency;
	}
	/**
	 * Initially set to 1. This is the main multiplier for score. As more planes leave airspace 
	 * it may be incremented based on the value of multiplierVariable (the interval it is currently in).
	 */	
	private int multiplier = 1; 
	
	/**
	 * Initially 0 (i.e. the meter is empty when you start the game).
	 * Set the level at which to fill the multiplier meter on the GUI.
	 * Used to increase and decrease the multiplier when it exceeds certain bounds -> currently less than 0 and greater than 256.
	 */
	private int meter_fill = 0;
	
	private int target_meter_fill = 0;
	
	/**
	 * This variable determines the current level of the multiplier. Each level has an associated multiplier value
	 * e.g. multiplier_level = 1 -> multiplier = 1, multiplier_level = 2 -> multiplier = 3, multiplier_level = 3 -> multiplier = 5.
	 * Increased when meter_fill >= 256, decreased when meter_fill < 0.
	 * Also used in Demo to vary max_aircraft and aircraft_spawn rates.
	 */
	private int multiplierLevel = 1;
	
	/**
	 * Resets the multiplier_level to 1 and empties the meter.
	 */	
	public void resetMultiplier() {
		multiplierLevel = 1;
		multiplier = 1;
		target_meter_fill = 0;
		meter_fill = 0;
	} 
	
	private boolean meter_draining = false;
	
	/**
	 * Used to get multiplierLevel variable outside of Demo class.
	 * @return multiplierLevel variable that is used to increase main multiplier for score.
	 */	
	public int getMultiplierLevel() {
		return multiplierLevel;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public int getMeterFill() {
		return meter_fill;
	}
	public int getTargetMeterFill() {
		return target_meter_fill;
	}
	
	
	// Necessary for testing
		
	/**
	 * This method should only be used publically for unit testing. Its purpose is to update multiplierVariable
	 * outside of Demo class. 
	 * @param difference
	 */
	public void increaseMultiplierLevel() {
		if (multiplierLevel <= 5) {
			multiplierLevel += 1;
			setMultiplier();
		}
		
	}
	
	public void decreaseMultiplierLevel() {
		if (multiplierLevel >= 1) {
			multiplierLevel -= 1;
			setMultiplier();
		}
	}
		
	/**
	 * Updates multiplier based on the multiplierLevel, each multiplierLevel has an associated value to set the multiplier at. 
	 * Is updated whenever multiplierLevel changes.
	 */		
	private void setMultiplier() {
		switch(multiplierLevel) {
		case 1:
			multiplier = 1;
			break;
		case 2:
			multiplier = 3;
			break;
		case 3:
			multiplier = 5;
			break;
		case 4:
			multiplier = 7;
			break;
		case 5:
			multiplier = 10;
			break;
		}
	}
	
	private void updateMultiplierLevel() {
		if (meter_fill >= 256) {
			if (multiplierLevel != 5) {
				increaseMultiplierLevel();
				meter_fill -= 256;
				target_meter_fill -= 256;
			}
			else {
				meter_fill = 256;
				target_meter_fill = 256;
			}
		}
			
		if (meter_fill < 0) {
			if (multiplierLevel != 1) {
				decreaseMultiplierLevel();
				meter_fill += 256;
				target_meter_fill += 256;
			}
			else {
				meter_fill = 0;
				target_meter_fill = 0;
			}
		}	
	}
	
	public void increaseMeterFill(int change_to_meter) {
		target_meter_fill += change_to_meter;
	}
	
	public void draw() {
		drawScore();
		drawMultiplier();
	}
	
	private void drawScore() {
		/**
		 * Takes the maximum possible digits in the score and calculates how many of them are currently 0.
		 * 
		 */
		current_digits_in_score = (getTotalScore() != 0) ? (int)Math.log10(getTotalScore()) + 1 : 0; // exception as log10(0) is undefined.
		char[] chars = new char[MAX_DIGITS_IN_SCORE - current_digits_in_score];
		Arrays.fill(chars, '0');
		String zeros = new String(chars);
		
		/**
		 * Prints the unused score digits as 0s, and the current score.
		 */
		graphics.setColour(graphics.green_transp);
		graphics.print(zeros, 264, 3, 5);
		graphics.setColour(graphics.green);
		if (getTotalScore() != 0) graphics.printRight(String.valueOf(getTotalScore()), 544, 3, 5, 0);
		
		
	}
	
	private void drawMultiplier() {
		int bar_segments = 16;
		int bar_segment_dif = 24;
		int bar_x_offset = 608;
		int bar_y_offset = 8;
		int segment_width = 16;
		int segment_height = 32;
		
		int red = 0;
		int green = 128;
		
		if (meter_draining) {
			red = 128;
			green = 0;
		}
		for (int i = 0; i < bar_segments; i++) {
			graphics.setColour(red, green, 0, 64);
			graphics.rectangle(true, bar_x_offset, bar_y_offset, segment_width, segment_height);
			graphics.setColour(red, green, 0);
			drawMultiplierSegment(meter_fill, i, bar_x_offset, bar_y_offset, segment_width, segment_height);
			bar_x_offset += bar_segment_dif;
		}
		graphics.setColour(graphics.green);
		
		bar_x_offset += 16;
		String mul_var = String.format("%d", multiplier);
		graphics.print("x", bar_x_offset, 18, 3);
		graphics.print(mul_var, bar_x_offset + 32, 4, 5);
	}


	private void drawMultiplierSegment(int meter_fill, int segment_number, int bar_x_offset, int bar_y_offset, int segment_width, int segment_height) {
		int start_x = segment_number*segment_width;
		int end_x = start_x + segment_width;
		
		if ((meter_fill >= start_x) && (meter_fill < end_x)) {
			graphics.rectangle(true, bar_x_offset, bar_y_offset, (meter_fill - start_x), segment_height);
		}
		if (meter_fill >= end_x) {
			graphics.rectangle(true, bar_x_offset, bar_y_offset, segment_width, segment_height);
		}
		else;
	}
	
	public void update() {
		if (target_score - total_score <= 9) 
			total_score = target_score;
		else
			total_score += multiplier*2 + 1; // Add 1 so it's an odd number and will affect all digits
		if (target_meter_fill != meter_fill) {
			if (target_meter_fill > meter_fill) {
				meter_draining = false;
				meter_fill++;
			} else {
				meter_draining = true;
				if (meter_fill - target_meter_fill > 2)
					meter_fill -= 2;
				else
					meter_fill--;
			}
			updateMultiplierLevel();
		} else
			meter_draining = false;
	}
}