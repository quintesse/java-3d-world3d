/*
 * Created on 21-mrt-2003
 */
package org.codejive.world3d;

import java.util.*;

import javax.vecmath.*;

import org.codejive.utils4gl.*;
import org.codejive.world3d.net.*;
import org.codejive.world3d.physics.PhysicsEngine;
import org.codejive.world3d.physics.SimplePhysicsEngine;

/**
 * 
 * @author tako
 */
public abstract class Universe implements ActiveForce, NetworkEncoder, NetworkDecoder {
	private Landscape m_landscape;
	private PhysicsEngine m_physicsEngine;
	private List m_renderables;
	private List m_physicalEntities;
	private List m_terminalEntities;
	private List m_liveEntities;
	private Vector3f m_gravity;
	private long m_lBigBang;

	private static boolean m_bLoggingEnabled = true;
	private static boolean m_bLogUnspecifiedClasses = true;
	private static boolean m_bLogUnspecifiedObjects = true;
	private static Map m_loggingClasses;
	private static Map m_loggingObjects;
	
	private short m_nClassIndex;
	private short m_nIstanceId;

	public static final float ALMOST_ZERO = 0.00001f;
	
	public Universe() {
		m_renderables = new LinkedList();
		m_physicalEntities = new LinkedList();
		m_terminalEntities = new LinkedList();
		m_liveEntities = new LinkedList();
		m_gravity = new Vector3f(0.0f, -1.0f, 0.0f);
		m_lBigBang = System.currentTimeMillis();

		m_bLoggingEnabled = true;
		m_bLogUnspecifiedClasses = true;
		m_bLogUnspecifiedObjects = true;
		m_loggingClasses = new HashMap();
		m_loggingObjects = new HashMap();
		
		// TODO: We shouldn't use getServerCache() here because Universe is also used for the Client!!!
		m_nClassIndex = NetworkClassCache.getServerCache().getClassIndex(this.getClass().getName());
		m_nIstanceId = NetworkInstanceIdGenerator.getNewId();

		m_physicsEngine = new SimplePhysicsEngine(this);
		m_landscape = null;
	}
	
	/**
	 * Returns the landscape on which or over which the Entities live
	 * 
	 * @return The landscape
	 */
	public Landscape getLandscape() {
		return m_landscape;
	}

	public void setLandscape(Landscape _landscape) {
		m_landscape = _landscape;
	}
	
	public PhysicsEngine getPhysicsEngine() {
		return m_physicsEngine;
	}
	
	public float getAge() {
		return (float)(System.currentTimeMillis() - m_lBigBang) / 1000.0f;
	}
	
	public SurfaceInformation newSurfaceInformation() {
		return getLandscape().createSurfaceInformation();
	}
	
	public void getSurfaceAt(Point2f _pos, SurfaceInformation _si) {
		getLandscape().getSurfaceAt(_pos, _si);
	}
	
	public void getSurfaceAt(float _x, float _y, SurfaceInformation _si) {
		getLandscape().getSurfaceAt(_x, _y, _si);
	}
	
	/**
	 * Adds a rendarable object to the Universe
	 * 
	 * @param _object The rendarable object to add
	 */
	public void addRenderable(Renderable _object) {
		m_renderables.add(_object);
	}
	
	/**
	 * Removes a rendarable object from the Universe
	 * 
	 * @param _object The rendarable object to remove
	 */
	public void removeRenderable(Renderable _object) {
		m_renderables.remove(_object);
	}
	
	/**
	 * Returns a list of renderable objects.
	 *
	 * @return A List containing all the renderable objects
	 */
	public List getRenderablesList() {
		return Collections.unmodifiableList(m_renderables);
	}
	
	/**
	 * Returns an iterator to iterate over the list of renderable objects.
	 *
	 * @return An Iterator over all the renderable objects
	 */
	public Iterator getRenderables() {
		return getRenderablesList().iterator();
	}
	
	/**
	 * Add an Entity to the list of "physical" entities which will make it adhere to
	 * the "laws of nature" we implement in our universe.
	 * An Entity is considered physical when it implements the interface PhysicalEntity
	 * and when it is part of the list of physical entities. Every physical Entity will
	 * have its updateLocation() called every frame.
	 *   
	 * @param _entity The Entity to make adhere to the laws of nature
	 */
	public void addPhysicalEntity(PhysicalEntity _entity) {
		m_physicalEntities.add(_entity);
		log(this, "Entering the realm of physics " + _entity.toString());
	}
	
	/**
	 * Remove an Entity from the list of "live" entities.
	 *   
	 * @param _entity The Entity that doesn't need to adhere to the laws of nature
	 */
	public void removePhysicalEntity(PhysicalEntity _entity) {
		m_physicalEntities.remove(_entity);
		log(this, "Leaving the realm of physics " + _entity.toString());
	}
	
	/**
	 * Returns a list of physical entities.
	 *
	 * @return A List containing all the physical entities
	 */
	public List getPhysicalEntitiesList() {
		return Collections.unmodifiableList(m_physicalEntities);
	}
	
	/**
	 * Returns an iterator to iterate over the list of physical entities.
	 *
	 * @return An Iterator over all the physical entities
	 */
	public Iterator getPhysicalEntities() {
		return getPhysicalEntitiesList().iterator();
	}
	
	/**
	 * Add an Entity to the list of "terminal" entities which means that the Entity
	 * will have a limited life span.
	 * An Entity is considered terminal when it implements the interface TerminalEntity
	 * and when it is part of the list of terminal entities. Every terminal Entity will
	 * have its terminateEntity() called at the end of its life span.
	 *   
	 * @param _entity The Entity to make terminal
	 */
	public void addTerminalEntity(TerminalEntity _entity) {
		m_terminalEntities.add(_entity);
		log(this, "Becoming mortal " + _entity.toString());
	}
	
	/**
	 * Remove an Entity from the list of "terminal" entities.
	 *   
	 * @param _entity The Entity that will live forever
	 */
	public void removeTerminalEntity(TerminalEntity _entity) {
		m_terminalEntities.remove(_entity);
		log(this, "Becoming immortal " + _entity.toString());
	}
	
	/**
	 * Returns a list of terminal entities.
	 *
	 * @return A List containing all the terminal entities
	 */
	public List getTerminalEntitiesList() {
		return Collections.unmodifiableList(m_terminalEntities);
	}
	
	/**
	 * Returns an iterator to iterate over the list of terminal entities.
	 *
	 * @return An Iterator over all the terminal entities
	 */
	public Iterator getTerminalEntities() {
		return getTerminalEntitiesList().iterator();
	}
	
	/**
	 * Add an Entity to the list of "live" entities.
	 * An Entity is considered live when it implements the interface LiveEntity
	 * and when it is part of the list of live entities. Every live Entity will
	 * have its heartbeat() called regularly.
	 *   
	 * @param _entity The Entity to "awaken"
	 */
	public void addLiveEntity(LiveEntity _entity) {
		m_liveEntities.add(_entity);
		log(this, "Heartbeat started for " + _entity.toString());
	}
	
	/**
	 * Remove an Entity from the list of "live" entities.
	 *   
	 * @param _entity The Entity to "put to sleep"
	 */
	public void removeLiveEntity(LiveEntity _entity) {
		m_liveEntities.remove(_entity);
		log(this, "Heartbeat stopped for " + _entity.toString());
	}
	
	/**
	 * Returns a list of live entities.
	 *
	 * @return A List containing all the live entities
	 */
	public List getLiveEntitiesList() {
		return Collections.unmodifiableList(m_liveEntities);
	}
	
	/**
	 * Returns an iterator to iterate over the list of live entities.
	 *
	 * @return An Iterator over all the live entities
	 */
	public Iterator getLiveEntities() {
		return getLiveEntitiesList().iterator();
	}
	
	/**
	 * Returns an iterator to iterate over the list of live entities in a certain
	 * spherical part of the Universe. The result can be limited to Entities of a
	 * certain EntityClass and optionally be required to be targetable.
	 * @param _center The center of the sperical area in which we are interested
	 * @param _fRadius The radius of the spherical area in which we are interested
	 * @param _class The EntityClass of the Entities in which we are interested or null for any class 
	 * @param _bMustBetargetable Determines if the Entities must be targetable
	 * @return An Iterator over all the live entities in a certain area
	 */
	public Iterator getLiveEntitiesWithinRadius(Point3f _center, float _fRadius, Class _class, boolean _bMustBetargetable) {
		return new EntitiesWithinRangeIterator(getLiveEntities(), _center, _fRadius, _class, _bMustBetargetable);
	}

	/**
	 * Returns an iterator to iterate over the list of live entities in a certain
	 * spherical part of the Universe. The result can be limited to Entities of a
	 * certain EntityClass. All Entities are required to be targetable.
	 * @param _center The center of the sperical area in which we are interested
	 * @param _fRadius The radius of the spherical area in which we are interested
	 * @param _class The EntityClass of the Entities in which we are interested or null for any class 
	 * @return An Iterator over all the live entities in a certain area
	 */
	public Iterator getLiveEntitiesWithinRadius(Point3f _center, float _fRadius, Class _class) {
		return getLiveEntitiesWithinRadius(_center, _fRadius, _class, true);
	}

	/**
	 * Returns an iterator to iterate over the list of live entities in a certain
	 * spherical part of the Universe. All Entities are required to be targetable.
	 * @param _center The center of the sperical area in which we are interested
	 * @param _fRadius The radius of the spherical area in which we are interested
	 * @return An Iterator over all the live entities in a certain area
	 */
	public Iterator getLiveEntitiesWithinRadius(Point3f _center, float _fRadius) {
		return getLiveEntitiesWithinRadius(_center, _fRadius, null, true);
	}

	public Vector3f getGravity() {
		return m_gravity;
	}
	
	public void setGravity(float _fX, float _fY, float _fZ) {
		m_gravity.set(_fX, _fY, _fZ);
	}
	
	public void setGravity(Vector3f _gravity) {
		setGravity(_gravity.x, _gravity.y, _gravity.z);
	}
	
	public void handleFrame(float _fElapsedTime) {
		List ents;
		Iterator i;
		
		ents = new ArrayList(getTerminalEntitiesList());
		i = ents.iterator();
		while (i.hasNext()) {
			TerminalEntity e = (Entity)i.next();
			if (e.getAge() > e.getLifetime()) {
				e.terminateEntity();
			}
		}
		
		ents = new ArrayList(getPhysicalEntitiesList());
		i = ents.iterator();
		while (i.hasNext()) {
			PhysicalEntity e = (PhysicalEntity)i.next();
			e.updatePhysics(getAge());
		}
		
		ents = new ArrayList(getLiveEntitiesList());
		i = ents.iterator();
		while (i.hasNext()) {
			LiveEntity e = (LiveEntity)i.next();
			e.heartbeat(getAge());
		}
	}
	
	// ActiveForce ////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see test.ActiveForce#getForce(test.PhysicalEntity)
	 */
	public Vector3f getForce(PhysicalEntity _entity) {
		return getGravity();
	}
	
	// NetworkEncoder /////////////////////////////////////////////////////////////

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
		_writer.writeFloat(m_gravity.x);
		_writer.writeFloat(m_gravity.y);
		_writer.writeFloat(m_gravity.z);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeKill(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeKill(MessageWriter _writer) {
		// TODO Handle Universe kill message writing
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkEncoder#writeUpdate(org.codejive.world3d.net.MessageWriter)
	 */
	public void writeUpdate(MessageWriter _writer) {
		// TODO Handle Universe update message writing
	}
	
	// NetworkDecoder /////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netInit(org.codejive.world3d.net.MessageReader)
	 */
	public void netInit(MessageReader _reader) {
		m_gravity.set(_reader.readFloat(), _reader.readFloat(), _reader.readFloat());
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netKill(org.codejive.world3d.net.MessageReader)
	 */
	public void netKill(MessageReader _reader) {
		// TODO Handle Universe kill message writing
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.net.NetworkDecoder#netUpdate(org.codejive.world3d.net.MessageReader)
	 */
	public void netUpdate(MessageReader _reader) {
		// TODO Handle Universe update message writing
	}

	// Oracle /////////////////////////////////////////////////////////////////////

	/**
	 * Writes a text to the log 
	 * @param _sText The text to write to the log
	 */	
	public static void log(Class _loggingClass, String _sText) {
		if (m_bLoggingEnabled) {
			boolean bLog = m_bLogUnspecifiedClasses;
			// Has logging been specifically turned on or off for objects of this class?
			if (m_loggingClasses.containsKey(_loggingClass)) {
				bLog = ((Boolean)m_loggingClasses.get(_loggingClass)).booleanValue();
			}
			if (bLog) {
				log(_loggingClass.toString() + ": " + _sText);
			}
		}
	}

	/**
	 * Writes a text to the log 
	 * @param _sText The text to write to the log
	 */	
	public static void log(Object _loggingObject, String _sText) {
		if (m_bLoggingEnabled) {
			boolean bLog = m_bLogUnspecifiedObjects;
			// Has logging been specifically turned on or off for this object?
			if (m_loggingObjects.containsKey(_loggingObject)) {
				bLog = ((Boolean)m_loggingObjects.get(_loggingObject)).booleanValue();
			} else {
				// Has logging been specifically turned on or off for objects of the same class?
				if (m_loggingClasses.containsKey(_loggingObject.getClass())) {
					bLog = ((Boolean)m_loggingClasses.get(_loggingObject.getClass())).booleanValue();
				}
			}
			if (bLog) {
				log(_loggingObject.toString() + ": " + _sText);
			}
		}
	}

	/**
	 * Writes a text to the log 
	 * @param _sText The text to write to the log
	 */	
	public static void log(String _sText) {
		if (m_bLoggingEnabled) {
			System.out.println(_sText);
		}
	}
}
