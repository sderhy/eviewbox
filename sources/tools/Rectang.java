/***
****/
/****/

package tools ;
import java.awt.*;
public class Rectang /*extends java.awt.Rectangle*/ {
	int x,y,h,w ;
	
	public Rectang( int x ,int y , int w, int h){
		//super(x,y,w,h);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public Rectang(int w,int h){ this(0,0,w,h);}
	public Rectang(int w){ this(0,0,w,w);}
	
	public void paintInside(Graphics g) {
		Color c = g.getColor() ;
		g.setColor(c.brighter().brighter().brighter());
		g.drawRect(x,y ,w-1,h-1) ;
		
		g.setColor(c.darker().darker().darker().darker());
		g.drawRect(x+1,y+1,w-1,h-1) ;
		}
		
	/*	
		public void paintInside(Graphics g) {
		Color c = g.getColor() ;
		g.setColor(c.brighter().brighter().brighter());
		g.drawRect(x+1,y+1,w-2,h-2) ;
		
		g.setColor(c.darker().darker().darker().darker());
		g.drawRect(x+2,y+2,w-2,h-2) ;
	}
	*/	
		
	public void paintThick(Graphics g, int thick){
		g.setColor( g.getColor().darker().darker().darker().darker());
		for (int i= 0 ; i <  thick ; i++)
			g.drawRect(x -i,y -i, w -(i*2), h-(i*2));
	}
		
		
	}
