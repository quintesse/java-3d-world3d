/*
 * Created on 18-mrt-2003
 */
package org.codejive.world3d;

import java.util.*;

import javax.vecmath.*;

import org.codejive.world3d.net.*;
import org.codejive.utils4gl.Vectors;

/**
 * This class represents any basic object in the Universe that could be considered
 * to have its own "identity". An Entity has a visual representation (shape) in
 * the form of 3D geometry. That shape has a position and is facing a certain direction
 * (orientation). It also has a vector that determines which way is up for the
 * Entity.
 *  
 * @author Tako
 */
public class Entity implements PhysicalEntity, TerminalEntity, NetworkEncoder, NetworkDecoder {
	protected Universe m_universe;
	private EntityClass m_class;
	protected Point3f m_position;
	protected Vector3f m_orientation;
	protected Vector3f m_impulse;
	protected LinkedList m_forces;
	private float m_fGravityFactor;
	private float m_fLifetime;
	protected boolean m_bIsTargetable;
	protected boolean m_bIsSolid;
	
	private long m_lTimeOfBirth;
	protected boolean m_bIsPhysical = false;
	protected boolean m_bIsTerminal = false;
	private long m_nFirstPhysicsUpdate = 0;
	private long m_nLastPhysicsUpdate = 0;

	private short m_nClassIndex;
	private short m_nIstanceId;
	
	// Only here to speed things up
	protected Matrix4f m_transform = new Matrix4f();
	
	public Entity() {
		m_universe = null;
		m_class = null;
		m_position = new Point3f();
		m_orientation = new Vector3f();
		m_impulse = new Vector3f();
		m_forces = new LinkedList();
		m_fGravityFactor = 1.0f;
		setLifetime(INFINITE);
		m_bIsTargetable = false;
		m_bIsSolid = false;
		
		m_nClassIndex = -1;
		m_nIstanceId = -1;
	}

	/**
	 * Constructor for a new Entity with eternal life
	 * @param _class The EntityClass for this Entity 
	 * @param _position The position for the new Entity
	 * @param _orientation The orientation of the Entity
	 * @param _impulse The Entity's impulse vector
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, EntityClass _class, Point3f _position, Tuple3f _orientation, Vector3f _impulse, float _fGravityFactor) {
		this();
		setUniverse(_universe);
		m_class = _class;
		setPosition(_position);
		setOrientation(_orientation);
		setImpulse(_impulse);
		setGravityFactor(_fGravityFactor);
		m_nClassIndex = NetworkClassCache.getServerCache().getClassIndex(m_class.getClass().getName());
		m_nIstanceId = NetworkInstanceIdGenerator.getNewId();
	}

	/**
	 * Constructor for a new Entity with eternal life
	 * @param _class The EntityClass for this Entity 
	 * @param _position The position for the new Entity
	 * @param _impulse The Entity's impulse vector
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, EntityClass _class, Point3f _position, Vector3f _impulse, float _fGravityFactor) {
		this(_universe, _class, _position, new Vector3f(Vectors.VECTF_ZERO), _impulse, _fGravityFactor);
	}

	/**
	 * Constructor for a new Entity facing positive Z en up being positive Y
	 * @param _class The EntityClass for this Entity 
	 * @param _position The position for the new Entity
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, EntityClass _class, Point3f _position, float _fGravityFactor) {
		this(_universe, _class, _position, new Vector3f(Vectors.VECTF_ZERO), new Vector3f(Vectors.VECTF_ZERO), _fGravityFactor);
	}

	/**
	 * Returns the EntityRenderer object for this Entity
	 * @return The Entity's EntityRenderer
	 */
	public EntityClass getEntityClass() {
		return m_class;
	}
	
	public Universe getUniverse() {
		return m_universe;
	}

	public void setUniverse(Universe _universe) {
		m_universe = _universe;
		m_lTimeOfBirth = m_universe.getAge();
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#getPosition()
	 */
	public Point3f getPosition() {
		return m_position;
	}
	
	/* (non-Javadoc)
	 * @see test.PhysicalEntity#setPosition(javax.vecmath.Tuple3f)
	 */
	public void setPosition(Tuple3f _position) {
		setPosition(_position.x, _position.y, _position.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#setPosition(float, float, float)
	 */
	public void setPosition(float _fX, float _fY, float _fZ) {
		m_position.set(_fX, _fY, _fZ);
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#getOrientation()
	 */
	public Vector3f getOrientation() {
		return m_orientation;
	}
	
	/* (non-Javadoc)
	 * @see test.PhysicalEntity#setOrientation(javax.vecmath.Tuple3f)
	 */
	public void setOrientation(Tuple3f _orientation) {
		setOrientation(_orientation.x, _orientation.y, _orientation.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.PhysicalEntity#setOrientation(float, float, float)
	 */
	public void setOrientation(float _fX, float _fY, float _fZ) {
		m_orientation.set(_fX, _fY, _fZ);
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#getImpulse()
	 */
	public Vector3f getImpulse() {
		return m_impulse;
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#setImpulse(javax.vecmath.Vector3f)
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
	 * @see test.PhysicalEntity#addForce(test.ActiveForce)
	 */
	public void addForce(ActiveForce _force) {
		m_forces.add(_force);
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#removeForce(test.ActiveForce)
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
	 * @see test.PhysicalEntity#getGravityFactor()
	 */
	public float getGravityFactor() {
		return m_fGravityFactor;
	}
	
	/* (non-Javadoc)
	 * @see test.PhysicalEntity#setGravityFactor(float)
	 */
	public void setGravityFactor(float _fGravityFactor) {
		m_fGravityFactor = _fGravityFactor;
	}

	/* (non-Javadoc)
	 * @see test.TerminalEntity#getLifetime()
	 */
	public float getLifetime() {
		return m_fLifetime;
	}
	
	/* (non-Javadoc)
	 * @see test.TerminalEntity#setLifetime(float)
	 */
	public void setLifetime(float _fLifetime) {
		m_fLifetime = _fLifetime;
		if (m_fLifetime >= 0.0f) {
			if (!m_bIsTerminal) {
				m_universe.addTerminalEntity(this);
				m_bIsTerminal = true;
			}
		} else {
			if (m_bIsTerminal) {
				m_universe.removeTerminalEntity(this);
				m_bIsTerminal = false;
			}
		}
	}
	
	public long getTimeOfBirth() {
		return m_lTimeOfBirth;
	}

	public float getAge() {
		return (m_universe.getAge() - m_lTimeOfBirth) / 1000.0f;
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#updateState()
	 */
	public void updateState() {
		m_transform.set(m_orientation);
		Vector3f p = new Vector3f(m_position);
		m_transform.setTranslation(p);
		
		float fSpeed = m_impulse.length();
		if ((fSpeed > Universe.ALMOST_ZERO) || (Math.abs(getGravityFactor()) > Universe.ALMOST_ZERO)) {
			if (!m_bIsPhysical) {
				m_universe.addPhysicalEntity(this);
				m_bIsPhysical = true;
			}
		} else {
			if (m_bIsPhysical) {
				m_universe.removePhysicalEntity(this);
				m_bIsPhysical = false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#updatePhysics(long)
	 */
	public void updatePhysics(long _time) {
		if (m_nLastPhysicsUpdate > 0) {
			float fSecs = (float)(_time - m_nLastPhysicsUpdate) / 1000;
			m_universe.getPhysicsEngine().update(this, fSecs);
			updateState();
		} else {
			m_nFirstPhysicsUpdate = _time;
		}
		m_nLastPhysicsUpdate = _time;
	}

	/* (non-Javadoc)
	 * @see test.TerminalEntity#terminateEntity()
	 */
	public void terminateEntity() {
		Universe.log(this, "Terminating " + this);
		m_universe.removePhysicalEntity(this);
		m_universe.removeTerminalEntity(this);
	}
	
	/**
	 * Indicates of the Entity can be targeted by others. This doesn not necessarily
	 * refer to any specific kind of targeting but is more like a very general
	 * indication which objects the system can assume are "not interesting".
	 * 
	 * @return A boolean indicating if the Entity can be targeted or not
	 */	
	public boolean isTargetable() {
		return m_bIsTargetable;
	}

	/**
	 * Indicates of the Entity can be considered solid or non-transparant.
	 * A solid object will occlude objects from a viewer if it is on the line of sight
	 * while non-solid/transparent will not.
	 * 
	 * @return A boolean indicating if the Entity can be considered solid or not
	 */	
	public boolean isSolid() {
		return m_bIsSolid;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#getClassIndex()
	 */
	public short getClassIndex() {
		return m_nClassIndex;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#getInstanceId()
	 */
	public short getInstanceId() {
		return m_nIstanceId;
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeInit(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeInit(MessageWriter _writer) {
		_writer.writeShort(m_universe.getInstanceId());
		_writer.writeFloat(m_position.x);
		_writer.writeFloat(m_position.y);
		_writer.writeFloat(m_position.z);
		_writer.writeFloat(m_orientation.x);
		_writer.writeFloat(m_orientation.y);
		_writer.writeFloat(m_orientation.z);
		_writer.writeFloat(m_impulse.x);
		_writer.writeFloat(m_impulse.y);
		_writer.writeFloat(m_impulse.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeUpdate(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeUpdate(MessageWriter _writer) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeKill(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeKill(MessageWriter _writer) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.ConnectedMessagePort)
	 */
	public void netInit(MessageReader _reader) {
		short nUniverseId = _reader.readShort();
		setUniverse((Universe)NetworkClassCache.getClientCache().getInstance(nUniverseId));
		m_position.set(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		m_orientation.set(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		m_impulse.set(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netUpdate(org.codejive.world3d.net.ConnectedMessagePort)
	 */
	public void netUpdate(MessageReader _reader) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netKill(org.codejive.world3d.net.ConnectedMessagePort)
	 */
	public void netKill(MessageReader _reader) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * 
	 * Adds info about the Entity's class, position, impulse and speed
	 */
	public String toString() {
		String sRes = super.toString();
		if (getEntityClass() != null) {
			sRes += " (" + getEntityClass().getName() + ")";
		}
		sRes += " p:" + getPosition() + " i:" + getImpulse();
		return sRes;
	}
}
