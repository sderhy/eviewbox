package com.sderhy ;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.*;
//import PixObject ;
import tools.Tools.*;
/**
* Called by Multiplanar.class
**/
public abstract class Mpr extends Frame implements WindowListener, ActionListener{
			Image  img , zImg ;
			Frame zFrame;// frame for the zImg
			Vector vimages ;
			Multiplanar parent ;
			int numberOfImages ;
			int  currentImage ;
			int  thickness  = 6 ; //2 mm in pixels ( 28 pixels /cm at  72 dpi )
			// Output height forced by the user (height slider) ; -1 = automatic
			// (physical when the stack has positions, thickness*n otherwise).
			int manualHeight = -1 ;
			int  w, h ;
			ZCanvas zCanvas ;
//contructor :
		public Mpr( Multiplanar orig ) {
			super("Multiplanar reconstruction");
		this.parent  = orig ;
		vimages = orig.getVector();
			numberOfImages = vimages.size() ;
			this.setSize(w + 20 , h +10) ;
				setResizable(false);
			    this.addWindowListener(this);
			currentImage = orig.currentImage ;
			if(currentImage < 0 || currentImage >= numberOfImages) currentImage = numberOfImages - 1;
			img =  getImageNumber(currentImage) ;
			w = img.getWidth(this) ;
			h = img.getHeight(this) ;
			thickness = parent.getSliceSpacingInPixels(thickness);
		}

	/**
	*	zconstruct()  has to contruct the zImg from the stack of images , included in
	*	the  vimages vector, it is the heart of the program.
	**/
		public abstract void zconstruc() ;

/** Dispose method to shut the zFrame */
		public void dispose(){
			if (zFrame!= null)zFrame.dispose() ;
			super.dispose() ;
		}

		protected Image getImageNumber(int num){
			PixObject po = (PixObject)vimages.elementAt(num) ;
			return po.image ;
		}

		// Upper bound on the reconstructed image height (the screen height).
		protected int maxDisplayHeight(){
			int screenH = Toolkit.getDefaultToolkit().getScreenSize().height ;
			return Math.max(256, screenH) ;
		}

		//ActionListener : subclasses handle their own buttons.
		public void actionPerformed(ActionEvent e){ }

		//windowListener;
		public void windowClosing(WindowEvent e) { this.dispose(); }
		public void windowOpened(WindowEvent e) {}
		public void windowClosed(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowActivated(WindowEvent e) {}
		public void windowDeactivated(WindowEvent e) {}

  }

//////////////////////////////////////////////////////////////////////////////////////
