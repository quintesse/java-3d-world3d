/*
 * Created on Dec 2, 2003
 */
package org.codejive.world3d;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3f;

/**
 * @author tako
 * @version $Revision: $
 */
public abstract class DefaultPhysicalEntityImpl extends DefaultStaticEntityImpl implements PhysicalEntity {
	protected Vector3f m_impulse;
	protected LinkedList m_forces;
	private float m_fGravityFactor;

	public DefaultPhysicalEntityImpl() {
		m_impulse = new Vector3f();
		m_forces = new LinkedList();
		m_fGravityFactor = 1.0f;
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#getImpulse()
	 */
	public Vector3f getImpulse() {
		return m_impulse;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#setImpulse(javax.vecmath.Vector3f)
	 */
	public void setImpulse(Vector3f _impulse) {
		setImpulse(_impulse.x, _impulse.y, _impulse.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#setImpulse(float, float, float)
	 */
	public void setImpulse(float _fX, float _fY, float _fZ) {
		m_impulse.set(_fX, _fY, _fZ);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#addForce(org.codejive.world3d.ActiveForce)
	 */
	public void addForce(ActiveForce _force) {
		m_forces.add(_force);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#removeForce(org.codejive.world3d.ActiveForce)
	 */
	public void removeForce(ActiveForce _force) {
		m_forces.remove(_force);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#getForces()
	 */
	public List getForces() {
		return m_forces;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#getGravityFactor()
	 */
	public float getGravityFactor() {
		return m_fGravityFactor;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#setGravityFactor(float)
	 */
	public void setGravityFactor(float _fFactor) {
		m_fGravityFactor = _fFactor;
	}
}

/*
 * $Log: $
 */