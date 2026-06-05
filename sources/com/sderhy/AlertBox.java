// AlertBox.java
package com.sderhy ;
import java.awt.*;
import java.io.*;

/** Application AlertBox : a simple alert box to display a simple text. 
 *  @author   Serge Derhy	sderhy@imaginet.fr
 *  @version  1.0
 */
public class AlertBox extends Dialog{
		private Button OK;
		private Label label;
/**
*	first constructor, display the title and a message
*/
	public AlertBox(Frame parent, String title, String message ){
		super(parent,title, false);
		this.setBackground(Color.lightGray);
		Panel p1= new Panel();
		label = new Label(message,Label.CENTER);
		Font f =new Font("TimesRoman", Font.BOLD, 14);
		FontMetrics fm = getFontMetrics(f);
		int w =  fm.stringWidth(message);
		label.setFont( f);
		p1.add(label);
		this.add("Center",p1);
		Panel p2 = new Panel( );
		OK = new Button ( "     OK     " );
		p2.add(OK);
		this.add("South",p2);
		this.resize( w+90, 120 );
		this.setResizable(false);
		this.move(300,200);
	
		this.toFront();
		this.show();
		
		p1.repaint() ;
			p2.repaint();
		this.repaint() ;
		
	}// end of constructor

/**
*	Second constructor, overrides the first display only the message
*/
	
	public AlertBox(Frame parent, String message){
		this(parent," DicomJavaViewer ",message);
	}
  public boolean handleEvent( Event e){  
		if ( e.id == Event.WINDOW_DESTROY) return getOut();
		else if (e.id == Event.ACTION_EVENT && e.target instanceof Button) return getOut();
		else if(e.id == Event.KEY_PRESS && e.key == '\n' ) return getOut();
		else if (e.id == Event.GOT_FOCUS&& e.key == '\n')  return getOut();
		else	return false;
	}//end of 	public boolean handleEvent( Event e)
   
   
/**
*		getOut() a way to getout of here !
*/

	private boolean getOut(){	
		this.hide();
		this.dispose();
		return true;
	}// end of getOut

/**
*	A main method to test the class 
*/	
	public static void main(String args[])
	{
		Frame f = new Frame( "AlertBox Test" );
		f.resize(200,200);
		f.show(); 
		AlertBox  Ab= new AlertBox(f,"MyAlertBox","This is a Serge Derhy' s Application");
	}

}// end of  dialog


