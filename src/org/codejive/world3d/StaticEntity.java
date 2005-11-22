/*
 * Created on Dec 2, 2003
 */
package org.codejive.world3d;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * @author tako
 * @version $Revision: $
 */
public interface StaticEntity {
	
	/**
	 * Returns the universe in which this entity exists
	 * @return A reference to a Universe object
	 */
	public Universe getUniverse();

	/**
	 * Sets the universe where this entity exists
	 * @param _universe A reference to a Universe object
	 */
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
	 * @param _orientation A vector indicating the new orientation of the Entity
	 */
	public void setOrientation(Vector3f _orientation);

	/**
	 * Sets the current orientation of this Entity.
	 * You should always advertise any changes by calling updateState().
	 * 
	 * @param _fX The X component of the vector indicating the new orientation of the Entity
	 * @param _fY The Y component of the vector indicating the new orientation of the Entity
	 * @param _fZ The Z component of the vector indicating the new orientation of the Entity
	 */
	public void setOrientation(float _fX, float _fY, float _fZ);

}
/*
 * $Log: $
 */