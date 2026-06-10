package com.sderhy ;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.*;
/*import PixObject ;
import tools.Tools.*;
*/
/**
* Called by Multiplanar.class
**/
public class DrawableFrame extends Frame implements WindowListener, ActionListener{
	 Image  img , zImg ;
	 ImageViewer resultViewer ;
	 Vector vimages ;
	 Multiplanar parent ;
	 int numberOfImages ;
	 int  currentImage ;
	 int  thickness  =  14 ; // in pixels ( 28 pixels /cm at  72 dpi )
	 int  w, h ;
	 int zw ;
	 int zh ;
	 int mode = Multiplanar.FRONTAL ;
	 DrawableCanvas dc ;
	 private int[][] cachedPixels ;   // full pixels of each slice, grabbed once
	 private boolean disposed = false ;
	 private Runnable onDispose ;     // notified when the reconstruction is closed
	 // Oblique cut (crosshair) : the active line passes through (cutCX,cutCY) with
	 // direction angle cutPhi. When set, it replaces the axis-aligned row/column.
	 private boolean oblique = false ;
	 private double cutCX, cutCY, cutPhi ;
	 private boolean previewMode = false ;   // low-res live preview while dragging
	 private static final int PREVIEW_SLICES = 200 ;  // slices sampled in preview mode
	Panel supP,infP,leftP,rightP ;

	public MenuBar mb ;
	public String[] menuFile  = new String[] {"Close" ,"close", "Print...", "print" , "Save...","save" };
	public String[] menuThick = new String[] { "1 mm","1","2 mm","2","5mm","5","10 mm","10"};

  public DrawableFrame( Multiplanar orig ) {
    this(orig, Multiplanar.FRONTAL);
  }

  public DrawableFrame( Multiplanar orig, int mode ) {
    super(mode == Multiplanar.SAGITTAL ? "Sagittal reconstruction" : "Frontal reconstruction");
    this.parent  = orig ;
    this.mode = mode ;

    vimages = orig.getVector();
    numberOfImages = vimages.size() ;
    currentImage = orig.currentImage ;
    if(currentImage < 0 || currentImage >= numberOfImages) currentImage = numberOfImages - 1;

    img =  getImageNumber(currentImage) ;
    w = img.getWidth(this) ;
    h = img.getHeight(this) ;
    zw = getReconstructionWidth();
    // Without DICOM spacing (e.g. plain JPEG slices) assume isotropic voxels :
    // one output row per slice. The old 14 px guess over-stretched the through-
    // plane axis, so long stacks were shrunk to fit the screen and the
    // reconstruction came out as a thin, wrongly-narrow band.
    thickness = parent.getSliceSpacingInPixels(1);
    zh = thickness * numberOfImages ;

	 dc = new DrawableCanvas(this) ;
    // dc.setSize(w, h) ;
     this.add("North",supP = new Panel());
	 this.add("East",leftP =new Panel());
	 infP =new Panel();
	this.add("West",rightP = new Panel());
	this.add("Center", dc) ;

	zImg = zconstruc();
	resultViewer = new ImageViewer(zImg) ;
	resultViewer.setTitle(mode == Multiplanar.SAGITTAL ? "Sagittal reconstruction" : "Frontal reconstruction") ;
	installThicknessMenu() ;
	// Closing the result window tears down the whole reconstruction.
	resultViewer.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){ DrawableFrame.this.dispose() ; }
	}) ;
	/*
	infP.add(zCanvas);
	infP.setBackground(Color.pink) ;

	this.add("South", infP );
	//*/
	this.setSize(w + 20 , h +10/* + zh*/) ;
	setResizable(false);
    this.addWindowListener(this);
	resultViewer.setLocation( 10 , 10 ) ;
	resultViewer.show() ;

	arrange() ;
  //	addActionListener(this);

	this.setLocation( 100  , /*f.getSize().height/2 */+ 30 ) ;
	resultViewer.toFront();
   }

	protected void arrange(){
		MenuItem m = null;
		int index = 0;
		mb = new MenuBar() ;
		this.setMenuBar(mb);
		Menu file = new Menu( "File" );
			file.add(m = new MenuItem(menuFile[index], new MenuShortcut(KeyEvent.VK_W)));//Close
				m.addActionListener(this);m.setActionCommand(menuFile[++index]) ;
			file.add(m = new MenuItem(menuFile[++index], new MenuShortcut(KeyEvent.VK_P)));//Print
				m.addActionListener(this);m.setActionCommand(menuFile[++index]) ;
			file.add(m = new MenuItem(menuFile[++index], new MenuShortcut(KeyEvent.VK_S)));//Save
				m.addActionListener(this);m.setActionCommand(menuFile[index]) ;
		mb.add(file);
		 Menu sT = new Menu("Thickness" ) ;
			sT.add(m = new MenuItem(menuThick[index = 0]));//1mm
				m.addActionListener(this);m.setActionCommand(menuThick[++index]) ;

			sT.add(m = new MenuItem(menuThick[++index]));//2mm
				m.addActionListener(this);m.setActionCommand(menuThick[++index]) ;

			sT.add(m = new MenuItem(menuThick[++index]));//5mm
				m.addActionListener(this);m.setActionCommand(menuThick[++index]) ;

			sT.add(m = new MenuItem(menuThick[++index]));//10mm
				m.addActionListener(this);m.setActionCommand(menuThick[++index]) ;
		mb.add(sT);

	}


  public Image zconstruc() {
		if(oblique) return zconstrucOblique() ;
		// taille de l'image a reconstruire :
			ensurePixelCache();
			zw = getReconstructionWidth();
			int[] zpixels  = new int[zh * zw] ;
			int[][] rows = new int[numberOfImages][zw] ;
			int yy = Math.max(0, Math.min(h - 1, dc.y1)) ;   // frontal  : picked row
			int xx = Math.max(0, Math.min(w - 1, dc.x1)) ;   // sagittal : picked column
			for(int i =0 ; i < numberOfImages ; i++){
				int[] src = cachedPixels[i];
				if(src == null) continue ;
				if(isSagittal()){
					int[] dst = rows[i] ;
					for(int j = 0 ; j < h ; j++) dst[j] = src[j * w + xx] ;
				} else {
					System.arraycopy(src, yy * w, rows[i], 0, w) ;
				}
			}

			int offset = 0 ;
			for(int i =0 ; i < numberOfImages ; i++){
				int[] current = rows[i];
				int[] next = (i + 1 < numberOfImages) ? rows[i + 1] : current;
				for(int j = 0 ; j< thickness ; j++)	{
					float ratio = (thickness <= 1) ? 0f : (float)j / (float)thickness;
					interpolateRow(current, next, ratio, zpixels, offset, zw);
					offset += zw ;
				}//endfor j
			}//endfor i
			return  createImage(new MemoryImageSource(zw,zh,zpixels,0,zw));

		}

	/** Set the active crosshair line (image coords + direction angle) and rebuild. */
	public void setCut(double cx, double cy, double phi){ setCut(cx, cy, phi, false) ; }

	/** preview = true builds a fast, lower-slice-count image for live dragging. */
	public void setCut(double cx, double cy, double phi, boolean preview){
		oblique = true ;
		cutCX = cx ; cutCY = cy ; cutPhi = phi ;
		previewMode = preview ;
		zUpdate() ;
	}

	/**
	*	Reconstruct along an arbitrary (oblique) line : the chord of the image in
	*	direction cutPhi through (cutCX,cutCY) is sampled bilinearly in every slice ;
	*	those rows are stacked through the slices. Frontal (phi=0) and sagittal
	*	(phi=90deg) are just special cases.
	*/
	private Image zconstrucOblique(){
		ensurePixelCache() ;
		// In preview mode, sample only a subset of slices (≈ PREVIEW_SLICES) so the
		// live drag stays real-time ; the full stack is used on release.
		int step = previewMode ? Math.max(1, (numberOfImages + PREVIEW_SLICES - 1) / PREVIEW_SLICES) : 1 ;
		int ns = Math.max(1, (numberOfImages + step - 1) / step) ;
		int[] sliceIdx = new int[ns] ;
		for(int m = 0 ; m < ns ; m++) sliceIdx[m] = Math.min(m * step, numberOfImages - 1) ;
		sliceIdx[ns - 1] = numberOfImages - 1 ;   // always include the last slice

		double dx = Math.cos(cutPhi), dy = Math.sin(cutPhi) ;
		final double EPS = 1e-6 ;
		int owidth ;
		int[][] rows ;

		if(Math.abs(dy) < EPS){
			// FAST PATH — horizontal line : copy the exact image row (no in-plane
			// interpolation needed). This is the old frontal speed (System.arraycopy).
			int yy = clampInt((int)Math.round(cutCY), 0, h - 1) ;
			owidth = w ;
			rows = new int[ns][] ;
			for(int m = 0 ; m < ns ; m++){
				int[] src = cachedPixels[sliceIdx[m]] ;
				int[] dst = new int[w] ;
				if(src != null) System.arraycopy(src, yy * w, dst, 0, w) ;
				rows[m] = dst ;
			}
		} else if(Math.abs(dx) < EPS){
			// FAST PATH — vertical line : read the exact image column (old sagittal).
			int xx = clampInt((int)Math.round(cutCX), 0, w - 1) ;
			owidth = h ;
			rows = new int[ns][] ;
			for(int m = 0 ; m < ns ; m++){
				int[] src = cachedPixels[sliceIdx[m]] ;
				int[] dst = new int[h] ;
				if(src != null) for(int j = 0 ; j < h ; j++) dst[j] = src[j * w + xx] ;
				rows[m] = dst ;
			}
		} else {
			// OBLIQUE — sample bilinearly along the chord of the image.
			double[] seg = LineClip.segment(cutCX, cutCY, dx, dy, w, h) ;
			if(seg == null) return zImg ;
			double x0 = seg[0], y0 = seg[1], x1 = seg[2], y1 = seg[3] ;
			double ddx = x1 - x0, ddy = y1 - y0 ;
			owidth = Math.max(1, (int)Math.round(Math.hypot(ddx, ddy)) + 1) ;
			rows = new int[ns][owidth] ;
			double stepT = (owidth == 1) ? 0 : 1.0 / (owidth - 1) ;
			for(int m = 0 ; m < ns ; m++){
				int[] src = cachedPixels[sliceIdx[m]] ;
				if(src == null) continue ;
				int[] dst = rows[m] ;
				for(int k = 0 ; k < owidth ; k++){
					double t = k * stepT ;
					dst[k] = sampleBilinear(src, (float)(x0 + t * ddx), (float)(y0 + t * ddy)) ;
				}
			}
		}
		zw = owidth ;

		// Cap the output height : a reconstruction taller than the screen is
		// pointless and just costs memory / time. The slice axis is resampled into
		// outH rows (each maps to a fractional position among the sampled slices).
		int fullH = thickness * numberOfImages ;
		int outH = Math.max(1, Math.min(fullH, maxDisplayHeight())) ;
		int[] zpixels = new int[outH * owidth] ;
		for(int r = 0 ; r < outH ; r++){
			double p = (outH == 1) ? 0 : (double)r * (ns - 1) / (outH - 1) ;
			int m0 = (int)p ;
			int m1 = Math.min(m0 + 1, ns - 1) ;
			float ratio = (float)(p - m0) ;
			interpolateRow(rows[m0], rows[m1], ratio, zpixels, r * owidth, owidth) ;
		}
		return createImage(new MemoryImageSource(owidth, outH, zpixels, 0, owidth)) ;
	}

	// Upper bound on the reconstructed image height (the screen height).
	private int maxDisplayHeight(){
		int screenH = Toolkit.getDefaultToolkit().getScreenSize().height ;
		return Math.max(256, screenH) ;
	}

	private static int clampInt(int v, int lo, int hi){ return v < lo ? lo : (v > hi ? hi : v) ; }

	/**
	*	Bilinear sample at a sub-pixel (fx,fy), in fixed-point integer arithmetic :
	*	no Math.round / Math.floor, no per-channel method calls. The four corner
	*	weights are 0..65536 and sum to 65536, so a single >>>16 averages them.
	*/
	private int sampleBilinear(int[] src, float fx, float fy){
		if(fx < 0) fx = 0 ; else if(fx > w - 1) fx = w - 1 ;
		if(fy < 0) fy = 0 ; else if(fy > h - 1) fy = h - 1 ;
		int x0 = (int)fx, y0 = (int)fy ;
		int x1 = (x0 < w - 1) ? x0 + 1 : x0 ;
		int y1 = (y0 < h - 1) ? y0 + 1 : y0 ;
		int tx = (int)((fx - x0) * 256f + 0.5f), ty = (int)((fy - y0) * 256f + 0.5f) ;
		int itx = 256 - tx, ity = 256 - ty ;
		int o0 = y0 * w, o1 = y1 * w ;
		int p00 = src[o0 + x0], p10 = src[o0 + x1] ;
		int p01 = src[o1 + x0], p11 = src[o1 + x1] ;
		int w00 = itx * ity, w10 = tx * ity, w01 = itx * ty, w11 = tx * ty ;
		int r = ((p00 >>> 16 & 0xff) * w00 + (p10 >>> 16 & 0xff) * w10
		       + (p01 >>> 16 & 0xff) * w01 + (p11 >>> 16 & 0xff) * w11) >>> 16 ;
		int g = ((p00 >>> 8 & 0xff) * w00 + (p10 >>> 8 & 0xff) * w10
		       + (p01 >>> 8 & 0xff) * w01 + (p11 >>> 8 & 0xff) * w11) >>> 16 ;
		int b = ((p00 & 0xff) * w00 + (p10 & 0xff) * w10
		       + (p01 & 0xff) * w01 + (p11 & 0xff) * w11) >>> 16 ;
		return 0xff000000 | (r << 16) | (g << 8) | b ;
	}

	/**
	*	Grab every slice's full pixels once. The cut line only selects a row
	*	(frontal) or a column (sagittal) from these cached arrays, so dragging the
	*	line no longer re-grabs N images through a PixelGrabber each time.
	*/
	private void ensurePixelCache(){
		if(cachedPixels != null) return ;
		cachedPixels = new int[numberOfImages][] ;
		for(int i = 0 ; i < numberOfImages ; i++){
			PixObject po = (PixObject)vimages.elementAt(i) ;
			int[] buf = new int[w * h] ;
			PixelGrabber pg = new PixelGrabber(po.image, 0, 0, w, h, buf, 0, w) ;
			try{ pg.grabPixels() ; } catch(InterruptedException e){}
			cachedPixels[i] = buf ;
		}
	}

	private void interpolateRow(int[] current, int[] next, float ratio, int[] destination, int offset, int width){
		for(int x = 0 ; x < width ; x++){
			destination[offset + x] = interpolatePixel(current[x], next[x], ratio);
		}
	}

	private int interpolatePixel(int a, int b, float ratio){
		int alpha = (a >>> 24);
		int red = interpolateChannel((a >> 16) & 0xff, (b >> 16) & 0xff, ratio);
		int green = interpolateChannel((a >> 8) & 0xff, (b >> 8) & 0xff, ratio);
		int blue = interpolateChannel(a & 0xff, b & 0xff, ratio);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private int interpolateChannel(int a, int b, float ratio){
		return a + Math.round((b - a) * ratio);
	}

	public void zUpdate(){

		zImg = zconstruc();
		if(resultViewer != null) resultViewer.setImage(zImg) ;

	}

	/** Set the cut line externally (driven by the live image viewer) and rebuild.
	*	pos is a row for a frontal reconstruction, a column for a sagittal one. */
	public void setCutPosition(int pos){
		if(isSagittal()){
			if(pos < 0) pos = 0 ; if(pos > w - 1) pos = w - 1 ;
			dc.x1 = dc.x2 = pos ;
		} else {
			if(pos < 0) pos = 0 ; if(pos > h - 1) pos = h - 1 ;
			dc.y1 = dc.y2 = pos ;
		}
		zUpdate() ;
	}

	public int imageW(){ return w ; }
	public int imageH(){ return h ; }
	public void setOnDispose(Runnable r){ onDispose = r ; }

	// Add a Thickness menu to the result window (the hidden engine's own menu
	// bar is never shown when driven from the live viewer).
	private void installThicknessMenu(){
		MenuBar rb = resultViewer.getMenuBar() ;
		if(rb == null) return ;
		Menu sT = new Menu("Thickness") ;
		addThick(sT, "1 mm", 1) ;
		addThick(sT, "2 mm", 2) ;
		addThick(sT, "5 mm", 5) ;
		addThick(sT, "10 mm", 10) ;
		rb.add(sT) ;
	}
	private void addThick(Menu menu, String label, final int mm){
		MenuItem mi = new MenuItem(label) ;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ setThickness(mm) ; }
		}) ;
		menu.add(mi) ;
	}

	public void dispose(){
		if(disposed) return ;
		disposed = true ;
		if(onDispose != null){ try{ onDispose.run() ; } catch(Throwable t){} }
		if(resultViewer != null) resultViewer.dispose() ;
		super.dispose() ;
		}

/*Doc
public PixelGrabber(Image img,int x, int y,  int w, int h, int pix[],int off,int scansize)

Create a PixelGrabber object to grab the (x, y, w, h) rectangular section of pixels from the specified image
into the given array. The pixels are stored into the array in the default RGB ColorModel.

The RGB  data for pixel (i, j) where (i, j) is inside the rectangle (x, y, w, h) is stored
in the array at pix[(j - y) * scansize + (i - x) + off].

Parameters:
img - the image to retrieve pixels from
x - the x coordinate of the upper left corner of the rectangle of pixels to retrieve from the
image, relative to the default (unscaled) size of the image
y - the y coordinate of the upper left corner of the rectangle of pixels to retrieve from the image

w - the width of the rectangle of pixels to retrieve

h - the height of the rectangle of pixels to retrieve pix - the array of integers which are to be used
to hold the RGB pixels retrieved from the image

off - the offset into the array of where to store the first pixel

scansize - the distance from one row of pixels to the next  in the array

*/


public void paint( Graphics g){
	//g.drawImage(img,0,0,w,h,0,0,w,h,this) ;
	//g.drawImage(img,0,0,w,h,this) ;//marche pas !
	// Only repaint the cut-selection canvas ; the reconstruction is rebuilt on
	// demand (cut-line drag, thickness change), not on every expose.
	dc.repaint() ;
}
public void update(Graphics g){

	paint(g) ;
}

public void show(){
	super.show() ;
	repaint() ;
	}

protected Image getImageNumber(int num){
	 PixObject po = (PixObject)vimages.elementAt(num) ;
	 return po.image ;

}
public void setThickness(int mm){
		this.thickness = mm * 3 ;
		zh = thickness * numberOfImages ;
		zImg = zconstruc();
		if(resultViewer != null) resultViewer.setImage(zImg) ;
		repaint() ;
}

public boolean isDental(){ return parent.dental ;}
public boolean isSagittal(){ return mode == Multiplanar.SAGITTAL ;}
private int getReconstructionWidth(){ return isSagittal() ? h : w ;}

//ActionListener :
public void actionPerformed(ActionEvent e){
		String s = e.getActionCommand() ;
		if (s.equals("close"))  this.dispose() ;
		else if(s.equals("print")) tools.Tools.debug(this, " Print command not  implemented yet") ;
		else if(s.equals("save")) tools.Tools.debug(this, " Save command not  implemented yet") ;
		else if(s.equals("1")) setThickness(1) ;
		else if(s.equals("2")) setThickness(2) ;
		else if(s.equals("5")) setThickness(5) ;
		else if(s.equals("10")) setThickness(10) ;

}

//windowListener;
  public void windowClosing(WindowEvent e) { this.dispose(); }
  public void windowOpened(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowActivated(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}
}// end of class

//////////////////////////////////////////////////////////////////////////////////////
