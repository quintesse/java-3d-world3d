/*
 * Created on Oct 14, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public interface MessageReader extends ByteReader {

	public int readUnsignedByte();

	public void readByteArray(byte _values[], int _pos, int _length);

	public short readShort();
	
	public int readInt();
	
	public long readLong();
		
	public float readFloat();
	
	public double readDouble();
	
	public String readString();
}
