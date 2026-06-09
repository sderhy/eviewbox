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
	private int[][] cachedPixels ;          // full pixels of each slice, grabbed once
	private ImageViewer resultViewer ;      // persistent result window (reused on recompute)
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
		// Tessellated curve (sub-pixel positions, image coordinates).
		float[] cx = dCanvas.getCurveX() ;
		float[] cy = dCanvas.getCurveY() ;
		int zw = cx.length ;          // width of the new image = curve arc length
		if(zw == 0) return ;
		ensurePixelCache() ;
		int n = vimages.size() ;
		int[][] rows = new int[n][zw] ;

		// Sample each slice along the curve, bilinearly (sub-pixel). Natural slice
		// order (i = 0..n-1) so the panoramic image is right-side up.
		for(int i = 0 ; i < n ; i++){
			int[] src = cachedPixels[i] ;
			if(src == null) continue ;
			int[] dst = rows[i] ;
			for(int k = 0 ; k < zw ; k++) dst[k] = sampleBilinear(src, cx[k], cy[k]) ;
		}

		int[] zpixels = new int[zh * zw] ;
		int offset = 0 ;
		for(int i = 0 ; i < n ; i++){
			int[] current = rows[i] ;
			int[] next = (i + 1 < n) ? rows[i + 1] : current ;
			for(int j = 0 ; j < thickness ; j++){
				float ratio = (thickness <= 1) ? 0f : (float)j / (float)thickness ;
				interpolateRow(current, next, ratio, zpixels, offset, zw) ;
				offset += zw ;
			}//endfor j
		}//endfor i
		zImg = createImage(new MemoryImageSource(zw, zh, zpixels, 0, zw)) ;
		tools.Chrono.stop() ;
		display() ;
	}//END OF ZCONSTRUCT

	/** Grab every slice's full pixels once ; recompute only re-samples them. */
	private void ensurePixelCache(){
		if(cachedPixels != null) return ;
		int n = vimages.size() ;
		cachedPixels = new int[n][] ;
		for(int i = 0 ; i < n ; i++){
			PixObject po = (PixObject)vimages.elementAt(i) ;
			int[] buf = new int[w * h] ;
			PixelGrabber pg = new PixelGrabber(po.image, 0, 0, w, h, buf, 0, w) ;
			try{ pg.grabPixels() ; } catch(InterruptedException e){}
			cachedPixels[i] = buf ;
		}
	}

	// Bilinear sample of a slice's pixels at a sub-pixel (fx, fy) position.
	private int sampleBilinear(int[] src, float fx, float fy){
		if(fx < 0) fx = 0 ; if(fx > w - 1) fx = w - 1 ;
		if(fy < 0) fy = 0 ; if(fy > h - 1) fy = h - 1 ;
		int x0 = (int)Math.floor(fx), y0 = (int)Math.floor(fy) ;
		int x1 = Math.min(x0 + 1, w - 1), y1 = Math.min(y0 + 1, h - 1) ;
		float tx = fx - x0, ty = fy - y0 ;
		int top = interpolatePixel(src[y0 * w + x0], src[y0 * w + x1], tx) ;
		int bot = interpolatePixel(src[y1 * w + x0], src[y1 * w + x1], tx) ;
		return interpolatePixel(top, bot, ty) ;
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

		super.actionPerformed(e);     // Thickness menu updates thickness / zh here

		// A thickness change re-stretches the through-plane axis : rebuild live.
		String s = e.getActionCommand() ;
		if((s.equals("1") || s.equals("2") || s.equals("5") || s.equals("10"))
				&& dCanvas.sampleCount() > 0)
			zconstruc() ;
	}

	// Reuse one result window across recomputes instead of opening a new one.
	private void display(){
		if(resultViewer == null){
			resultViewer = new ImageViewer(zImg) ;
			resultViewer.setTitle("Curved reconstruction") ;
			resultViewer.show() ;
		} else {
			resultViewer.setImage(zImg) ;
		}
	}

	public void dispose(){
		if(resultViewer != null){ resultViewer.dispose() ; resultViewer = null ; }
		super.dispose() ;
	}
}//end of class



