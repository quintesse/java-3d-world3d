/*
 * Created on 3-apr-2003
 */
package org.codejive.world3d;

import java.util.List;

import javax.vecmath.*;

/**
 * @author Tako
 */
public interface PhysicalEntity {

	public static final float INFINITE = -1.0f;
	
	public Universe getUniverse();

	public void setUniverse(Universe _universe);

	/**
	 * Returns the current position for this Entity.
	 * NB: you get a reference to the actual internal position,
	 * changes made to it will affect the position of the Entity.
	 * You should always advertise any changes by calling updateState().
	 * @return A point indicating the Entity's position
	 */
	public Point3f getPosition();
	
	/**
	 * Sets the current position for this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _position A point indicating the Entity's new position
	 */
	public void setPosition(Tuple3f _position);

	/**
	 * Sets the current position for this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _fX The X coordinate of the point indicating the Entity's new position
	 * @param _fY The Y coordinate of the point indicating the Entity's new position
	 * @param _fZ The Z coordinate of the point indicating the Entity's new position
	 */
	public void setPosition(float _fX, float _fY, float _fZ);
	
	
	/**
	 * Returns the current orientation of this Entity.
	 * NB: you get a reference to the actual internal orientation,
	 * changes made to it will affect the orientation of the Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @return A vector indicating the orientation of the Entity
	 */
	public Vector3f getOrientation();
	
	/**
	 * Sets the current orientation of this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _direction A vector indicating the new orientation of the Entity
	 */
	public void setOrientation(Tuple3f _orientation);

	/**
	 * Sets the current orientation of this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _fX The X component of the vector indicating the new orientation of the Entity
	 * @param _fY The Y component of the vector indicating the new orientation of the Entity
	 * @param _fZ The Z component of the vector indicating the new orientation of the Entity
	 */
	public void setOrientation(float _fX, float _fY, float _fZ);

	/**
	 * Returns the current impulse vector for this Entity.
	 * NB: you get a reference to the actual internal direction,
	 * changes made to it will affect the impulse vector for the Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @return A vector indicating the direction and speed the Entity is moving
	 */
	public Vector3f getImpulse();
	
	/**
	 * Sets the current impulse vector for this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _impulse A vector indicating the new direction and speed for the Entity
	 */
	public void setImpulse(Vector3f _impulse);
	
	/**
	 * Sets the current impulse vector for this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _fX The X component of the vector indicating the new direction and speed for the Entity
	 * @param _fY The Y component of the vector indicating the new direction and speed for the Entity
	 * @param _fZ The Z component of the vector indicating the new direction and speed for the Entity
	 */
	public void setImpulse(float _fX, float _fY, float _fZ);
	
	/**
	 * Adds a force to the list of forces currently influencing the Entity.
	 * 
	 * @param _force An ActiveForce object
	 */
	public void addForce(ActiveForce _force);
	
	/**
	 * Removes a force from the list of forces currently influencing the Entity.
	 * 
	 * @param _force An ActiveForce object
	 */
	public void removeForce(ActiveForce _force);
	
	/**
	 * Returns the list of forces currently influencing the Entity.
	 * 
	 * @return A list of ActiveForce objects
	 */
	public List getForces();
		
	/**
	 * Determines the influence gravity has on the Entity.
	 * 
	 * @return A multiplication factor indicating how much the entity is affected by gravity
	 */
	public float getGravityFactor();
	
	/**
	 * Sets the influence gravity has on the Entity.
	 * 
	 * @param __fFactor A multiplication factor indicating the amount of influence gravity has
	 */
	public void setGravityFactor(float _fFactor);
	
	/**
	 * This method must/will be called after every change to position, direction, speed or lifetime
	 */
	public void updateState();

	/**
	 * Updates the physical parameters of the Entity according its position, direction, speed or lifetime.
	 * NB: You should not call this method directly!
	 * 
	 * @param _time The current time of the Universe at the moment of the call
	 */
	public void updatePhysics(long _time);
}
