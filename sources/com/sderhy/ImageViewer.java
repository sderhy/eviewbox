
package com.sderhy;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;

public class ImageViewer extends com.sderhy.myFrame 
{
	Image image ;
	Image offs ;
	Image origPic ;
	boolean DrawLayout = false ;
	String  layoutString ;
	Graphics offg ;
	LWWidget lWW ; // The window level frame that appears when setting window and level .
	int w , h ;	// the image Width
	int destw,desth ;//ImageWidth and Height when displayed
	
	/** 
	*	x and y are the upper corner of the image.
	*	when working on windows you have to know what is the insets
	*	maybe the good option is x = this.getInsets().left
	*	y = this.getInsets().top
	*/
	
	int x =0, y ;//upper corner of the image///ATTENTION  read the comment.
	
	int lastx,lasty ;//for Mouse dragg
	int screenH,screenW ;
	protected PopupMenu popup;
	/** mouseAction tells which contextual menu has been selected default to 0 
	*	used for dragging the mouse :
	*	if mouseAction == 0 ;  translate the image
	*	if mouseAction ==1 ;	zoom
	*	if mouseAction ==2 ;	contrast/brightness
	*/
	int mouseAction = 2 ; // set default mouse action to contrast/brightness
	static String[] labels =  new String[]{"Zoom","zoom","Translate","translate",
	"Contrast","contrast","-","-","Normal Size","NormalSize","Sharpen","sharpen", "Blur", "blur",
	
	"Measure","mesure", "Reset", "reset"};		
	
	public ImageViewer(Image  image){
	 	setBackground(Color.black);
	 	this.image = image ;
		w = image.getWidth(this) ;
	 	h= image.getHeight(this) ;	
	 	destw = w; desth = h ;
	 	addComponentListener(this);	
		 this.enableEvents(AWTEvent.MOUSE_EVENT_MASK|
      				AWTEvent.MOUSE_MOTION_EVENT_MASK );
		setPopup();
		int wn = getWindowNumber() ;
	 	screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
	 	screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
	 	int position = (wn*50)% screenH ;
	 	setTitle(" Image  number " + wn); 
	 	setLocation (position, position/2 ) ;
		reSizeIfTooBig() ;
	 	setSize(destw,desth+getInsets().top );
	 	origPic = image ;// save the original image .
	 	layoutString ="";
	 	//centerImage();
	 
	}
	/** Resizes to 95 * 90 the size of the screen */
	public void reSizeIfTooBig(){
		if(w>screenW && (w>h)){
	 		destw= screenW*95/100;
	 		desth=( h*screenW*95 )/(w*100) ;
	 	}
	 	if(h >screenH && (h>=w)){
	 		desth = screenH*90/100 ;
	 		destw = w * screenH*90/(h*100) ;
	 	}
	}
	
	public void centerImage(){
		
		x = (getSize().width  - 	destw)/2 ;
		y = (getSize().height -   desth)/2 ;
		y += this.getInsets().top/2 ;
	}

	protected void arrangeIt(){
	 	destw = w; desth = h ;
	 	reSizeIfTooBig();
	 	resize(destw,desth+getInsets().top);
	 	centerImage();
	}
	
	public Dimension getPreferredSize() { return new Dimension(w,h+getInsets().top);} 
	
	public void update (Graphics g){ paint(g);}

	public void paint(Graphics g){ 

		if(offs==null){
			offs = createImage(getSize().width,getSize().height);
			offg = offs.getGraphics() ;
			offg.setClip(0,0,getSize().width,getSize().height);
		}
		offg.setColor(this.getBackground());
		offg.fillRect(0,0,getSize().width,getSize().height);
		offg.setColor(this.getForeground());
		offg.drawImage(image,x,y,x+destw,y+desth,0,0,w,h,this) ;
		drawLayout(offg) ;
		g.drawImage(offs,0,0,null);
		
	}
	public void  setPopup(){
	
	 	popup = new PopupMenu();           
       for(int i=0;i<labels.length ; i+=2 ){ 
        	MenuItem mi = new MenuItem(labels[i]); 
        	mi.setActionCommand(labels[i+1]);      
        	mi.addActionListener(this);    
        	popup.add(mi);      
        }
		this.add(popup);
	}
	
	public void  isResized(){
		offs = null;
		tools.Tools.gc();
		centerImage() ;
		repaint() ;
	}

	public void imgSetSize(int c, int r){
		if(c ==0 || r ==0) return ;
		destw = c ; desth = r ; 
		centerImage();
		repaint() ;
	}
	
	public void actionPerformed (ActionEvent e){
		String command = e.getActionCommand() ;
		if(command == "NormalSize") this.imgSetSize(w,h) ;
		else if(command == "by25") this.imgSetSize(w/4,h/4) ;
		else if(command == "by50") this.imgSetSize(w/2,h/2) ;
		else if(command == "by75") this.imgSetSize((w*3)/4,(h*3)/4) ;
		else if(command == "by200") this.imgSetSize(w*2,h*2) ;
		else if(command == "by300") this.imgSetSize(w*3,h*3) ;
		else if(command == "zoom" ) mouseAction = 1;
		else if(command == "translate" ) mouseAction = 0;
		else if(command == "contrast" ){
				 mouseAction = 2;
				 if(lWW ==  null)lWW = new LWWidget(100,100) ;// initialize window level.
					
				}
		else if (command == "pseudoColor"){
			
				image = ProcessImage.pseudoColor(this ,image) ;
				repaint();				
		}
		else if (command == "gray"){
			
				image = ProcessImage.grayIt(this ,image) ;
				repaint();
			
		}
		else if (command == "flipV") {
				image = ProcessImage.flipVertical(this , image );
				repaint() ;
				}
		else if (command == "flipH") {
				image = ProcessImage.flipHorizontal(this , image );
				repaint() ;
		}
		else if (command == "brighten"){
				image = ProcessImage.brighten(this ,image) ;
				repaint();
		}
		else if (command == "invertLut"){
				image = ProcessImage.invertLut(this ,image) ;
				repaint();
		}
		else if (command == "save"){
				Futil.saveToGif(image,this) ;
				}
		else if (command == "rotateL"){
				
				image = ProcessImage.rotateImage(this ,image);
				
				int temp = w 	;w = h 		;h =temp ;
				temp = destw 	;destw = desth	; desth = temp ;
				resize(destw,desth);
				repaint() ;
			}

		else if (command == "rotateR"){
				
				image = ProcessImage.rotateRight(this ,image);
				
				int temp = w 	;w = h 		;h =temp ;
				temp = destw 	;destw = desth	; desth = temp ;
				resize(destw,desth);
				repaint() ;
			
			}
		else if(command == "sharpen" ){	
				image = ProcessImage.sharpen(this, image ) ;
				repaint() ;
		}
		else if(command == "blur" ){	
				image = ProcessImage.blur(this, image ) ;
				repaint() ;
		}
		
		else super.actionPerformed (e);
	}
	

public void processMouseEvent(MouseEvent e) {
		if (e.isPopupTrigger())  
			popup.show(this, e.getX(), e.getY());
			
        else if (e.getID() == MouseEvent.MOUSE_PRESSED) { 
    		lastx = e.getX();
			lasty = e.getY() ;
     //if(e.getClickCount() > 1 && lastSel> -1)open() ;//doubleClick on an image.
     			
     	}
   //   else super.processMouseEvent(e);  
    }
    /**
    *	 this method process the image, based on a contextual menu,
    *	 Default action is zooming,
    *	 Can be changed 
    *	if mouseAction == 2 calls ProcessImage.brighten2(this , image , dx , dy )
    *	in order to change window and levels.
    **/
    
    public void processMouseMotionEvent(MouseEvent e) {
      if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      
      		int dx = e.getX() -lastx;
  			int dy = e.getY() -lasty;
  			lastx = e.getX(); lasty = e.getY() ;
  	
  			if(mouseAction== 0){x += dx; y +=dy;}// Translate
  			if(mouseAction== 1 ){	// Zoom
  				
  				desth += dy ;
  				destw  += (dy*w)/h ;
  				centerImage();
  				
  				  //imgSetSize(w+dx , h*(w+dx)/w);
  			}
  			if(mouseAction ==2){ // WINDOW LEVEL
  				DrawLayout = true ;
  				image = ProcessImage.brighten2(this ,origPic,dx,dy) ;//x = brightness , y= contrast
  				layoutString = " "+ ProcessImage.contrast + " "+ ProcessImage.brightness ;
  				
  				if( lWW != null){
  					if(!lWW.isShowing())lWW.show() ;
  					lWW.setl( ProcessImage.brightness); //think contrast
  					lWW.setw( ProcessImage.contrast);// think brightness
				}
			}
			repaint();
			
      }
      super.processMouseMotionEvent(e);  // Important!
    }//end of processMouseMotionEvent
    

	protected void drawLayout(Graphics g){
			
		java.awt.Color c = g.getColor();
		g.setColor(Color.yellow);
		g.drawString( layoutString,10,10);
		g.setColor(c);		
		
	}

//redefine hide in order to get rid of the lWW window :
		public void  hide(){
				if(lWW !=null){
					lWW.hide() ;
					lWW.dispose() ;
					}
				super.hide() ;
	}



// componentListener :
	
	public  void componentResized(ComponentEvent e ){
			 isResized();
	};

	
}//END OF CLASS 


