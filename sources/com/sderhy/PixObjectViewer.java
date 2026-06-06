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

public class PixObjectViewer extends ImageViewer implements KeyListener {
	PixObject po ;
	PixCanvas sourceCanvas ;
	InfoFrame infoFrame ;
	boolean isShowingInfo = false ;
	boolean dicomMenuInstalled = false ;

		public PixObjectViewer ( PixObject po){
			this(po, null);
			}// end Of constructor

		public PixObjectViewer ( PixObject po, PixCanvas sourceCanvas){
			super(po.image) ;
			this.po = po;
			this.sourceCanvas = sourceCanvas;
			addKeyListener(this);
			enableEvents(AWTEvent.KEY_EVENT_MASK);
			if(po.isDicom) init() ;
			}// end Of constructor

		public void init()	{
			if(dicomMenuInstalled) return ;

			MenuBar mb = this.getMenuBar() ;
			Menu dicomMenu =  new Menu("Dicom") ;
			MenuItem m ;
			dicomMenu.add(m = new MenuItem("See Attributes"));//Undo
			m.addActionListener(this);
			dicomMenu.add(m = new MenuItem("Hide Attributes"));//Undo
			m.addActionListener(this);
			mb.add(dicomMenu);
			dicomMenuInstalled = true ;



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
				if(po.getInfo() == null) return ;
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
		 public void show(){
			super.show() ;
			po.isShowing = true ;
			requestFocus();
		 }

		public void keyPressed(KeyEvent e){
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				showSibling(1);
				e.consume();
			}
			else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				showSibling(-1);
				e.consume();
			}
		}

		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}

		private void showSibling(int increment){
			if(sourceCanvas == null || sourceCanvas.vimages == null || sourceCanvas.vimages.isEmpty()) return ;
			int index = sourceCanvas.vimages.indexOf(po);
			if(index < 0) return ;
			int size = sourceCanvas.vimages.size();
			int nextIndex = (index + increment + size) % size;
			PixObject next = (PixObject)sourceCanvas.vimages.elementAt(nextIndex);
			if(next == null) return ;
			int previousWidth = w;
			int previousHeight = h;
			int previousDestWidth = destw;
			int previousDestHeight = desth;
			int previousX = x;
			int previousY = y;
			Dimension windowSize = getSize();
			boolean keepWindowLevel = DrawLayout && layoutString != null && layoutString.length() > 0;
			String previousLayoutString = layoutString;

			if(isShowingInfo && infoFrame != null){
				infoFrame.hide();
				infoFrame.dispose();
				isShowingInfo = false;
				infoFrame = null;
			}
			po.isShowing = false ;
			po = next ;
			po.isShowing = true ;
			image = po.image ;
			origPic = image ;
			w = po.w ;
			h = po.h ;
			if(previousWidth == w && previousHeight == h){
				destw = previousDestWidth;
				desth = previousDestHeight;
				x = previousX;
				y = previousY;
			}
			else{
				destw = (previousWidth == 0) ? w : (previousDestWidth * w) / previousWidth;
				desth = (previousHeight == 0) ? h : (previousDestHeight * h) / previousHeight;
				centerImage();
			}
			if(keepWindowLevel){
				image = ProcessImage.brightenCurrent(this, origPic);
				DrawLayout = true ;
				layoutString = previousLayoutString;
			}
			else{
				DrawLayout = false ;
				layoutString = "";
			}
			setSize(windowSize);
			offs = null;
			sourceCanvas.lastSel = nextIndex ;
			sourceCanvas.frame.TF.setText(po.url.toString());
			sourceCanvas.repaint();
			if(po.isDicom) init() ;
			setTitle("Image " + (nextIndex + 1) + " / " + size);
			repaint();
			requestFocus();
		}

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
