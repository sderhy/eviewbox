/**
*	@author  sderhy
*	@ date 26/03/1998
*/
package com.sderhy;
import tools.* ;
import java.net.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.datatransfer.*;


public class PixObject implements java.io.Serializable,
java.lang.Cloneable/* Transferable , ClipboardOwner */ {

	static int num =0 ;//image number


	public URL url ;
	public Image image , scaled ;
	public String etiquette ;
	protected  static int stamp = 120 ;
		public boolean isDicom = false ;
		public boolean isShowing = false ;
		public double sliceThickness = -1 ;
		public double spacingBetweenSlices = -1 ;
		public double sliceLocation = Double.NaN ;   // NaN = absent (locations may be negative)
		public double pixelSpacingRow = -1 ;
		public double pixelSpacingColumn = -1 ;
		// Hounsfield-Unit window/level support. When hasHU is true, `hu` holds the
		// full 16-bit rescaled values and the displayed image is computed from them
		// through a real DICOM window transform (center/width), not a min/max stretch.
		public short[] hu ;
		public boolean hasHU = false ;
		public double windowCenter, windowWidth ;     // current window
		public double defaultCenter, defaultWidth ;   // header default (for reset)
		private boolean infoFlag = false ;
	protected Canvas c  ;//c for Canvas
	//private Canvas cv ;
	public int w , h ;
	private int  dx1, dx2 ,dy1 ,dy2 ;
	protected String[] DicomAttributes;

	//constructeur 1 :
		public PixObject( URL url , Image image, Canvas c ,boolean isDicom,String[] info){
			this.url = url ;
			this.c = c ;
			this.image = image ;
			w = image.getWidth(c) ;
			h = image.getHeight(c) ;
			scaled = getStamp() ;

			if(info == null) infoFlag = false ;
			else infoFlag = true ;

			this.DicomAttributes = info ;



	//		this.cv = new Canvas() ;
	//		cv.setSize(stamp,stamp);

	}


		//constructeur 1 ://utilized for clone() method
	public PixObject( URL url , Image im ,int num){
			this.url = url ;
			this.image = im ;
			this.num = num ;
			this.c = c ;
			w = image.getWidth(c) ;
			h = image.getHeight(c) ;
			scaled = getStamp() ;
			this.isDicom = isDicom ;

	}
	public Dimension getSize(){ return new Dimension(w,h) ;}

	public static int getStampSize(){ return stamp ;}
	public static  void setStampSize(int aSize ){
		if(aSize >16 && aSize< 300 ) stamp =aSize ;//checkBounds
		}
	public void repaint(){
			scaled = getStamp() ;

	}
	public void changeBackground(){
		scaled = getStamp() ;// regenerate : shadow and frame depend on the background
	}



	public Image getStamp(){
		// margin lets the icons breathe and leaves room for the selection frame
		int margin = Math.max(4, stamp/24) ;
		int avail = stamp - 2*margin ;
		int ws , hs ;
		if(w >= h){ ws = avail ; hs = Math.max(1, avail * h / Math.max(1,w)) ; }
		else      { hs = avail ; ws = Math.max(1, avail * w / Math.max(1,h)) ; }
		dx1 = (stamp - ws)/2 ;
		dy1 = (stamp - hs)/2 ;
		dx2 = dx1 + ws ;
		dy2 = dy1 + hs ;

		BufferedImage out = new BufferedImage(stamp, stamp, BufferedImage.TYPE_INT_RGB) ;
		Graphics2D g = out.createGraphics() ;
		Color bg = (c != null) ? c.getBackground() : Color.gray.darker().darker() ;
		g.setColor(bg) ;
		g.fillRect(0, 0, stamp, stamp) ;
	// soft drop shadow under the image
		g.setColor(new Color(0, 0, 0, 90)) ;
		g.fillRect(dx1 + 3, dy1 + 3, ws, hs) ;
		g.setColor(new Color(0, 0, 0, 45)) ;
		g.fillRect(dx1 + 4, dy1 + 4, ws, hs) ;
	// smooth (bilinear) scaling instead of nearest neighbour
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR) ;
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY) ;
		g.drawImage(image, dx1,dy1, dx2,dy2, 0,0,w,h, c);
	// thin light frame around the image
		g.setColor(new Color(255, 255, 255, 70)) ;
		g.drawRect(dx1 - 1, dy1 - 1, ws + 1, hs + 1) ;
		g.dispose() ;
	return out ;
	}//end of getStamp()



	public String[]  getInfo(){ return DicomAttributes ;}

//////////////////////////////////////////////////////////////////////
//	Hounsfield-Unit window / level
//////////////////////////////////////////////////////////////////////

	/** Builds a DICOM PixObject whose displayed image is windowed from the HU data. */
	public static PixObject dicom(URL url, Canvas c, int w, int h, short[] hu,
								double center, double width, String[] info){
		Image initial = renderWindow(w, h, hu, center, width) ;
		PixObject po = new PixObject(url, initial, c, true, info) ;
		po.hu = hu ;
		po.hasHU = true ;
		po.windowCenter = center ; po.windowWidth = width ;
		po.defaultCenter = center ; po.defaultWidth = width ;
		return po ;
	}

	/** Re-window this object's HU data ; updates the stored center/width and
	*	returns a freshly rendered 8-bit image (does NOT replace this.image). */
	public Image renderWindow(double center, double width){
		windowCenter = center ; windowWidth = width ;
		return renderWindow(w, h, hu, center, width) ;
	}

	/** Maps 16-bit HU values to an 8-bit gray image using the DICOM linear
	*	window function (PS3.3 C.11.2.1.2). */
	public static Image renderWindow(int w, int h, short[] hu, double center, double width){
		if(width < 1) width = 1 ;
		double c0 = center - 0.5 ;
		double range = width - 1 ; if(range < 1) range = 1 ;
		double lo = c0 - range / 2.0 ;
		double scale = 255.0 / range ;
		byte[] g = new byte[hu.length] ;
		for(int i = 0 ; i < hu.length ; i++){
			double t = (hu[i] - lo) * scale ;
			int p = (t <= 0) ? 0 : (t >= 255) ? 255 : (int)(t + 0.5) ;
			g[i] = (byte)(p & 0xff) ;
		}
		byte[] ramp = new byte[256] ;
		for(int i = 0 ; i < 256 ; i++) ramp[i] = (byte)i ;
		ColorModel cm = new IndexColorModel(8, 256, ramp, ramp, ramp) ;
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, cm, g, 0, w)) ;
	}

	public Object clone(){
			return new PixObject( url, image , num) ;
	}

	public void flush(){
		image.flush();
		scaled.flush() ;
		tools.Tools.gc("pixObject flush()");
	}

//////////////////////////////////////////////////////////////////////
/*
	//Transferable :

	//define a DataFlavor :
	 public static final DataFlavor flavor =
			new DataFlavor (PixObject.class, "PixData class");

	 public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { flavor };
      }

      public boolean isDataFlavorSupported(DataFlavor f) {
        return f.equals(flavor);
      }

      public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
             if (f.equals(flavor)) return this;
             else throw new UnsupportedFlavorException(f);
      }
      //ClipboardOwner
      public void lostOwnership(Clipboard c, Transferable t) {
        //selection = null;
      }
*/
}// end of class

