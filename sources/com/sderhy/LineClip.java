package com.sderhy ;

/**
* Clips an infinite line — given a point on it and a direction — to the image
* rectangle [0, w-1] x [0, h-1], returning the chord's two endpoints. Shared by
* the oblique linear reconstruction engine (DrawableFrame) and the crosshair
* overlay (PixObjectViewer) so both agree on where a cut line crosses the image.
*/
public final class LineClip {
	private LineClip(){}

	/** @return {x0,y0,x1,y1} of the chord, or null if the line misses the rect. */
	public static double[] segment(double cx, double cy, double dirX, double dirY, int w, int h){
		double tmin = -1e18, tmax = 1e18 ;
		double[] xRange = slab(cx, dirX, 0, w - 1) ;
		if(xRange == null) return null ;
		tmin = Math.max(tmin, xRange[0]) ; tmax = Math.min(tmax, xRange[1]) ;
		double[] yRange = slab(cy, dirY, 0, h - 1) ;
		if(yRange == null) return null ;
		tmin = Math.max(tmin, yRange[0]) ; tmax = Math.min(tmax, yRange[1]) ;
		if(tmin > tmax) return null ;
		return new double[]{ cx + tmin * dirX, cy + tmin * dirY,
		                     cx + tmax * dirX, cy + tmax * dirY } ;
	}

	// Range of t for which (origin + t*dir) stays within [lo,hi] along one axis.
	private static double[] slab(double origin, double dir, double lo, double hi){
		if(Math.abs(dir) < 1e-9)
			return (origin < lo || origin > hi) ? null : new double[]{ -1e18, 1e18 } ;
		double ta = (lo - origin) / dir, tb = (hi - origin) / dir ;
		return new double[]{ Math.min(ta, tb), Math.max(ta, tb) } ;
	}
}
