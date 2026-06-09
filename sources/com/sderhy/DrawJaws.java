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
public  class DrawJaws extends Mpr implements KeyListener {
	Button clear, done ;
	Label imgLabel ;
	DCanvas dCanvas ;
	private int[][] sampledRows ;           // n x zw : each slice sampled ALONG the curve
	private float[] cachedCx, cachedCy ;    // the curve sampledRows were built for
	private Image prevResult ;              // last result image, flushed on the next build
	private ImageViewer resultViewer ;      // persistent result window (reused on recompute)
	public DrawJaws( Multiplanar orig ) {

		super(orig);
		// Left panel : a compact column of natural-width buttons pinned to the top.
		Button undo = new Button("Undo") ; undo.setActionCommand("undo") ; undo.addActionListener(this) ;
		done  = new Button("Compute") ; done.addActionListener(this) ;
		clear = new Button("Clear")   ; clear.addActionListener(this) ;
		imgLabel = new Label("", Label.CENTER) ;

		Panel buttons = new Panel(new GridLayout(0, 1, 0, 6)) ;
		buttons.add(done) ;
		buttons.add(clear) ;
		buttons.add(undo) ;
		buttons.add(imgLabel) ;
		Label hint = new Label("← → slices", Label.CENTER) ;   // arrow-key hint
		buttons.add(hint) ;
		// FlowLayout wrapper keeps the column at its preferred (narrow) width
		// instead of stretching the buttons across the whole side panel.
		Panel holder = new Panel(new FlowLayout(FlowLayout.CENTER, 6, 10)) ;
		holder.add(buttons) ;
		Panel leftP = new Panel(new BorderLayout()) ;
		leftP.add("North", holder) ;
		this.add("West", leftP) ;

		dCanvas = new DCanvas(this) ;
		add("Center", dCanvas) ;

		// Left / right arrows navigate slices ; wire the listener to every place
		// keyboard focus may land so the arrows work right after any click.
		dCanvas.setFocusable(true) ;
		dCanvas.addKeyListener(this) ;
		done.addKeyListener(this) ;
		clear.addKeyListener(this) ;
		undo.addKeyListener(this) ;
		this.addKeyListener(this) ;

		pack() ;
		updateImageLabel() ;
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
		sampleAlongCurve(cx, cy) ;    // (re)samples only when the curve changed
		int n = vimages.size() ;

		// Stack the sampled rows, interpolating between slices by thickness.
		int[] zpixels = new int[zh * zw] ;
		int offset = 0 ;
		for(int i = 0 ; i < n ; i++){
			int[] current = sampledRows[i] ;
			int[] next = (i + 1 < n) ? sampledRows[i + 1] : current ;
			for(int j = 0 ; j < thickness ; j++){
				float ratio = (thickness <= 1) ? 0f : (float)j / (float)thickness ;
				interpolateRow(current, next, ratio, zpixels, offset, zw) ;
				offset += zw ;
			}//endfor j
		}//endfor i
		Image newImg = createImage(new MemoryImageSource(zw, zh, zpixels, 0, zw)) ;
		zpixels = null ;
		// Release the previous result's native buffer before showing the new one.
		if(prevResult != null) prevResult.flush() ;
		prevResult = newImg ;
		zImg = newImg ;
		tools.Chrono.stop() ;
		display() ;
	}//END OF ZCONSTRUCT

	/**
	*	Sample every slice ALONG the curve into sampledRows (n x zw). Only the
	*	thin row of pixels under the curve is kept per slice — not the whole
	*	slice — so memory stays a few MB even for long stacks. Each slice is
	*	grabbed once through a single reusable buffer. Skipped entirely when the
	*	curve is unchanged (e.g. a thickness-only recompute), which is instant.
	*/
	private void sampleAlongCurve(float[] cx, float[] cy){
		// CurveModel returns the SAME array instance until the curve is edited,
		// so reference identity is a cheap, exact "did the curve change ?" test.
		if(sampledRows != null && cx == cachedCx && cy == cachedCy) return ;
		int n = vimages.size() ;
		int zw = cx.length ;
		int[][] rows = new int[n][zw] ;
		int[] buf = new int[w * h] ;          // one reusable grab buffer
		for(int i = 0 ; i < n ; i++){
			PixObject po = (PixObject)vimages.elementAt(i) ;
			PixelGrabber pg = new PixelGrabber(po.image, 0, 0, w, h, buf, 0, w) ;
			try{ pg.grabPixels() ; } catch(InterruptedException e){}
			int[] dst = rows[i] ;
			for(int k = 0 ; k < zw ; k++) dst[k] = sampleBilinear(buf, cx[k], cy[k]) ;
		}
		buf = null ;
		sampledRows = rows ;
		cachedCx = cx ; cachedCy = cy ;
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
		updateImageLabel() ;
		dCanvas.repaint() ;
	}
	private void next(){
		currentImage++ ;
		if(currentImage>(numberOfImages - 1)) currentImage = numberOfImages - 1 ;
		img = getImageNumber(currentImage);
		updateImageLabel() ;
		dCanvas.repaint() ;
	}

	private void updateImageLabel(){
		if(imgLabel != null) imgLabel.setText((currentImage + 1) + " / " + numberOfImages) ;
	}

	// Left / right arrows move through the slice stack.
	public void keyPressed(KeyEvent e){
		int c = e.getKeyCode() ;
		if(c == KeyEvent.VK_RIGHT){ next() ; e.consume() ; }
		else if(c == KeyEvent.VK_LEFT){ previous() ; e.consume() ; }
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}

	public void show(){
		super.show() ;
		if(dCanvas != null) dCanvas.requestFocus() ;
	}

	public void actionPerformed(ActionEvent e){
		if( e.getSource() == clear) dCanvas.clear();
		if( e.getSource() == done ) zconstruc();
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
			// Closing the result window (it disposes itself) must drop our handle
			// so the next Compute opens a fresh one instead of poking a dead frame.
			resultViewer.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){ resultViewer = null ; }
			}) ;
			resultViewer.show() ;
		} else {
			resultViewer.setImage(zImg) ;
		}
	}

	public void dispose(){
		if(resultViewer != null){ resultViewer.dispose() ; resultViewer = null ; }
		if(prevResult != null){ prevResult.flush() ; prevResult = null ; }
		// Drop the large per-slice buffers so closing the window frees the memory.
		sampledRows = null ;
		cachedCx = cachedCy = null ;
		zImg = null ;
		tools.Tools.gc() ;
		super.dispose() ;
	}
}//end of class



