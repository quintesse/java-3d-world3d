/*
 * Created on Aug 2, 2003
 */
package org.codejive.world3d;

import javax.vecmath.Vector3f;

import org.codejive.utils4gl.Vectors;

/**
 * @author tako
 */
public class SurfaceInformation {
	private float m_fHeight;
	private Vector3f m_normal = new Vector3f(Vectors.VECTF_ZERO);
	
	public float getHeight() {
		return m_fHeight;
	}
	
	public void setHeight(float _fHeight) {
		m_fHeight = _fHeight;
	}
	
	public Vector3f getNormal() {
		return m_normal;
	}
	
	public void setNormal(Vector3f _normal) {
		m_normal.set(_normal);
	}
}
