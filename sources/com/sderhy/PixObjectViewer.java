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
	private static final int AUTO_SCROLL_DELAY_MS = 200 ;
	PixObject po ;
	PixCanvas sourceCanvas ;
	InfoFrame infoFrame ;
	boolean isShowingInfo = false ;
	volatile boolean autoScrolling = false ;
	Thread autoScrollThread ;
	MenuItem autoScrollMenu ;

		public PixObjectViewer ( PixObject po){
			this(po, null);
			}// end Of constructor

		public PixObjectViewer ( PixObject po, PixCanvas sourceCanvas){
			super(po.image) ;
			this.po = po;
			this.sourceCanvas = sourceCanvas;
			addKeyListener(this);
			enableEvents(AWTEvent.KEY_EVENT_MASK);
			initReconstructionMenu();
			initAutoScrollMenu();
			// Reinstall the Dicom menu whenever this window regains focus :
			// on the shared macOS menu bar it is wiped when another window takes over.
			addWindowListener(new WindowAdapter(){
				public void windowActivated(WindowEvent e){
					if(po.isDicom) init() ;
				}
			});
			if(po.isDicom) init() ;
			}// end Of constructor

		private void initReconstructionMenu(){
			if(sourceCanvas == null) return ;
			Menu reconstruction = new Menu("Reconstruction");
			MenuItem m ;
			reconstruction.add(m = new MenuItem("Frontal Linear"));
			m.setActionCommand("mprFrontal");
			m.addActionListener(this);
			reconstruction.add(m = new MenuItem("Sagittal Linear"));
			m.setActionCommand("mprSagittal");
			m.addActionListener(this);
			reconstruction.add(m = new MenuItem("Curved Linear"));
			m.setActionCommand("mprCurved");
			m.addActionListener(this);
			popup.addSeparator();
			popup.add(reconstruction);
		}

		private void initAutoScrollMenu(){
			if(sourceCanvas == null) return ;
			popup.addSeparator();
			autoScrollMenu = new MenuItem("Start Auto Scroll");
			autoScrollMenu.setActionCommand("autoScroll");
			autoScrollMenu.addActionListener(this);
			popup.add(autoScrollMenu);
		}

		public void init()	{
			MenuBar mb = this.getMenuBar() ;
			if(mb == null) return ;
			// Idempotent : only add the menu if the current MenuBar does not have it yet.
			// The MenuBar may be rebuilt (myFrame.arrange) and lose the menu, so we
			// can't rely on a one-shot flag : we scan the live bar instead.
			if(findMenu(mb, "Dicom") != null) return ;

			Menu dicomMenu =  new Menu("Dicom") ;
			MenuItem m ;
			dicomMenu.add(m = new MenuItem("See Attributes"));//Undo
			m.addActionListener(this);
			dicomMenu.add(m = new MenuItem("Hide Attributes"));//Undo
			m.addActionListener(this);
			mb.add(dicomMenu);
	}//endOfInit(

		// Returns the menu with the given label in the bar, or null if absent.
		private Menu findMenu(MenuBar mb, String label){
			if(mb == null) return null ;
			for(int i = 0 ; i < mb.getMenuCount() ; i++){
				Menu menu = mb.getMenu(i) ;
				if(menu != null && label.equals(menu.getLabel())) return menu ;
			}
			return null ;
		}


	public void  hide(){
				stopAutoScroll();
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
		else if(e.getActionCommand() == "autoScroll"){
			toggleAutoScroll();
			}
		else if(e.getActionCommand() == "mprFrontal"){
			startReconstruction(Multiplanar.FRONTAL);
			}
		else if(e.getActionCommand() == "mprSagittal"){
			startReconstruction(Multiplanar.SAGITTAL);
			}
		else if(e.getActionCommand() == "mprCurved"){
			startReconstruction(Multiplanar.CURVED);
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
			Winager.keepMainBehindApplicationWindows(this);
			requestFocus();
		 }

		private void startReconstruction(int mode){
			if(sourceCanvas == null || sourceCanvas.vimages == null) return ;
			new Multiplanar(sourceCanvas, mode, sourceCanvas.vimages.indexOf(po));
		}

		private void toggleAutoScroll(){
			if(autoScrolling) stopAutoScroll();
			else startAutoScroll();
		}

		private void startAutoScroll(){
			if(sourceCanvas == null || sourceCanvas.vimages == null || sourceCanvas.vimages.size() < 2) return ;
			autoScrolling = true ;
			if(autoScrollMenu != null) autoScrollMenu.setLabel("Stop Auto Scroll");
			autoScrollThread = new Thread(new Runnable(){
				public void run(){
					while(autoScrolling){
						try{ Thread.sleep(AUTO_SCROLL_DELAY_MS); }
						catch(InterruptedException interrupted){ return ;}
						EventQueue.invokeLater(new Runnable(){
							public void run(){
								if(autoScrolling) showSibling(1);
							}
						});
					}
				}
			});
			autoScrollThread.setDaemon(true);
			autoScrollThread.start();
		}

		private void stopAutoScroll(){
			autoScrolling = false ;
			if(autoScrollMenu != null) autoScrollMenu.setLabel("Start Auto Scroll");
			if(autoScrollThread != null){
				autoScrollThread.interrupt();
				autoScrollThread = null ;
			}
		}

		public void keyPressed(KeyEvent e){
			if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				toggleAutoScroll();
				e.consume();
			}
			else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
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
