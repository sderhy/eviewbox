package com.sderhy ;
import java.awt.*;
import java.awt.event.*;

/**
* The curve editor of the curved (curvilinear) reconstruction window. The user
* clicks control points on the displayed slice ; a natural cubic spline is
* fitted through them (see CurveModel) and drawn as a green path. DrawJaws then
* samples the slice stack along that path.
*
* Coordinates here are 1:1 with the image (the slice is drawn at its native
* w x h), so canvas coordinates ARE image coordinates.
*/
public class DCanvas extends Canvas implements MouseListener, MouseMotionListener {
	DrawJaws dj ;
	private int h, w ;
	private final CurveModel curve = new CurveModel() ;
	private Image offscreen ;
	private Graphics og ;

	public DCanvas(DrawJaws dj){
		this.setBackground(Color.gray) ;
		this.dj = dj ;
		this.addMouseListener(this) ;
		this.addMouseMotionListener(this) ;
		h = dj.h ; w = dj.w ;
		this.setSize(w, h) ;
	}

	public void mousePressed(MouseEvent e){
		requestFocus() ;             // keep arrow-key slice navigation working
		int x = e.getX(), y = e.getY() ;
		if(x < 0) x = 0 ; if(x > w - 1) x = w - 1 ;
		if(y < 0) y = 0 ; if(y > h - 1) y = h - 1 ;
		curve.addPoint(x, y) ;
		repaint() ;
	}

	// Tessellated curve in image coordinates, consumed by DrawJaws.zconstruc().
	public float[] getCurveX(){ return curve.sampleX() ; }
	public float[] getCurveY(){ return curve.sampleY() ; }
	public int sampleCount(){ return curve.sampleCount() ; }

	public void clear(){ curve.clear() ; repaint() ; }
	public void undo(){ curve.undo() ; repaint() ; }

	public void update(Graphics g){ paint(g) ; }

	private void myPaint(Graphics g){
		g.drawImage(dj.img, 0, 0, w, h, 0, 0, w, h, this) ;
		// Control points (yellow squares).
		g.setColor(Color.yellow) ;
		for(int i = 0 ; i < curve.pointCount() ; i++)
			g.fillRect((int)curve.controlX(i) - 2, (int)curve.controlY(i) - 2, 5, 5) ;
		// Tessellated spline (green path).
		g.setColor(Color.green) ;
		float[] cx = curve.sampleX(), cy = curve.sampleY() ;
		for(int i = 1 ; i < cx.length ; i++)
			g.drawLine((int)cx[i-1], (int)cy[i-1], (int)cx[i], (int)cy[i]) ;
	}

	public void paint(Graphics g){
		if(offscreen == null){
			offscreen = createImage(w, h) ;
			og = offscreen.getGraphics() ;
			og.setClip(0, 0, w, h) ;
		}
		myPaint(og) ;
		g.drawImage(offscreen, 0, 0, null) ;
	}

	public void mouseDragged(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}
}
