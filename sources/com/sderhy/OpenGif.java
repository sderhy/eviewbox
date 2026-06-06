/**
*	Classe  pour ne pas surcharger MainClass
*/
package com.sderhy;
import java.awt.* ;
import java.io.* ;
import java.net.* ;
import tools.Tools ;
import com.sderhy.*;


public class OpenGif  {

	public static int numImage = 0 ;
	public static boolean fromFile(String fileName, MainClass mc){
		return openFile(fileName, mc);
		}
	///////////////////////////////////////////////////////////////////////////////////////
	public static boolean fromFile(MainClass mc ) {
		String file = Futil.openDialog(mc);
		return openFile(file, mc);
		}//end of openGIF.fromFile()

	private static boolean openFile(String file, MainClass mc ) {
		if(file == null) return false;

		TextField TF = mc.TF ;
		PixCanvas canvas  = mc.canvas ;
		String suffix ="";
		URL url = null;
		try{
			url = new File(file).toURI().toURL() ;
			TF.setText( url.toString()  ) ;
		}
			catch (MalformedURLException e){
				TF.setText("Not a valid File URL" );
				return false;
		}

		if(file.length()>=4) suffix = file.substring( file.length()-4) ;
		if (suffix.equalsIgnoreCase(".ppm"))
			return OpenOther.fromURL(url,mc);
		if (suffix.equalsIgnoreCase(".bmp"))
			return OpenOther.BMPfromURL(url,mc);
		if (suffix.equalsIgnoreCase(".dic")||
			suffix.equalsIgnoreCase(".dc3")||
			suffix.equalsIgnoreCase(".dcm")||
			suffix.equalsIgnoreCase("dicm")
			)return OpenDicom.fromURL(url,mc);



		Toolkit tk = Toolkit.getDefaultToolkit() ;
		Image image = tk.getImage(url) ;
		if(image == null){
			TF.setText("Error not a typical image Jpg/GIF format" );
			return false;
		}//end if image ==null

		MediaTracker tr = new MediaTracker(canvas ) ;
		tr.addImage(image,0);

		try{tr.waitForID(0) ;} catch(InterruptedException e) {};
		if (tr.isErrorID(0)){
			TF.setText("Not a .gif or .jpeg file...  trying DICOM...");
			if( OpenDicom.fromURL(url,mc) )return true ;
		    return false;
		}
		PixObject po = new PixObject(url, image,  canvas,false,null ) ;
		mc.vimages.addElement(po) ;
		TF.setText(url.toString()) ;

			canvas.refresh() ;
			return true ;
			}


/////////////////////////////////////////////////////////////////////////////////////////////
		public static boolean fromURL(MainClass mc,URL url ) {
		TextField TF = mc.TF ;
		PixCanvas canvas  = mc.canvas ;

		String fileURL = url.toString();
		String suffix = fileURL.substring( fileURL.length()-4) ;
		if (suffix.equalsIgnoreCase(".ppm"))
			return OpenOther.fromURL(url,mc);
		if (suffix.equalsIgnoreCase(".bmp"))
			return OpenOther.BMPfromURL(url,mc);
		if (suffix.equalsIgnoreCase(".dic")||
			suffix.equalsIgnoreCase(".dc3")||
			suffix.equalsIgnoreCase(".dcm")||
			suffix.equalsIgnoreCase(".acr")||
			suffix.equalsIgnoreCase("dicm")
			)			return OpenDicom.fromURL(url,mc);

///////////////POUR L'APPLET
		if (!suffix.equalsIgnoreCase(".jpg")&&
			!suffix.equalsIgnoreCase(".jpeg")&&
			!suffix.equalsIgnoreCase(".gif")
			)			return OpenDicom.fromURL(url,mc);

//////////////////////////


		Toolkit tk = Toolkit.getDefaultToolkit() ;
		Image image = tk.getImage(url) ;
		if(image == null){
			TF.setText("Error not a typical image Jpg/GIF format" );
			return false;
		}//end if image ==null

		MediaTracker tr = new MediaTracker(canvas ) ;
		tr.addImage(image,0);

		try{tr.waitForID(0) ;} catch(InterruptedException e) {};
		//check for error :
		if (tr.isErrorID(0)){
		//	TF.setText("Not a Gif or Jpeg file...  trying PPM...");
		//	if(OpenOther.fromURL(url,mc))	return true ;
			//if (OpenOther.BMPfromURL(url,mc)) return true ;
			TF.setText("Not a .jpeg or .gif file...  trying DICOM...");
			if( OpenDicom.fromURL(url,mc) )return true ;
		    return false;

		}
		PixObject po = new PixObject(url, image,  canvas, false , null  ) ;
		mc.vimages.addElement(po) ;
		TF.setText(url.toString()) ;
		canvas.refresh() ;
		return true ;
		}//end of openGIF.fromFile()
}//end of class
