package cls;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

import scn.Demo;
import lib.RandomNumber;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;

/**
 * <h1>Aircraft</h1>
 * <p>Represents an aircraft. Calculates velocity, route-following, etc.</p>
 */
public class Aircraft {
	private final static int RADIUS = 16; // The physical size of the plane in pixels. This determines crashes.
	private final static int MOUSE_LENIENCY = 32;  // How far away (in pixels) the mouse can be from the plane but still select it.
	public final static int COMPASS_RADIUS = 64; // How large to draw the bearing circle.
	private final static audio.Sound WARNING_SOUND = audio.newSoundEffect("sfx" + File.separator + "beep.ogg"); // Used during separation violation
	
	private static int minimumSeparation; // Depends on difficulty

	private graphics.Image image; // The plane image
	private double turnSpeed; // How much the plane can turn per second - in radians.
	private String flightName; // Unique and generated randomly - format is Flight followed by a random number between 100 and 900 e.g Flight 404
	private double creationTime; // Used to calculate how long an aircraft spent in the airspace
	private double optimalTime; // Optimal time a plane needs to reach its exit point
	
	private Vector position, velocity;
	private boolean isManuallyControlled = false;
	private boolean hasFinished = false; // If destination is airport, must be given a land command bnefore it returns True 
	public boolean isWaitingToLand; // If the destination is the airport, True until land() is called. 
	private double turningBy = 0; // In radians
	private int verticalVelocity; // The speed to climb or fall by. Depends on difficulty
	private FlightPlan flightPlan;
	private boolean isLanding = false;
	private int turningCumulative = 0;
	
	public Vector currentTarget; // The position the plane is currently flying towards (if not manually controlled).
	private double manualBearingTarget = Double.NaN;
	private int currentRouteStage = 0;
	private int altitudeState; // Whether the plane is climbing or falling

	private double departureTime; // Used when calculating when a label representing the score a particular plane scored should disappear

	private boolean collisionWarningSoundFlag = false;
	
	private int baseScore; // Each plane has its own base score that increases total score when a plane successfully leaves the airspace
	private int individualScore;
	private int additionToMultiplier = 1; // This variable increases the multiplierVariable when a plane successfully leaves the airspace.
	
	private java.util.ArrayList<Aircraft> planesTooNear = new java.util.ArrayList<Aircraft>(); // Holds a list of planes currently in violation of separation rules with this plane
	
	/**
	 * Static ints for use where altitude state is to be changed.
	 */
	public static final int ALTITUDE_CLIMB = 1;
	public static final int ALTITUDE_FALL = -1;
	public static final int ALTITUDE_LEVEL = 0;
	
	// Getters
	/**
	 * Used to get (system) time when an aircraft was created.
	 * @return Time when aircraft was created.
	 */
	public double getTimeOfCreation() {
		return creationTime;
	}	

	/**
	 * Used to get (system) time when an aircraft successfully departed.
	 * @return Time when aircraft departed.
	 */
	public double getTimeOfDeparture() {
		return departureTime;
	}
	
	/**
	 * Getter for optimal time.
	 * @return Optimal time for an aircraft to complete its path.
	 */
	public double getOptimalTime() {
		return optimalTime;
	}
	
	/**
	 * Used to get a base score per plane outside of Aircraft class.
	 * @return base score for plane
	 */
	public int getBaseScore() {
		return baseScore;
	}

	/**
	 * Gets the score for a specific aircraft.
	 * @return individual score for plane
	 */
	public int getScore() {
		return individualScore;
	}
	
	/**
	 * Used to get a additionToMultiplier outside of Aircraft class.
	 * @return additionToMultiplier
	 */
	public int getAdditionToMultiplier() {
		return additionToMultiplier;
	}
	
	public Vector getPosition() {
		return position;
	}

	public String getName() {
		return flightName;
	}

	public boolean isFinished() { // Returns whether the plane has reached its destination
		return hasFinished;
	}

	public boolean isManuallyControlled() {
		return isManuallyControlled;
	}

	public int getAltitudeState() {
		return altitudeState;
	}
	
	public double getBearing() {
		return Math.atan2(velocity.getY(), velocity.getX());
	}

	public double getSpeed() {
		return velocity.magnitude();
	}
	
	public FlightPlan getFlightPlan() {
		return flightPlan;
	}
	
	// Setters
	/**
	 * Used outside of Aircraft class to assign a (system) time to a plane that successfully left airspace
	 * @param departureTime (system time when a plane departed)
	 */
	public void setDepartureTime(double departureTime) {
		this.departureTime = departureTime;
	}

	/**
	 * Sets the score for a specific aircraft.
	 */
	public void setScore(int score) {
		this.individualScore = score;
	}

	/**
	 * Used to set additionToMultiplier outside of Aircraft class.
	 * @param number
	 */
	public void setAdditionToMultiplier(int multiplierLevel) {
		switch (multiplierLevel) {
		case 1:
			additionToMultiplier = 64;
			break;
		case 2:
			additionToMultiplier = 32;
			break;
		case 3:
			additionToMultiplier = 32;
			break;
		case 4:
			additionToMultiplier = 16;
			break;
		case 5:
			additionToMultiplier = 8;
			break;
		}
	}
	
	public void setBearing(double newHeading) {
		this.manualBearingTarget = newHeading;
	}
	
	private void setAltitude(int height) {
		this.velocity.setZ(height);
	}
	
	public void setAltitudeState(int state) {
		this.altitudeState = state; // Either climbing or falling
	}

	/**
	 * Constructor for an aircraft.
	 * @param name the name of the flight.
	 * @param nameOrigin the name of the location from which the plane hails.
	 * @param nameDestination the name of the location to which the plane is going.
	 * @param originPoint the point to initialise the plane.
	 * @param destinationPoint the end point of the plane's route.
	 * @param img the image to represent the plane.
	 * @param speed the speed the plane will travel at.
	 * @param sceneWaypoints the waypoints on the map.
	 * @param difficulty the difficulty the game is set to
	 */
	public Aircraft(String name, String nameDestination, String nameOrigin, Waypoint destinationPoint, Waypoint originPoint, graphics.Image img, double speed, Waypoint[] sceneWaypoints, int difficulty) {
		flightName = name;		
		flightPlan = new FlightPlan(sceneWaypoints, nameOrigin, nameDestination, originPoint, destinationPoint);		
		image = img;
		creationTime = System.currentTimeMillis() / 1000; // System time when aircraft was created in seconds.
		position = originPoint.getLocation();
		
		if (originPoint.getLocation() == Demo.airport.getLocation()) {
			position = position.add(new Vector(-100, -70, 0)); // Start at departures
		}
		int altitudeOffset = RandomNumber.randInclusiveInt(0, 1) == 0 ? 28000 : 30000;
		position = position.add(new Vector(0, 0, altitudeOffset));

		// Calculate initial velocity (direction)
		currentTarget = flightPlan.getRoute()[0].getLocation();
		double x = currentTarget.getX() - position.getX();
		double y = currentTarget.getY() - position.getY();
		velocity = new Vector(x, y, 0).normalise().scaleBy(speed);

		isWaitingToLand = flightPlan.getDestination().equals(Demo.airport.getLocation());

		// Speed up plane for higher difficulties
		switch (difficulty) {
		// Adjust the aircraft's attributes according to the difficulty of the parent scene
		// 0 has the easiest attributes (slower aircraft, more forgiving separation rules)
		// 2 has the hardest attributes (faster aircraft, least forgiving separation rules)
		case Demo.DIFFICULTY_EASY:
			minimumSeparation = 64;
			turnSpeed = Math.PI / 4;
			verticalVelocity = 500;
			baseScore = 60;
			optimalTime = flightPlan.getTotalDistance() / speed;
		break;

		case Demo.DIFFICULTY_MEDIUM:
			minimumSeparation = 96;
			velocity = velocity.scaleBy(2);
			turnSpeed = Math.PI / 3;
			verticalVelocity = 300;
			baseScore = 150;
			optimalTime = flightPlan.getTotalDistance() / (speed * 2);
		break;
			
		case Demo.DIFFICULTY_HARD:
			minimumSeparation = 128;
			velocity = velocity.scaleBy(3);
			// At high velocities, the aircraft is allowed to turn faster - this helps keep the aircraft on track.
			turnSpeed = Math.PI / 2;
			verticalVelocity = 200;
			baseScore = 300;
			additionToMultiplier = 3;
			optimalTime = flightPlan.getTotalDistance() / (speed * 3);
		break;

		default:
			Exception e = new Exception("Invalid Difficulty: " + difficulty + ".");
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the angle from the plane's position, to its current target.
	 * @return the angle in radians to the plane's current target.
	 */
	private double angleToTarget() {
		if (isManuallyControlled) {
			return (manualBearingTarget == Double.NaN) ? getBearing(): manualBearingTarget;
		} else {
			return Math.atan2(currentTarget.getY() - position.getY(), currentTarget.getX() - position.getX());
		}
	}

	public boolean isOutOfAirspaceBounds() {
		double x = position.getX();
		double y = position.getY();
		return (x < (RADIUS/2) || x > window.width() - (RADIUS/2) || y < (RADIUS/2) || y > window.height() + RADIUS - 176);
	}

	public boolean isAt(Vector point) {
		turningCumulative = 0;
		double dy = point.getY() - position.getY();
		double dx = point.getX() - position.getX();
		return dy*dy + dx*dx < 6;
	}

	public boolean isTurningLeft() {
		return turningBy < 0;
	}

	public boolean isTurningRight() {
		return turningBy > 0;
	}
	
	/**
	 * Edits the plane's path by changing the waypoint it will go to at a certain stage in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		if ((!newWaypoint.isEntryOrExit()) && (routeStage > -1)) {
			flightPlan.alterPath(routeStage, newWaypoint);
			if (!isManuallyControlled)
				resetBearing();
			if (routeStage == currentRouteStage) {
				currentTarget = newWaypoint.getLocation();
				//turnTowardsTarget(0);
			}
		}
	}

	public boolean isMouseOver(int mx, int my) {
		double dx = position.getX() - mx;
		double dy = position.getY() - my;
		return dx * dx + dy * dy < MOUSE_LENIENCY * MOUSE_LENIENCY;
	}

	/**
	 * Calls {@link isMouseOver()} using {@link input.mouseX()} and {@link input.mouseY()} as the arguments.
	 * @return true, if the mouse is close enough to this plane. False, otherwise.
	 */
	public boolean isMouseOver() {
		return isMouseOver(input.mouseX() - Demo.xOffset, input.mouseY() - Demo.yOffset);
	}
	
	public boolean isAtDestination() {
		if (flightPlan.getDestination().equals(Demo.airport.getLocation())) { // At airport
			turningCumulative = 0;
			return Demo.airport.isWithinArrivals(position, false); // Within Arrivals rectangle
		} else {
			return isAt(flightPlan.getDestination()); // Very close to destination
		}
	}

	/**
	 * Updates the plane's position and bearing, the stage of its route, and whether it has finished its flight.
	 * @param time_difference
	 */
	public void update(double time_difference) {
		if (hasFinished) return;
		
		// Update altitude
		if (isLanding) {
			if (position.getZ() > 100) { 
				position.setZ(position.getZ() - 2501 * time_difference); // Decrease altitude rapidly (2501/second), ~11 seconds to fully descend
			} else { // Gone too low, land it now
				Demo.airport.isActive = false;
				hasFinished = true;
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
		Vector dv = velocity.scaleBy(time_difference);
		position = position.add(dv);

		turningBy = 0;

		// Update target		
		if (currentTarget.equals(flightPlan.getDestination()) && isAtDestination()) { // At finishing point
			if (!isWaitingToLand) { // Ready to land
				hasFinished = true;
				if (flightPlan.getDestination().equals(Demo.airport.getLocation())) { // Landed at airport
					Demo.airport.isActive = false;
				}
			}
		} else if (isAt(currentTarget)) {
			currentRouteStage++;
			// Next target is the destination if you're at the end of the plan, otherwise it's the next waypoint
			currentTarget = currentRouteStage >= flightPlan.getRoute().length ? flightPlan.getDestination() : flightPlan.getRoute()[currentRouteStage].getLocation();
		}

		// Update bearing
		if (Math.abs(angleToTarget() - getBearing()) > 0.01) {
			turnTowardsTarget(time_difference);
		}
	}

	public void turnLeft(double time_difference) {
		turnBy(time_difference * -turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	public void turnRight(double time_difference) {
		turnBy(time_difference * turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	/**
	 * Turns the plane by a certain angle (in radians). Positive angles turn the plane clockwise.
	 * @param angle the angle by which to turn.
	 */
	private void turnBy(double angle) {
		turningBy = angle;
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double x = velocity.getX();
		double y = velocity.getY();
		velocity = new Vector(x*cosA - y*sinA, y*cosA + x*sinA, velocity.getZ());
	}

	private void turnTowardsTarget(double time_difference) {
		// Get difference in angle
		double angleDifference = (angleToTarget() % (2 * Math.PI)) - (getBearing() % (2 * Math.PI));
		boolean crossesPositiveNegativeDivide = angleDifference < -Math.PI * 7 / 8;
		// Correct difference
		angleDifference += Math.PI;
		angleDifference %= (2 * Math.PI);
		angleDifference -= Math.PI;
		// Get which way to turn.
		int angleDirection = (int) (angleDifference /= Math.abs(angleDifference));
		if (crossesPositiveNegativeDivide)
			angleDirection *= -1;
		double angleMagnitude = Math.min(Math.abs((time_difference * turnSpeed)), Math.abs(angleDifference));
		if (Math.abs(angleToTarget()) >= (Math.PI / 2)) angleMagnitude *= 1.75;
		turnBy(angleMagnitude * angleDirection);
	}

	/**
	 * Draws the plane and any warning circles if necessary.
	 * @param The altitude to highlight aircraft at
	 */
	public void draw(int highlightedAltitude) {
		double alpha;
		if (position.getZ() >= 28000 && position.getZ() <= 29000) { // 28000-29000
			alpha = highlightedAltitude == 28000 ? 255 : 128; // 255 if highlighted, else 128
		} else if (position.getZ() <= 30000 && position.getZ() >= 29000) { // 29000-30000
			alpha = highlightedAltitude == 30000 ? 255 : 128; // 255 if highlighted, else 128
		} else { // If it's not 28000-30000, then it's currently landing
			alpha = 128; 
		}
		double scale = 2*(position.getZ()/30000); // Planes with lower altitude are smaller
		
		// Draw plane image
		graphics.setColour(128, 128, 128, alpha);
		graphics.draw(image, scale, position.getX()-image.width()/2, position.getY()-image.height()/2, getBearing(), 8, 8);
		
		// Draw altitude label
		graphics.setColour(128, 128, 128, alpha/2.5);
		graphics.print(String.format("%.0f", position.getZ()) + "+", position.getX()+8, position.getY()-8); // � displayed as ft
		drawWarningCircles();
	}

	/**
	 * Draws the compass around this plane - Used for manual control
	 */
	public void drawCompass() {
		graphics.setColour(graphics.green);
		
		// Centre positions of aircraft
		Double xpos = position.getX()-image.width()/2 + Demo.xOffset; 
		Double ypos = position.getY()-image.height()/2 + Demo.yOffset;
		
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
			double r = Math.atan2(input.mouseY() - position.getY(), input.mouseX() - position.getX());
			x = xpos + (COMPASS_RADIUS * Math.cos(r));
			y = ypos + (COMPASS_RADIUS * Math.sin(r));
			// Draw several lines to make the line thicker
			graphics.line(xpos, ypos, x, y);
			graphics.line(xpos-1, ypos, x, y);
			graphics.line(xpos, ypos-1, x, y);
			graphics.line(xpos+1, ypos, x, y);
			graphics.line(xpos+1, ypos+1, x, y);
			graphics.setColour(0, 128, 0, 16);
		}

		// Draw current bearing line
		x = xpos + (COMPASS_RADIUS * Math.cos(getBearing()));
		y = ypos + (COMPASS_RADIUS * Math.sin(getBearing()));
		// Draw several lines to make it thicker
		graphics.line(xpos, ypos, x, y);
		graphics.line(xpos-1, ypos, x, y);
		graphics.line(xpos, ypos-1, x, y);
		graphics.line(xpos+1, ypos, x, y);
		graphics.line(xpos+1, ypos+1, x, y);
	}

	/**
	 * Draws warning circles around this plane and any others that are too near.
	 */
	private void drawWarningCircles() {
		for (Aircraft plane : planesTooNear) {
			Vector midPoint = position.add(plane.position).scaleBy(0.5);
			double radius = position.sub(midPoint).magnitude() * 2;
			graphics.setColour(graphics.red);
			graphics.circle(false, midPoint.getX(), midPoint.getY(), radius);
		}
	}

	/**
	 * Draws lines starting from the plane, along its flight path to its destination.
	 */
	public void drawFlightPath(boolean is_selected) {
		if (is_selected) {
			graphics.setColour(0, 128, 128);
		} else {
			graphics.setColour(0, 128, 128, 128);
		}

		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();
		
		if (currentTarget != destination) {
			// Draw line from plane to next waypoint
			graphics.line(position.getX()-image.width()/2, position.getY()-image.height()/2, route[currentRouteStage].getLocation().getX(), route[currentRouteStage].getLocation().getY());
		} else {
			// Draw line from plane to destination
			graphics.line(position.getX()-image.width()/2, position.getY()-image.height()/2, destination.getX(), destination.getY());			
		}
		
		for (int i = currentRouteStage; i < route.length-1; i++) { // Draw lines between successive waypoints
			graphics.line(route[i].getLocation().getX(), route[i].getLocation().getY(), route[i+1].getLocation().getX(), route[i+1].getLocation().getY());	
		}
	}

	/**
	 * Visually represents the waypoint being moved.
	 * @param modified the index of the waypoint being modified
	 * @param mouseX current position of mouse
	 * @param mouseY current position of mouse
	 */
	public void drawModifiedPath(int modified, double mouseX, double mouseY) {
		graphics.setColour(0, 128, 128, 128);
		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();
		if (currentRouteStage > modified - 1) {
			graphics.line(getPosition().getX(), getPosition().getY(), mouseX, mouseY);
		} else {
			graphics.line(route[modified-1].getLocation().getX(), route[modified-1].getLocation().getY(), mouseX, mouseY);
		}
		if (currentTarget == destination) {
			graphics.line(mouseX, mouseY, destination.getX(), destination.getY());
		} else {
			int index = modified + 1;
			if (index == route.length) { // Modifying final waypoint in route
				// Line drawn to final waypoint
				graphics.line(mouseX, mouseY, destination.getX(), destination.getY());
			} else {
				graphics.line(mouseX, mouseY, route[index].getLocation().getX(), route[index].getLocation().getY());
			}
		}
	}

	/**
	 * Updates the number of planes that are violating the separation rule. Also checks for crashes.
	 * @param time_difference the time elapsed since the last frame.
	 * @param aircraftList all aircraft in the airspace
	 * @param global score object used to decrement score if separation is breached
	 * @return index of plane breaching separation distance with this plane, or -1 if no planes are in violation.
	 */
	public int updateCollisions(double time_difference,	ArrayList<Aircraft> aircraftList, Score score) {
		planesTooNear.clear();
		for (int i = 0; i < aircraftList.size(); i++) {
			Aircraft plane = aircraftList.get(i);
			if (plane != this && isWithin(plane, RADIUS)) { // Planes crash
				hasFinished = true;
				return i;
			} else if (plane != this && isWithin(plane, minimumSeparation)) { // Breaching separation distance
				planesTooNear.add(plane);
				score.increaseMeterFill(-1); // Punishment for breaching separation rules (applies to all aircraft involved - usually 2)
				if (!collisionWarningSoundFlag) {
					collisionWarningSoundFlag = true;
					WARNING_SOUND.play();
				}
			}
		}
		if (planesTooNear.isEmpty()) {
			collisionWarningSoundFlag = false;
		}
		return -1;
	}

	/**
	 * Checks whether an aircraft is within a certain distance from this one.
	 * @param aircraft the aircraft to check.
	 * @param distance the distance within which to care about.
	 * @return true, if the aircraft is within the distance. False, otherwise.
	 */
	private boolean isWithin(Aircraft aircraft, int distance) {
		double dx = aircraft.getPosition().getX() - position.getX();
		double dy = aircraft.getPosition().getY() - position.getY();
		double dz = aircraft.getPosition().getZ() - position.getZ();
		return dx*dx + dy*dy + dz*dz < distance*distance;
	}

	public void toggleManualControl() {
		if (isLanding) { // Can't manually control while landing
			isManuallyControlled = false;
		} else {
			isManuallyControlled = !isManuallyControlled;
			if (isManuallyControlled) {
				setBearing(getBearing());
			} 
			else {
				resetBearing();
			}
		}
	}

	private void resetBearing() {
		if (currentRouteStage < flightPlan.getRoute().length & flightPlan.getRoute()[currentRouteStage] != null) {
			currentTarget = flightPlan.getRoute()[currentRouteStage].getLocation();
		}
		turnTowardsTarget(0);
	}

	private void climb() {
		if (position.getZ() < 30000 && altitudeState == ALTITUDE_CLIMB)
			setAltitude(verticalVelocity);
		if (position.getZ() >= 30000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 30000);
		}
	}

	private void fall() {
		if (position.getZ() > 28000 && altitudeState == ALTITUDE_FALL)
			setAltitude(-verticalVelocity);
		if (position.getZ() <= 28000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 28000);
		}
	}

	public void land() {
		isWaitingToLand = false;
		isLanding = true;
		isManuallyControlled = false;
		Demo.airport.isActive = true;
	}

	public void takeOff() {
		Demo.airport.isActive = true;
		Demo.takeOffSequence(this);
		creationTime = System.currentTimeMillis() / 1000; // Reset creation time
	}

	/**
	 * Checks if an aircraft is close to an its parameter (entry point).
	 * @param position of a waypoint
	 * @return True it if it close
	 */
	public boolean isCloseToEntry(Vector position) {
		double x = this.getPosition().getX() - position.getX();
		double y = this.getPosition().getY() - position.getY();
		return x*x + y*y <= 300*300;
	}
}