/*
 * Created on Oct 6, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public class TypePacker {
	
	private static int byte2uint(byte _value) {
		if ((_value & 0x80) == 0) {
			return _value;
		} else {
			return (_value & 0x7f) | 0x80;
		}
	}
	
	public static int readUnsignedByte(ByteReader _reader) {
		int nValue = byte2uint(_reader.readByte());
		return  nValue;
	}

	public static void writeByteArray(ByteWriter _writer, byte _values[], int _pos, int _length) {
		for (int i = 0; i < _length; i++) {
			_writer.writeByte(_values[_pos + i]);
		}
	}

	public static void readByteArray(ByteReader _reader, byte _values[], int _pos, int _length) {
		while (_length-- > 0) {
			_values[_pos++] = _reader.readByte();
		}
	}

	public static void writeShort(ByteWriter _writer, short _nValue) {
		_writer.writeByte((byte)((_nValue >> 8) & 0xff));
		_writer.writeByte((byte)(_nValue & 0xff));
	}

	public static short readShort(ByteReader _reader) {
		return (short)((readUnsignedByte(_reader) << 8) | readUnsignedByte(_reader));
	}

	public static void writeInt(ByteWriter _writer, int _nValue) {
		_writer.writeByte((byte)((_nValue >> 24) & 0xff));
		_writer.writeByte((byte)((_nValue >> 16) & 0xff));
		_writer.writeByte((byte)((_nValue >> 8) & 0xff));
		_writer.writeByte((byte)(_nValue & 0xff));
	}

	public static int readInt(ByteReader _reader) {
		return ((readUnsignedByte(_reader) << 24) | (readUnsignedByte(_reader) << 16) | (readUnsignedByte(_reader) << 8) | readUnsignedByte(_reader));
	}
	
	public static void writeLong(ByteWriter _writer, long _lValue) {
		_writer.writeByte((byte)((_lValue >> 56) & 0xff));
		_writer.writeByte((byte)((_lValue >> 48) & 0xff));
		_writer.writeByte((byte)((_lValue >> 40) & 0xff));
		_writer.writeByte((byte)((_lValue >> 32) & 0xff));
		_writer.writeByte((byte)((_lValue >> 24) & 0xff));
		_writer.writeByte((byte)((_lValue >> 16) & 0xff));
		_writer.writeByte((byte)((_lValue >> 8) & 0xff));
		_writer.writeByte((byte)(_lValue & 0xff));
	}

	public static long readLong(ByteReader _reader) {
		return (((long)readUnsignedByte(_reader) << 56) | ((long)readUnsignedByte(_reader) << 48) | ((long)readUnsignedByte(_reader) << 40) | ((long)readUnsignedByte(_reader) << 32) | ((long)readUnsignedByte(_reader) << 24) | ((long)readUnsignedByte(_reader) << 16) | ((long)readUnsignedByte(_reader) << 8) | readUnsignedByte(_reader));
	}	
		
	public static void writeFloat(ByteWriter _writer, float _fValue) {
		writeInt(_writer, Float.floatToIntBits(_fValue));
	}
	
	public static float readFloat(ByteReader _reader) {
		return Float.intBitsToFloat(readInt(_reader));
	}
	
	public static void writeDouble(ByteWriter _writer, double _dValue) {
		writeLong(_writer, Double.doubleToLongBits(_dValue));
	}
	
	public static double readDouble(ByteReader _reader) {
		return Double.longBitsToDouble(readLong(_reader));
	}
	
	public static void writeString(ByteWriter _writer, String _sValue) {
		for (int i = 0; i < _sValue.length(); i++) {
			_writer.writeByte((byte)_sValue.charAt(i));
		}
		_writer.writeByte((byte)0);
	}
	
	public static String readString(ByteReader _reader) {
		String s = "";
		byte b;
		while ((b = _reader.readByte()) != 0) {
			s += (char)b;
		}
		return s;
	}

}
