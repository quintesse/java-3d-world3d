/*
 * Created on 18-mrt-2003
 */
package org.codejive.world3d;

/**
 * @author Tako
 */
public class EntityClass {
	private String m_sClassName;
	private boolean m_bIsTargetable;
	private boolean m_bIsSolid;
	
	public EntityClass(String _sClassName, boolean _bIsTargetable, boolean _bIsSolid) {
		m_sClassName = _sClassName;
		m_bIsTargetable = _bIsTargetable;
		m_bIsSolid = _bIsSolid;
	}
	
	public String getName() {
		return m_sClassName;
	}
	
	public boolean isTargetable() {
		return m_bIsTargetable;
	}
	
	public boolean isSolid() {
		return m_bIsSolid;
	}
}
