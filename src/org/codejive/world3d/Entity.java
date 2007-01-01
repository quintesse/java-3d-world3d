/*
 * Created on 18-mrt-2003
 */
package org.codejive.world3d;

import java.util.logging.Logger;

import javax.vecmath.*;

import org.codejive.world3d.net.*;
import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.Renderable;
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
public abstract class Entity extends DefaultPhysicalEntityImpl implements TerminalEntity, Renderable, NetworkEncoder, NetworkDecoder {
	private float m_fLifetime;
	protected boolean m_bIsTargetable;
	protected boolean m_bIsSolid;
	
	private float m_fTimeOfBirth;
	protected boolean m_bIsPhysical = false;
	protected boolean m_bIsTerminal = false;
	private float m_fFirstPhysicsUpdate = 0;
	private float m_fLastPhysicsUpdate = 0;
	private boolean m_bReadyForRendering = false;

	private short m_nClassIndex;
	private short m_nIstanceId;
	
	// Only here to speed things up
	protected Matrix4f m_transform = new Matrix4f();
	
	private static Logger logger = Logger.getLogger(Entity.class.getName());
	
	/**
	 * Constructor for a very generic Entity
	 */
	public Entity() {
		setLifetime(INFINITE);
		m_bIsTargetable = false;
		m_bIsSolid = false;
		
		m_nClassIndex = -1;
		m_nIstanceId = -1;
	}

	/**
	 * Constructor for a new Entity with eternal life
	 * @param _universe The Universe where the new entity should exist
	 * @param _position The position for the new Entity
	 * @param _orientation The orientation of the Entity
	 * @param _impulse The Entity's impulse vector
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, Point3f _position, Vector3f _orientation, Vector3f _impulse, float _fGravityFactor) {
		this();
		setUniverse(_universe);
		setPosition(_position);
		setOrientation(_orientation);
		setImpulse(_impulse);
		setGravityFactor(_fGravityFactor);
		m_nClassIndex = NetworkClassCache.getServerCache().getClassIndex(getClass().getName());
		m_nIstanceId = NetworkInstanceIdGenerator.getNewId();
	}

	/**
	 * Constructor for a new Entity with eternal life
	 * @param _universe The Universe where the new entity should exist
	 * @param _position The position for the new Entity
	 * @param _impulse The Entity's impulse vector
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, Point3f _position, Vector3f _impulse, float _fGravityFactor) {
		this(_universe, _position, new Vector3f(Vectors.VECTF_ZERO), _impulse, _fGravityFactor);
	}

	/**
	 * Constructor for a new Entity facing positive Z en up being positive Y
	 * @param _universe The Universe where the new entity should exist
	 * @param _position The position for the new Entity
	 * @param _fGravityFactor Determines the amount the Entity is affected by gravity
	 */
	public Entity(Universe _universe, Point3f _position, float _fGravityFactor) {
		this(_universe, _position, new Vector3f(Vectors.VECTF_ZERO), new Vector3f(Vectors.VECTF_ZERO), _fGravityFactor);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.StaticEntity#setUniverse(org.codejive.world3d.Universe)
	 */
	@Override
	public void setUniverse(Universe _universe) {
		super.setUniverse(_universe);
		m_fTimeOfBirth = _universe.getAge();
		_universe.addRenderable(this);
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
				getUniverse().addTerminalEntity(this);
				m_bIsTerminal = true;
			}
		} else {
			if (m_bIsTerminal) {
				getUniverse().removeTerminalEntity(this);
				m_bIsTerminal = false;
			}
		}
	}
	
	public float getTimeOfBirth() {
		return m_fTimeOfBirth;
	}

	public float getAge() {
		return (getUniverse().getAge() - m_fTimeOfBirth);
	}

	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}
	
	public void initRendering(RenderContext _context) {
		m_bReadyForRendering = true;
	}
	
	public void updateRendering(RenderContext _context) {
		// No default implementation, override when necessary
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#updateState()
	 */
	public void updateState() {
		m_transform.set(getOrientation());
		Vector3f p = new Vector3f(getPosition());
		m_transform.setTranslation(p);
		
		float fSpeed = m_impulse.length();
		if ((fSpeed > Universe.ALMOST_ZERO) || (Math.abs(getGravityFactor()) > Universe.ALMOST_ZERO)) {
			if (!m_bIsPhysical) {
				getUniverse().addPhysicalEntity(this);
				m_bIsPhysical = true;
			}
		} else {
			if (m_bIsPhysical) {
				getUniverse().removePhysicalEntity(this);
				m_bIsPhysical = false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see test.PhysicalEntity#updatePhysics(long)
	 */
	public void updatePhysics(float _fTime) {
		if (m_fLastPhysicsUpdate > 0) {
			float fSecs = _fTime - m_fLastPhysicsUpdate;
			getUniverse().getPhysicsEngine().update(this, fSecs);
			updateState();
		} else {
			m_fFirstPhysicsUpdate = _fTime;
		}
		m_fLastPhysicsUpdate = _fTime;
	}

	/* (non-Javadoc)
	 * @see test.TerminalEntity#terminateEntity()
	 */
	public void terminateEntity() {
		logger.info("Terminating " + this);
		if (this instanceof LiveEntity) {
			getUniverse().removeLiveEntity((LiveEntity)this);
		}
		getUniverse().removePhysicalEntity(this);
		getUniverse().removeTerminalEntity(this);
		getUniverse().removeRenderable(this);
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
		_writer.writeShort(getUniverse().getInstanceId());
		_writer.writeFloat(getPosition().x);
		_writer.writeFloat(getPosition().y);
		_writer.writeFloat(getPosition().z);
		_writer.writeFloat(getOrientation().x);
		_writer.writeFloat(getOrientation().y);
		_writer.writeFloat(getOrientation().z);
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
		setPosition(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		setOrientation(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		m_impulse.set(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
		updateState();
		if (this instanceof LiveEntity) {
			getUniverse().addLiveEntity((LiveEntity)this);
		}
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
		sRes += " p:" + getPosition() + " i:" + getImpulse();
		return sRes;
	}
}
