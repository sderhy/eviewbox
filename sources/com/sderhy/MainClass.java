/** Application MainClass.
 *  @author   Serge Derhy
 *  @version  1.0  last Modif. 140898 1738
 *	@ date 01/01/98
 * <A HREF ="mailto:sderhy@imaginet.fr"> Serge Derhy</A>
 */
package com.sderhy ; 
import java.net.* ;
import java.awt.*;
import java.io.* ;
import java.util.Vector;     
import tools.Chrono ;
import tools.Tools ;
import java.awt.event.* ; 

public class MainClass extends Frame  implements WindowListener{

	public static boolean isApplet = false ;
	public TextField TF;
	public PixCanvas canvas;
	public Vector vimages ;
	public java.applet.Applet applet = null ;
	public MainClass( String title){
		super( title ) ;
		TF = new TextField(80) ;
		//TA = new TextArea();
		vimages = new Vector(32,32);
		
		int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
	 	int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
		
		canvas = new PixCanvas(this,600,300);///CHANGER ICI  POUR OCCUPER PLUS  l4ecran
		// setting the window  Layout
		com.sderhy.MainLayout.setLayout(this);
		addComponentListener(new CompoListener(this));
		setSize(screenW-100, screenH -100) ;
		setLocation(0,0);
		TF.setEditable(true) ;
		Winager.add(this);
		//MainLayout.initMenus(this);
		show();
		
	}// end of constructeur
/**
*	doCommand is  intended for answering to menu command
*/	
	public void doCommand(String command){
		if (command.equals ("OpenGIF")) OpenGif.fromFile(this) ;
		else if (command.equals("OpenDicom"))OpenDicom.fromFile(this) ;
		else if (command.equals("clear")) canvas.clear();
      	else if (command.equals("open")) canvas.open();
      	else if (command.equals("clearAll")) canvas.clearAll();
      	else if (command.equals("new"))  this.doCommand("OpenGIF") ;
      	else if (command.equals("Cut")) canvas.cut();
      	else if (command.equals("Copy")) canvas.copy();
      	else if (command.equals("Past")) canvas.paste();
      	else if (command.equals("bg")) canvas.changeBackground() ;
      	else if (command.equals("size")) canvas.changeSize() ;
		else if (command.equals("Close")) close() ;
		else if (command.equals("OpenFolder")) OpenFolder();
		else if (command.equals("Quit")) close();
		else if (command.equals("SaveAsGIF")) canvas.savePixCanvas() ;
		else if (command.equals("ParseFile")) canvas.parseFile() ;	
		else if (command.equals("PrintImage")) canvas.print() ;	
		else if (command.equals("OpenFromURL")) this.openURLDialog() ;
		else if (command.equals("linear")) new Multiplanar(canvas, false);
		else if (command.equals("curve")) new Multiplanar(canvas, true);
		else if (command.equals("SaveFileSet")) canvas.saveFileSet();
		else if (command.equals("LoadFileSet")) canvas.loadFileSet();
		else if (command.equals("batch")) canvas.batch();// batch saving the images
		else if (command.equals("batchSmall")) canvas.batchSmall();// batch saving the images
		else if (command.equals("TextField")) OpenOther.openStringURL(TF.getText(), this);
		else if (command.equals("About")) about();
		else if (command.equals("Diaporama")) canvas.diaporama();
		else if (command.equals("GenerateHtml")) canvas.generateHtml();
		else if (command.equals("NextWindow")) Winager.next();
		else if (command.equals("LastWindow")) Winager.previous();
		else if (command.equals("MailTo")) {
											MailerWidget MW = new MailerWidget() ;
											MW.setVisible(true );
											
		}
		else if (command.equals("MailToTheAuthor")){
											MailerWidget MW = new MailerWidget("sderhy@imaginet.fr") ;
											MW.setVisible(true );
										}
				
		
		
		else Tools.debug(this,command);
	
	}// end of doCommand()		

	 public void close(){
	 	if(!isApplet){
	 		Tools.debug("isApplet is " + isApplet ) ;
    		this.dispose();
    		return ;
    		//System.exit(0);
   	}else{ 
   	this.dispose() ;
    	Tools.gc() ;
    	applet.destroy();
    	}
    }

	public void openURLDialog(){
		com.sderhy.openURLDialog d = new com.sderhy.openURLDialog("http://wwwusers.imaginet.fr/~sderhy/vascular/sderhy.jpg", this ) ;
	 	d.show() ;
	 	String theURL = d.getResult() ;
	 	OpenOther.openStringURL(theURL, this) ;
		if(d != null) d.dispose() ;
	}

	public void about(){
		AlertBox Ab = new AlertBox(this, "Copyright Serge Derhy  1998" ,
						"An Application from Serge Derhy, sderhy@imaginet.fr") ;
	}

 public void OpenFolder(){
 	Tools.debug(this, "OpenFolder") ;
 	FileLister fL = new FileLister(System.getProperty("user.dir"),null,this) ;
 	fL.show() ;
 	java.io.File dir = fL.getChoosenDirectory() ;
 	Tools.debug("choosen dir = "+ dir);	
 	if(dir != null){
 		if(!dir.isDirectory()) return ;
 		fL.dispose() ;
 		String[] files = dir.list() ;
		for (int i = 0 ; i< files.length ; i++){
	 		File f = new File(dir, files[i]);
	 		if(!f.isDirectory()) {
	 				String theFile = f.getAbsolutePath();
		 			Tools.debug("The image number  " + i + " is " + theFile);
		 			OpenGif.fromFile( theFile,this);
		 			repaint() ;
			}//endif
		} //endFor
	}//end if(dir != null)
	else{
		File [] chosenFiles =fL.getChosenFiles();
		fL.dispose() ;
		if (chosenFiles == null) return ;	
			for (int i = 0 ; i< chosenFiles.length ; i++){
				String theFile = chosenFiles[i].getAbsolutePath();
		 		Tools.debug("The image number  " + i + " is " + theFile);
	 			OpenGif.fromFile( theFile,this);
	 			repaint() ;
		}//end for	
	}
	
 }

 
  public void windowClosing(WindowEvent e) { 	
 	 	this.hide();
 	 	close();
 }

		

  public void windowOpened(WindowEvent e) {repaint() ;}
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {repaint();}
  public void windowDeiconified(WindowEvent e) {repaint() ;}
  public void windowActivated(WindowEvent e) {repaint();}
  public void windowDeactivated(WindowEvent e) {repaint();}
 
 

/** The main entry point to the class.
 */
	public static void main(String args[])
	{
		MainClass MC = new MainClass("EViewBox" );	
	}//end of Main()
	public static void main()
	{
		MainClass MC = new MainClass("eViewBox " );	
	}//end of Main()


/**
*	A component Listener InnerClass :
*/
	
	
	public class CompoListener extends ComponentAdapter {
			MainClass mc ;
			public 	CompoListener(MainClass mc){
				this.mc = mc ;
				}
			public void componentResized(ComponentEvent e){
				 Dimension d = mc.getSize() ;	
				 mc.canvas.setSize(d) ;
				 repaint();
							
			}//endofComponentResized	
			
			public   void componentHidden(ComponentEvent e){mc.repaint() ;} ;
			public   void componentMoved(ComponentEvent e){mc.repaint();};
			public  void componentShown(ComponentEvent e){mc.repaint();}
				
	}//EndOfInnerClass


}//end of class


