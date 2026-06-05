package com.sderhy ;
import java.awt.*;
import java.awt.image.* ;

class ZCanvas extends Canvas{

	public Image  img ;
	int w ,  h ;

		public ZCanvas(Image img, int w, int h){
			this.img = img ;
			this.w =w;
			this.h = h ;
			}
		public void setImage(Image i){ img =  i ;}	
		public void update(Graphics g){ paint(g) ;}
		public void paint(Graphics g){
		
				g.drawImage(img,0,0,w,h,0,0,w,h,this) ;
		}

}
