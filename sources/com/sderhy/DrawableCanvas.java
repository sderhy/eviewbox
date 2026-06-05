package com.sderhy ;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.*;
/*import PixObject ;
import tools.Tools.*;
*/
 public class DrawableCanvas extends Canvas implements MouseListener,  MouseMotionListener {
  	private int last_x, last_y;
	DrawableFrame df ;	
	
  	public int x1,x2,y1,y2 ;
  	private int h , w ;
  	boolean canMove = false ;
  	boolean canTurn = false ;
  	boolean isDental = false ;
  	boolean firstPass = true ;
 	private Image  offscreen ;
  	private 	Graphics og ;
    
  public DrawableCanvas(DrawableFrame df) {  
  	this.setBackground( Color.black) ;
    this.df = df ;
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    h= df.h ; w = df.w ;
    x1 = 0; x2 = w ; y1 = y2 = h/2 ;
    isDental = df.isDental();
    this.setSize(w ,h) ;
  }

  public void mousePressed(MouseEvent e) {
 //   last_x = e.getX();
  //  last_y = e.getY();
  }

  public void mouseDragged(MouseEvent e) {
    Graphics g = this.getGraphics();
    int x = e.getX(), y = e.getY();
    g.setClip(0,last_y,w , y-last_y);// clipping g
    last_x = x; last_y = y;
    y1 = y2= last_y ;
    repaint();
      df.zUpdate();
     }
  
 public void update(Graphics g) {
      paint(g);

  }
  

  public void myPaint(Graphics og) {
  	og.drawImage(df.img,0,0,w,h,0,0,w,h,this) ;
	og.drawLine(x1 , y1 , x2 ,y2 ) ;
	
	}

  public void paint( Graphics g){
      if(offscreen == null) {
         offscreen = createImage(w,h);
      	 og = offscreen.getGraphics();
    	 og.setClip(0,0,w, h);
      }

      myPaint(og);
      g.drawImage(offscreen, 0, 0, null);
	}//end of paint
	
  // The other, unused methods of the MouseListener interface.
  public void mouseReleased(MouseEvent e) {;}
  public void mouseClicked(MouseEvent e) {;}
  public void mouseEntered(MouseEvent e) {;}
  public void mouseExited(MouseEvent e) {;}

  // The other method of the MouseMotionListener interface.
  public void mouseMoved(MouseEvent e) {;}
}
