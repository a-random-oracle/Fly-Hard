package cls;

import cls.Aircraft;

public class Score {

	private int totalScore = 0;
	
	
	/**
	 * Method for adding score to total score after a plane
	 * has landed or completed its flight plan.
	 * @param aircraft
	 * 			the aircraft to add the score of
	 */
	public void addScore(Aircraft aircraft){
		this.totalScore += aircraft.getScore();
	}
	
	public int getScore() {
		return this.totalScore;
	}
}
