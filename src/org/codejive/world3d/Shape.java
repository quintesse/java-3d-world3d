/*
 * Created on 18-mrt-2003
 */
package org.codejive.world3d;

import org.codejive.utils4gl.*;

/**
 * @author Tako
 */
public abstract class Shape extends Entity implements Renderable {
	private boolean m_bReadyForRendering = false;

	public void setUniverse(Universe _universe) {
		super.setUniverse(_universe);
		_universe.addRenderable(this);
	}

	/* (non-Javadoc)
	 * @see org.codejive.world3d.Renderable#readyForRendering()
	 */
	public boolean readyForRendering() {
		return m_bReadyForRendering;
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.Renderable#initRendering(org.codejive.world3d.RenderContext)
	 */
	public void initRendering(RenderContext _context) {
		m_bReadyForRendering = true;
	}
	
	/* (non-Javadoc)
	 * @see org.codejive.world3d.Renderable#updateRendering(org.codejive.world3d.RenderContext)
	 */
	public void updateRendering(RenderContext _context) {
	}
}
