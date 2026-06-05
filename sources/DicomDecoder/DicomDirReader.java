/**
* A simple way to use it  ... 
*  If  you want to retrieve an image :
* <pre>
//First instanciate a DicomReader 
	import DicomDecoder.* ;
	...
	DicomReader dr = new DicomReader( the_dicom_url_you_want_to_fetch) ;
	// then fetch the image !
	java.awt.image img = dr.getimage();  // get the image !
	
	//to get some more information you need to get a dicomheaderreader :	
	DicomHeaderReader dhr = dr.getDicomHeaderReader() ;
	string  manufacturer	= getaString (0x0008,0x0070); // retrieve a string 
	int width = getAnInt(0x0028, 0x0010, index )  ; 
	
*</pre>
*	modification:23/03/01
**/
	package DicomDecoder ;
	import java.awt.*;
	import java.awt.image.* ;
	import java.util.*;
	import java.net.* ;
	import java.io.*;
	
	
public class DicomDirReader{
	public static final int _ImplicitVRLittleEndian = 0 ;
	// if the VR is explicit : the tag is > 0
	public static final int _ExplicitVRLittleEndian = 1 ;
	public static final int _ExplicitVRBigEndian = 2 ;
	public static final int _ImplicitVRBigEndian = -2 ; //this one should not exists !
	public static final int _JPEGCompression = 10 ;
	public static final int _RLECompression = 20 ;
	public static final int _notUnderstood = -1000 ;
	private String VRString = "default implicitVR little endian";
	private int VR	= _ExplicitVRLittleEndian ;  //=0, default value representation 
	
	byte[]  array ; 
	Map  dictionary  ; 

	public DicomDirReader(URL url)throws java.io.IOException { 
		this.dictionary  = new DicomDictionary() . getDictionary() ; 	
		
		URLConnection    u              = url.openConnection();
		int              size           = u.getContentLength() ;	
		this            .array          = new byte[size];
		int              bytes_read     = 0;
		DataInputStream  in             = new DataInputStream(u.getInputStream()) ;

		while(bytes_read < size){	
			bytes_read += in.read(array, bytes_read, size - bytes_read);
		}//endwhile
		in.close();
		System.out.println("Size :"+ size + " bytes\n") ; 
		
	}


	public boolean existsPreamble(byte[] array){
		return ( array[128]=='D' && array[129]=='I' && array[130]== 'C' && array[131]=='M') ; 
	}

	public void readDicomdir(){
		int offset = 0 ; 	
		boolean p = this.existsPreamble( this.array)	 ; 
		System.out.println( "128 bytes Dicom preamble followed by DICM :" + p ) ; 
		if(p){
			offset=132 ; 
		}
		readDicom( offset, array, array.length ) ;
	}

/**

	private static final int AE=0x4145, AS=0x4153, AT=0x4154, CS=0x4353, DA=0x4441, DS=0x4453, DT=0x4454,
		FD=0x4644, FL=0x464C, IS=0x4953, LO=0x4C4F, LT=0x4C54, PN=0x504E, SH=0x5348, SL=0x534C, 
		SS=0x5353, ST=0x5354, TM=0x544D, UI=0x5549, UL=0x554C, US=0x5553, UT=0x5554,
		OB=0x4F42, OW=0x4F57, SQ=0x5351, UN=0x554E, QQ=0x3F3F;
		
**/ 
	public int  readDicom( int offset, byte[] data , int length  ){
		while (offset <  length){ 
			String tag = readTag(offset) ; 
			offset +=4 ; 

			String  val = (String) dictionary.get(tag) ; 
			
			VRLength vrl  = readVRMessageLength(data, offset ) ; 
			System.out.print("Position: "
				+(offset-4)+ " Tag " + tag + "  [ "+val+ " ] Length:" 
				+ vrl.length + "bytes,"+ vrl.VR +" skip: " + vrl.skip + " :") ; 

			if( vrl.VR .equals("UL") ){
				int ul = readInt32( data, offset + vrl.skip  ) ;
				System.out.println( "     ul=" + ul )  ;
				
			}else if( vrl.VR .equals("US") ){
				int sh = readInt16( data, offset + vrl.skip +2  ) ;
				System.out.println( "     sh=" + sh )  ;
				
			}
			else if(vrl.VR.equals("SQ")) {
				readDicom( offset + vrl.skip, data ,  offset+vrl.skip + vrl.length ); 

			} 
			else if( vrl.length < 250){ 
				System.out.println( readString( data, offset , vrl ))  ;
			}
			offset +=vrl.skip + vrl.length ; 
		}
		return offset ; 
	}

	public String readString(byte[] data, int pos , VRLength vrl ){
		char[] result = new char[vrl.length ];
		for (int i = 0; i < vrl.length ;i++)
			result[i] = (char) data[pos +vrl.skip+ i ];

		return  new String(result) ; 
	}
	
	public String  readTag(int pos){
		String.format("%02X", array[pos +1]) ;
		String groupElement = String.format("%02X", array[pos +1]) + String.format("%02X", array[pos]) ;
		pos +=2 ;  
		String dataElement = String.format("%02X", array[pos +1]) + String.format("%02X", array[pos]) ;
		pos +=2 ;  
		return   groupElement + dataElement  ; 
	}
	
	public VRLength   readVRMessageLength(byte[] data, int pos){
		if(VR == _ImplicitVRLittleEndian ){
			return new VRLength("", readInt32(data, pos) , 4 )  ;
		}
		// case  explicit VR with of OB OW SQ or UN 
		String VRTypeOf = new String(new byte[]{data[pos],data[pos+1]});	
		//System.out.println ("VR type of : "+ VRTypeOf );

		if( VRTypeOf.equals("OB")| VRTypeOf.equals("OW")| VRTypeOf.equals("SQ")| VRTypeOf.equals("UN")){
			// bytesFromTagToValue = 12;
			//return readInt32(tagPos + 8 );
			return new VRLength(VRTypeOf , readInt32(data, pos+4) , 8 )  ;
		}
	 	// case  explicit VR with value other than OB OW SQ or UN 
	 	else{
	 		//bytesFromTagToValue = 8 ;
			return new VRLength(VRTypeOf , readInt16(data, pos+2) , 4 )  ;
		}
	}

	private int readInt32(byte[] data, int i){
		int i0 = 	(int) (data[i ] & 0xff);
	 	int i1 =	(int) (data[i + 1 ] &0xff);
	 	int i2 =	(int) (data[i + 2 ] &0xff);
	 	int i3 =	(int) (data[i + 3 ] &0xff);
		return i3<<24| i2<<16 |i1<< 8|i0 ;	
	}


	private int readInt16( byte[] data, int i ){
	 	int  i1 = data[i+1]&0xff ;
	 	int  i0 = data[i]&0xff ;	
	 	int anInt =  i1<<8|i0 ;
		//case BE swap bytes :
	 	if (anInt < -1){ 
			anInt= (int)(data[i]*256)&0xff + data[i+1]&0xff ;
	 	  	System.out.println ("Byte swapped at readInt16 :" + anInt) ;
	 	}
		return anInt ;
	}

	public static void main(String[] args) throws Exception {
		
		DicomDirReader  ddr = new DicomDirReader( new URL(args[0])) ; 
		ddr.readDicomdir() ; 
	}
	
	public class VRLength{
		String VR ; 
		int length ; 
		int skip ; 
		public VRLength(String VR , int length , int offsetToValue ){
			this.VR = VR ; 
			this.length = length ; 
			this.skip = offsetToValue ; 
		}
	}

}


	 		
