/**
*	OpenDicom is a class called by OpenGif (misnamed class) which
*	open  DICOM images, and creates a pixObject that displays each
* image in a the MainClass pixCanvas
*/
package com.sderhy ;
import java.awt.* ;
import java.io.* ;
import java.net.* ;
import tools.Tools ;
import DicomDecoder.* ;
import java.net.*;
public class OpenDicom {

	public static boolean fromURL(URL url, MainClass mc){
		String [] info ;
		TextField TF = mc.TF ;
		PixCanvas canvas  = mc.canvas ;
		java.awt.Image [] images ;
		TF.setText( url.toString() ) ;
		DataInputStream  in ;
		File file;
			try{
				URLConnection u = url.openConnection();
				in = new DataInputStream(u.getInputStream()) ;
			int size = u.getContentLength() ;

			byte[] array = new byte[size];
			int bytes_read = 0;
			while(bytes_read < size){
						bytes_read += in.read(array, bytes_read, size - bytes_read);
			}//endwhile
			in.close();

				DicomReader dR = new DicomReader(array) ;

				images = dR.getImages();

				info =  dR.getInfos();
				DicomHeaderReader header = dR.getDicomHeaderReader();
				// "See Attributes" now shows every parsed element, not just
				// the curated summary.
				String[] allTags = header.getAllElements();
				if(allTags != null && allTags.length > 0) info = allTags;

				// A great modification made by Michael Pasternak to
				//open multiple images : (Thank you Mike !)
			  MediaTracker tr = new MediaTracker(canvas ) ;
					System.out.println("Images found " + images.length);

					for (int i = 0 ; i < images.length; i++) {
		      tr.addImage(images[i], i);
		      try{tr.waitForID(i) ;} catch(InterruptedException e) {};
		      if (tr.isErrorID(i)){
		        TF.setText("Error while loading file...  try again");
		        Tools.debug(tr.getErrorsAny().toString() ) ;
		        return  false;
		      }
		      System.out.println("Adding image " + i);
		      if (images[i] == null) {
		        System.out.println("Image is null!");
		      }
		      PixObject po = new PixObject(url, images[i], canvas, true,info ) ;
		      po.sliceThickness = header.getSliceThicknessValue();
		      po.spacingBetweenSlices = header.getSpacingBetweenSlicesValue();
		      po.sliceLocation = header.getSliceLocationValue();
		      po.pixelSpacingRow = header.getPixelSpacingRowValue();
		      po.pixelSpacingColumn = header.getPixelSpacingColumnValue();
		      mc.vimages.addElement(po) ;
		      po.isDicom = true ;
		      canvas.refresh() ;
		    }
			}catch (IOException e){
				tools.Tools.debug("exception "+ e);
				TF.setText("Error file format not recognized" );
				return false;
			}//end of try-catch
			return true;
		}//end fromFile
/**************************************************************************/
	public static boolean fromFile(MainClass mc ) {
		String fileURL = "File:" + Futil.openDialog(mc);
		URL url = null;
		try{  url = new URL(fileURL) ;}
		catch (MalformedURLException e){
				mc.TF.setText("Not a valid File URL" );
				return  false;
		}
		return fromURL(url, mc)	;
	}//end of openDicom.fromFile()

}//end of class
