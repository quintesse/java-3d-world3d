/*
 * Created on Sep 30, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
interface PacketHandler {
	public void handlePacket(MessagePort _port, MessagePacket _packet);
}
