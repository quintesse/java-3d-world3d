/*
 * Created on Oct 5, 2003
 */
package org.codejive.world3d.net;

/**
 * @author Tako
 */
public class NetworkException extends RuntimeException {

	public NetworkException() {
		super();
	}

	public NetworkException(String arg0) {
		super(arg0);
	}

	public NetworkException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NetworkException(Throwable arg0) {
		super(arg0);
	}

}
