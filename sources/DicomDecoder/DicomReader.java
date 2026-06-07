/**
* @date 15 jun 97
* Copyright 1997 Serge Derhy                                                 
* Dicom Decoder package was written and released by :
*                                  serge derhy	<br>
*                                serge@derhy.com<br>
* @version  0.9 , last modif = 25/04/98
* @author <a href="http://www.derhy.com/dicom/index.html">serge derhy</a>
* Selling the code for this program without prior written consent is         
* expressly forbidden.  In other words, please ask first before you try and  
* make money off of my program.                                              
*                                                                            	
* This package is provided WITHOUT ANY WARRANTY either expressed or implied.
* You may study, use, modify, and distribute it for non-commercial purposes
* as long as this copyright notice remain intact.
**
* A simple way to use it  ... 
*  If  you want to retrieve an image :
	* <pre>
	//First instanciate a DicomReader 
	import DicomDecoder.* ;
	...
	DicomReader dr = new DicomReader( the_Dicom_Url_You_Want_To_Fetch) ;
        // then fetch the image !
	java.awt.Image img = dr.getImage();  // get the image !
	
        //To get some more information you need to get a DicomHeaderReader :	
	DicomHeaderReader dhr   = dr.getDicomHeaderReader() ;
	String  manufacturer	= getaString (0x0008,0x0070); // retrieve a String 
	int width = getAnInt(0x0028, 0x0011, index ) ;// retrieve an Int
	
	*</pre>
*
*	
**/
	package DicomDecoder ;
	import java.awt.*;
	import java.awt.image.* ;
	import java.util.*;
	import java.net.* ;
	import java.io.*;
	
	
public class DicomReader{
	int w, h , highBit, n  ; // highBit is for littleEndian
	boolean signed ;
	final static boolean DEBUG = true ;
	boolean ignoreNegValues ;
	int bitsStored, bitsAllocated ;
	int samplesPerPixel ;
	int numberOfFrames ;
	byte[] pixData;
	String filename ;
	DicomHeaderReader dHR ;
	
	
	public DicomReader( DicomHeaderReader dHR )throws java.io.IOException{ // called by :ImageScan
		this.dHR = dHR ;
		h 				= dHR.getRows() ;
		w 				= dHR.getColumns() ;
		highBit 		= dHR.getHighBit() ;
		bitsStored 		= dHR.getBitStored() ;
		bitsAllocated	= dHR.getBitAllocated();
		n 				= (bitsAllocated/8) 	;// = 1 or 2 
		signed 			= (dHR.getPixelRepresentation() == 1) ;
		samplesPerPixel = dHR.getSamplesPerPixel()  ;
		this.pixData 	= dHR.getPixels();	// It throws the exception .
		ignoreNegValues = true  ; 			// How do you know when ?
		samplesPerPixel = dHR.getSamplesPerPixel()  ;
		numberOfFrames	= dHR.getNumberOfFrames() ;
		//dbg("Number of Frames " + numberOfFrames) ;
	}// endofConstructor


	public DicomReader(byte[] array )throws java.io.IOException{
		this(new DicomHeaderReader(array));
	}
	
	
	public DicomReader(URL url)throws java.io.IOException { 
		
		URLConnection u = url.openConnection();
		int size 		= u.getContentLength() ;	
		byte[] array 	= new byte[size];
		int bytes_read 	= 0;
		DataInputStream  in = new DataInputStream(u.getInputStream()) ;
		while(bytes_read < size){	
			bytes_read += in.read(array, bytes_read, size - bytes_read);
					}//endwhile
		in.close();
		
		this.dHR                        = new DicomHeaderReader(array);
		h 				= dHR.getRows() ;
		w 				= dHR.getColumns() ;
		highBit 		        = dHR.getHighBit() ;
		bitsStored 		        = dHR.getBitStored() ;
		bitsAllocated	                = dHR.getBitAllocated();
		n 				= (bitsAllocated/8) 	;// = 1 or 2 
		signed 			        = (dHR.getPixelRepresentation() == 1) ;
		this.pixData 	                =  dHR.getPixels();	// It throws the exception .
		ignoreNegValues                 = true  ; 			// How do you when ?/*
		samplesPerPixel                 = dHR.getSamplesPerPixel()  ;
		numberOfFrames	                = dHR.getNumberOfFrames() ;
		//dbg("Number of Frames " + numberOfFrames) ;
	}
		
		
	public DicomReader( byte[] pixels,
						int w,
						int h,
						int highBit,	
						int bitsStored,
						int bitsAllocated,
						boolean signed,
						int samplesPerPixel ,
						int numberOfFrames,
						boolean ignoreNegValues ){
	
		this.h 				= 	h;
		this.w 				= 	w;
		this.highBit 		        = 	highBit;
		this.bitsStored 	        = 	bitsStored ;
		this.bitsAllocated	        = 	bitsAllocated;
		this.n 				= 	bitsAllocated/8 	;// = 1 or 2 
		this.signed 		        = 	signed ;
		this.pixData 		        =  	pixels ;	// It throws the exception .
		this.ignoreNegValues            =       ignoreNegValues  ; 			// How do you when ?/*
		this.samplesPerPixel            =       samplesPerPixel  ;
		this.numberOfFrames	        =       numberOfFrames 	;
	
	}
//////////////////////////////////////////////////////////////////////////////////////////////
	
	public DicomHeaderReader  getDicomHeaderReader() { return dHR; }
	
	public int getNumberOfFrames(){ return numberOfFrames ;}	
	
	public String[]    getInfos(){  return dHR.getInfo() ; }
	
	public byte[] getPixels( ){ return pixData ; }

	public int getWidth(){ return w ; }
	public int getHeight(){ return h ; }

/**
*	Returns the frame's pixels converted to Hounsfield Units (or, more generally,
*	the modality-rescaled value : stored*RescaleSlope + RescaleIntercept), kept
*	at full 16-bit precision. This is the data a real window/level transform must
*	work on — NOT the 8-bit min/max-stretched image getImage() produces.
*	@param frame 1-based frame index.
*/
	public short[] getHU(int frame) throws IOException{
		byte[] pd = dHR.getPixels(frame) ;
		return decodeHU(pd, dHR.getRescaleSlope(), dHR.getRescaleIntercept()) ;
	}

	private short[] decodeHU(byte[] pd, double slope, double intercept){
		int len = w * h ;
		short[] hu = new short[len] ;
		if(n == 1){					// 8 bits / pixel
			for(int i = 0 ; i < len && i < pd.length ; i++){
				int s = pd[i] & 0xff ;
				hu[i] = clampShort(s * slope + intercept) ;
			}
		} else {					// 16 bits / pixel
			for(int i = 0 ; i < len ; i++){
				int idx = 2 * i ;
				if(idx + 1 >= pd.length) break ;
				int raw ;
				if(highBit >= 8) raw = ((pd[idx+1] & 0xff) << 8) | (pd[idx] & 0xff) ;   // little-endian
				else             raw = ((pd[idx]   & 0xff) << 8) | (pd[idx+1] & 0xff) ; // big-endian
				int s = signed ? (int)(short)raw : (raw & 0xffff) ;
				hu[i] = clampShort(s * slope + intercept) ;
			}
		}
		return hu ;
	}

	private static short clampShort(double v){
		long r = Math.round(v) ;
		if(r > Short.MAX_VALUE) r = Short.MAX_VALUE ;
		if(r < Short.MIN_VALUE) r = Short.MIN_VALUE ;
		return (short)r ;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////
/** method getImage()  uses the Toolkit to create a 256 shades of gray image  */
	public Image getImage(){
		/* 
		* 	No more size limit! machines are fast from now on !
		if (w > 2048){ //make a size limit
			dbg(" w > 2048 " + "  width  : "+w+ "   height  : "+h) ;
		 	return scaleImage() ;
		 }		
		*/
		ColorModel cm = grayColorModel() ;
		dbg("  width  : "+w+ "   height  : "+h) ;
		if( n == 1){// in case it's a  8 bit/pixel image 
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h,cm, pixData, 0, w));
		
		}//endif
		
		
		else if ( !signed) {	 
			dbg(" not signed: ") ;
			
			byte[] destPixels = to8PerPix( pixData) ;
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h,cm,destPixels, 0, w));
		}			
		
		else if (signed ){	
			
			byte[] destPixels =	signedTo8PerPix( pixData) ;	
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h,cm,destPixels, 0, w));
		}
		
		else return null ;
	}// end of getImage .
	
	

///////////////////////////////////////////////////////////////////////

	public Image[]	getImages() throws IOException{
		Image[] images = new Image[numberOfFrames ] ;
		for( int  i = 1 ; i <= numberOfFrames ; i++ ){
			pixData = dHR.getPixels(i);
			images[i-1] = getImage() ;
		}
		return images ;
	}
////////////////////////////////////////////////////////////////////////
	protected Image scaleImage(){
		ColorModel cm = grayColorModel() ;
		int scaledWidth = w/2 ;
		int scaledHeight = h/2 ;
		int index =0 ;
		int value = 0 ;
		byte[] destPixels = null;
		System.gc() ;

	//scales the pixels
		if(n ==1 ){//1 byte/pixel
			destPixels = new byte[scaledWidth * scaledHeight];
			for(int i = 0 ; i<h ; i+=2){
				for(int j = 0 ; j<w ; j+=2){
					destPixels[index++] = pixData[(i*w) + j] ;
				}
			}	
			pixData = null;// should be replace by flush
			//Tools.gc("PIXDATA == NULL");
			
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w/2, h/2,cm, destPixels, 0, w/2));
		}	//endIf(n==1)
			// suppose n == 2 and unsigned value 
		else if(n==2 && bitsStored<=8){//Special case for Philips : here we don't scale
			dbg("w =   "+ w + "  h ==  "+ h);
			dbg("PixData.length = "+ pixData.length );
			dbg(" h * w  =  " + ( h * w )) ;
			destPixels = new byte[w * h] ;
			int len = w * h;
			for (int i = 0 ; i< len ; i++ ){
					value =	(int)(pixData[i*2])&0xff ;
					destPixels[i] = (byte)value;
			
			}//END FOR
			pixData = null;
			//Tools.gc("PIXDATA == NULL");
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h,cm, destPixels, 0, w));

		}//end Elseif
		
		else if(!signed){
			int[] intPixels = new int[scaledWidth * scaledHeight] ;
			dbg(" !signed");
			int maxValue = 0;
			int minValue = 0xffff ;
				if(highBit >= 8){
				for(int i = 0 ; i<h ; i+=2){
					for(int j = 0 ; j<w ; j+=2){
					value = ((int)( pixData[(2*(i*w+j))+1] & 0xff )<<8)| (int)( pixData[2*(i*w+j)] & 0xff)  ;
					if(value>maxValue) maxValue = value;
					if(value< minValue) minValue = value;
						intPixels[index++] = value ;
					}
				}
			
			}//endif
			int scale = maxValue-minValue ;
				if( scale == 0 ) scale = 1;
			pixData = null;
			//Tools.gc( "pix Data null") ;
			destPixels = new byte[scaledWidth * scaledHeight];
			for (int i =0 ; i< intPixels.length ; i++){
				value = (intPixels[i] - minValue )*256;
				value /=scale ;
				destPixels[i] = (byte)(value&0xff);
				}
			intPixels = null ;
			//Tools.gc("iNTPixels ==null") ;
			return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w/2, h/2,cm, destPixels, 0, w/2));
		}//endElseif(!signed...)
		else if(signed){
			byte[] pixels = signedTo8PerPix(pixData) ;
			pixData = pixels;
			for(int i = 0 ; i<h ; i+=2){
				for(int j = 0 ; j<w ; j+=2){
					destPixels[index++] = pixData[(i*w) + j] ;
				}
			}	
		pixData = null;
		//Tools.gc( "pixData = null") ;
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w/2, h/2,cm, destPixels, 0, w/2));
		}//endIfSigned
				
	return null ;
	}
	

	
////////////////////////////////////////////////////////////////////////////////////////////
	private byte[] to8PerPix(byte[] pixData){
	// suppose n == 2 and unsigned value 
	if(bitsStored<=8){//Special case for Philips
			dbg("w =   "+ w + "  h ==  "+ h);
			dbg("PixData.length = "+ pixData.length );
			dbg(" h * w  =  " + ( h * w )) ;
			byte[] destPixels = new byte[w * h] ;
			int len = w * h;
			int value = 0;
			for (int i = 0 ; i< len ; i++ ){
					value =	(int)(pixData[i*2])&0xff ;
					destPixels[i] = (byte)value;
			}
		return destPixels ;	
		}//
		
		int[] pixels = new int[w*h] ;
		int value =0;
		int msb =0 ; 
		int lsb =0 ;
		// case littleEndian or highBit  and unsigned ;
		if(highBit >= 8){
			
			int maxsb = 1 ;
			for(int i=1 ; i<= (highBit - 7) ; i++) maxsb= maxsb*2; 
			
			dbg(" Mask:" + maxsb +" / Highbit: " + highBit );	
			
			for( int i = 0 ; i< pixels.length ; i++){
	//			value = ((int)( pixData[(2*i)+1] & 0xff )<< 8)| (int)( pixData[(2*i)] & 0xff)  ;//msb first !
				msb = Math.min( maxsb, (int)( pixData[(2*i)+1] & 0xff )  )  ; //* 256  +   
				lsb =  (int)( pixData[(2*i)] & 0xff)   ;//msb first !
				
				pixels[i] = msb*256 + lsb  ;
			}
		}
		// case bigEndian and unsigned :
		else if( highBit <= 7){
			dbg("DicomReader.to8PerPix highBit == 7 ");
			for( int i = 0 ; i< pixels.length ; i++){	
				value = ((int)( pixData[(2*i)] & 0xff )<<8)| (int)( pixData[(2*i)+1] & 0xff)  ;//lsb first !
				pixels[i] = value ;
			}
		}
		//look for the Max value	 and minValue	
			int maxValue = 0;
			int minValue = 0xffff;
//			int[] grayScale = new int[256];
//			for (int i=0; i<256 ; i++) grayScale[i] = 0 ;
	 		for ( int i = 0 ; i < pixels.length ; i++){
				if ( pixels[i] > maxValue) maxValue = pixels[i] ;
				if ( pixels[i] < minValue ) minValue = pixels[i] ;
//				grayScale[pixels[i]] ++ ;
			}
			
			dbg( " minValue: "+ minValue +"; maxValue: " + maxValue );
//			for (int i=0; i<256 ; i++) 
//				if(grayScale[i]>0) System.out.println( "i : "+ i +"\t" + grayScale[i] ) ;
	 		
		
		// setUp a new grayScale :
			int scale = maxValue - minValue ;
			if(scale == 0) {
				scale =1 ;
				System.out.println("DicomReader.to8PerPix :scale == error ");
			}
			byte[] destPixels = new byte[w * h] ;
			for (int i = 0 ; i < pixels.length ; i++ ){
				value = ((pixels[i] - minValue )*255 ) /scale ;
				destPixels[i] = (byte)(value&0xff);
				//pixels[i] = (255<<24)|( value<<16)|(value<<8)| value ;
			}
		
			return destPixels ;	
						
	}//endOfMethod to8PerPix
////////////////////////////////////////////////////////////////////////////////////////////
		private  byte[] signedTo8PerPix	(byte[] pixData){
		int[] pixels = new int[w * h] ;
		short shValue = 0 ;// dont forget the SIGNED value !!!
		int value = 0 ;
		// case signed and  littleEndian :
		if ( highBit >= 8 ){
			for (int i = 0 ; i < pixels.length ; i++ ){
			shValue = (short)((( pixData[(2*i)+1] & 0xff )<<8)| ( pixData[(2*i)] & 0xff) ) ;//msb first !
				value = (int ) shValue ;
				if(value<0 && ignoreNegValues ) value = 0 ;
				pixels[i] = value  ;
				}
		}
		// case signed and  bigEndian :
		if ( highBit <= 7 ){
			for (int i = 0 ; i < pixels.length ; i++ ){
				shValue = (short)((( pixData[(2*i)+1] & 0xff )<<8)| ( pixData[(2*i)] & 0xff) ) ;//msb first !
				value = (int) shValue ;
				if(value<0 && ignoreNegValues ) value = 0 ;
				pixels[i] = value  ;
				}
			}
		//look for the Max value	 and minValue	
		int maxValue = 0;
		int minValue = 0xffff;
 		for ( int i = 0 ; i < pixels.length ; i++){
			if ( pixels[i] > maxValue) maxValue = pixels[i] ;
			if ( pixels[i] < minValue ) minValue = pixels[i] ;
			
		}
		byte[] destPixels = new byte[w*h] ;
		int scale = maxValue - minValue ;
			if(scale == 0){ scale = 1 ; System.out.println(" Error in VR form SignedTo8..DicomReader");}
		for (int i = 0 ; i < pixels.length ; i++ ){
			value = ((pixels[i] - minValue )*255) /scale ;
			//pixels[i] = (255<<24)|( value<<16)|(value<<8)| value ;
			destPixels[i] =(byte)(value&0xff) ;
		}		
		return destPixels ;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
	protected  ColorModel grayColorModel(){
		byte[] r = new byte[256] ;
		for (int i = 0; i <256 ; i++ )
			r[i] = (byte)(i & 0xff ) ;
	return (new IndexColorModel(8,256,r,r,r));
	}

/////////////////////////////////////////////////////////////////////////////////////////////
	public void flush(){ 
		pixData = null ;
		System.gc();
		System.gc();
	}
	void dbg(String s){
		 if(DEBUG) System.out.println(this.getClass().getName() + s);
		}
}// end of class .
