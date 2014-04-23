package cls;

import java.util.HashMap;

import scn.Game;
import scn.MultiPlayerGame;

import cls.Powerup.PowerUp;
import net.NetworkManager;

public class PowerupEffect {
	
	private static final HashMap<Effect, Integer> DURATIONS = new HashMap<Effect, Integer>();
	private enum Effect {FOG, SPEED_UP, SLOW_DOWN, TRANSFER};
	private Effect effect;
	

	public PowerupEffect(Powerup powerup) {
		this.effect = getEffectFromType(powerup.getType());
		
		// Set up the durations map
		DURATIONS.put(Effect.FOG,  5000);
		DURATIONS.put(Effect.SLOW_DOWN,  5000);
		DURATIONS.put(Effect.SPEED_UP,  5000);
		DURATIONS.put(Effect.TRANSFER,  5000);
		
	}

	// Powerup Handler

	public static void doPowerup(Powerup powerup, Aircraft aircraft){
		switch (powerup.getType()) {
		case FOG:
			//handleFog();
			break;
		case SLOW_DOWN:
			//handleSlowDown();
			break;
		case SPEED_UP:
			//handleSpeedUp();
			break;
		case TRANSFER:
			handleTransfer(aircraft);
			break;	
		}
	}
	
	private Effect getEffectFromType(PowerUp type) {
		switch (type) {
		case FOG:
			return Effect.FOG;
		case SLOW_DOWN:
			return Effect.SLOW_DOWN;
		case SPEED_UP:
			return Effect.SPEED_UP;
		case TRANSFER:
			return Effect.TRANSFER;
			
			default: 
				return null;
		}
	}

	/** Transfers control of a players plane to the other player */
	public static void handleTransfer(Aircraft aircraft){

		if (aircraft != null) {
			((MultiPlayerGame) Game.getInstance()).getAircraftUnderTransfer().add(aircraft);
			((MultiPlayerGame) Game.getInstance()).getOpposingPlayer().getAircraft().add(aircraft);
			((MultiPlayerGame) Game.getInstance()).getPlayer().getAircraft().remove(aircraft);

			NetworkManager.sendData(-1, new Player[] {
					((MultiPlayerGame) Game.getInstance()).getPlayer(),
					((MultiPlayerGame) Game.getInstance()).getOpposingPlayer()
			});

			Game.getInstance().deselectAircraft(((MultiPlayerGame) Game.getInstance()).getPlayer());
		}
	}

	/** Speeds up other players planes, 2x speed */
	public static void handleSpeedUp(Aircraft aircraft){
		for (Aircraft a : ((MultiPlayerGame) Game.getInstance()).getOpposingPlayer().getAircraft()){
			a.getVelocity().scaleBy(2);

			NetworkManager.sendData(-1, new Player[] {
					((MultiPlayerGame) Game.getInstance()).getPlayer(),
					((MultiPlayerGame) Game.getInstance()).getOpposingPlayer()
			});

		}


	}

}
