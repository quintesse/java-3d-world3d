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
public abstract class DefaultStaticEntityImpl implements StaticEntity {
	private Universe m_universe;
	private Point3f m_position;
	private Vector3f m_orientation;

	public DefaultStaticEntityImpl() {
		m_universe = null;
		m_position = new Point3f();
		m_orientation = new Vector3f();
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#getUniverse()
	 */
	public Universe getUniverse() {
		return m_universe;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setUniverse(org.codejive.world3d.Universe)
	 */
	public void setUniverse(Universe _universe) {
		m_universe = _universe;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#getPosition()
	 */
	public Point3f getPosition() {
		return m_position;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setPosition(javax.vecmath.Tuple3f)
	 */
	public void setPosition(Tuple3f _position) {
		setPosition(_position.x, _position.y, _position.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setPosition(float, float, float)
	 */
	public void setPosition(float _fX, float _fY, float _fZ) {
		m_position.set(_fX, _fY, _fZ);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#getOrientation()
	 */
	public Vector3f getOrientation() {
		return m_orientation;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setOrientation(javax.vecmath.Vector3f)
	 */
	public void setOrientation(Vector3f _orientation) {
		setOrientation(_orientation.x, _orientation.y, _orientation.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setOrientation(float, float, float)
	 */
	public void setOrientation(float _fX, float _fY, float _fZ) {
		m_orientation.set(_fX, _fY, _fZ);
	}
}

/*
 * $Log: $
 */