/*
 * Created on 6-apr-2003
 */
package org.codejive.world3d;

/**
 * @author Tako
 */
public interface TerminalEntity {

	/**
	 * Returns the lifetime of the Entity in seconds
	 * 
	 * @return The Entity's current lifetime
	 */
	public float getLifetime();
	
	/**
	 * Sets the lifetime of the Entity in seconds
	 * 
	 * @param _fLifetime The Entity's new lifetime
	 */
	public void setLifetime(float _fLifetime);

	public float getTimeOfBirth();

	public float getAge();

	/**
	 * Is called to terminate the entity.
	 * Currently this means that it will be removed from any global
	 * entity lists it might be part of.
	 */
	public void terminateEntity();
	
}
