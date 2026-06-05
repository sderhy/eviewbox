package com.sderhy;
import java.applet.*;
import java.awt.*;
import java.io.*;
import com.sderhy.Spline;

public class PlotSpline2D extends Canvas {
	private Spline		mSplineX		= null;
	private Spline		mSplineY		= null;
	private Color		mForeColor;
	private Color		mBackColor;

	private final float	kError		= 0.5f;

	private final int		kPointLength	= 100;
	private float []		mT			= new float[kPointLength];
	private float []		mX			= new float[kPointLength];
	private float []		mY			= new float[kPointLength];
	private int			mNumPoints	= 0;

	// --------------------------------------------------
	//	constructor
	// --------------------------------------------------
	
	public PlotSpline2D() {
		super();
		setForeground( Color.black );
		setBackground( Color.white );
	}
	// --------------------------------------------------
	//	PlotSpline2D.mouseDown()
	// --------------------------------------------------

	public boolean mouseDown( Event event, int x, int y ) {
		AddPoint( getGraphics(), x, y );
		SetupSpline();
		repaint() ;
		return true;
	}

	// --------------------------------------------------
	//	PlotSpline2D.AddPoint()
	// --------------------------------------------------
	public void AddPoint( Graphics g, int x, int y ) {

		if ( mNumPoints >= kPointLength-1 ) return;
	
		mT[mNumPoints] = (float) mNumPoints;
		mX[mNumPoints] = (float) x;
		mY[mNumPoints] = (float) y;
		mNumPoints++;

		drawAnchor( g, mX[mNumPoints-1], mY[mNumPoints-1], Color.blue );
	}

	// --------------------------------------------------
	//	PlotSpline2D.RemovePoint()
	// --------------------------------------------------
	public void RemovePoint( Graphics g ) {
		if ( mNumPoints == 0 ) return;
		drawAnchor( g, mX[mNumPoints-1], mY[mNumPoints-1], Color.red );
		mNumPoints--;
	}


	// --------------------------------------------------
	//	PlotSpline2D.RemoveAllPoints()
	// --------------------------------------------------

	public void RemoveAllPoints() {	mNumPoints = 0;}

	// --------------------------------------------------
	//	PlotSpline2D.SetupSpline()
	// --------------------------------------------------

	public void SetupSpline( ) {
		mSplineX		= null;
		mSplineY		= null;

			//	if ( 2 * mNumPoints - 1 <= 3 ) return;
			 	if(mNumPoints < 2 ) return ;
				mSplineX = new Spline( mT, mX, mNumPoints );
				mSplineY = new Spline( mT, mY, mNumPoints );
	}			


	// --------------------------------------------------
	//	PlotSpline2D.CalcSpline()
	// --------------------------------------------------

	public boolean CalcSpline( float t, float [] pxy ) {

				if ( mSplineX == null || mSplineY == null ) return false;
				pxy[0] = mSplineX.CalcValue( t );
				pxy[1] = mSplineY.CalcValue( t );
	
		return true;

	}

	// --------------------------------------------------
	//	PlotSpline2D.drawSpline()
	// --------------------------------------------------
	public void drawSpline( Graphics g ) {
		SetupSpline();
		paint( g );
	}
	// --------------------------------------------------
	//	PlotSpline2D.paint()
	// --------------------------------------------------
	
	public void paint( Graphics g ) {
		
		drawAnchors( g );

		float []	knotXY1 = new float [2];
		float []	knotXY2 = new float [2];

		if ( ! CalcSpline( mT[0], knotXY1 ) ) return;

		for ( int i = 0; i < mNumPoints-1; i++ ) {

			if ( ! CalcSpline( mT[i+1], knotXY2 ) ) continue;
			float	dx = knotXY2[0] - knotXY1[0];
			float	dy = knotXY2[1] - knotXY1[1];
			int	numDiv = (int) ( Math.sqrt( dx * dx + dy * dy ) / 5.0f ) + 1;

			float []	pxy1		= new float [2];
			float []	pxy2		= new float [2];
			if ( ! CalcSpline( mT[i], pxy1 ) ) continue;
			
			for ( int j = 0; j < numDiv; j++ ) {
				float t = mT[i] + (mT[i+1] - mT[i]) * (float) (j+1) / (float) numDiv;
				if ( ! CalcSpline( t, pxy2 ) ) continue;

				PlotLine( g, pxy1[0], pxy1[1], pxy2[0], pxy2[1] );

				pxy1[0] = pxy2[0];
				pxy1[1] = pxy2[1];

			}

			knotXY1[0] = knotXY2[0];
			knotXY1[1] = knotXY2[1];

		}
	}


	// --------------------------------------------------
	//	PlotSpline2D.drawAnchors()
	// --------------------------------------------------
	
	public void drawAnchors( Graphics g ) {

		for ( int i = 0;  i < mNumPoints;  i++ )
			PlotAnchor( g, mX[i], mY[i] );

	}


	public void drawAnchor( Graphics g, float x, float y, Color c ) {

		g.setColor( c );
		PlotAnchor( g, x, y );
		g.setColor( mForeColor );

	}
	// --------------------------------------------------
	//	PlotSpline2D.PlotLine()
	// --------------------------------------------------
	
	private void PlotLine( Graphics g, float x1, float y1, float x2, float y2 ) {

		g.drawLine( (int)(x1+kError), (int)(y1+kError),
				(int)(x2+kError), (int)(y2+kError) );

	}
	// --------------------------------------------------
	//	PlotSpline2D.PlotAnchor()
	// --------------------------------------------------
	private void PlotAnchor( Graphics g, float x1, float y1 ) {
		g.drawRect( (int)(x1+kError)-1, (int)(y1+kError)-1, 3, 3 );
	}

	// --------------------------------------------------
	//	PlotSpline2D.Erase()
	// --------------------------------------------------
	public void Erase() {
		Graphics	g = getGraphics();
		Rectangle	r = bounds();
		g.setColor( getBackground() );
		g.fillRect( 0, 0, r.width, r.height );
		g.setColor( getForeground() );
		RemoveAllPoints();
	}
}

// end of program
