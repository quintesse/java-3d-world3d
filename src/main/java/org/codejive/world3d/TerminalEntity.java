/*
 * Created on 6-apr-2003
 */
package org.codejive.world3d;

/**
 * @author Tako
 */
public interface TerminalEntity {

	/**
	 * Use <code>INFINITE</code> for the entity's lifetime if it should live forever
	 */
	public static final float INFINITE = -1.0f;
	
	/**
	 * Returns the lifetime of the Entity in seconds
	 * @return The Entity's current lifetime
	 */
	public float getLifetime();
	
	/**
	 * Sets the lifetime of the Entity in seconds
	 * @param _fLifetime The Entity's new lifetime
	 */
	public void setLifetime(float _fLifetime);

	/**
	 * Returns the Universal Time at the moment of this entity's "birth"
	 * @return Universal Time in seconds since the "Big Bang"
	 */
	public float getTimeOfBirth();

	/**
	 * Returns the age of this entity in seconds
	 * @return Age in seconds
	 */
	public float getAge();

	/**
	 * Is called to terminate the entity.
	 * Currently this means that it will be removed from any global
	 * entity lists it might be part of.
	 */
	public void terminateEntity();
	
}
