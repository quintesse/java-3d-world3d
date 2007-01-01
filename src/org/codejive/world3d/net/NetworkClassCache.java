/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

import java.util.*;

/**
 * @author Tako
 */
public class NetworkClassCache {
	private ArrayList<NetworkClass> m_classes = new ArrayList<NetworkClass>();
	private HashMap<Short, NetworkDecoder> m_instances = new HashMap<Short, NetworkDecoder>();

	public void registerClass(String _sServerClassName, String _sClientClassName) {
		m_classes.add(new NetworkClass(_sServerClassName, _sClientClassName));
	}

	public short getClassIndex(String _sName) {
		short nIndex = -1;
		for (int i = 0; i < m_classes.size(); i++) {
			NetworkClass nc = m_classes.get(i);
			if (nc.sServerClassName.equals(_sName)) {
				nIndex = (short)i;
				break;
			}
		}
		return nIndex;
	}	
	
	public List<NetworkClass> getRegisteredClasses() {
		return Collections.unmodifiableList(m_classes);	
	}
	
	public void clearRegisteredClasses() {
		m_classes.clear();
	}
	
	public String getClientClassName(int _nIndex) {
		NetworkClass nc = m_classes.get(_nIndex);
		return nc.sClientClassName;
	}
	
	@SuppressWarnings("unchecked")
	public Class<NetworkClass> getClientClass(int _nIndex) {
		Class<NetworkClass> cls = null;
		NetworkClass nc = m_classes.get(_nIndex);
		if (nc.clientClass == null) {
			try {
				cls = (Class<NetworkClass>) Class.forName(nc.sClientClassName);
				nc.clientClass = cls;
			} catch (ClassNotFoundException e) {
				/* ignore */
			}
		}
		return cls;
	}
	
	public void registerInstance(short _nInstanceId, NetworkDecoder _object) {
		m_instances.put(_nInstanceId, _object);
	}
	
	public NetworkDecoder getInstance(short _nInstanceId) {
		return m_instances.get(_nInstanceId);
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
