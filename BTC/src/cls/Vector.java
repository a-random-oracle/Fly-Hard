package cls;

import java.io.Serializable;

import lib.jog.window;
import scn.Game;
import btc.Main;

/**
 * Simplified 3D vector class with basic operations.
 */
public class Vector implements Serializable {
	
	// TODO last updated: 2014.03.19 23:45
	private static final long serialVersionUID = -5086652815637818053L;

	/** The vector's x position */
	private double x;
	
	/** The vector's y position */
	private double y;
	
	/** The vector's z position */
	private double z;
	
	/**
	 * Constructor for a vector.
	 * @param x the vector's x position
	 * @param y the vector's y position
	 * @param z the vector's z position
	 */
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Gets the x position of the vector.
	 * @return the x position of the vector
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gets the y position of the vector.
	 * @return the y position of the vector
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Gets the z position of the vector.
	 * @return the z position of the vector
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * Sets the z value of the vector.
	 * @param z the z value to be set
	 */
	public void setZ(double z) {
		this.z = z;
	}
	
	/**
	 * Calculates the magnitude of the vector.
	 * @return the magnitude of the vector
	 */
	public double magnitude() {
		return Math.sqrt(magnitudeSquared());
	}
	
	/**
	 * Calculates the square of the magnitude of the vector.
	 * @return the square of the magnitude of the vector
	 */
	public double magnitudeSquared() {
		return (x*x) + (y*y) + (z*z);
	}
	
	/**
	 * Normalises the vector.
	 * @return a normalised vector
	 */
	public Vector normalise() {
		return this.scaleBy(1/magnitude());
	}
	
	/**
	 * Scales the vector by a given scalar.
	 * @param n the scalar to scale by
	 * @return the scaled vector
	 */
	public Vector scaleBy(double n) {
		return new Vector(x * n, y * n, z * n);
	}
	
	/**
	 * Adds two vectors together.
	 * @param v a vector to be added
	 * @return the sum of the vectors
	 */
	public Vector add(Vector v) {
		return new Vector(x + v.getX(), y + v.getY(), z + v.getZ());
	}
	
	/**
	 * Subtracts two vectors
	 * @param v a vector to be subtracted
	 * @return the result of the subtractions
	 */
	public Vector sub(Vector v) {
		return new Vector(x - v.getX(), y - v.getY(), z - v.getZ());
	}
	
	/**
	 * Gets the angle between this vector and a specified vector.
	 * @param v the vector to find the angle to
	 * @return the angle between this vector and another
	 */
	public double angleBetween(Vector v) {
		double a = Math.acos( (x*v.x + y*v.y + z*v.z) / (magnitude() * v.magnitude()));
		if (v.y < y) a *= -1;
		return a;
	}
	
	/**
	 * Maps between a position on the target screen and the actual screen.
	 */
	public Vector remapPosition() {
		double newX = (window.width() - (2 * Game.getXOffset())) * this.x;
		double newY = (window.height() - (2 * Game.getYOffset())) * this.y;
		
		return new Vector(newX, newY, this.z);
	}
	
	/**
	 * Checks a vector for equality with this vector.
	 * @param o the object to be tested for equality
	 * @return a boolean result of the equality test
	 */
	@Override
	public boolean equals(Object o) {
		if (o.getClass() != Vector.class) { 
			return false;
		} else {
			Vector v = (Vector) o;
			return (x == v.getX()) && (y == v.getY()) && (z == v.getZ());
		}
	}
	
	/**
	 * Returns a textual representation of the vector.
	 * @return a textual representation of the vector
	 */
	@Override
	public String toString() {
		return "< Vector: X = " + x + " Y = " + y + " Z = " + z + " >";
	}

}