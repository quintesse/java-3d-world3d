/*
 * Created on Nov 13, 2003
 */
package org.codejive.world3d;

import javax.vecmath.Point2f;

import org.codejive.world3d.net.NetworkDecoder;
import org.codejive.world3d.net.NetworkEncoder;

/**
 * @author tako
 */
public interface Landscape extends NetworkEncoder, NetworkDecoder {
	public abstract SurfaceInformation createSurfaceInformation();
	public abstract void getSurfaceAt(float _x, float _y, SurfaceInformation _info);
	public abstract void getSurfaceAt(Point2f _pos, SurfaceInformation _info);
}