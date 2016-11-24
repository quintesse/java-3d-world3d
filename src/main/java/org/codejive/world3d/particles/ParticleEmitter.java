package org.codejive.world3d.particles;

import org.codejive.utils4gl.Renderable;

/**
 * @author Tako
 *
 */
public interface ParticleEmitter extends Renderable {
	public int getMaximumParticleCount();	
	public Particle newParticle();
	public void update(float _fElapsedTime);
}
