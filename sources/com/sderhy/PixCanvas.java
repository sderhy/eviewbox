/**
*	@author serge derhy
*	@date 25/03/98
*	@version 190497 1725
**/
package com.sderhy;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.Vector;
import tools.*;
import java.io.*;
//import com.sderhy.* ;

  public class PixCanvas extends  Canvas implements ActionListener,KeyListener {

    public  Vector vimages ;  // Store the images
    protected int width, height;                   // The preferred size
    protected PopupMenu popup , popup2;                     // The popup menu
    protected MainClass frame;             // The frame we are within
    PixObject po ;
    PixObject exPo ;
    Image  offScreen ;
    private static int compteur = 0;
    public  int lastSel = -1 ;//last image selected// no image ==-1
	static int stampW = PixObject.getStampSize() ;
	static int stampH = stampW ;
	static int gap = 2 ;
    tools.FindWhereInComp fwic;

    /** This constructor requires a Frame and a desired size */
    public PixCanvas(MainClass frame, int width, int height) {
      this.frame = frame;
      this.width = width;
      this.height = height;
      vimages = frame.vimages ;
      this.enableEvents(AWTEvent.MOUSE_EVENT_MASK|
      				AWTEvent.MOUSE_MOTION_EVENT_MASK );

	 // Create the popup menu.
      String[] labels = new String[] { "Open Image","New Image...", "-","Clear", "Cut", "Copy", "Paste","-","Clear All","Change Background...","Change Size ..." };
      String[] commands = new String[] { "open","new","-","clear", "cut", "copy", "paste" ,"-","clearAll", "bg","size"};
      popup = new PopupMenu();                   // Create the menu
      for(int i = 0; i < labels.length; i++) {
        MenuItem mi = new MenuItem(labels[i]);   // Create a menu item
        mi.setActionCommand(commands[i]);        // Set its action command
        mi.addActionListener(this);              // And its action listener
        popup.add(mi);
                             // Add item to the popup menu
      }//endfor
      	this.add(popup);
		this.setBackground(new Color(0x0879ed6)) ;
		//this.setBackground(Color.gray) ; //old version
      	lastSel = -1 ;
    }//end of constructor


    public Dimension getPreferredSize() {
    	return frame.getSize();}

	public void changeSize(){
		//eraseRect();
		int temp = stampW ;
		compteur++;
		compteur = compteur%3 ;
		switch( compteur ){
			case 0 :
				PixObject.setStampSize(140);
				break ;
			case 1	:
				PixObject.setStampSize(100);
				break ;
			case 2  :
				PixObject.setStampSize(80);
				break ;
		}
		stampH = PixObject.getStampSize() ;
		stampW = stampH ;

// POurquoi on n'instancie pas fwic dans le constructeur??? à essayer(le 23/01/98 )
		if(fwic==null)fwic = new FindWhereInComp(this,stampW,stampH);
		fwic.stampH = stampH ;
		fwic.stampW = stampW ;

		repaintAllPixObjects() ;
		repaint();


	 }// end of changeSize

    /** This is the ActionListener method invoked by the popup menu items */
    public void actionPerformed(ActionEvent event) {
      String command = event.getActionCommand();

      if (command.equals("clear")) clear();
      else if (command.equals("open")) open();
      else if (command.equals("clearAll")) clearAll();
      else if (command.equals("new"))  frame.doCommand("OpenGIF") ;
      else if (command.equals("cut")) cut();
      else if (command.equals("copy")) copy();
      else if (command.equals("paste")) paste();
      else if (command.equals("bg")) changeBackground() ;
      else if (command.equals("size")) changeSize() ;


    }


/**
* Override paint
*/
    public void paint(Graphics g) {
   		 for(int i = 0; i < vimages.size(); i++) {
    		PixObject po = (PixObject)vimages.elementAt(i);
    		paintStamp(g,i, po) ;
    		paintRect() ;
   		}
    }

    public void paintStamp( Graphics g , int number, PixObject po){
			int numRow = this.getSize().width / stampW ;
			int numCol = this.getSize().height/ stampH ;
			int xPos  = (number % numRow)* stampW ;
			int yPos  =  (number/numRow)/*%numCol */;//Modification au 140698
			yPos *= stampH ;

			if(  (yPos+stampH) > getSize().height) {
				setSize(this.getSize().width , yPos+stampH)   ;
				}
			g.drawImage(po.scaled, xPos+gap/2, yPos+gap/2 ,this );
	}


    public void open(){

    		if(lastSel == -1) return ;// alertbox to plug here
    		else{
    			PixObject po = (PixObject)vimages.elementAt(lastSel) ;
    				if(po != null){
    					if(po.isShowing == false ){
    					PixObjectViewer iw = new PixObjectViewer( po ) ;

    					iw.show();
    					frame.TF.setText( "Opening image " + po.url ) ;
		    			po.isShowing = true;
		    		}else{
		    				Frame f=Winager.getPixObject(po);
		    				if(f != null) f.toFront() ;
		    		}
		    	}
		    }
    		}

    public void print(){
    	Toolkit toolkit = getToolkit() ;
		java.util.Properties printPrefs = new java.util.Properties();
		PrintJob job = toolkit.getPrintJob(frame, "Print Image", printPrefs);
		if(job == null) return ;
    	Graphics g  = job.getGraphics() ;
			g.translate(50,50) ;
			Dimension size = this.getSize() ;
			g.drawString ("Printing main canvas ..." , -2, -12);
			g.drawRect(-1,-1,size.width +1, size.height +1);
			g.setClip(0,0, size.width, size.height);
			//g.drawImage(applet.toDisplay , 10 ,20, this );
			paint(g) ;
			g.dispose();
			job.end();
    }


    public void changeBackground(){
    	com.sderhy.ColorBox cb = new ColorBox(new java.awt.Frame(),this.getBackground() );
    	cb.show() ;
    	this.setBackground(cb.getColor());
    	frame.setBackground(cb.getColor()) ;
 		cb.dispose() ;
    	cb = null ;
    	for(int i =0 ; i < vimages.size() ; i++){
	 		PixObject po = (PixObject)vimages.elementAt(i);
	 		po.changeBackground();
	 	}
    }


    public void parseFile(){
    	Tools.gc();//util ??
    	repaint() ;
    	String fileName = Futil.openDialog(frame) ;
    	if(fileName == null) return ;

    	try{FileParser FP = new FileParser(fileName, frame );
    	 }
    	catch(IOException e){ Tools.debug(this , " Xcptn at parseFile" + e);}

	}///END OF FILEPARSE

   public void savePixCanvas() {
	  	offScreen = createImage(this.getSize().width , this.getSize().height) ;
		Graphics offg = offScreen.getGraphics();
		this.paint(offg);
		offg.dispose();
		Tools.gc();
		Futil.saveToGif( offScreen,frame) ;
		return ;
   }//endof savePixCanvas()

    public void processMouseEvent(MouseEvent e) {

		if (e.isPopupTrigger())  popup.show(this, e.getX(), e.getY());
        else if (e.getID() == MouseEvent.MOUSE_PRESSED) {

     		if(fwic==null)fwic = new FindWhereInComp(this,stampW,stampH);
     		if(lastSel != -1) {//  if there was an image selected before
     			if(fwic == null) return ;
     			 fwic.clearRect(lastSel); // clear the Rect
     			lastSel = -1 ;//reset the rect
     		}
    		lastSel = fwic.findWhere(e.getX(),e.getY(),vimages.size()) ;
     		if(lastSel != -1){
     			paintRect() ;// go and clear the textField
     		}
     		if(e.getClickCount() > 1 && lastSel> -1){
     				open() ;//doubleClick on an image.
     		}
     	}
      else super.processMouseEvent(e);
    }

    public void processMouseMotionEvent(MouseEvent e) {
      //if (e.getID() == MouseEvent.MOUSE_DRAGGED) {}
      	super.processMouseMotionEvent(e);  // Important!
    }//end of processMouseMotionEvent

//////////////KeyEvent///////////////////////////////////////

    public  void keyTyped(KeyEvent e){}
    public  void keyPressed(KeyEvent e){
    	Tools.debug(this, "hello from processKeyEvent") ;
    }
    public  void keyReleased(KeyEvent e){}



/**
*	paintRect() invoked by paint() to know if it is necessary to draw a rect around
*	the selected image
*/
    public void paintRect(){
    	if(lastSel <0 ) return ;
    	if(fwic == null) return ;
    		PixObject po = (PixObject)vimages.elementAt(lastSel) ;
    		frame.TF.setText(po.url.toString());
    		fwic.drawRect(lastSel);
    }


/**
* clearAll invoked by popup
*/
	public void repaintAllPixObjects(){
	 for(int i =0 ; i < vimages.size() ; i++){
	 	PixObject po = (PixObject)vimages.elementAt(i);
	 	po.repaint() ;
	 	}
	}
     public void clearAll(){
     	vimages.removeAllElements() ;
     	if(fwic != null)fwic = null;
     	lastSel = -1;
     	repaint() ;
     	Tools.gc("Clear all") ;
     }//end of clearAll()


  public void loadFileSet(){
  		String where = Futil.openDialog(frame );
  		String line = "";
  		if(where == null ) return ;
  	// filter extension ,
	//	if (!where.endsWith(".jdv")) return ;
	// load file
		try{
			FileInputStream fis = new FileInputStream(where);
			DataInputStream in = new DataInputStream(fis) ;

			line = in.readLine() ;
			if(!line.equalsIgnoreCase("This file is automatically produced by a viewer")) return ;

			in.readLine() ; // allow for a line of comment.

			do{
				line = in.readLine();
				if(!OpenOther.openStringURL(line, frame)) break ;
			}while( line !=  null);

			in.close();
			fis.close();
		}
		catch(IOException ie){tools.Tools.debug( "IOE exception "+ ie);}

  }

  /**
  * loadFileSet , intended to load a file set from a URL.
  */

   public void loadFileSet(java.net.URL  url){
  		if(url == null ) return ;
  		String line = "";
  	// check that you have a valid file :
		if (!url.toString().endsWith(".jdv")) return ;


	// load file
		try{

			java.net.URLConnection u = url.openConnection();
			DataInputStream  in = new DataInputStream(u.getInputStream()) ;
			line = in.readLine() ;
			if(!line.equalsIgnoreCase("This file is automatically produced by a viewer")) return ;
			in.readLine() ; // allow for a line of comment.
	/////pourquoi do-while ????????????

	//		while(line != null){
			do{
				line = in.readLine();
				if(line ==null)break;
				if(!OpenOther.openStringURL(line, frame)) continue ;

			}while( line !=  null);
//			}//endWhile
			in.close();
		}catch(Exception ie){tools.Tools.debug( "IOE exception "+ ie);}

  }
  /**
  *  Diaporama
  **/

  	public void diaporama(){
  		if(vimages.isEmpty() ) return ;
  		Diaporama d = new Diaporama( vimages) ;
  		d.show() ;
  		d.start() ;

  	}
  /** Batch saving someone asked me to  save a bunch of images...*/
  public void batch(){
  	if (vimages.isEmpty())  {
    		AlertBox AB = new AlertBox(frame , "Nothing to save !!" ) ;
    		return ;
    	}
 	FileLister fL = new FileLister(System.getProperty("user.dir"),null,frame) ;
 	fL.show() ;
 	java.io.File dir = fL.getChoosenDirectory() ;
 	if(dir == null) return ;
 	if(!dir.isDirectory()) return ;
	tools.Tools.debug("choosen dir = "+ dir);
 	fL.dispose() ;


 	for(int i =0 ; i < vimages.size() ; i++){

	 PixObject po = (PixObject)vimages.elementAt(i);
	 File  theFile = new File(po.url.getFile());

	try{
	 GIFEncoder.GIFEncoder gifEncoder =  new GIFEncoder.GIFEncoder(po.image);
	 String whereToSave = theFile.getName()+ ".gif" ;
	 FileOutputStream fos = new FileOutputStream(new File(dir,whereToSave));
	 gifEncoder.Write(fos) ;
	 fos.close() ;
	 }//end try
	   catch(IOException ioe){
	   	tools.Tools.debug( "IOE exception "+ ioe);
	   }// end catch IOException
	   catch(AWTException awtex){
			tools.Tools.debug(" Caught an AWTException while saving pixCanvas "+ awtex);
			String whereToSave = theFile.getName()+".jpg" ;
			try{
				FileOutputStream fos = new FileOutputStream(new File(dir,whereToSave));
				Futil.saveToJPG(po.image , 90, fos) ;
				fos.close();
				}catch( IOException ioe){;}

	   }// end Catch AWTE...


	}//end for...
   }// End of method  batch()


  	public void batchSmall(){
  		if (vimages.isEmpty())  {
    		AlertBox AB = new AlertBox(frame , "Nothing to save !!" ) ;
    		return ;
    	}
 		FileLister fL = new FileLister(System.getProperty("user.dir"),null,frame) ;
 		fL.show() ;
 		java.io.File dir = fL.getChoosenDirectory() ;
 		if(dir == null) return ;
 		if(!dir.isDirectory()) return ;
		tools.Tools.debug("choosen dir = "+ dir);
 		fL.dispose() ;

 		for(int i =0 ; i < vimages.size() ; i++){

	 	PixObject po = (PixObject)vimages.elementAt(i);
	 	File  theFile = new File(po.url.getFile());

	 	//String whereToSave = theFile.getName() ;


	try{
	 GIFEncoder.GIFEncoder gifEncoder =  new GIFEncoder.GIFEncoder(po.scaled);
	 String whereToSave = theFile.getName()+".gif" ;
	 FileOutputStream fos = new FileOutputStream(new File(dir,whereToSave));
	 gifEncoder.Write(fos) ;
	 fos.close() ;
	 }//end try

		   catch(AWTException awtex){
				String whereToSave = theFile.getName()+".jpg" ;
				try{

					FileOutputStream fos = new FileOutputStream(new File(dir,whereToSave));
					Futil.saveToJPG(po.scaled , 60, fos) ;
					fos.close();
					}catch( IOException ioe){;}
			}// end Catch AWTE...
	 catch(IOException ioe){
		   		tools.Tools.debug( "IOE exception "+ ioe);
	 }// end catch IOException





	}//end for...
   }// End of method  batchSmall()




  /**
   *	SaveFileSet .. save the url as a text file .
   **/

    public void saveFileSet( ){
    	if (vimages.isEmpty())  {
    		AlertBox AB = new AlertBox(frame , "Nothing to save !!" ) ;
    		return ;
    	}

   	    String whereToSave = Futil.openSaveDialog(frame);
   		if(whereToSave == null ) return ;
   		Tools.debug( this , whereToSave ) ;
		StringBuffer  buffer = new StringBuffer() ;
		buffer.append( "This file is automatically produced by a viewer\n");
		buffer.append("It should be edited with care. Copyright 1998 Serge Derhy\n");
		for(int i =0 ; i < vimages.size() ; i++){
	 		PixObject po = (PixObject)vimages.elementAt(i);
			buffer.append( po.url.toString() + "\n" ) ;
		}
	//	buffer.append(".") ;
		Futil.saveText(whereToSave , buffer.toString() ,".jdv" ) ;

	}

/**
*	ListPixObjectURL is intended to be used with  undo ...
*/

	private String listPixObjectURL(){
		if(vimages.isEmpty() ) return null ;
		StringBuffer  buffer = new StringBuffer() ;
		for(int i =0 ; i < vimages.size() ; i++){
	 		PixObject po = (PixObject)vimages.elementAt(i);
			buffer.append( po.url.toString() + "\n" ) ;
		}
	return buffer.toString() ;
	}


	public void generateHtml(){
		if(vimages.isEmpty() ) return  ;
		 String whereToSave = Futil.openSaveDialog(frame);
   		if(whereToSave == null ) return ;
		HtmlEncoder hE = new HtmlEncoder() ;
		for(int i =0 ; i < vimages.size() ; i++){
	 		PixObject po = (PixObject)vimages.elementAt(i);
			hE.encodeImg(po.url.toString(), po.w, po.h);
		}
		String theHtml = hE.getHtmlCode() ;
		Futil.saveText(whereToSave , theHtml ,".html" ) ;

	}

 ////////////////////////////////////////clear copy cut///////////////////////////////
    /** Clear  Invoked by popup menu
    *	clears the selected image if any
    */
    void clear() {
    	if (lastSel > -1){
    		vimages.removeElementAt(lastSel);
     	    lastSel-- ;
     	    repaint();
     	 return ;
     	 }
    }

    /*
    public static final DataFlavor
    	dataFlavor = new DataFlavor(PixObject.class, "A Pixel Object ");
  	*/
    public void copy() {
    	if (lastSel > -1)
      		exPo = (PixObject)vimages.elementAt(lastSel) ;
     	else {
        		this.getToolkit().beep();
        		Tools.debug(this, "transferable == null") ;
       			 return;
     	 	}//endELSE
     }//end of copy


    public void cut() { copy(); clear();  }

    public void paste() {

      if (exPo == null) {              // If there is nothing to paste, beep
        this.getToolkit().beep();
        Tools.debug(this, "transferable == null") ;
        return;
      }

      	if(lastSel < 0 )
        	vimages.addElement(exPo);
        else
        	vimages.insertElementAt( exPo ,lastSel) ;
        repaint();
      }//end of past

    }//end of class

//////////////////////////////////////////////////////////////////////////////////////////

//!}//end of class.

