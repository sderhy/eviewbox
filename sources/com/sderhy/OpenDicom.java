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
				DicomHeaderReader header = dR.getDicomHeaderReader();

				info =  dR.getInfos();
				// "See Attributes" now shows every parsed element, not just
				// the curated summary.
				String[] allTags = header.getAllElements();
				if(allTags != null && allTags.length > 0) info = allTags;

				// Build HU-aware PixObjects (real window/level) instead of the
				// min/max-stretched images.
				PixObject[] pos = buildDicomPixObjects(dR, url, canvas, info) ;

				// A great modification made by Michael Pasternak to
				//open multiple images : (Thank you Mike !)
			  MediaTracker tr = new MediaTracker(canvas ) ;
					System.out.println("Images found " + pos.length);

					for (int i = 0 ; i < pos.length; i++) {
		      tr.addImage(pos[i].image, i);
		      try{tr.waitForID(i) ;} catch(InterruptedException e) {};
		      if (tr.isErrorID(i)){
		        TF.setText("Error while loading file...  try again");
		        Tools.debug(tr.getErrorsAny().toString() ) ;
		        return  false;
		      }
		      System.out.println("Adding image " + i);
		      mc.vimages.addElement(pos[i]) ;
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

/**
*	Builds one HU-aware PixObject per frame : keeps the 16-bit Hounsfield values
*	and renders the initial image through the header's default window (or a
*	statistical fallback when the header carries none). Shared by OpenDicom and
*	FileParser so both display paths get proper windowing.
*/
	static PixObject[] buildDicomPixObjects(DicomReader dR, URL url, Canvas canvas, String[] info) throws IOException {
		DicomHeaderReader header = dR.getDicomHeaderReader();
		int frames = dR.getNumberOfFrames();
		if(frames < 1) frames = 1 ;
		int w = header.getColumns(), h = header.getRows();
		double dc = header.getWindowCenter(), dw = header.getWindowWidth();
		PixObject[] out = new PixObject[frames] ;
		for(int i = 1 ; i <= frames ; i++){
			short[] hu = dR.getHU(i);
			double center, width ;
			if(Double.isNaN(dc) || Double.isNaN(dw) || dw <= 0){
				double[] aw = autoWindow(hu); center = aw[0]; width = aw[1];
			} else { center = dc; width = dw; }
			PixObject po = PixObject.dicom(url, canvas, w, h, hu, center, width, info) ;
			po.isDicom = true ;
			po.sliceThickness       = header.getSliceThicknessValue();
			po.spacingBetweenSlices = header.getSpacingBetweenSlicesValue();
			po.sliceLocation        = header.getSliceLocationValue();
			po.pixelSpacingRow      = header.getPixelSpacingRowValue();
			po.pixelSpacingColumn   = header.getPixelSpacingColumnValue();
			out[i-1] = po ;
		}
		return out ;
	}

	/** Statistical fallback window (full min/max) when no Window Center/Width is present. */
	static double[] autoWindow(short[] hu){
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE ;
		for(int i = 0 ; i < hu.length ; i++){ int v = hu[i]; if(v < min) min = v; if(v > max) max = v; }
		if(max <= min) max = min + 1 ;
		return new double[]{ (min + max) / 2.0, (max - min) } ;
	}

}//end of class
