/*
 * Created on Oct 14, 2003
 */
package org.codejive.world3d.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Tako
 */
public class MessageStream implements MessageReader, MessageWriter {
	private Socket m_socket;
	private InputStream m_in;
	private OutputStream m_out;
	private int m_buffedByte;

	public MessageStream(Socket _socket) {
		m_socket = _socket;
		try {
			m_in = _socket.getInputStream();
			m_out = _socket.getOutputStream();
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
		m_buffedByte = -2;
	}
	
	public boolean hasMoreData() {
		try {
			if (m_buffedByte == -2) {
				m_buffedByte = m_in.read();
			}
			return (m_buffedByte != -1);
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
	}

	public void writeByte(byte _nValue) {
		try {
			m_out.write(_nValue);
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
	}

	public byte readByte() {
		try {
			if (m_buffedByte == -2) {
				m_buffedByte = m_in.read();
			}
			byte cResult = (byte)m_buffedByte;
			m_buffedByte = m_in.read();
			return cResult;
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
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

	public void flush() {
		try {
			m_out.flush();
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
	}
	
	public void close() {
		try {
			m_socket.close();
		} catch (IOException e) {
			throw new NetworkException("Connection error", e);
		}
	}
}
