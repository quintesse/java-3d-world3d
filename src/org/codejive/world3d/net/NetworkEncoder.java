/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public interface NetworkEncoder {
	public short getClassIndex();
	public short getInstanceId();
	public void writeInit(MessageWriter _writer);
	public void writeUpdate(MessageWriter _writer);
	public void writeKill(MessageWriter _writer);
}
