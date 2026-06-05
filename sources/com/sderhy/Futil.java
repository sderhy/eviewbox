/**
*	Futil
*/
package com.sderhy ;
import java.awt.* ;
import java.io.* ;

public class Futil {

static boolean firstPass = true ;
static String lastDir ;

public static String openDialog(java.awt.Frame f) {
		String fileString  = "";
		FileDialog fd = new FileDialog(f,"Open file",FileDialog.LOAD) ;	
			
		if( firstPass){// get the file dialog to the user.dir at first time .
			fd.setDirectory(System.getProperty("user.dir"));
			firstPass = false ;
			}// end  if first pass
		else  
			fd.setDirectory(lastDir) ;
		fd.pack();  // bug workaround
		fd.show();  // blocks until user selects a file
		if( fd.getFile() == null) return null ;
				
		fileString =  fd.getDirectory()+fd.getFile() ;
		if (fd.getDirectory() != null) lastDir = fd.getDirectory() ;
		fd.dispose();
		return fileString ;
		}//en of openDialog
		
	
	public static String openSaveDialog(java.awt.Frame parent){
		FileDialog fd = new FileDialog(parent,"Saving file",FileDialog.SAVE);
		fd.pack();
		fd.show();
		String theFile = fd.getFile();
		if(theFile == null ) return null ;
		String nameFile = fd.getDirectory()+fd.getFile() ;
		fd.dispose();
		return nameFile;
	}//endOfopenSaveDialog ;
	
	
	
	public static boolean saveToGif(Image img,java.awt.Frame frame)  {
	//Choisir un dialog ;
	String whereToSave = Futil.openSaveDialog(frame) ;//frameis MainClass
	// instancier le GIFEncoder :
	if(whereToSave == null) return true;
	
	String suffix = whereToSave.substring( whereToSave.length()-4) ;
	if (!suffix.equalsIgnoreCase(".gif"))
	whereToSave += ".gif" ;
	
	try{
		GIFEncoder.GIFEncoder gifEncoder =  new GIFEncoder.GIFEncoder(img);

		FileOutputStream fos = new FileOutputStream(whereToSave);  
	
		gifEncoder.Write(fos) ; 
		
		fos.close() ;
		
   }//end try
   catch(IOException ioe){ tools.Tools.debug( "IOE exception "+ ioe);
   							return false ;
   						}
   catch(AWTException awtex){
		tools.Tools.debug(" Caught an AWTException while saving pixCanvas "+ awtex);
		return Futil.saveToJPG(img , 90, whereToSave) ;
		//return false ;
	}
	
	return true ;
  }

		
	public static void  saveText( String whereToSave, String content , String extension) {
		
		if (whereToSave == null) return ;	
		String suffix = whereToSave.substring( whereToSave.length()-4) ;
		
		if (!suffix.equals(extension))
			whereToSave += extension ;
			
		try{		
			FileOutputStream fos = new FileOutputStream(whereToSave);
			PrintStream ps = new PrintStream(fos) ;
			ps.println(content) ;
			ps.close() ;
			fos.close();
		}
		catch(IOException ie){tools.Tools.debug( "IOE exception "+ ie);}
	
	}
	/*
	public static String  loadText(String whereTo , String extension) {
	// filter extension ,
		if(extension != null)
			if (!whereTo.endsWith(extension)) return  null;
	// load file
		try{		
			FileInputStream fis = new FileInputStream(whereTo);
			DataInputStream dis = new DataInputStream(fis) ;
			fis.close();
		}
		catch(IOException ie){tools.Tools.debug( "IOE exception "+ ie);}
	
	return null;	}

	//*/
	public static boolean saveToPPM(Image img , java.awt.Frame frame)  {
	//Choisir un dialog ;
	String whereToSave = Futil.openSaveDialog(frame) ;//frameis MainClass
	// instancier le GIFEncoder :
	if(whereToSave == null) return true;
	
	String suffix = whereToSave.substring( whereToSave.length()-4) ;
	
	if (!suffix.equalsIgnoreCase(".ppm"))
		whereToSave += ".ppm" ;

	try{
		FileOutputStream fos = new FileOutputStream(whereToSave);  
		PPM.PPMEncoder encoder = new PPM.PPMEncoder(img) ;
		encoder.write(fos) ;
		fos.close();
		//Acme.JPM.Encoders.PpmEncoder ppmFile = new Acme.JPM.Encoders.PpmEncoder( img, fos );  
		//ppmFile.encode() ;
		
		//gifEncoder.Write(fos) ; 
   	}//end try
   catch(IOException ioe){ tools.Tools.debug( "IOE exception "+ ioe);
   							return false ;
   						}
 
	//catch(Exception my){tools.Tools.debug( my.toString());}
	return true ;	
	
	}
		

	public static boolean saveToPPM(Image img , java.awt.Frame frame, String whereToSave)  {
	
		if(whereToSave == null) return true;
		String suffix = whereToSave.substring( whereToSave.length()-4) ;
		if (suffix.equalsIgnoreCase(".gif"))
			whereToSave = whereToSave.substring(0, whereToSave.length()-4);
		if (!suffix.equalsIgnoreCase(".ppm"))
		whereToSave += ".ppm" ;

	try{
		FileOutputStream fos = new FileOutputStream(whereToSave);  
		PPM.PPMEncoder encoder = new PPM.PPMEncoder(img) ;
		encoder.write(fos) ;
		fos.close();
   	}//end try
   catch(IOException ioe){ tools.Tools.debug( "IOE exception "+ ioe);
   							return false ;
   						}
 
	return true ;	
	
	}
///////////////////////////////////////////////////////////////////////////////////	
	
	public static boolean saveToJPG(Image img ,int quality, String whereToSave){
		if(whereToSave == null) return true;
		whereToSave += ".jpg" ;

	try{
		FileOutputStream fos = new FileOutputStream(whereToSave);  
		JpegEncoder.JpegEncoder jpg = new JpegEncoder.JpegEncoder(img, quality, fos);
		jpg.Compress();
		fos.close();
   	}//end try
   catch(IOException ioe){ tools.Tools.debug( "IOE exception "+ ioe);
   							return false ;
   						}
 
	return true ;	
	
	}
	
	public static boolean saveToJPG(Image img ,int quality, OutputStream os){

		JpegEncoder.JpegEncoder jpg = new JpegEncoder.JpegEncoder(img, quality, os);
		jpg.Compress();
  		return true ;	
	
	}
	
}// end of Class
