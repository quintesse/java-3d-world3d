package org.codejive.world3d.particles;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codejive.utils4gl.RenderContext;
import org.codejive.world3d.LiveEntity;

/**
 * @author Tako
 *
 */
public abstract class BaseParticleEmitterImpl implements ParticleEmitter, LiveEntity {
	private int m_nMaxParticleCount;
	
	protected List m_liveParticles;
	protected List m_deadParticles;

	private long m_lLastSystemTime;
	private boolean m_bReadyForRendering;

	public BaseParticleEmitterImpl(int _nMaxParticleCount) {
		m_nMaxParticleCount = _nMaxParticleCount;
		m_liveParticles = new LinkedList();
		m_deadParticles = new LinkedList();
		for (int i = 0; i < _nMaxParticleCount; i++) {
			m_deadParticles.add(newParticle());
		}
		m_bReadyForRendering = false;
	}

	public int getMaximumParticleCount() {
		return m_nMaxParticleCount;		
	}
	
	public abstract Particle newParticle();

	public void heartbeat(long _time) {
		if (m_lLastSystemTime > 0) {
			float fElapsedTime = (float)(_time - m_lLastSystemTime) / 1000;
			update(fElapsedTime);
		}
		m_lLastSystemTime = _time;
	}
	
	public abstract void update(float _fElapsedTime);

	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}

	public void initRendering(RenderContext _context) {
		Iterator i = m_deadParticles.iterator();
		while (i.hasNext()) {
			Particle particle = (Particle)i.next();
			particle.initRendering(_context);
		}
		m_bReadyForRendering = true;
	}

	public void render(RenderContext _context) {
		Iterator i = m_liveParticles.iterator();
		while (i.hasNext()) {
			Particle particle = (Particle)i.next();
			particle.render(_context);
		}
	}
}
