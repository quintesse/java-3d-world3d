/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

import java.util.*;

/**
 * @author Tako
 */
public class NetworkClassCache {
	private ArrayList m_classes = new ArrayList();
	private HashMap m_instances = new HashMap();

	public void registerClass(String _sServerClassName, String _sClientClassName) {
		m_classes.add(new NetworkClass(_sServerClassName, _sClientClassName));
	}

	public short getClassIndex(String _sName) {
		short nIndex = -1;
		for (int i = 0; i < m_classes.size(); i++) {
			NetworkClass nc = (NetworkClass)m_classes.get(i);
			if (nc.sServerClassName.equals(_sName)) {
				nIndex = (short)i;
				break;
			}
		}
		return nIndex;
	}	
	
	public List getRegisteredClasses() {
		return Collections.unmodifiableList(m_classes);	
	}
	
	public void clearRegisteredClasses() {
		m_classes.clear();
	}
	
	public String getClientClassName(int _nIndex) {
		NetworkClass nc = (NetworkClass)m_classes.get(_nIndex);
		return nc.sClientClassName;
	}
	
	public Class getClientClass(int _nIndex) {
		Class cls = null;
		NetworkClass nc = (NetworkClass)m_classes.get(_nIndex);
		if (nc.clientClass == null) {
			try {
				cls = Class.forName(nc.sClientClassName);
				nc.clientClass = cls;
			} catch (ClassNotFoundException e) {
				/* ignore */
			}
		}
		return cls;
	}
	
	public void registerInstance(short _nInstanceId, NetworkDecoder _object) {
		m_instances.put(new Short(_nInstanceId), _object);
	}
	
	public NetworkDecoder getInstance(short _nInstanceId) {
		return (NetworkDecoder)m_instances.get(new Short(_nInstanceId));
	}
	
	public void clearRegisteredInstances() {
		m_instances.clear();	
	}
	
	private static NetworkClassCache m_serverCache = new NetworkClassCache();
	private static NetworkClassCache m_clientCache = new NetworkClassCache();

	public static NetworkClassCache getServerCache() {
		return m_serverCache;
	}

	public static NetworkClassCache getClientCache() {
		return m_clientCache;
	}
}
