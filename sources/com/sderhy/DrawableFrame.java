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
  /* Aeffacer :
		int[] points  = new int [w] ;

	// position des points
		for (int i = 0; i < w ; i ++ )
			points[i] = 	y*w + i ;
///*/

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
