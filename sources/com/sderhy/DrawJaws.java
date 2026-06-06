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
			int[][] rows = new int[vimages.size()][zw] ;

			//Grabbing each image from one to another
			int[] srcPixels = new int[w * h] ;
	//OLD VERSION			for(int i =0 ; i < vimages.size() ; i++){
			for( int i = vimages.size()-1 ; i>=0 ; i-- ){

				PixObject po = (PixObject)vimages.elementAt(i);
				Image srcImg = po.image ;
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
					PixelGrabber pg = new PixelGrabber(srcImg,0,0,w,h, srcPixels,0,w);
					try{ pg.grabPixels();} catch(InterruptedException e){;}
				// getTheSelected pixels for this frame :
					for (int k = 0; k< zw ;k++){
						rows[i][k] =  srcPixels[ points[k] ] ;
					}
				}//endfor i

				int offset = 0 ; // allow to copy each line.
				for( int i = vimages.size()-1 ; i>=0 ; i-- ){
					int[] current = rows[i];
					int[] next = (i - 1 >= 0) ? rows[i - 1] : current;
					for(int j = 0 ; j< thickness ; j++)	{
						float ratio = (thickness <= 1) ? 0f : (float)j / (float)thickness;
						interpolateRow(current, next, ratio, zpixels, offset, zw);
						offset += zw ;
					}//endfor j
				}//endfor i
				zImg =  createImage(new MemoryImageSource(zw,zh,zpixels,0,zw));
				// make some cleaning :
					srcPixels = null ;
				points = null;
				tools.Chrono.stop();
				tools.Tools.gc() ;
				display() ;
		}//END OF ZCONSTRUCT

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



