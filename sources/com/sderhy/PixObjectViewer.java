/*
*	@author Serge Derhy
*	@date 10 mai 1998
*/

package com.sderhy;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;
//import PixObject ;
import tools.* ;

public class PixObjectViewer extends ImageViewer {
PixObject po ;
InfoFrame infoFrame ;
boolean isShowingInfo = false ;

	public PixObjectViewer ( PixObject po){
		super(po.image) ;
		this.po = po;
		if(po.isDicom) init() ;
		}// end Of constructor
		
	public void init()	{
	
		MenuBar mb = this.getMenuBar() ;
	 	Menu dicomMenu =  new Menu("Dicom") ;
	 	MenuItem m ;
	 	dicomMenu.add(m = new MenuItem("See Attributes"));//Undo
	 	m.addActionListener(this);
		mb.add(dicomMenu);	
		dicomMenu.add(m = new MenuItem("Hide Attributes"));//Undo
	 	m.addActionListener(this);
		mb.add(dicomMenu);
		
		
		
	}//endOfInit(
	
	
	public void  hide(){
				if(isShowingInfo && infoFrame!=null){
					infoFrame.hide() ;
					infoFrame.dispose() ;
					}
				po.isShowing = false ;
				super.hide() ;
				}
/*	
	public void dispose(){
		hide();
		super.dispose() ;
	}
//*/
	public  void actionPerformed(ActionEvent e){
		
		if(e.getActionCommand() == "print"){
			 print() ;
		
		}
		else if(e.getActionCommand() == "See Attributes"){
			if(isShowingInfo && infoFrame!=null){
					infoFrame.toFront();
					 return ;
			}
			infoFrame = new InfoFrame (this,"Attributes") ;
			TextArea ta = new TextArea(  ) ;
			ta.setText("Here are the information for the file : \n\n") ;
			ta.appendText("URL : " + po.url.toString()+ "\n\n");
			
			ta.setFont(new Font("Monaco",Font.PLAIN, 10));
			infoFrame.add(ta) ;
			String[] attribute =	po.getInfo() ;
			for( int i =  0 ; i < attribute.length ; i++ ){
				ta.appendText(attribute[i] + " \n");
				}
			infoFrame.resize( 400,300) ;
			infoFrame.show();
			isShowingInfo = true ;
		//	infoFrame.addWindowListener()
		
				
		}
		else if(e.getActionCommand() == "Hide Attributes"){
			if(isShowingInfo && infoFrame!=null){
					infoFrame.hide() ;
					isShowingInfo = false ;
					infoFrame.dispose() ;
					}
			}
		else if(e.getActionCommand() == "reset"){
			image = po.image;
			super.arrangeIt();
			repaint();
			}

		else super.actionPerformed(e);
	}
	/*
	 public void show(){
	 	
	 	super.show() ;
	 	po.isShowing = true ;
	 }
	//*/
	 public void print(){
    	
    	Toolkit toolkit = getToolkit() ;
		
		java.util.Properties printPrefs = new java.util.Properties();
		
		PrintJob job = toolkit.getPrintJob(this, "Print Image", printPrefs);
		if(job == null) return ;
    	Graphics g  = job.getGraphics() ;
			g.translate(50,50) ;
			Dimension size = this.getSize() ;
			g.drawString ("URL ://" + po.url , -2, -12);
			g.drawRect(-1,-1,size.width +1, size.height +1);
			g.setClip(0,0, size.width, size.height);
			//g.drawImage(applet.toDisplay , 10 ,20, this );		
			paint(g) ;
			g.dispose();
			job.end();
    }
/////////////////////InfoFrame///////////////////////////////////////	
class InfoFrame extends java.awt.Frame implements WindowListener {
	PixObjectViewer pov ;
		public InfoFrame(PixObjectViewer pov , String title) {
			super(title) ;
			this.pov = pov ;
			this.addWindowListener(this) ;
			}
			
			
	public void windowClosing(WindowEvent e) {
			 pov.isShowingInfo = false ;
			 this.hide() ;	
			 this.dispose(); 
	}
  	public void windowOpened(WindowEvent e) {}
  	public void windowClosed(WindowEvent e) {}
  	public void windowIconified(WindowEvent e) {}
  	public void windowDeiconified(WindowEvent e) {}
  	public void windowActivated(WindowEvent e) {}
  	public void windowDeactivated(WindowEvent e) {}		
			
	
	}//end of class infoFrame	
	
	
}// end of class


