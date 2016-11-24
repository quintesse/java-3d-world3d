/*
 * Created on Oct 14, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public interface MessageWriter extends ByteWriter {

	public void writeByteArray(byte _values[], int _pos, int _length);

	public void writeShort(short _nValue);

	public void writeInt(int _nValue);

	public void writeLong(long _lValue);

	public void writeFloat(float _fValue);
	
	public void writeDouble(double _dValue);
	
	public void writeString(String _sValue);
}
