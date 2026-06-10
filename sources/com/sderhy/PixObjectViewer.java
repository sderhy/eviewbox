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
	boolean showDensity = false ;          // HU readout under the cursor
	String densityText ;                   // current overlay text (null = nothing)
	MenuItem densityItemDicom, densityItemPopup ; // the two toggle items (kept in sync)
	private DrawableFrame recon ;          // hidden oblique engine (result shown)
	private boolean crossMode = false ;    // crosshair (two orthogonal lines) is active
	private double crCX, crCY ;            // crosshair centre, image coords
	private double crAngle = 0 ;           // orientation of line 0 (radians)
	private int activeLine = 0 ;           // which orthogonal line is reconstructed : 0 or 1
	private int dragMode = 0 ;             // 0 none, 1 translate, 2 rotate
	private boolean crossGesture = false ; // a crosshair press is in progress
	private static final int HANDLE_PX = 12 ;   // screen tolerance for centre / endpoints

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
			initDensityMenu();
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
			reconstruction.add(m = new MenuItem("Linear Reconstruction"));
			m.setActionCommand("mprLinear");
			m.addActionListener(this);
			// One crosshair (two orthogonal lines) replaces the fixed frontal /
			// sagittal cuts : move the centre, rotate it, click a line to pick the
			// one that is reconstructed. Curved stays in the main Multiplanar menu.
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

		private void initDensityMenu(){
			if(po == null || !po.hasHU) return ;
			popup.addSeparator();
			densityItemPopup = new MenuItem(densityLabel()) ;
			densityItemPopup.setActionCommand("toggleDensity") ;
			densityItemPopup.addActionListener(this) ;
			popup.add(densityItemPopup) ;
		}

		private String densityLabel(){ return showDensity ? "Hide Density" : "Display Density" ; }

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
			// Hounsfield window presets (only when HU data is available).
			if(po.hasHU){
				Menu window = new Menu("Window") ;
				addPreset(window, "Lung",                  -600, 1500) ;
				addPreset(window, "Mediastinum (soft tissue)", 40,  400) ;
				addPreset(window, "Abdomen (liver)",         60,  400) ;
				addPreset(window, "Bone",                   500, 2000) ;
				addPreset(window, "Brain",                   40,   80) ;
				window.addSeparator();
				MenuItem reset = new MenuItem("Reset (header default)") ;
				reset.setActionCommand("winReset") ;
				reset.addActionListener(this) ;
				window.add(reset) ;
				dicomMenu.add(window) ;
				dicomMenu.addSeparator();
				densityItemDicom = new MenuItem(densityLabel()) ;
				densityItemDicom.setActionCommand("toggleDensity") ;
				densityItemDicom.addActionListener(this) ;
				dicomMenu.add(densityItemDicom) ;
			}
			mb.add(dicomMenu);
	}//endOfInit(

		private void addPreset(Menu m, String label, double center, double width){
			MenuItem mi = new MenuItem(label + "   (C " + (int)center + " / W " + (int)width + ")") ;
			mi.setActionCommand("win:" + center + ":" + width) ;
			mi.addActionListener(this) ;
			m.add(mi) ;
		}

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
				stopReconstruction();
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

		String cmd = e.getActionCommand() ;
		// Window presets are built dynamically, so compare by value (not ==).
		if(cmd != null && cmd.startsWith("win:")){
			int c1 = cmd.indexOf(':') ;
			int c2 = cmd.indexOf(':', c1 + 1) ;
			try{
				double center = Double.parseDouble(cmd.substring(c1 + 1, c2)) ;
				double width  = Double.parseDouble(cmd.substring(c2 + 1)) ;
				applyWindow(center, width) ;
			}catch(Exception ex){}
			return ;
		}
		if("winReset".equals(cmd)){ applyWindow(po.defaultCenter, po.defaultWidth) ; return ; }
		if("toggleDensity".equals(cmd)){ toggleDensity() ; return ; }

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
		else if(e.getActionCommand() == "mprLinear"){
			startReconstruction();
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

		private void startReconstruction(){
			if(sourceCanvas == null || sourceCanvas.vimages == null) return ;
			int index = sourceCanvas.vimages.indexOf(po) ;
			// Draw a crosshair on THIS live viewer (navigable with the arrow keys)
			// and reconstruct the active (oblique) line into a single result window.
			stopReconstruction() ;
			Multiplanar mp = new Multiplanar(sourceCanvas, Multiplanar.FRONTAL, index, false) ;
			if(!mp.valid) return ;     // verification failed (alert already shown)
			recon = mp.buildFrame() ;
			if(recon.resultViewer != null) recon.resultViewer.setTitle("Linear reconstruction") ;
			recon.setOnDispose(new Runnable(){ public void run(){ clearCut() ; } }) ;
			crossMode = true ;
			crCX = w / 2.0 ; crCY = h / 2.0 ; crAngle = 0 ; activeLine = 0 ;
			pushCut(false) ;
			repaint() ;
		}

		private void stopReconstruction(){
			if(recon != null){ DrawableFrame r = recon ; recon = null ; r.dispose() ; }
			crossMode = false ;
		}

		// Called back when the result window is closed : drop the crosshair overlay.
		private void clearCut(){
			recon = null ;
			crossMode = false ;
			repaint() ;
		}

		// Direction angle of the active orthogonal line.
		private double activePhi(){ return crAngle + activeLine * (Math.PI / 2) ; }

		// Push the active line to the engine and rebuild the reconstruction.
		// preview = true asks for a fast, low-slice-count image (live dragging).
		private void pushCut(boolean preview){
			if(recon != null) recon.setCut(crCX, crCY, activePhi(), preview) ;
			repaint() ;
		}

		// Throttle the live preview so a fast drag doesn't queue dozens of rebuilds.
		private long lastPreviewMs = 0 ;
		private static final int PREVIEW_INTERVAL_MS = 50 ;
		private void schedulePreview(){
			long now = System.currentTimeMillis() ;
			if(now - lastPreviewMs >= PREVIEW_INTERVAL_MS){
				lastPreviewMs = now ;
				pushCut(true) ;
			}
		}

		// --- crosshair geometry, in image coordinates -------------------------

		// Endpoints of one orthogonal line (index 0 or 1), clipped to the image.
		private double[] lineSegment(int line){
			double phi = crAngle + line * (Math.PI / 2) ;
			return LineClip.segment(crCX, crCY, Math.cos(phi), Math.sin(phi), w, h) ;
		}

		private int imgToScreenX(double ix){ return x + (int)Math.round(ix * destw / w) ; }
		private int imgToScreenY(double iy){ return y + (int)Math.round(iy * desth / h) ; }
		private double screenToImgX(int sx){ return (double)(sx - x) * w / destw ; }
		private double screenToImgY(int sy){ return (double)(sy - y) * h / desth ; }

		// Decide what a press grabs : centre (translate), an endpoint (rotate, and
		// activate that line), or a line body (just activate it).
		private void pressCross(int sx, int sy){
			if(destw <= 0 || desth <= 0) return ;
			crossGesture = true ;
			// Centre handle ?
			if(dist(sx, sy, imgToScreenX(crCX), imgToScreenY(crCY)) <= HANDLE_PX){
				dragMode = 1 ; return ;
			}
			// Nearest endpoint of either line ?
			int bestLine = -1 ; double best = HANDLE_PX + 1 ;
			for(int line = 0 ; line < 2 ; line++){
				double[] s = lineSegment(line) ;
				if(s == null) continue ;
				double[][] ends = {{s[0], s[1]}, {s[2], s[3]}} ;
				for(int e = 0 ; e < 2 ; e++){
					double d = dist(sx, sy, imgToScreenX(ends[e][0]), imgToScreenY(ends[e][1])) ;
					if(d < best){ best = d ; bestLine = line ; }
				}
			}
			if(bestLine >= 0){ activeLine = bestLine ; dragMode = 2 ; rotateTo(sx, sy) ; return ; }
			// Otherwise : a click near a line body selects it as active.
			int pick = nearestLine(sx, sy) ;
			if(pick >= 0) activeLine = pick ;
			dragMode = 0 ;
			repaint() ;
		}

		// During a drag the crosshair follows in real time and a throttled low-res
		// preview is rebuilt ; the full-quality image is produced on release.
		private void dragCross(int sx, int sy){
			if(dragMode == 1){
				crCX = clamp(screenToImgX(sx), 0, w - 1) ;
				crCY = clamp(screenToImgY(sy), 0, h - 1) ;
			} else if(dragMode == 2){
				double a = Math.atan2(screenToImgY(sy) - crCY, screenToImgX(sx) - crCX) ;
				crAngle = a - activeLine * (Math.PI / 2) ;
			} else return ;
			repaint() ;
			schedulePreview() ;
		}

		// Rotate so the active line points at the cursor (overlay only, on grab).
		private void rotateTo(int sx, int sy){
			double a = Math.atan2(screenToImgY(sy) - crCY, screenToImgX(sx) - crCX) ;
			crAngle = a - activeLine * (Math.PI / 2) ;
			repaint() ;
		}

		// Index of the line whose body is closest to (sx,sy), or -1 if none within tol.
		private int nearestLine(int sx, int sy){
			int best = -1 ; double bestD = 8 ;
			for(int line = 0 ; line < 2 ; line++){
				double[] s = lineSegment(line) ;
				if(s == null) continue ;
				double d = pointToSegment(sx, sy,
					imgToScreenX(s[0]), imgToScreenY(s[1]), imgToScreenX(s[2]), imgToScreenY(s[3])) ;
				if(d < bestD){ bestD = d ; best = line ; }
			}
			return best ;
		}

		private static double dist(int x0, int y0, int x1, int y1){
			double dx = x0 - x1, dy = y0 - y1 ; return Math.sqrt(dx * dx + dy * dy) ;
		}
		private static double clamp(double v, double lo, double hi){
			return v < lo ? lo : (v > hi ? hi : v) ;
		}
		private static double pointToSegment(int px, int py, int ax, int ay, int bx, int by){
			double vx = bx - ax, vy = by - ay ;
			double len2 = vx * vx + vy * vy ;
			double t = (len2 == 0) ? 0 : ((px - ax) * vx + (py - ay) * vy) / len2 ;
			t = clamp(t, 0, 1) ;
			double cx = ax + t * vx, cy = ay + t * vy ;
			double dx = px - cx, dy = py - cy ;
			return Math.sqrt(dx * dx + dy * dy) ;
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
			// Carry the current HU window over to the next slice (radiology convention).
			double carryCenter = po.hasHU ? po.windowCenter : Double.NaN ;
			double carryWidth  = po.hasHU ? po.windowWidth  : Double.NaN ;
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
			if(po.hasHU && !Double.isNaN(carryCenter)){
				image = po.renderWindow(carryCenter, carryWidth) ;
				origPic = image ;
				DrawLayout = true ;
				layoutString = windowLevelLabel() ;
			}
			else if(keepWindowLevel){
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

		// HU window/level mouse drag : horizontal moves the center (level),
		// vertical moves the width. Returns null for non-DICOM images so the
		// generic brightness/contrast path takes over.
		protected Image windowLevelDrag(int dx, int dy){
			if(po == null || !po.hasHU) return null ;
			double center = po.windowCenter + dx ;          // ~1 HU per pixel
			double width  = po.windowWidth  + dy * 2.0 ;    // ~2 HU per pixel
			if(width < 1) width = 1 ;
			Image img = po.renderWindow(center, width) ;
			if(lWW != null){
				if(!lWW.isShowing()) lWW.show() ;
				lWW.setl((int)Math.round(center)) ;
				lWW.setw((int)Math.round(width)) ;
			}
			return img ;
		}

		protected String windowLevelLabel(){
			return " C:" + Math.round(po.windowCenter) + "  W:" + Math.round(po.windowWidth) ;
		}

		// Toggle the Hounsfield readout ; keep both menu items (Dicom + popup) in sync.
		private void toggleDensity(){
			showDensity = !showDensity ;
			String label = densityLabel() ;
			if(densityItemDicom != null) densityItemDicom.setLabel(label) ;
			if(densityItemPopup != null) densityItemPopup.setLabel(label) ;
			if(!showDensity) densityText = null ;
			repaint() ;
		}

		// Track the cursor to read the HU value of the pixel under it.
		public void processMouseEvent(MouseEvent e){
			// In crosshair mode a press grabs the centre/an endpoint/a line
			// (right-click still pops the menu).
			if(crossMode && e.getID() == MouseEvent.MOUSE_PRESSED && !e.isPopupTrigger())
				pressCross(e.getX(), e.getY()) ;
			if(crossMode && e.getID() == MouseEvent.MOUSE_RELEASED && crossGesture){
				crossGesture = false ;
				dragMode = 0 ;
				pushCut(false) ;   // full-quality rebuild at the end of the gesture
			}
			super.processMouseEvent(e) ;
		}

		public void processMouseMotionEvent(MouseEvent e){
			if(crossMode && e.getID() == MouseEvent.MOUSE_DRAGGED){
				dragCross(e.getX(), e.getY()) ; // dragging moves/rotates the crosshair, not window/level
				return ;
			}
			super.processMouseMotionEvent(e) ;     // keep zoom / translate / window-level
			if(showDensity && po != null && po.hasHU) updateDensity(e.getX(), e.getY()) ;
		}

		private void updateDensity(int sx, int sy){
			if(destw <= 0 || desth <= 0){ return ; }
			int ix = (sx - x) * w / destw ;
			int iy = (sy - y) * h / desth ;
			String previous = densityText ;
			if(ix < 0 || iy < 0 || ix >= w || iy >= h){
				densityText = null ;
			} else {
				int idx = iy * w + ix ;
				densityText = (idx >= 0 && idx < po.hu.length)
					? ("HU " + po.hu[idx] + "    (" + ix + ", " + iy + ")")
					: null ;
			}
			if(densityText != previous && (densityText == null || !densityText.equals(previous))) repaint() ;
		}

		// Append the HU readout to the bottom-left overlay.
		protected void drawLayout(Graphics g){
			super.drawLayout(g) ;
			// Crosshair drawn over the live image : active line green, other cyan.
			if(crossMode && destw > 0 && desth > 0){
				Color saved = g.getColor() ;
				for(int line = 0 ; line < 2 ; line++){
					double[] s = lineSegment(line) ;
					if(s == null) continue ;
					int ax = imgToScreenX(s[0]), ay = imgToScreenY(s[1]) ;
					int bx = imgToScreenX(s[2]), by = imgToScreenY(s[3]) ;
					g.setColor(line == activeLine ? Color.green : Color.cyan) ;
					g.drawLine(ax, ay, bx, by) ;
					// Rotation handles at the line endpoints.
					g.fillRect(ax - 2, ay - 2, 5, 5) ;
					g.fillRect(bx - 2, by - 2, 5, 5) ;
				}
				// Centre (translation) handle.
				int cx = imgToScreenX(crCX), cy = imgToScreenY(crCY) ;
				g.setColor(Color.magenta) ;
				g.fillRect(cx - 3, cy - 3, 7, 7) ;
				g.setColor(saved) ;
			}
			if(showDensity && densityText != null){
				Color saved = g.getColor() ;
				g.setColor(Color.yellow) ;
				g.drawString(densityText, 10, getSize().height - 12) ;
				g.setColor(saved) ;
			}
		}

		// Apply a fixed window (preset or reset) and refresh the display.
		private void applyWindow(double center, double width){
			if(po == null || !po.hasHU) return ;
			image = po.renderWindow(center, width) ;
			origPic = image ;
			DrawLayout = true ;
			layoutString = windowLevelLabel() ;
			if(lWW != null && lWW.isShowing()){
				lWW.setl((int)Math.round(center)) ;
				lWW.setw((int)Math.round(width)) ;
			}
			repaint() ;
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
