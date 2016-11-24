/*
 * Created on Oct 24, 2003
 */
package org.codejive.world3d.physics;

import org.codejive.world3d.PhysicalEntity;

/**
 * @author tako
 */
public interface PhysicsEngine {
	void update(PhysicalEntity _entity, float _fTimePassed);
}
