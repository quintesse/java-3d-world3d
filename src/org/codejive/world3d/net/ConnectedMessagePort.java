/*
 * Created on Oct 3, 2003
 */
package org.codejive.world3d.net;

import java.net.InetAddress;

/**
 * @author Tako
 */
public class ConnectedMessagePort extends MessagePort {
	private int m_nLastPacketSent;
	private int m_nLastPacketReceived;
	private int m_nLastPacketAcknowledged;
	private int m_nPacketsLost;

	public ConnectedMessagePort(String _sName) {
		this(_sName, -1, null, -1);
	}
	
	public ConnectedMessagePort(String _sName, InetAddress _destAddress, int _nDestPort) {
		this(_sName, -1, _destAddress, _nDestPort);
	}
	
	public ConnectedMessagePort(String _sName, int _nPort) {
		this(_sName, _nPort, null, -1);
	}
	
	public ConnectedMessagePort(String _sName, int _nPort, InetAddress _destAddress, int _nDestPort) {
		super(_sName, _nPort, _destAddress, _nDestPort);
	}
	
	public int getLastPacketSent() {
		return m_nLastPacketSent;
	}
	
	public int getLastPacketReceived() {
		return m_nLastPacketReceived;
	}
	
	public int getPacketsLost() {
		return m_nPacketsLost;
	}
	
	public void initPacket(MessagePacket _packet) {
		super.initPacket(_packet);
		_packet.writeInt(0); // Packet nr, but we don't know it yet at this point
		_packet.writeInt(0); // Last packet received nr, but we don't know it yet at this point
		_packet.writeByte((byte)0); // Packet flags, but we don't know them yet at this point
	}

	protected void handlePacket(MessagePacket _packet) {
		int nPacketNr = _packet.readInt();
		if (nPacketNr > (m_nLastPacketReceived + 1)) {
			m_nPacketsLost += (nPacketNr - m_nLastPacketReceived - 1);
		}
		m_nLastPacketReceived = nPacketNr;
		m_nLastPacketAcknowledged = _packet.readInt();
		byte nFlags = _packet.readByte();
	}

	public void sendPacket(MessagePacket _packet) {
		m_nLastPacketSent++;
		
		byte nFlags = 0; // Not used yet

		// Dirty hack
		int nSize = _packet.getSize();
		_packet.clear();
		_packet.writeInt(m_nLastPacketSent);
		_packet.writeInt(m_nLastPacketReceived);
		_packet.writeByte(nFlags);
		_packet.setSize(nSize); // Restore original size
		
		super.sendPacket(_packet);
	}
	
	public String toString() {
		return super.toString() + ", #lost:" + getPacketsLost();
	}
}
