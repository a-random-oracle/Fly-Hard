package cls;

import java.util.Arrays;

import lib.jog.graphics;

public class Score {
	
	
	private final int maxScore = 9999999;
	private final int maxDigitsInScore = (int)Math.log10(maxScore) + 1;
	
	private int currentDigitsInScore;
	
	/**
	 * Records the total score the user has achieved at a given time.
	 */	
	private int totalScore = 0;
	private int targetScore = 0;
	
	/**
	 * Getter for total score in case it is needed outside the Demo class.
	 * @return totalScore
	 */	
	public int getTotalScore() {
		if (totalScore > maxScore) totalScore = maxScore;
		return totalScore;
	}
	
	public int getTargetScore() {
		return targetScore;
	}
	
	/**
	 * Allows for increasing total score outside of Demo class.
	 * @param scoreDifference
	 */	
	public void increaseTotalScore(int amount) {
		if (amount > 0)
			targetScore += amount;
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
	private int meterFill = 0;
	
	private int targetMeterFill = 0;
	
	/**
	 * This variable determines the current level of the multiplier. Each level has an associated multiplier value
	 * e.g. multiplier_level = 1 -> multiplier = 1, multiplier_level = 2 -> multiplier = 3, multiplier_level = 3 -> multiplier = 5.
	 * Increased when meterFill >= 256, decreased when meterFill < 0.
	 * Also used in Demo to vary max_aircraft and aircraft_spawn rates.
	 */
	private int multiplierLevel = 1;
	
	/**
	 * Resets the multiplier_level to 1 and empties the meter.
	 */	
	public void resetMultiplier() {
		multiplierLevel = 1;
		multiplier = 1;
		targetMeterFill = 0;
		meterFill = 0;
	} 
	
	private boolean meterDraining = false;
	
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
		return meterFill;
	}
	public int getTargetMeterFill() {
		return targetMeterFill;
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
		if (meterFill >= 256) {
			if (multiplierLevel != 5) {
				increaseMultiplierLevel();
				meterFill -= 256;
				targetMeterFill -= 256;
			}
			else {
				meterFill = 256;
				targetMeterFill = 256;
			}
		}
			
		if (meterFill < 0) {
			if (multiplierLevel != 1) {
				decreaseMultiplierLevel();
				meterFill += 256;
				targetMeterFill += 256;
			}
			else {
				meterFill = 0;
				targetMeterFill = 0;
			}
		}	
	}
	
	public void increaseMeterFill(int change_to_meter) {
		targetMeterFill += change_to_meter;
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
		currentDigitsInScore = (getTotalScore() != 0) ? (int)Math.log10(getTotalScore()) + 1 : 0; // exception as log10(0) is undefined.
		char[] chars = new char[maxDigitsInScore - currentDigitsInScore];
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
		
		if (meterDraining) {
			red = 128;
			green = 0;
		}
		for (int i = 0; i < bar_segments; i++) {
			graphics.setColour(red, green, 0, 64);
			graphics.rectangle(true, bar_x_offset, bar_y_offset, segment_width, segment_height);
			graphics.setColour(red, green, 0);
			drawMultiplierSegment(meterFill, i, bar_x_offset, bar_y_offset, segment_width, segment_height);
			bar_x_offset += bar_segment_dif;
		}
		graphics.setColour(graphics.green);
		
		bar_x_offset += 16;
		String mul_var = String.format("%d", multiplier);
		graphics.print("x", bar_x_offset, 18, 3);
		graphics.print(mul_var, bar_x_offset + 32, 4, 5);
	}


	private void drawMultiplierSegment(int meterFill, int segment_number, int bar_x_offset, int bar_y_offset, int segment_width, int segment_height) {
		int start_x = segment_number*segment_width;
		int end_x = start_x + segment_width;
		
		if ((meterFill >= start_x) && (meterFill < end_x)) {
			graphics.rectangle(true, bar_x_offset, bar_y_offset, (meterFill - start_x), segment_height);
		}
		if (meterFill >= end_x) {
			graphics.rectangle(true, bar_x_offset, bar_y_offset, segment_width, segment_height);
		}
		else;
	}
	
	public void update() {
		if (targetScore - totalScore <= 9) 
			totalScore = targetScore;
		else
			totalScore += multiplier*2 + 1; // Add 1 so it's an odd number and will affect all digits
		if (targetMeterFill != meterFill) {
			if (targetMeterFill > meterFill) {
				meterDraining = false;
				meterFill++;
			} else {
				meterDraining = true;
				if (meterFill - targetMeterFill > 2)
					meterFill -= 2;
				else
					meterFill--;
			}
			updateMultiplierLevel();
		} else
			meterDraining = false;
	}
}