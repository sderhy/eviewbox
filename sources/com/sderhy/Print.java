package com.sderhy ;
import java.awt.*;

public class Print implements Runnable{

Component c ;
PrintJob job ;
Thread runner ;

	public Print( Component c){
			
    	Toolkit toolkit = c.getToolkit() ;
		
		java.util.Properties printPrefs = new java.util.Properties();
		
		job = toolkit.getPrintJob( new java.awt.Frame(), "Print Image", printPrefs);
		if(job == null) return ;
		runner = new Thread(this) ;
		runner.start() ;
	
    	
    	}


 	public void run(){	
 			
 			
			tools.Tools.debug(this, " run ") ;
			Graphics g  = job.getGraphics() ;
			g.translate(50,50) ;
			
			Dimension size = c.getSize() ;
			g.drawString ("Something to draw" , -2, -12);
			g.drawRect(-1,-1,size.width +1, size.height +1);
	
			g.setClip(0,0, size.width, size.height);
			c.paint(g) ;
			g.dispose();
			job.end();
	
		
	}

	public void done(){
		job.end();
		runner.stop() ;
		runner = null ;
		
	}

}


