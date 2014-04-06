package cls;

import java.io.Serializable;

import cls.Aircraft;

public class Score implements Serializable {

	// TODO last updated: 2014.04.06 01:15
	private static final long serialVersionUID = -1518491377911752885L;
	
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
