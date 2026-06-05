/**
* 	@author Serge Derhy
*	Inset Panel  , class for a better display
*/
package com.sderhy;
import java.awt.*;
public class InsetPanel extends Panel{
	
	private int top = 10,left= 10 ,right= 10 ,bottom = 10;
	private boolean isEtched = false ;
	private boolean  isEtchedIn = true ;
	
	public InsetPanel(int t, int l){
		super();
		top = bottom = t;
		left= right =  l;
		
	}
	
	public InsetPanel(int t, int l,Color c){
		this(t,l);
		this.setBackground(c);
	}
	
	public InsetPanel(){this(10,10);}
	public InsetPanel(Color c){this(10,10,c);}
	
	
	public void setEtched(boolean yesOrNo, boolean in ){
		this.isEtched = yesOrNo ;
		this.isEtchedIn = in ;
	}
	public void setEtched(boolean yesOrNo){
		this.isEtched = yesOrNo ;
		this.isEtchedIn = true ;
	}
	
	public void paint(Graphics g){
	
	if( isEtched){	
		int w = this.getSize().width	-right		    ;
		int h = this.getSize().height-bottom			;
		if (isEtchedIn){
				g.setColor(getBackground().darker().darker())			;
				g.drawRect( top/2,left/2,w,h);
				g.setColor(getBackground().brighter())				;
				g.drawRect(top/2+1,left/2+1,w,h);
			}
		else{	
				g.setColor(getBackground().brighter())				;
				g.drawRect( top/2,left/2,w,h);
		
				g.setColor(getBackground().darker().darker())			;
				g.drawRect(top/2+1,left/2+1,w,h);
			}//endelse	
		}// endif		
	}//endpaint	
		
		
	public Insets getInsets(){
			return new Insets( top,left,bottom,right);
		}
	public  static  final void auteur(){
		System.out.println("L'auteur de cette classe est Serge Derhy") ;
		
	}
		
}// end of class
