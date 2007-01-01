/*
 * Created on Oct 24, 2003
 */
package org.codejive.world3d.physics;

import javax.vecmath.Vector3f;

import org.codejive.world3d.ActiveForce;
import org.codejive.world3d.PhysicalEntity;
import org.codejive.world3d.SurfaceInformation;
import org.codejive.world3d.Universe;
import org.codejive.utils4gl.Vectors;

/**
 * @author tako
 */
public class SimplePhysicsEngine implements PhysicsEngine {
	private Universe m_universe;
	
	private Vector3f m_positionVector = new Vector3f();
	private Vector3f m_forceVector = new Vector3f();
	private Vector3f m_moveVector = new Vector3f();
	private Vector3f m_tmpVector = new Vector3f();
	
	public SimplePhysicsEngine(Universe _universe) {
		m_universe = _universe;
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.physics.PhysicsEngine#update(long)
	 */
	public void update(PhysicalEntity _entity, float _fTimePassed) {
		SurfaceInformation si = m_universe.newSurfaceInformation();

		m_positionVector.set(_entity.getPosition());
		m_universe.getSurfaceAt(m_positionVector.x, m_positionVector.z, si);
		// Add all forces currently acting on the Entity
		m_forceVector.set(Vectors.VECTF_ZERO);
		// The Universe's force handles the gravity
		m_tmpVector.set(m_universe.getForce(_entity));
		m_tmpVector.scale(_entity.getGravityFactor());
		m_forceVector.add(m_tmpVector);
		if (_entity.getForces() != null) {
			for (ActiveForce force : _entity.getForces()) {
				m_forceVector.add(force.getForce(_entity));
			}
		}

		// The Entity is not actually touching the surface
		// but assume that it is if the difference is really small
		boolean bOnSurface = ((m_positionVector.y - si.getHeight()) < 0.2);
		
		if (bOnSurface) {
//			Vector3f vt = new Vector3f();
//			vt.cross(m_forceVector, si.getNormal());
//			Vector3f va = new Vector3f();
//			va.cross(si.getNormal(), vt);
//Universe.log(this, "f: " + m_forceVector + ", n: " + si.getNormal() + ", a: " + va + ", |a|:" + va.length());
//			m_forceVector.set(va);

			m_moveVector.set(_entity.getImpulse());
			m_moveVector.scale(1.0f - 0.1f * _fTimePassed); // Damping TEST!!
			m_moveVector.add(m_forceVector);

//			vt = new Vector3f();
//			vt.cross(m_moveVector, si.getNormal());
//			va = new Vector3f();
//			va.cross(si.getNormal(), vt);
//			m_moveVector.set(va);
		} else {
			m_moveVector.set(_entity.getImpulse());
			m_moveVector.add(m_forceVector);
		}

		_entity.setImpulse(m_moveVector);
		m_moveVector.scale(_fTimePassed);
		m_positionVector.add(m_moveVector);
	
		if (m_positionVector.y <= si.getHeight()) {
			// If we are below ground make sure to put us on top again
			m_positionVector.y = si.getHeight();
			// and make our Y speed 0
			m_moveVector.set(_entity.getImpulse());
			m_moveVector.y = 0.0f;
			//m_moveVector.x = 0.0f;
			//m_moveVector.z = 0.0f;
			_entity.setImpulse(m_moveVector);
		}
		
		_entity.setPosition(m_positionVector);
	}
}
