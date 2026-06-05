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
	 Frame f;
	 Vector vimages ;
	 Multiplanar parent ;
	 int numberOfImages ;
	 int  currentImage ;
	 int  thickness  =  14 ; // in pixels ( 28 pixels /cm at  72 dpi )
	 int  w, h ;
	 int zh ;
	 DrawableCanvas dc ;
	 ZCanvas zCanvas ;
	Panel supP,infP,leftP,rightP ;

	public MenuBar mb ;
	public String[] menuFile  = new String[] {"Close" ,"close", "Print...", "print" , "Save...","save" };
	public String[] menuThick = new String[] { "1 mm","1","2 mm","2","5mm","5","10 mm","10"};

  public DrawableFrame( Multiplanar orig ) {
    super("Multiplanar reconstruction");
    this.parent  = orig ;

    vimages = orig.getVector();
    numberOfImages = vimages.size() ;
    currentImage =numberOfImages - 1;

    img =  getImageNumber(currentImage) ;
    w = img.getWidth(this) ;
    h = img.getHeight(this) ;
    thickness = getSliceThicknessInPixels();
    zh = thickness * numberOfImages ;

	 dc = new DrawableCanvas(this) ;
    // dc.setSize(w, h) ;
     this.add("North",supP = new Panel());
	 this.add("East",leftP =new Panel());
	 infP =new Panel();
	this.add("West",rightP = new Panel());
	this.add("Center", dc) ;

	zImg = zconstruc();
	zCanvas = new ZCanvas( zImg, w ,zh ) ;
	f = new Frame("Z Axis Reconstruction") ;
	f.add("Center" ,zCanvas) ;
	f.setSize(w , zh+ 20 ) ;
	/*
	infP.add(zCanvas);
	infP.setBackground(Color.pink) ;

	this.add("South", infP );
	//*/
	this.setSize(w + 20 , h +10/* + zh*/) ;
	setResizable(false);
    this.addWindowListener(this);
	f.setLocation( 10 , 10 ) ;
	f.show() ;

	arrange() ;
  //	addActionListener(this);

	this.setLocation( 100  , /*f.getSize().height/2 */+ 30 ) ;
	f.toFront();
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

	// taille de l'image à reconstruire :
		int[] zpixels  = new int[zh * w] ;
		int y = dc.y1 ;
	// Remplir le tableau de pixels :
		 PixObject po = null;
		 Image img = null ;
		 int[] pixels = new int [w] ;
		 int offset = 0 ;
		 for(int i =0 ; i < vimages.size() ; i++){
			 po = (PixObject)vimages.elementAt(i);
			img   = po.image ;

			PixelGrabber pg = new PixelGrabber(img,0,y,w,1,pixels,0,w);
			try{ pg.grabPixels();} catch(InterruptedException e){;}
			// remplir le tableau zpixels

			for(int j = 0 ; j< thickness ; j++)	{
				System.arraycopy(pixels, 0, zpixels,offset, w);
				 //(Object src, intsrc_position,object dest ,int dest_postion,int length )
				offset += w ;
			}//endfor j
		}//endfor i
		return  createImage(new MemoryImageSource(w,zh,zpixels,0,w));

	}

	public void zUpdate(){

		zCanvas.setImage( zconstruc() );
		zCanvas.repaint() ;
//		f.repaint() ;

	}
	public void dispose(){
		f.dispose() ;
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
	dc.repaint() ;
	zUpdate() ;
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
private int getSliceThicknessInPixels(){
	if(vimages == null || vimages.isEmpty()) return thickness ;
	PixObject po = (PixObject)vimages.firstElement();
	double sliceSpacing = po.spacingBetweenSlices;
	if(sliceSpacing <= 0) sliceSpacing = po.sliceThickness;
	if(sliceSpacing <= 0 && vimages.size() > 1){
		PixObject next = (PixObject)vimages.elementAt(1);
		if(po.sliceLocation > -1 && next.sliceLocation > -1)
			sliceSpacing = Math.abs(next.sliceLocation - po.sliceLocation);
	}
	double pixelSpacing = po.pixelSpacingRow;
	if(pixelSpacing <= 0) pixelSpacing = po.pixelSpacingColumn;
	if(sliceSpacing <= 0 || pixelSpacing <= 0) return thickness ;
	return Math.max(1, (int)Math.round(sliceSpacing / pixelSpacing));
}
public void setThickness(int mm){
		this.thickness = mm * 3 ;
		zh = thickness * numberOfImages ;
		zImg = zconstruc();
		zCanvas.setImage(zImg ) ;
		f.setSize(w , zh+ 20 ) ;
		repaint() ;
}

public boolean isDental(){ return parent.dental ;}

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
