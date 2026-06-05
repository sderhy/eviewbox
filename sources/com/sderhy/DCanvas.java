package com.sderhy ;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.*;
import tools.Tools.*;

 public class DCanvas extends Canvas implements MouseListener,  MouseMotionListener {
  	
  // For Spline construct :
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
// end of Spline  	

 // 	private int last_x, last_y;
	DrawJaws dj ;
	private int firstLine= 0; 
	private int lastLine = 0;	
	private  Vector lines = new Vector(32,32); 
		
  	public int x1,x2,y1,y2 ;
  	private int h , w ;
  	public int maxLength ;
  	boolean firstPass = true ;
 	private Image  offscreen ;
  	private 	Graphics og ;
    

  public DCanvas(DrawJaws dj) {  
  	this.setBackground( Color.gray) ;
    this.dj = dj ;
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    h= dj.h ; w = dj.w ; 
  	maxLength = 3*w;
  	this.setSize(w ,h) ;
    }

  public void mousePressed(MouseEvent e) {
  //	if(firstPass)
		  	
  		if(maxLength < Line.length ) return ;
	   	int x = e.getX(), y = e.getY();
  		if(x < 0)  x = 0;
    	if(y < 0) y=0 ;
   		if(x > w) x = w ;
   		if(y > h) y = h ;
  		firstPass = false ;
 		if ( mNumPoints >= kPointLength-1 ) return;
		mT[mNumPoints] = (float) mNumPoints;
		mX[mNumPoints] = (float) x;
		mY[mNumPoints] = (float) y;
		mNumPoints++;
		repaint();

	}

public void SetupSpline( ) {
		mSplineX		= null;
		mSplineY		= null;
		if(mNumPoints < 2 ) return ;
		mSplineX = new Spline( mT, mX, mNumPoints );
		mSplineY = new Spline( mT, mY, mNumPoints );
		
		lines.removeAllElements();
      	Line.length = 0;
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
				
				Line  l =   new Line((int)(pxy1[0]+kError),(int)(pxy1[1]+kError),  (int)(pxy2[0]+kError) , (int)(pxy2[1]+kError) ) ;
			    lines.addElement(l) ;

				pxy1[0] = pxy2[0];
				pxy1[1] = pxy2[1];

			}
			knotXY1[0] = knotXY2[0];
			knotXY1[1] = knotXY2[1];
		}
		
}	

	
	private void drawAnchors( Graphics g ) {
		for ( int i = 0;  i < mNumPoints;  i++ )
			g.drawRect( (int)(mX[i]+kError)-1, (int)(mY[i]+kError)-1, 3, 3 );
	}	
	
	
	private boolean CalcSpline( float t, float [] pxy ) {
				if ( mSplineX == null || mSplineY == null ) return false;
				pxy[0] = mSplineX.CalcValue( t );
				pxy[1] = mSplineY.CalcValue( t );
	
		return true;
	}
	
	
  public void mouseDragged(MouseEvent e) { }
 
 
  	public void update(Graphics g) { paint(g); }
  
  	public void myPaint(Graphics og) {
  			og.drawImage(dj.img,0,0,w,h,0,0,w,h,this) ;
			SetupSpline();
			drawAnchors( og );

  			for(int i = 0; i < lines.size(); i++) {
        		Line l = (Line)lines.elementAt(i);
        		og.drawLine(l.x1, l.y1, l.x2, l.y2);
    		  }
    
  	}

  	public void paint( Graphics g){
      if(offscreen == null) {
         offscreen = createImage(w,h);
      	 og = offscreen.getGraphics();
    	 og.setClip(0,0,w, h);
 		og.setColor(Color.yellow);
      }

      myPaint(og);
      g.drawImage(offscreen, 0, 0, null);
	}//end of paint
	
	public  void clear() {	
      	lines.removeAllElements();
      	Line.length = 0;
      	firstPass = true ;
      	firstLine = 0 ;
      	lastLine  = 0 ;
      	mNumPoints = 0;
      	repaint(); 
    }
    
    public void undo(){
    	 	mNumPoints--;
	    	 repaint();
    }
    private	 void resetLines(){
		resetLines(true);
	}
	private void resetLines(boolean pass){
		firstPass = pass ;
		if(pass) firstLine = lastLine ;
		Line.length = 0;
		mNumPoints = 0;
	}
    
	public int[] getChoosenPixels(){
		int[] choosenPixels = new int[Line.length] ;
		int index = 0 ;
		int xi, yi ;
		
	//	for(int i = firstLine; (i < lines.size() && i < lastLine); i++) {
     	for(int i = 0; i < lines.size(); i++) {
    		Line l = (Line)lines.elementAt(i);
        	for (int j = 0 ; j< l.len ; j++){		
        		xi = l.x1 + (int)Math.round( j * l.cosinus) ;
        		yi = l.y1 + (int)Math.round( j * l.sinus) ;
        		choosenPixels[index]	 = yi* w + xi ;  
        		
        		index++ ;		
        	}// end of innerloop		
        }// endOfFor loop		
		//resetLines() ;
		return choosenPixels;
	}
	
  static class Line {
			
			public static int length = 0 ;	
				
   	   		public int x1, y1, x2, y2, len ;
   	   		public double sinus , cosinus;
    	  	
    	  	public Line(int x1, int y1, int x2, int y2) {
		    	this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		    	computeLength() ;
    		}
    		public void computeLength() {
    			len = ( (x2-x1)*(x2-x1) )+ ((y2-y1)*(y2-y1)) ;
    			len = (int)Math.sqrt(len) ;
    			sinus = (double)(y2-y1)/len ;
    			cosinus = (double)(x2-x1)/len;
    			length += len ;
    		}
    }
	
  	public void mouseReleased(MouseEvent e) {;}
  	public void mouseClicked(MouseEvent e) {;}
  	public void mouseEntered(MouseEvent e) {;}
  	public void mouseExited(MouseEvent e) {;}
	// MouseMotionListener interface.
  	public void mouseMoved(MouseEvent e) {;}
}
