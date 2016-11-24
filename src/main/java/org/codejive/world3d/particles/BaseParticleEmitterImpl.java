package org.codejive.world3d.particles;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.RenderObserver;
import org.codejive.world3d.LiveEntity;

/**
 * @author Tako
 *
 */
public abstract class BaseParticleEmitterImpl implements ParticleEmitter, LiveEntity {
	private int m_nMaxParticleCount;
	
	protected List<Particle> m_liveParticles;
	protected List<Particle> m_deadParticles;

	private float m_fLastSystemTime;
	private boolean m_bReadyForRendering;

	public BaseParticleEmitterImpl(int _nMaxParticleCount) {
		m_nMaxParticleCount = _nMaxParticleCount;
		m_liveParticles = new LinkedList<Particle>();
		m_deadParticles = new LinkedList<Particle>();
		for (int i = 0; i < _nMaxParticleCount; i++) {
			m_deadParticles.add(newParticle());
		}
		m_bReadyForRendering = false;
	}

	public int getMaximumParticleCount() {
		return m_nMaxParticleCount;		
	}
	
	public abstract Particle newParticle();

	public void heartbeat(float _fTime) {
		if (m_fLastSystemTime > 0) {
			float fElapsedTime = _fTime - m_fLastSystemTime;
			update(fElapsedTime);
		}
		m_fLastSystemTime = _fTime;
	}
	
	public abstract void update(float _fElapsedTime);

	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}

	public void initRendering(RenderContext _context) {
		Iterator<Particle> i = m_deadParticles.iterator();
		while (i.hasNext()) {
			Particle particle = i.next();
			particle.initRendering(_context);
		}
		m_bReadyForRendering = true;
	}

	public void render(RenderContext _context, RenderObserver _observer) {
		Iterator<Particle> i = m_liveParticles.iterator();
		while (i.hasNext()) {
			Particle particle = i.next();
			particle.render(_context, _observer);
		}
	}
}
