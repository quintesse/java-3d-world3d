/*
 * Created on Nov 10, 2003
 */
package org.codejive.world3d;

import java.util.Iterator;

import javax.vecmath.Point3f;

import org.codejive.utils4gl.ConditionalIterator;

/**
 * @author tako
 */
public class EntitiesWithinRangeIterator extends ConditionalIterator {
	private Point3f m_center;
	private float m_fRangeSquared;
	private Class m_class;
	private boolean m_bMustBeTargetable;
	
	/**
	 * @param _iter
	 */
	public EntitiesWithinRangeIterator(Iterator _iter, Point3f _center, float _fRange, Class _class, boolean _bMustBeTargetable) {
		super(_iter);
		m_center = _center;
		m_fRangeSquared = _fRange;
		m_class = _class;
		m_bMustBeTargetable = _bMustBeTargetable;
	}

	/* (non-Javadoc)
	 * @see org.codejive.utils4gl.ConditionalIterator#includeElement(java.lang.Object)
	 */
	protected boolean includeElement(Object _element) {
		boolean bInclude = false;
		Entity e = (Entity)_element;
		Point3f pos = e.getPosition();
		if (m_center.distanceSquared(pos) <= m_fRangeSquared) {
			if ((m_class == null) || m_class.isInstance(e.getEntityClass())) {
				if (!m_bMustBeTargetable || e.isTargetable()) {
System.err.println(this + " x " + _element + " dist = " + m_center.distanceSquared(pos));
					bInclude = true;
				}
			}
		}
		return bInclude;
	}

}
