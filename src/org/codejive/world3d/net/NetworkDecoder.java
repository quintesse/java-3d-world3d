/*
 * Created on Oct 1, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public interface NetworkDecoder {
	public void netInit(MessageReader _reader);
	public void netUpdate(MessageReader _reader);
	public void netKill(MessageReader _reader);
}
