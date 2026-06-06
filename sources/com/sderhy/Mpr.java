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
			int  w, h ;
			int zh ;
			ZCanvas zCanvas ;
			public MenuBar mb ;
			public String[] menuFile  = new String[] {"Close" ,"close", "Print...", "print" , "Save...","save" };
			public String[] menuEdit = new String[] {"Undo" , "undo"};
			public String[] menuThick = new String[] { "1 mm","1","2 mm","2","5mm","5","10 mm","10"};
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
			zh = thickness * numberOfImages ;
				arrangeLayout() ;
		}

//arrangeLayout : a convenient way to set up the menu bar.
		protected void arrangeLayout(){
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

			Menu edit = new Menu("Edit") ;
				edit.add(m = new MenuItem(menuEdit[index =0 ], new MenuShortcut(KeyEvent.VK_Z)));// Undo
				m.setActionCommand(menuEdit[++index]) ;
				m.addActionListener(this);
				mb.add(edit) ;

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

		public void setThickness(int mm){
			this.thickness = mm * 2 ;
			zh = thickness * numberOfImages ;
		}
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

  }

//////////////////////////////////////////////////////////////////////////////////////
