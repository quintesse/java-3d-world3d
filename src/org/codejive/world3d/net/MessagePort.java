/*
 * Created on Oct 3, 2003
 */
package org.codejive.world3d.net;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

import org.codejive.world3d.*;

/**
 * @author Tako
 */
public class MessagePort implements Runnable {
	private String m_sName;
	private int m_nPort;
	private InetAddress m_destAddress;
	private int m_nDestPort;
	
	private Thread m_thread;
	private DatagramSocket m_socket;
	private boolean m_bListening;
	private InetAddress m_broadcastAddress;
	private MessagePacket m_packets[];
	private boolean m_obtainedPackets[];
	private LinkedList m_packetsRead;
	private int m_nPacketsReceived;
	private int m_nPacketsSent;
	private int m_nPacketsDropped;
	
	public MessagePort(String _sName) {
		this(_sName, -1, null, -1);
	}
	
	public MessagePort(String _sName, InetAddress _destAddress, int _nDestPort) {
		this(_sName, -1, _destAddress, _nDestPort);
	}
	
	public MessagePort(String _sName, int _nPort) {
		this(_sName, _nPort, null, -1);
	}
	
	public MessagePort(String _sName, int _nPort, InetAddress _destAddress, int _nDestPort) {
		m_sName = _sName;
		m_nPort = _nPort;
		m_destAddress = _destAddress;
		m_nDestPort = _nDestPort;

		m_thread = null;
		m_socket = null;
		m_bListening = false;
		
		m_packets = new MessagePacket[10];
		m_obtainedPackets = new boolean[10];
		for (int i = 0; i < m_packets.length; i++) {
			m_packets[i] = new MessagePacket();
			initPacket(m_packets[i]);
			m_obtainedPackets[i] = false;
		}
		m_packetsRead = new LinkedList();
		m_nPacketsReceived = 0;
		m_nPacketsSent = 0;
	}
	
	public String getName() {
		return m_sName;
	}
		
	public void setName(String _sName) {
		m_sName = _sName;
	}
		
	public InetAddress getAddress() {
		return m_socket.getLocalAddress();
	}
	
	public int getPort() {
		return m_socket.getLocalPort();
	}
	
	public InetAddress getDestinationAddress() {
		return m_destAddress;
	}
	
	public int getDestinationPort() {
		return m_nDestPort;
	}
	
	public int getPacketsReceived() {
		return m_nPacketsReceived;
	}
	
	public int getPacketsSent() {
		return m_nPacketsSent;
	}
	
	public int getPacketsDropped() {
		return m_nPacketsDropped;
	}
	
	public boolean start() {
		boolean bOk = false;
		try {
//			m_broadcastAddress = InetAddress.getByName("127.0.0.255");

			byte[] address = InetAddress.getLocalHost().getAddress();
			address[3] = (byte)255;
			m_broadcastAddress = InetAddress.getByAddress(address);
			
			if (m_nPort >= 0) {
				m_socket = new DatagramSocket(m_nPort);
			} else {
				m_socket = new DatagramSocket();
			}
			Universe.log(this, "Created socket on port " + m_socket.getLocalPort());
			if ((m_destAddress != null) && (m_nDestPort != -1)) {
				bind(m_destAddress, m_nDestPort);
			}
			
			m_bListening = true;
			m_thread = new Thread(this); 
			m_thread.start();
				
			bOk = true;
		} catch (UnknownHostException e) {
			Universe.log(this, "Could not determine broadcast address: " + e.getMessage());
		} catch (SocketException e) {
			Universe.log(this, "Could not create socket: " + e.getMessage());
		}
		return bOk;
	}
		
	public void stop() {
		if (m_bListening) {
			synchronized(this) {
				m_bListening = false;
				if (m_thread != null) {
					m_thread.interrupt();
				}
				if (m_socket != null) {
					m_socket.close();
				}
			}
			Universe.log(this, "stopped");
		} else {
			Universe.log(this, "already stopped");
		}
	}
		
	public void bind(InetAddress _destAddress, int _nDestPort) {
		Universe.log(this, "bound to " + _destAddress + ":" + _nDestPort);
		m_socket.connect(_destAddress, _nDestPort);
	}
	
	protected void finalize() throws Throwable {
		stop();
	}

	public void run() {
		try {
			DatagramPacket udpPacket = new DatagramPacket(m_packets[0].getBytes(), m_packets[0].getMaxSize());
			
			// Not connected yet
			while (m_bListening) {
				MessagePacket msgPacket = obtainPacket();
				udpPacket.setData(msgPacket.getBytes(), 0, msgPacket.getMaxSize());
				m_socket.receive(udpPacket);
				m_nPacketsReceived++;
				Universe.log(this, "packet received (" + udpPacket.getLength() + " bytes)");

				if (udpPacket.getLength() > 0) {
					msgPacket.clear();
					msgPacket.setAddress(udpPacket.getAddress());
					msgPacket.setPort(udpPacket.getPort());
					msgPacket.setSize(udpPacket.getLength());

					handlePacket(msgPacket);

					m_packetsRead.add(msgPacket);
					synchronized(m_packetsRead) {
						m_packetsRead.notify();
					}
				}
			}
			synchronized(this) {
				m_socket = null;
				m_thread = null;
			}
		} catch (IOException e) {
			Universe.log(this, "Could not receive packet: " + e.getMessage());
		} catch (InterruptedException e) {
			Universe.log(this, "read thread interrupted");
		}
	}
	
	protected MessagePacket obtainPacket() throws InterruptedException {
		while (true) {
			for (int i = 0; i < m_packets.length; i++) {
				if (!m_obtainedPackets[i]) {
					m_obtainedPackets[i] = true;
					return m_packets[i];
				}
			}
			// No packets available so we wait
			synchronized(m_packets) {
				m_packets.wait();
			}
		}
	}
	
	public void releasePacket(MessagePacket _packet) {
		for (int i = 0; i < m_packets.length; i++) {
			if (m_packets[i] == _packet) {
				m_obtainedPackets[i] = false;
				synchronized(m_packets) {
					m_packets.notify();
				}
			}
		}
	}
	
	public void initPacket(MessagePacket _packet) {
		_packet.clear();
	}

	protected void handlePacket(MessagePacket _packet) {
	}
	
	public boolean isClosed() {
		return (m_socket == null) || m_socket.isClosed();
	}
		
	public boolean hasMoreData() throws InterruptedException {
		boolean bResult;
		
		bResult = (m_packetsRead.size() > 0);

		return bResult;
	}

	public MessagePacket receivePacket() throws InterruptedException {
		MessagePacket packet = null;
		while (packet == null) {
			synchronized(m_packetsRead) {
				if (m_packetsRead.size() > 0) {
					packet = (MessagePacket)m_packetsRead.removeFirst();
				}
				if (packet == null) {
					Thread.currentThread().interrupted();
					m_packetsRead.wait();
				}
			}
		}
		return packet;
	}
	
	public void sendPacket(MessagePacket _packet) {
		sendPacket(m_socket.getInetAddress(), m_socket.getPort(), _packet);
	}
	
	public void sendPacket(InetAddress _destAddress, int _nDestPort, MessagePacket _packet) {
		try {
			DatagramPacket packet = new DatagramPacket(_packet.getBytes(), _packet.getSize(), _destAddress, _nDestPort);
			Universe.log(this, "send packet of " + _packet.getSize() + " bytes to " + _destAddress + ":" + _nDestPort);
			m_socket.send(packet);
			m_nPacketsSent++;
		} catch (IOException e) {
			Universe.log(this, "Could not send packet: " + e.getMessage());
		}
	}
	
	public void sendBroadcastPacket(MessagePacket _packet) {
		sendPacket(m_broadcastAddress, _packet.getPort(), _packet);
	}
	
	public void sendBroadcastPacket(int _nDestPort, MessagePacket _packet) {
		sendPacket(m_broadcastAddress, _nDestPort, _packet);
	}
	
	public String toString() {
		return getClass().getName() + " '" + getName() + "', #in:" + getPacketsReceived() + ", #out:" + getPacketsSent();
	}
}
