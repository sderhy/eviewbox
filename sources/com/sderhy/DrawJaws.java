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
* Multiplanar reconstruction is useful when dealing with jaws
* Reconstruction is beeing made in the mandibular plane which 
* is not flat.
**/
public  class DrawJaws extends Mpr{
	Button clear, done , bprevious , bnext ;	
	DCanvas dCanvas ;
	public DrawJaws( Multiplanar orig ) { 
    		
    		super(orig);
    		//leftPanel setup :
    		InsetPanel leftP = new InsetPanel(Color.gray) ;
    		leftP.setLayout(new GridLayout(4,1,10,10));
	     		leftP.setEtched(true);

	     		done  = new Button("Compute" );done.addActionListener(this) ;leftP.add(done);
	     		clear = new Button("Clear All");clear.addActionListener(this);leftP.add(clear);

				Button undo = new Button("Undo");undo.setActionCommand("undo") ;undo.addActionListener(this) ;
				leftP.add(undo) ;
		
				InsetPanel p2 = new InsetPanel(Color.gray);
				p2.setEtched(true);
				p2.setLayout(new BorderLayout()) ;
				Label l1 = new Label( "  Image :" );
				//l1.setFont( new Font("TimeRoman",Font.PLAIN, 10));
				
				p2.add("North", l1);
				bprevious = 	new Button("<<") ;
				bprevious.addActionListener(this) ;
				bnext = 	new Button(">>") ;
				bnext.addActionListener(this) ;
				p2.add("West",bprevious) ;
				p2.add("East",bnext);
				leftP.add(p2);
			this.add("West",leftP) ;
	
			dCanvas = new DCanvas(this) ;
			add("Center", dCanvas ) ;
			pack() ;
			
	
	}  
/**
*	zconstruct do the actual multiplanar reconstruction by
*	grabbing the pixels from each frame, multipliing it by
*	the number of  pixels for a given thickness.
*	Then by creating an array of pixels corresponding to the
*	new image.
*/
	public void zconstruc(){
		tools.Tools.debug(this," zconstuct called ") ;
		tools.Chrono.start();
		//user selected points,
		int points[] = dCanvas.getChoosenPixels() ;
		int zw = points.length ;// width of the new image ;
		if(zw ==0) return ;
  		// zpixels of the image to build : 
		int[] zpixels  = new int[zh * zw] ;
		
		// pixels from a row of the image :
		int[] zrow = new int[zw] ;
		
		//Grabbing each image from one to another 
		 	PixObject po = null;
		 	Image  srcImg =null;
		 	PixelGrabber pg ;
		 	int[] srcPixels = new int[w * h] ;
		 	int offset = 0 ; // allow to copy each line.
		 // For each frame
//OLD VERSION 		 	for(int i =0 ; i < vimages.size() ; i++){
		for( int i = vimages.size()-1 ; i>0 ; i-- ){
		 		
		 	po = (PixObject)vimages.elementAt(i);
			srcImg   = po.image ;
//Other version ://////////////////////////////////////////////////////////////	
/*	
				int xi , yi , pi ;
				//grab the whole pixel source :	
				try{
					for(int k= 0 ; k < zw ; k++ ){	
						pi = points[k] ;
						xi = pi % w		;
						yi = pi/w ;
						pg = new PixelGrabber(srcImg,xi,yi,1,1,zrow,k,1);
					 	pg.grabPixels();
					 }
				 } catch(InterruptedException e){;}
/////////////////////////Other Solution ///////////////////////////////////////////*/
			//grab the whole pixel source :	
				pg = new PixelGrabber(srcImg,0,0,w,h, srcPixels,0,w);
				try{ pg.grabPixels();} catch(InterruptedException e){;}
			// getTheSelected pixels for this frame :
				for (int k = 0; k< zw ;k++){
					zrow[k] =  srcPixels[ points[k] ] ;
				}
		// fill in the next line because you must respect a thickness 
		// The best thing would be to make an interpolation from one frame
		// to another .
		 		for(int j = 0 ; j< thickness ; j++)	{
					System.arraycopy(zrow, 0, zpixels,offset, zw);
					 //(Object src, intsrc_position,object dest ,int dest_postion,int length )
					offset += zw ;
				}//endfor j
			}//endfor i
 			zImg =  createImage(new MemoryImageSource(zw,zh,zpixels,0,zw)); 
			// make some cleaning :
 				srcPixels = null ;
 				points = null;
 				zrow = null;
 				tools.Chrono.stop();
				tools.Tools.gc() ;
			display() ;
	}//END OF ZCONSTRUCT
	
	private void previous(){
		currentImage-- ;
		if(currentImage <0) currentImage = 0 ;
		img = getImageNumber(currentImage);
		dCanvas.repaint() ;
	}
	private void next(){
		currentImage++ ;
		if(currentImage>(numberOfImages - 1)) currentImage = numberOfImages - 1 ;
		img = getImageNumber(currentImage);
		dCanvas.repaint() ;
	}



	public void actionPerformed(ActionEvent e){
		if( e.getSource() == clear) dCanvas.clear();
		if( e.getSource() == done ) zconstruc();
		if( e.getSource() == bprevious ) previous();
		if( e.getSource() == bnext ) next();
		if(e.getActionCommand() == "undo") dCanvas.undo() ;
		
		
		super.actionPerformed(e);
	}
	
	private void display(){
			ImageViewer iv = new ImageViewer(zImg ) ;
			iv.show();
	
	}	
}//end of class



