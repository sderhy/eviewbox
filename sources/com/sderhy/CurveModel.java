package com.sderhy ;

/**
* Holds the control points (in image coordinates) that the user places to
* define a curved-reconstruction path, fits a natural cubic spline through
* them and tessellates that spline into densely-spaced sample points
* (about one pixel apart, so the reconstruction width equals the curve's arc
* length and there is no aliasing along it).
*
* Shared by the live viewer (overlay drawing + point editing) and the
* reconstruction engine (pixel sampling). Extracted from the old DCanvas so
* the curve can be drawn straight on the navigable image viewer, the same way
* the frontal / sagittal cut line now is.
*/
public class CurveModel {
	private static final int MAX_POINTS = 100 ;

	private final float[] px = new float[MAX_POINTS] ;   // control points (image coords)
	private final float[] py = new float[MAX_POINTS] ;
	private int nPoints = 0 ;

	private float[] sx = new float[0] ;                  // tessellated samples (image coords)
	private float[] sy = new float[0] ;

	public int pointCount(){ return nPoints ; }
	public float controlX(int i){ return px[i] ; }
	public float controlY(int i){ return py[i] ; }

	public int sampleCount(){ return sx.length ; }
	public float[] sampleX(){ return sx ; }
	public float[] sampleY(){ return sy ; }

	public void addPoint(float x, float y){
		if(nPoints >= MAX_POINTS) return ;
		px[nPoints] = x ; py[nPoints] = y ; nPoints++ ;
		rebuild() ;
	}

	public void undo(){ if(nPoints > 0){ nPoints-- ; rebuild() ; } }

	public void clear(){ nPoints = 0 ; rebuild() ; }

	/** Re-fit the spline and re-sample it at ~1 px spacing. */
	private void rebuild(){
		if(nPoints < 2){ sx = new float[0] ; sy = new float[0] ; return ; }

		float[] t = new float[nPoints] ;
		for(int i = 0 ; i < nPoints ; i++) t[i] = i ;
		Spline splineX = new Spline(t, px, nPoints) ;
		Spline splineY = new Spline(t, py, nPoints) ;

		// Walk the parameter finely, keeping a new sample whenever we have
		// moved at least one pixel away from the previous one.
		int steps = (nPoints - 1) * 200 ;
		float[] outX = new float[steps + 1] ;
		float[] outY = new float[steps + 1] ;
		int count = 0 ;
		float lastX = 0, lastY = 0 ;
		for(int s = 0 ; s <= steps ; s++){
			float tt = (float)(nPoints - 1) * s / steps ;
			float fx = splineX.CalcValue(tt) ;
			float fy = splineY.CalcValue(tt) ;
			if(count == 0){
				outX[count] = fx ; outY[count] = fy ; count++ ;
				lastX = fx ; lastY = fy ;
			} else {
				float dx = fx - lastX, dy = fy - lastY ;
				if(dx * dx + dy * dy >= 1.0f){
					outX[count] = fx ; outY[count] = fy ; count++ ;
					lastX = fx ; lastY = fy ;
				}
			}
		}
		sx = new float[count] ; sy = new float[count] ;
		System.arraycopy(outX, 0, sx, 0, count) ;
		System.arraycopy(outY, 0, sy, 0, count) ;
	}
}
