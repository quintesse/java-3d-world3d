/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public class NetworkClass {
	public String sServerClassName;
	public String sClientClassName;
		
	public Class serverClass;
	public Class clientClass;

	public NetworkClass(String _sServerClassName, String _sClientClassName) {
		sServerClassName = _sServerClassName;
		sClientClassName = _sClientClassName;
		serverClass = clientClass = null;
	}
}
