/*
 * Created on 8-apr-2003
 */
package org.codejive.world3d;

import javax.vecmath.Vector3f;

/**
 * @author Tako
 */
public interface ActiveForce {

	public Vector3f getForce(PhysicalEntity _entity);
}
