package cls;

import cls.Aircraft;
import scn.SinglePlayerGame;

public class Score {

	private int totalScore = 0;
	
	
	public int getScore(){
		return this.totalScore;
	}
	
	//method for adding score to total score after a plane has landed or completed its flight plan
	public void addScore(Aircraft aircraft){
		this.totalScore += aircraft.getScore();
		}
	
	}
