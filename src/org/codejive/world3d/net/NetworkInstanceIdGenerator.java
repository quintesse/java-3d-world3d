/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public class NetworkInstanceIdGenerator {
	static short m_nId = 0;
	
	public static short getNewId() {
		return ++m_nId;
	}
	
	public static void reset() {
		m_nId = 0;
	}
}
