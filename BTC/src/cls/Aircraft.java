package cls;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import btc.Main;
import scn.Game;
import scn.Game.DifficultySetting;
import scn.MultiPlayerGame;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;

/**
 * <h1>Aircraft</h1>
 * <p>
 * Represents an aircraft. Calculates velocity, route-following, etc.
 * </p>
 */
public class Aircraft implements Serializable {

	/** Serialisation ID */
	private static final long serialVersionUID = 3827944832233974467L;

	// Static ints for use where altitude state is to be changed
	public final static int ALTITUDE_CLIMB = 1;
	public final static int ALTITUDE_FALL = -1;
	public final static int ALTITUDE_LEVEL = 0;

	/** The size of the aircraft in pixels */
	private final static int RADIUS = 16;

	/** How far away (in pixels) the mouse can be from the plane
	 * but still select it */
	private final static int MOUSE_LENIENCY = 32;

	/** The size of the compass circle */
	public final static int COMPASS_RADIUS = 64;

	/** The sound to play when the separation distance is violated */
	private final static Sound WARNING_SOUND = audio.newSoundEffect("sfx"
			+ File.separator + "beep.ogg");

	/** The minimum distance planes should keep apart */
	private static int minimumSeparation;

	/** How much the plane can turn per second - in radians */
	private double turnSpeed;

	/** The unique name of the aircraft. Format is the flight's airline followed
	 * by a random number between 100 and 900. */
	private String flightName;

    /** The airline an aircraft is a 'member' of. Purely aesthetic used for the
     * flight strip output. */
    private String airline;

	/** The aircraft's current position */
	private Vector position;

	/** The aircraft's current velocity */
	private Vector velocity;

	/** The aircraft's score */
	private int score;

	/** Whether the aircraft is currently under manual control */
	private boolean isManuallyControlled = false;

	/** Whether the aircraft has reached its destination */
	private boolean hasFinished = false;

	/** Whether the aircraft is currently at an airport and waiting to land */
	public boolean isWaitingToLand;

	/** The speed at which the aircraft will ascend/descend */
	private int verticalVelocity;

	/** The plan the aircraft will follow to reach its destination */
	private FlightPlan flightPlan;

	/** A value representing whether the aircraft is currently landing */
	private boolean isLanding = false;

	/** The point the aircraft is currently heading towards */
	public Vector currentTarget;

	/** The bearing specified whilst the aircraft is under manual control */
	private double manualBearingTarget = Double.NaN;

	/** The stage of its flight path the aircraft is at */
	private int currentRouteStage = 0;

	/** A value representing whether the plane is climbing or falling */
	private int altitudeState;

	/** Whether the collision warning sound is currently playing */
	private boolean collisionWarningSoundFlag = false;

	/** A list of the aircraft violation this aircraft's separation distance */
	private ArrayList<Aircraft> planesTooNear = new ArrayList<Aircraft>();
	
	/** How long the aircraft has been waiting to take off in the airport */
	private int timeWaiting = 0;
	
	/** Whether or not a point penalty has been applied to an aircraft waiting to take off */
	private boolean airportPenaltyApplied = false;
	
	/** If fog is enabled */
	private boolean isFog;

	/**
	 * Constructor for an aircraft.
	 * @param name - the name of the flight
	 * @param nameOrigin - the name of the location from which the plane hails
	 * @param nameDestination - the name of the location to which the plane is going
	 * @param originPoint - the point to initialise the plane
	 * @param destinationPoint - the end point of the plane's route
	 * @param image - the image to represent the plane
	 * @param speed - the speed the plane will travel at
	 * @param sceneWaypoints - the waypoints on the map
	 * @param difficulty - the difficulty the game is set to
	 * @param originAirport - the airport this flight originated at
	 * @param destinationAirport - the airport this flight is heading towards at
	 */
	public Aircraft(String name, String carrier, String nameDestination, String nameOrigin,
			Waypoint destinationPoint, Waypoint originPoint,
			double speed, Waypoint[] sceneWaypoints,
			DifficultySetting difficulty, Airport originAirport,
			Airport destinationAirport) {

		this.flightName = name;
		this.airline = carrier;
		this.flightPlan = new FlightPlan(sceneWaypoints, nameOrigin,
				nameDestination, originPoint, destinationPoint, originAirport,
				destinationAirport);
		this.position = originPoint.getLocation();
		this.isWaitingToLand = (destinationAirport != null);
		this.score = 100;
		this.isFog = false;

		// Set aircraft's altitude to a random height
		int altitudeOffset = (Main.getRandom().nextInt(2)) == 0 ? 28000 : 30000;
		this.position = position.add(new Vector(0, 0, altitudeOffset));

		// Calculate initial velocity (direction)
		this.currentTarget = flightPlan.getRoute()[0].getLocation();
		double x = currentTarget.getX() - position.getX();
		double y = currentTarget.getY() - position.getY();
		this.velocity = new Vector(x, y, 0).normalise().scaleBy(speed);

		// Set the aircraft's difficulty settings
		// e.g. the minimum separation distance, turning speed, velocity
		setDifficultySettings(difficulty);
	}

	/**
	 * Adjust the aircraft's attributes according to the difficulty of the
	 * parent scene.
	 * <p>
	 * 0 has the easiest attributes (slower aircraft, more forgiving separation
	 * rules)
	 * </p>
	 * <p>
	 * 2 has the hardest attributes (faster aircraft, least forgiving separation
	 * rules)
	 * </p>
	 */
	private void setDifficultySettings(DifficultySetting difficulty) {
		switch (difficulty) {
		case EASY:
			minimumSeparation = 64;
			turnSpeed = Math.PI / 4;
			verticalVelocity = 500;
			break;
		case MEDIUM:
			minimumSeparation = 96;
			velocity = velocity.scaleBy(2);
			turnSpeed = Math.PI / 3;
			verticalVelocity = 300;
			break;
		case HARD:
			minimumSeparation = 128;
			velocity = velocity.scaleBy(3);
			// At high velocities, the aircraft is allowed to turn faster - this
			// helps keep the aircraft on track.
			turnSpeed = Math.PI / 2;
			verticalVelocity = 200;
			break;
		default:
			Exception e = new Exception("Invalid Difficulty: " + difficulty
					+ ".");
			e.printStackTrace();
		}
	}

	/**
	 * Updates the plane's position and bearing, the stage of its route, and
	 * whether it has finished its flight.
	 * @param timeDifference - the time since the last update
	 */
	public void update(double timeDifference) {
		if (hasFinished) return;

		// Update altitude
		if (isLanding) {
			if (position.getZ() > 100) {
				// Decrease altitude rapidly (2501/second),
				// ~11 seconds to fully descend
				position.setZ(position.getZ() - 2501 * timeDifference);
			} else { // Gone too low, land it now TODO (check this)
				if (flightPlan.getDestinationAirport() != null) {
					flightPlan.getDestinationAirport().isActive = false;
					hasFinished = true;
				}
			}
		} else {
			switch (altitudeState) {
			case -1:
				fall();
				break;
			case 0:
				break;
			case 1:
				climb();
				break;
			}
		}

		// Update position
		Vector dv = velocity.scaleBy(timeDifference);
		position = position.add(dv);

		// Update target
		if (currentTarget.equals(flightPlan.getDestination())
				&& isAtDestination()) { // At finishing point
			if (!isWaitingToLand) { // Ready to land
				hasFinished = true;
				if (flightPlan.getDestinationAirport() != null) { // Landed at airport
					flightPlan.getDestinationAirport().isActive = false;
				}
			}
		} else if (isAt(currentTarget)) {
			currentRouteStage++;
			// Next target is the destination if you're at the end of the plan,
			// otherwise it's the next waypoint
			currentTarget = (currentRouteStage >= flightPlan.getRoute().length) ? flightPlan
					.getDestination()
					: flightPlan.getRoute()[currentRouteStage].getLocation();
		}

		// Update bearing
		if (Math.abs(angleToTarget() - getBearing()) > 0.01) {
			turnTowardsTarget(timeDifference);
		}
	}

	/**
	 * Calculates the angle from the plane's position, to its current target.
	 * @return the angle in radians to the plane's current target
	 */
	private double angleToTarget() {
		if (isManuallyControlled) {
			return (manualBearingTarget == Double.NaN) ? getBearing()
					: manualBearingTarget;
		} else {
			return Math.atan2(currentTarget.getY() - position.getY()
					+ Waypoint.WAYPOINT_ICON_RADIUS/2,
					currentTarget.getX() - position.getX()
					+ Waypoint.WAYPOINT_ICON_RADIUS/2);
		}
	}

	/**
	 * Checks whether the aircraft is outside the game area.
	 * @return <code>true</code> if the aircraft is outside the game area
	 */
	public boolean isOutOfAirspaceBounds() {
		double x = position.getX();
		double y = position.getY();
		return ((x < (RADIUS / 2))
				|| (x > window.width() - (RADIUS / 2) - (2 * Game.X_OFFSET))
				|| (y < (RADIUS / 2)) || (y > window.height() + (RADIUS / 2)
						- (2 * Game.Y_OFFSET)));
	}

	/**
	 * Checks whether the aircraft is inside the middle zone.
	 * <p>
	 * Specific to multiplayer.
	 * </p>
	 * @return <code>true</code> if the aircraft is inside the middle zone
	 */
	public boolean isInMiddleZone() {
		double x = position.getX();

		return (x > (MultiPlayerGame.leftEntryX)
				&& x < (MultiPlayerGame.rightEntryX));
	}

	/**
	 * Checks whether the aircraft of a player is outside its own airspace.
	 * @return <code>true</code> if the player's aircraft goes into
	 * 			the other player's airspace
	 */
	public boolean isOutOfPlayersAirspace() {
		double x = position.getX() + Game.X_OFFSET;
		Player player = null;

		if (Game.getInstance() != null) {
			player = Game.getInstance().getPlayerFromAircraft(this);
		}

		if (player != null) {
			if (Game.getInstance().getPlayerFromAircraft(this).getID() == 0) {
				return (x > MultiPlayerGame.rightEntryX);
			} else {
				return (x < MultiPlayerGame.leftEntryX);
			}
		}
		return false;
	}

	/**
	 * Checks if the aircraft is at (or near to) a specified point.
	 * @param point - the point to check
	 * @return <code>true</code> if the aircraft is at the specified point
	 */
	public boolean isAt(Vector point) {
		double dy = point.getY() - position.getY();
		double dx = point.getX() - position.getX();
		return (dy * dy) + (dx * dx) < (10 * 10);
	}

	/**
	 * Edits the plane's path by changing the waypoint it will go to at a
	 * certain stage in its route.
	 * @param routeStage - the stage at which the new waypoint will replace the old
	 * @param newWaypoint - the new waypoint to travel to
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		if ((!newWaypoint.isEntryOrExit()) && (routeStage > -1)) {
			flightPlan.alterPath(routeStage, newWaypoint);
			//decrement score as a penalty for altering flightplan
			decrementScoreSmall();
			if (!isManuallyControlled)
				resetBearing();
			if (routeStage == currentRouteStage) {
				currentTarget = newWaypoint.getLocation();
				// turnTowardsTarget(0);
			}
		}
	}

	/**
	 * Checks whether the mouse cursor is over this aircraft.
	 * @param mx - the x coordinate of the mouse cursor
	 * @param my - the y coordinate of the mouse cursor
	 * @return <code>true</code>, if the mouse is close enough to this aircraft;
	 * 			<code>false</code> otherwise
	 */
	public boolean isMouseOver(int mx, int my) {
		double dx = position.getX() - mx;
		double dy = position.getY() - my;
		return dx * dx + dy * dy < MOUSE_LENIENCY * MOUSE_LENIENCY;
	}

	/**
	 * Calls {@link #isMouseOver(int, int)} using {@link lib.jog.input#mouseX()} and
	 * {@link  lib.jog.input#mouseY()} as the arguments.
	 * @return <code>true</code>, if the mouse is close enough to this aircraft;
	 * 			<code>false</code> otherwise
	 */
	public boolean isMouseOver() {
		return isMouseOver(input.mouseX() - Game.X_OFFSET, input.mouseY()
				- Game.Y_OFFSET);
	}

	/**
	 * Checks if the aircraft is at its destination.
	 * @return <code>true</code> if the aircraft is at its destination
	 */
	public boolean isAtDestination() {
		if (flightPlan.getDestinationAirport() != null) { // At airport
			return flightPlan.getDestinationAirport().isWithinArrivals(position, false);
		} else {
			//System.out.println(this.airline);
			//System.out.println(this.flightName);
			return isAt(flightPlan.getDestination());
		}
	}

	/**
	 * Causes the aircraft to turn left.
	 * @param timeDifference - the time since the last update
	 */
	public void turnLeft(double timeDifference) {
		turnBy(timeDifference * -turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	/**
	 * Causes the aircraft to turn right.
	 * @param timeDifference - the time since the last update
	 */
	public void turnRight(double timeDifference) {
		turnBy(timeDifference * turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	/**
	 * Turns the plane by a certain angle (in radians).
	 * <p>
	 * Positive angles turn the plane clockwise.
	 * </p>
	 * @param angle - the angle by which to turn
	 */
	private void turnBy(double angle) {
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double x = velocity.getX();
		double y = velocity.getY();

		velocity = new Vector((x * cosA) - (y * sinA), (y * cosA) + (x * sinA),
				velocity.getZ());
	}

	/**
	 * Causes the aircraft to turn towards its target.
	 * @param timeDifference - the time since the last update
	 */
	private void turnTowardsTarget(double timeDifference) {
		// Get difference in angle
		double angleDifference = (angleToTarget() % (2 * Math.PI))
				- (getBearing() % (2 * Math.PI));
		boolean crossesPositiveNegativeDivide = angleDifference < -Math.PI * 7 / 8;

		// Correct difference
		angleDifference += Math.PI;
		angleDifference %= (2 * Math.PI);
		angleDifference -= Math.PI;

		// Get which way to turn.
		int angleDirection = (int) (angleDifference /= Math
				.abs(angleDifference));
		if (crossesPositiveNegativeDivide)
			angleDirection *= -1;

		double angleMagnitude = Math.min(
				Math.abs((timeDifference * turnSpeed)),
				Math.abs(angleDifference));

		// Scale if the angle is greater than 90 degrees
		// This allows aircraft to break out of loops around waypoints
		if (Math.abs(angleToTarget()) >= (Math.PI / 2))
			angleMagnitude *= 1.75;
		turnBy(angleMagnitude * angleDirection);
	}

	/**
	 * Draws the plane and any warning circles if necessary.
	 * @param colour - the colour to draw the aircraft
	 * @param highlightedAltitude - the altitude to highlight aircraft at
	 */
	public void draw(Integer[] colour, int highlightedAltitude) {
		draw(colour, highlightedAltitude, null);
	}

	/**
	 * Draws the plane and any warning circles if necessary.
	 * <p>
	 * Also allows an offset to be applied.
	 * </p>
	 * @param colour - the colour to draw the aircraft
	 * @param highlightedAltitude - the altitude to highlight aircraft at
	 * @param offset - a manual offset to apply
	 */
	public void draw(Integer[] colour, int highlightedAltitude, Vector offset) {
		double alpha;
		if (position.getZ() >= 28000 && position.getZ() <= 29000) { // 28000-29000
			// 255 if highlighted, else 128
			alpha = highlightedAltitude == 28000 ? 255 : 128;
		} else if (position.getZ() <= 30000 && position.getZ() >= 29000) { // 29000-30000
			// 255 if highlighted, else 128
			alpha = highlightedAltitude == 30000 ? 255 : 128;
		} else { // If it's not 28000-30000, then it's currently landing
			alpha = 128;
		}

		// Draw planes with a lower altitude smaller
		double scale = 2 * (position.getZ() / 30000);

		// Draw plane image
		graphics.setColour(colour[0], colour[1], colour[2], alpha);

		if (offset != null) {
			graphics.draw(Game.aircraftImage, scale,
					position.getX() - (Game.aircraftImage.width() / 2)
					+ offset.getX(), position.getY()
					- (Game.aircraftImage.height() / 2)
					+ offset.getY(), getBearing(), (RADIUS / 2), (RADIUS / 2));
		} else {
			graphics.draw(Game.aircraftImage, scale,
					position.getX() - (Game.aircraftImage.width() / 2),
					position.getY()
					- (Game.aircraftImage.height() / 2), getBearing(),
					(RADIUS / 2), (RADIUS / 2));
		}

		// Draw altitude label
		graphics.setColour(128, 128, 128, alpha / 2.5);

		if (offset != null) {
			graphics.print(String.format("%.0f", position.getZ()) + "+",
					position.getX() + (RADIUS / 2) + offset.getX(),
					position.getY() - (RADIUS / 2) + offset.getY());
		} else {
			graphics.print(String.format("%.0f", position.getZ()) + "+",
					position.getX() + (RADIUS / 2), position.getY()
					- (RADIUS / 2));
		}


		
		drawWarningCircles(offset);
		
		//test draw score
		
		graphics.setColour(128, 128, 128, alpha / 2.5);

		/*if (offset != null) {
			graphics.print(String.format("%d", score) + "+",
					position.getX() + (RADIUS / 2) + offset.getX(),
					position.getY() - (RADIUS / 2) + offset.getY());
		} else {
			graphics.print(String.format("%d", score) + "+",
					position.getX() + (RADIUS / 2), position.getY()
					- (RADIUS / 2));
		}*/
	}

	/**
	 * Draws a compass around the aircraft.
	 */
	public void drawCompass() {
		graphics.setColour(graphics.green);

		// Centre positions of aircraft
		Double xpos = position.getX() - (Game.aircraftImage.width() / 2);
		Double ypos = position.getY() - (Game.aircraftImage.height() / 2);

		// Draw the compass circle
		graphics.circle(false, xpos, ypos, COMPASS_RADIUS, 30);

		// Draw the angle labels (0, 60 .. 300)
		for (int i = 0; i < 360; i += 60) {
			double r = Math.toRadians(i - 90);
			double x = xpos + (1.1 * COMPASS_RADIUS * Math.cos(r));
			double y = ypos - 2 + (1.1 * COMPASS_RADIUS * Math.sin(r));
			if (i > 170) x -= 24;
			if (i == 180) x += 12;
			graphics.print(String.valueOf(i), x, y);
		}

		// Draw the line to the mouse pointer
		double x, y;
		if (isManuallyControlled && input.isMouseDown(input.MOUSE_RIGHT)) {
			graphics.setColour(graphics.green_transp);
			double r = Math.atan2((input.mouseY() - Game.Y_OFFSET)
					- position.getY(),
					(input.mouseX() - Game.X_OFFSET)
					- position.getX());
			x = xpos + (COMPASS_RADIUS * Math.cos(r));
			y = ypos + (COMPASS_RADIUS * Math.sin(r));

			// Draw several lines to make the line thicker
			graphics.line(xpos, ypos, x, y);
			graphics.line(xpos - 1, ypos, x, y);
			graphics.line(xpos, ypos - 1, x, y);
			graphics.line(xpos + 1, ypos, x, y);
			graphics.line(xpos + 1, ypos + 1, x, y);
			graphics.setColour(0, 128, 0, 16);
		}

		// Draw current bearing line
		x = xpos + (COMPASS_RADIUS * Math.cos(getBearing()));
		y = ypos + (COMPASS_RADIUS * Math.sin(getBearing()));

		// Draw several lines to make it thicker
		graphics.line(xpos, ypos, x, y);
		graphics.line(xpos - 1, ypos, x, y);
		graphics.line(xpos, ypos - 1, x, y);
		graphics.line(xpos + 1, ypos, x, y);
		graphics.line(xpos + 1, ypos + 1, x, y);
	}

	/**
	 * Draws warning circles around this aircraft and any others
	 * that are too near.
	 */
	private void drawWarningCircles(Vector offset) {
		for (Aircraft plane : planesTooNear) {
			Vector midPoint = position.add(plane.position).scaleBy(0.5);
			double radius = position.sub(midPoint).magnitude() * 2;
			graphics.setColour(graphics.red);

			if (offset != null) {
				graphics.circle(false, midPoint.getX() + offset.getX(),
						midPoint.getY() + offset.getY(), radius);
			} else {
				graphics.circle(false, midPoint.getX(), midPoint.getY(), radius);
			}
		}
	}

	/**
	 * Draws lines starting from the aircraft, along its flight path to its
	 * destination.
	 */
	public void drawFlightPath(boolean isSelected) {
		if (isSelected) {
			graphics.setColour(0, 128, 128);
		} else {
			graphics.setColour(0, 128, 128, 128);
		}

		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();

		if (currentTarget != destination) {
			// Draw line from plane to next waypoint
			graphics.line(position.getX() - Game.aircraftImage.width() / 2,
					position.getY()
					- Game.aircraftImage.height() / 2, route[currentRouteStage]
							.getLocation().getX(), route[currentRouteStage]
									.getLocation().getY());
		} else {
			// Draw line from plane to destination
			graphics.line(position.getX() - Game.aircraftImage.width() / 2,
					position.getY()
					- Game.aircraftImage.height() / 2, destination.getX(),
					destination.getY());
		}

		// Draw lines between successive waypoints
		for (int i = currentRouteStage; i < route.length - 1; i++) {
			graphics.line(route[i].getLocation().getX(), route[i].getLocation()
					.getY(), route[i + 1].getLocation().getX(), route[i + 1]
							.getLocation().getY());
		}
	}

	/**
	 * Visually represents the waypoint being moved.
	 * @param modified - the index of the waypoint being modified
	 * @param mouseX - the current x position of the mouse
	 * @param mouseY - the current y position of the mouse
	 */
	public void drawModifiedPath(int modified, double mouseX, double mouseY) {
		graphics.setColour(0, 128, 128, 128);
		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();

		if (currentRouteStage > modified - 1) {
			graphics.line(getPosition().getX(), getPosition().getY(), mouseX,
					mouseY);
		} else {
			graphics.line(route[modified - 1].getLocation().getX(),
					route[modified - 1].getLocation().getY(), mouseX, mouseY);
		}

		if (currentTarget == destination) {
			graphics.line(mouseX, mouseY, destination.getX(),
					destination.getY());
		} else {
			int index = modified + 1;

			if (index == route.length) { // Modifying final waypoint in route
				// Line drawn to final waypoint
				graphics.line(mouseX, mouseY, destination.getX(),
						destination.getY());
			} else {
				graphics.line(mouseX, mouseY,
						route[index].getLocation().getX(), route[index]
								.getLocation().getY());
			}
		}
	}

	/**
	 * Updates the number of planes that are violating the separation rule. Also
	 * checks for crashes.
	 * @param timeDifference - the time elapsed since the last frame.
	 * @param aircraft - all aircraft in the airspace
	 * @return index of plane breaching separation distance with this plane, or
	 *         -1 if no planes are in violation.
	 */
	public int updateCollisions(double timeDifference, ArrayList<Aircraft> aircraft) {
		planesTooNear.clear();
		for (int i = 0; i < aircraft.size(); i++) {
			Aircraft plane = aircraft.get(i);
			if (plane != this && isWithin(plane, RADIUS)) { // Planes crash
				hasFinished = true;
				return i;
			} else if (plane != this && isWithin(plane, minimumSeparation)) {
				// Breaching separation distance
				planesTooNear.add(plane);
				if (!collisionWarningSoundFlag) {
					collisionWarningSoundFlag = true;
					WARNING_SOUND.play();
				}
				// Decrement score for getting within separation distance
				decrementScoreSmall();
			}
		}
		if (planesTooNear.isEmpty()) {
			collisionWarningSoundFlag = false;
		}
		return -1;
	}

	/**
	 * Checks whether an aircraft is within a certain distance from this one.
	 * @param aircraft - the aircraft to check.
	 * @param distance - the distance within which to care about.
	 * @return true, if the aircraft is within the distance. False, otherwise.
	 */
	private boolean isWithin(Aircraft aircraft, int distance) {
		double dx = aircraft.getPosition().getX() - position.getX();
		double dy = aircraft.getPosition().getY() - position.getY();
		double dz = aircraft.getPosition().getZ() - position.getZ();
		return dx * dx + dy * dy + dz * dz < distance * distance;
	}

	/**
	 * Causes manual control to toggle.
	 * <p>
	 * i.e. if the aircraft is under manual control, control is released
	 * (and vice-versa)
	 * </p>
	 */
	public void toggleManualControl() {
		if (isLanding) { // Can't manually control while landing
			isManuallyControlled = false;
		} else {
			isManuallyControlled = !isManuallyControlled;

			if (isManuallyControlled) {
				setBearing(getBearing());
			} else {
				resetBearing();
			}
		}
	}

	/**
	 * Resets the current target, and causes the aircraft to head towards
	 * the new target.
	 */
	private void resetBearing() {
		if (currentRouteStage < flightPlan.getRoute().length
				& flightPlan.getRoute()[currentRouteStage] != null) {
			currentTarget = flightPlan.getRoute()[currentRouteStage]
					.getLocation();
		}
		turnTowardsTarget(0);
	}

	/**
	 * Causes the aircraft to move to a higher altitude.
	 */
	private void climb() {
		if (position.getZ() < 30000 && altitudeState == ALTITUDE_CLIMB)
			setAltitude(verticalVelocity);
		if (position.getZ() >= 30000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 30000);
		}
	}

	/**
	 * Causes the aircraft to move to a lower altitude.
	 */
	private void fall() {
		if (position.getZ() > 28000 && altitudeState == ALTITUDE_FALL)
			setAltitude(-verticalVelocity);
		if (position.getZ() <= 28000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 28000);
		}
	}

	/**
	 * Causes the aircraft to land at its airport.
	 */
	public void land() {
		if (isFog != true) {
			isWaitingToLand = false;
			isLanding = true;
			isManuallyControlled = false;
			if (flightPlan.getDestinationAirport() != null) {
				flightPlan.getDestinationAirport().isActive = true;
			}
		}
	}

	/**
	 * Adds this aircraft to the player whose airport it is departing from.
	 */
	public void takeOff() {
		if (isFog != true) {
			if (flightPlan.getOriginAirport() != null) {

				// Add the aircraft to the player whose airport
				// it is departing from
				for (Airport airport : Game.getInstance().getAllAirports()) {
					if (airport.equals(flightPlan.getOriginAirport())) {
						Game.getInstance().getPlayerFromAirport(
								airport).getAircraft().add(this);
						return;
					}
				}
			}
		}
	}

	/**
	 * Checks if an aircraft is close to an its parameter (entry point).
	 * @param position - the position of the waypoint to test
	 * @return <code>true</code> if it is close
	 */
	public boolean isCloseToEntry(Vector position) {
		double x = this.getPosition().getX() - position.getX();
		double y = this.getPosition().getY() - position.getY();
		return x * x + y * y <= 300 * 300;
	}

	/**
	 * Gets the aircraft's position.
	 * @return the aircraft's position
	 */
	public Vector getPosition() {
		return position;
	}

    public int getAltitude() {
        return (int)(position.getZ());
    }

	/**
	 * Gets the aircraft's name.
	 * @return the aircraft's name
	 */
	public String getName() {
		return flightName;
	}

    /**
     * Gets the aircraft's carrier/airline.
     * @return the aircraft's carrier/airline.
     */
    public String getAirline() {
        return airline;
    }

	/**
	 * Gets whether or not the aircraft has completed its route.
	 * @return <code>true</code> if the aircraft has finished, otherwise
	 *         <code>false</code>
	 */
	public boolean isFinished() {
		return hasFinished;
	}

	/**
	 * Gets whether or not the aircraft is under manual control.
	 * @return <code>true</code> if the aircraft is under manual control,
	 *         otherwise <code>false</code>
	 */
	public boolean isManuallyControlled() {
		return isManuallyControlled;
	}

	/**
	 * Gets the aircraft's altitude state.
	 * @return the aircraft's altitude state
	 */
	public int getAltitudeState() {
		return altitudeState;
	}

	/**
	 * Gets the aircraft's bearing.
	 * @return the aircraft's bearing
	 */
	public double getBearing() {
		return Math.atan2(velocity.getY(), velocity.getX());
	}

	/**
	 * Gets the aircraft's speed.
	 * @return the aircraft's speed
	 */
	public double getSpeed() {
		return velocity.magnitude();
	}
	
	/** 
	 * Gets the aircrafts velocity
	 * @return the aircraft's velocity
	 */
	public Vector getVelocity() {
		return velocity;
	}

	/**
	 * Gets the aircraft's flight plan.
	 * @return the aircraft's flight plan
	 */
	public FlightPlan getFlightPlan() {
		return flightPlan;
	}
	
	/**
	 * Gets whether fog is enabled or not.
	 * @return whether fog is enabled or not
	 */
	public boolean getIsFog() {
		return isFog;
	}
	
	/**
	 * Sets whether there is fog.
	 * @param fog - whether there is fog
	 */
	public void setIsFog(boolean fog) {
		this.isFog = fog;
	}

	/**
	 * Sets the manual bearing the aircraft is following.
	 * <p>
	 * NOTE: the aircraft will only follow this heading if it is under manual
	 * control.
	 * </p>
	 * @param newHeading - the new bearing to follow
	 */
	public void setBearing(double newHeading) {
		this.manualBearingTarget = newHeading;
	}

	/**
	 * Sets the aircraft's altitude.
	 * @param height - the altitude to move the aircraft to
	 */
	private void setAltitude(int height) {
		this.velocity.setZ(height);
	}

	/**
	 * Sets the aircraft's altitude state to climbing, falling or level.
	 * @param state - the new altitude state: 0 = level, 1 = climbing and
	 *            		-1 = falling
	 */
	public void setAltitudeState(int state) {
		this.altitudeState = state;
	}

	public void overwrite(Aircraft updatedAircraft) {
		// Check that the update insn't null
		if (updatedAircraft == null) return;

		// Check that the IDs match
		if (updatedAircraft.getName() != flightName) return;

		turnSpeed = updatedAircraft.turnSpeed;
	    airline = updatedAircraft.airline;
		velocity = updatedAircraft.velocity;
		score = updatedAircraft.score;
		isManuallyControlled = updatedAircraft.isManuallyControlled;
		hasFinished = updatedAircraft.hasFinished;
		isWaitingToLand = updatedAircraft.isWaitingToLand;
		verticalVelocity = updatedAircraft.verticalVelocity;
		flightPlan = updatedAircraft.flightPlan;
		isLanding = updatedAircraft.isLanding;
		currentTarget = updatedAircraft.currentTarget;
		manualBearingTarget = updatedAircraft.manualBearingTarget;
		currentRouteStage = updatedAircraft.currentRouteStage;
		altitudeState = updatedAircraft.altitudeState;
		collisionWarningSoundFlag = updatedAircraft.collisionWarningSoundFlag;
		planesTooNear = updatedAircraft.planesTooNear;
		timeWaiting = updatedAircraft.timeWaiting;
		airportPenaltyApplied = updatedAircraft.airportPenaltyApplied;
		
		/** The aircraft's current position */
		//private Vector position;
	}
	
	/**
	 * Generates the hash code for this aircraft.
	 * @return the hash code for this aircraft
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((flightName == null) ? 0 : flightName.hashCode());
		return result;
	}

	/**
	 * Compares this aircraft to another aircraft.
	 * <p>
	 * Only the flight name is checked.
	 * </p>
	 * @param obj - the object to compare with
	 * @return <code>true</code> if and only if obj is equivalent
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Aircraft)) {
			return false;
		}
		Aircraft other = (Aircraft) obj;
		if (flightName == null) {
			if (other.flightName != null) {
				return false;
			}
		} else if (!flightName.equals(other.flightName)) {
			return false;
		}
		return true;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int newScore) {
		score = newScore;
	}
	
	public void decrementScoreSmall() {
		if(this.score > 0){
			this.score = this.score - 1;
		}
		else return;
	}
	
	public void decrementScoreLarge() {
		if(this.score > 0) {
			this.score = this.score - 10;
		}
		else return;
	}
	

	public boolean isAirportPenaltyApplied() {
		return airportPenaltyApplied;
	}

	public void setAirportPenaltyApplied(boolean airportPenaltyApplied) {
		this.airportPenaltyApplied = airportPenaltyApplied;
	}

	public int getTimeWaiting() {
		return timeWaiting;
	}

	public void setTimeWaiting(int timeWaiting) {
		this.timeWaiting = timeWaiting;
	}
	
	public void setPosition(Vector newPosition) {
		position = newPosition;
	}
	
	public int getCurrentRouteStage() {
		return currentRouteStage;
	}
	
	public void generateFlightPlan(Waypoint[] sceneWaypoints,
			String nameDestination, Waypoint destinationPoint,
			Airport destinationAirport) {
		flightPlan = new FlightPlan(sceneWaypoints,
				flightPlan.getOriginName(),
				nameDestination,
				new Waypoint(position.getX(), position.getY(),
						false, false),
				destinationPoint,
				flightPlan.getOriginAirport(),
				destinationAirport);
		
		currentRouteStage = 0;
	}

}
