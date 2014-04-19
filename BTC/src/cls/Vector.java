package cls;

import java.io.Serializable;

import lib.jog.window;
import scn.Game;

/**
 * Simplified 3D vector class with basic operations.
 */
public class Vector implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = -5086652815637818053L;

	/** The vector's x position */
	private double x;
	
	/** The vector's y position */
	private double y;
	
	/** The vector's z position */
	private double z;
	
	
	/**
	 * Constructor for a vector.
	 * @param x
	 * 			the vector's x position
	 * @param y
	 * 			the vector's y position
	 * @param z
	 * 			the vector's z position
	 */
	public Vector(double x, double y, double z) {
		this.x = x / (window.width() - (2 * Game.X_OFFSET));
		this.y = y / (window.height() - (2 * Game.Y_OFFSET));
		this.z = z;
	}
	
	/**
	 * Constructor for a relative vector.
	 * @param x
	 * 			the vector's x position
	 * @param y
	 * 			the vector's y position
	 * @param z
	 * 			the vector's z position
	 * @param relative
	 * 			this value is ignored
	 */
	public Vector(double x, double y, double z, boolean valueIgnored) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Gets the x position of the vector.
	 * @return the x position of the vector
	 */
	public double getX() {
		return x * (window.width() - (2 * Game.X_OFFSET));
	}

	/**
	 * Gets the y position of the vector.
	 * @return the y position of the vector
	 */
	public double getY() {
		return y * (window.height() - (2 * Game.Y_OFFSET));
	}
	
	/**
	 * Gets the relative x position of the vector.
	 * @return the relative x position of the vector
	 */
	public double getRelativeX() {
		return x;
	}

	/**
	 * Gets the relative y position of the vector.
	 * @return the relative y position of the vector
	 */
	public double getRelativeY() {
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
	 * @param z
	 * 		the z value to be set
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
		return (getX()*getX()) + (getY()*getY()) + (getZ()*getZ());
	}
	
	/**
	 * Normalises the vector.
	 * @return a normalised vector
	 */
	public Vector normalise() {
		return scaleBy(1/magnitude());
	}
	
	/**
	 * Scales the vector by a given scalar.
	 * @param n
	 * 			the scalar to scale by
	 * @return the scaled vector
	 */
	public Vector scaleBy(double n) {
		return new Vector(getX() * n, getY() * n, getZ() * n);
	}
	
	/**
	 * Adds two vectors together.
	 * @param v
	 * 			a vector to be added
	 * @return the sum of the vectors
	 */
	public Vector add(Vector v) {
		return new Vector(getX() + v.getX(),
				getY() + v.getY(),
				getZ() + v.getZ());
	}
	
	/**
	 * Subtracts two vectors
	 * @param v
	 * 			a vector to be subtracted
	 * @return the result of the subtractions
	 */
	public Vector sub(Vector v) {
		return new Vector(getX() - v.getX(),
				getY() - v.getY(),
				getZ() - v.getZ());
	}
	
	/**
	 * Gets the angle between this vector and a specified vector.
	 * @param v
	 * 			the vector to find the angle to
	 * @return the angle between this vector and another
	 */
	public double angleBetween(Vector v) {
		double a = Math.acos((getX()*v.getX() + getY()*v.getY() + getZ()*v.getZ())
				/ (magnitude() * v.magnitude()));
		
		return (v.getY() < getY()) ? a * -1 : a;
	}
	
	/**
	 * Checks a vector for equality with this vector.
	 * @param o
	 * 			the object to be tested for equality
	 * @return a boolean result of the equality test
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Vector) {
			Vector v = (Vector) o;
			return (getX() == v.getX())
					&& (getY() == v.getY())
					&& (getZ() == v.getZ());
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a textual representation of the vector.
	 * @return a textual representation of the vector
	 */
	@Override
	public String toString() {
		return "< Vector: X = " + getX()
				+ " Y = " + getY()
				+ " Z = " + getZ() + " >";
	}

}