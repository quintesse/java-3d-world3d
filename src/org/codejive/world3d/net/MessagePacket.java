/*
 * Created on Sep 30, 2003
 */
package org.codejive.world3d.net;

import java.net.InetAddress;

/**
 * @author Tako
 */
public class MessagePacket implements MessageReader, MessageWriter {
	private InetAddress m_senderAdress;
	private int m_nSenderPort;
	byte[] m_packetBuffer;
	int m_length;
	int m_readPosition;
	
	public MessagePacket() {
		m_packetBuffer = new byte[1024];
		clear();
	}
	
	public InetAddress getAddress() {
		return m_senderAdress;
	}
		
	public void setAddress(InetAddress _address) {
		m_senderAdress = _address;
	}
		
	public int getPort() {
		return m_nSenderPort;
	}

	public void setPort(int _nPort) {
		m_nSenderPort = _nPort;
	}

	public byte[] getBytes() {
		return m_packetBuffer;
	}
	
	public int getSize() {
		return m_length;
	}
	
	public void setSize(int _nSize) {
		m_length = _nSize;
	}
	
	public int getMaxSize() {
		return m_packetBuffer.length;
	}
	
	public void clear() {
		m_length = 0;
		m_readPosition = 0;
	}
	
	public void reset() {
		m_readPosition = 0;
	}

	public boolean hasMoreData() {
		return (m_length > 0) && (m_readPosition < m_length);
	}
	
	public void writeByte(byte _nValue) {
		m_packetBuffer[m_length++] = _nValue;
	}

	public byte readByte() {
		if (hasMoreData()) {
			return m_packetBuffer[m_readPosition++];
		} else {
			throw new NetworkException("Read past end of packet");
		}
	}

	public int readUnsignedByte() {
		return TypePacker.readUnsignedByte(this);
	}

	public void writeByteArray(byte _values[], int _pos, int _length) {
		TypePacker.writeByteArray(this, _values, _pos, _length);
	}

	public void readByteArray(byte _values[], int _pos, int _length) {
		TypePacker.readByteArray(this, _values, _pos, _length);
	}

	public void writeShort(short _nValue) {
		TypePacker.writeShort(this, _nValue);
	}

	public short readShort() {
		return TypePacker.readShort(this);
	}

	public void writeInt(int _nValue) {
		TypePacker.writeInt(this, _nValue);
	}

	public int readInt() {
		return TypePacker.readInt(this);
	}
	
	public void writeLong(long _lValue) {
		TypePacker.writeLong(this, _lValue);
	}

	public long readLong() {
		return TypePacker.readLong(this);
	}	
		
	public void writeFloat(float _fValue) {
		TypePacker.writeFloat(this, _fValue);
	}
	
	public float readFloat() {
		return TypePacker.readFloat(this);
	}
	
	public void writeDouble(double _dValue) {
		TypePacker.writeDouble(this, _dValue);
	}
	
	public double readDouble() {
		return TypePacker.readDouble(this);
	}
	
	public void writeString(String _sValue) {
		TypePacker.writeString(this, _sValue);
	}
	
	public String readString() {
		return TypePacker.readString(this);
	}
}
