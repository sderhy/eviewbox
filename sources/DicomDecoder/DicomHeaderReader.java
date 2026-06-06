/**
* @date 15 jun 97
* Copyright 1997 Serge Derhy
*
* Dicom Decoder package was written and released by :
* <center>
*                                  serge derhy	<br>
*                                serge  at derhy.com<br>
* </center>
* @author <a href="http://www.derhy.com/dicom/index.html">serge derhy</a>
* Do whatever you want with this code...
*
* This package is provided WITHOUT ANY WARRANTY either expressed or implied.<br>
* Last modification: 2026 - single-pass, length-aware parser.
*****************************************************************************/
/**
*	This class reads the header of a DICOM file.
*
*	The original implementation searched the whole byte array from offset 0
*	for every tag it needed (a brute-force pattern match repeated once per
*	tag). This version walks the dataset ONCE, element by element, using the
*	Value Length announced by each element to jump directly to the next one.
*	Every element found is recorded in a map keyed by (group << 16 | element);
*	the getters then resolve in O(1) instead of rescanning the file.
*
*	The public API is unchanged, so DicomReader and the rest of the
*	application keep working without modification.
*/

package DicomDecoder ;
import java.awt.* ;
import java.util.*;
import java.io.* ;


public  class DicomHeaderReader{
	byte[] data ;
	int index  ;
	private int dataLength;
	private static  final boolean DEBUG = false ;
	private int bytesFromTagToValue ;// kept for API compatibility
	public static final int MAX_HEADER_SIZE = 10000 ;
	private int maxHeaderSize  ;
	public static final String  ImplicitVRLittleEndian 	= "1.2.840.10008.1.2" 	;
	public static final String  ExplicitVRLittleEndian 	= "1.2.840.10008.1.2.1" 	;
	public static final String  ExplicitVRBigEndian 	= "1.2.840.10008.1.2.2" 	;
	public static final String  JPEGCompression 		= "1.2.840.10008.1.2.4." 	;
	public static final String  RLECompression 	        = "1.2.840.10008.1.2.5" 	;

	public static final int _ImplicitVRLittleEndian = 0 ;
	// if the VR is explicit : the tag is > 0
	public static final int _ExplicitVRLittleEndian = 1 ;
	public static final int _ExplicitVRBigEndian = 2 ;
	public static final int _ImplicitVRBigEndian = -2 ; //this one should not exists !
	public static final int _JPEGCompression = 10 ;
	public static final int _RLECompression = 20 ;
	public static final int _notUnderstood = -1000 ;

	private  boolean oneSamplePerPixel = true ;
	private  boolean oneFramePerFile   = true ;
	private int errorDetector = -1 ;

	private String VRString = "default implicitVR little endian";
	private String transfertSyntaxUID	= "" ;//	0x0002,0x0010
	private String imageType		= "unknown" ;//	0x0008,0x0008
	private String studyDate		= "unknown" ;//	0x0008,0x0030
	private String modality	          	= "unknown" ;// 0x0008,0x0060
	private String manufacturer	        = "unknown" ;//	0x0008,0x0070
	private String	institution	        = "unknown" ;//	0x0008,0x0080
	private String physician	        = "unknown" ;//	0x0008,0x0090
	private String patientName		= "unknown" ;//	0x0010,0x0010
		private String patientID		= "unknown" ;//	0x0010,0x0020
		private String patientBirthdate         = "unknown" ;//	0x0010,0x0030
		private String sex			= "unknown" ;//	0x0010,0x0040
		private String sliceThickness		= "Unknown" ;//	0x0018,0x0050
		private String spacingBetweenSlices	= "Unknown" ;//	0x0018,0x0088
		private String sliceLocation		= "Unknown" ;//	0x0020,0x1041
		private String pixelSpacing		= "Unknown" ;//	0x0028,0x0030

	private int numberOfFrames		=  1;	// 0x0028,0x0008 //IS : integer string
	private int samplesPerPixel 	        =  1;	// 0x0028,0x0002
	private int h 				= -1; 	// 0x0028,0x0010
	private int w 				= -1;	// 0x0028, 0x0011
	private int bitsAllocated		= -1;	// 0x0028, 0x0100
	private int bitsStored 			= -1;	// 0x0028, 0x0101
	private int highBit 			= -1; 	// 0x0028, 0x0102
	private int signed 			= -1;	// 0x0028, 0x0103(pixelRepresentation )
	private int size 			= -1;	// 0x7Fe0, 0x0010
	private int n 				= -1;	// = 1 or 2
	private int VR				= _ImplicitVRLittleEndian;  //=0, default value representation

	private boolean bE                      = false ;// bigEndian

	// ---- single-pass parser state ---------------------------------------
	/** One DICOM element: where its value lives and how it is encoded. */
	private static final class Element {
		final int vr;        // two VR chars packed in an int, 0 if implicit
		final int valuePos;  // offset of the value bytes in data[]
		final int valueLen;  // value length in bytes (-1 if undefined)
		Element(int vr, int valuePos, int valueLen){
			this.vr = vr; this.valuePos = valuePos; this.valueLen = valueLen;
		}
	}
	private final HashMap<Integer,Element> elements = new HashMap<Integer,Element>();
	private int pixelDataPos    = -1;   // offset of pixel data value
	private int pixelDataLength = -1;   // declared length (-1 if undefined / encapsulated)
	private boolean explicitVR  = false;

	private static final long UNDEFINED_LENGTH = 0xFFFFFFFFL;

	// Lazily-built tag-name dictionary (group+element hex -> name), shared.
	private static Map<String,String> DICT = null;
	private static synchronized Map<String,String> dict(){
		if(DICT == null){
			DICT = new HashMap<String,String>();
			Map raw = new DicomDictionary().getDictionary();
			java.util.Iterator it = raw.keySet().iterator();
			while(it.hasNext()){
				String k = (String) it.next();
				DICT.put(k.toUpperCase(), (String) raw.get(k));
			}
		}
		return DICT;
	}

	private static int key(int group, int element){
		return (group << 16) | (element & 0xffff);
	}


/**
*	There is only one main constructor, it needs an array of byte,
*	this array is the DICOM file you want to read.
*/
	public DicomHeaderReader ( byte [] dicomArray ){
		this.data = dicomArray ;
		dataLength = (data == null) ? 0 : data.length ;
		index = 0;
		initHeaderSize() ;
		parse() ;            // walk the dataset once
		getVR() ;            // resolve the transfer syntax label
		getEssentialData() ; // fill the legacy fields from the map
	}
/**
 *	Simpler constructor, kept to use some methods of DHR without parsing.
 **/
	public DicomHeaderReader ( byte [] dicomArray , boolean minimum ){
		this.data = dicomArray ;
		dataLength = (data == null) ? 0 : data.length ;
		index = 0;
	}

	protected void initHeaderSize(){
		maxHeaderSize = MAX_HEADER_SIZE * 3 ;
		if(maxHeaderSize > dataLength) maxHeaderSize = dataLength ;
	}

// ===================================================================
//  SINGLE-PASS PARSER
// ===================================================================
/**
*	Walk the byte array element by element. For each element we read its
*	header (group, element, VR if explicit, value length) and then jump
*	forward by the announced value length to reach the next element.
*	Tolerant of truncated input: stops cleanly at the end of the array,
*	which is what FileLister relies on (it passes a 10 kB slice).
*/
	private void parse(){
		if(data == null || dataLength < 8) return ;

		int p = 0;

		// DICOM Part-10 preamble: 128 bytes then the "DICM" magic.
		if(dataLength >= 132 &&
		   data[128]=='D' && data[129]=='I' && data[130]=='C' && data[131]=='M'){
			p = 132;
			// The File Meta group (0002,xxxx) is ALWAYS Explicit VR Little Endian.
			boolean savedExplicit = explicitVR; boolean savedBE = bE;
			explicitVR = true; bE = false;
			p = walk(p, true);   // parse only the meta group
			explicitVR = savedExplicit; bE = savedBE;
			// Decide the encoding of the dataset from the transfer syntax.
			deriveEncoding(rawString(key(0x0002,0x0010)));
		} else {
			// No preamble (legacy "detached" dataset): sniff the first element.
			detectEncoding(p);
		}

		// Main dataset.
		walk(p, false);
	}

/**
*	Core element walk. When metaOnly is true we stop as soon as we leave
*	group 0x0002. Returns the offset where parsing stopped.
*/
	private int walk(int p, boolean metaOnly){
		while(p + 8 <= dataLength){
			int group   = u16(p);
			int element = u16(p + 2);
			if(metaOnly && group != 0x0002) return p;   // hand over to the dataset pass
			p += 4;

			// Item / delimitation tags (FFFE,xxxx) never carry a VR.
			boolean isItemTag = (group == 0xFFFE);

			int vr = 0;
			long len;
			if(explicitVR && !isItemTag){
				if(p + 2 > dataLength) break;
				int c1 = data[p] & 0xff, c2 = data[p+1] & 0xff;
				vr = (c1 << 8) | c2;
				p += 2;
				if(isLongFormVR(vr)){
					p += 2;                 // 2 reserved bytes
					if(p + 4 > dataLength) break;
					len = u32(p); p += 4;
				} else {
					if(p + 2 > dataLength) break;
					len = u16(p); p += 2;
				}
			} else {
				if(p + 4 > dataLength) break;
				len = u32(p); p += 4;       // implicit VR: 32-bit length
			}

			int valuePos = p;

			// Pixel data: record it, then skip its value so any element that
			// follows the image is still captured (full-header parse).
			if(group == 0x7Fe0 && element == 0x0010){
				pixelDataPos = valuePos;
				pixelDataLength = (len == UNDEFINED_LENGTH) ? -1 : (int) len;
				elements.put(Integer.valueOf(key(group,element)),
				             new Element(vr, valuePos, pixelDataLength));
				if(len == UNDEFINED_LENGTH){
					p = skipUndefinedLength(valuePos);   // encapsulated fragments
				} else {
					long np = valuePos + len;
					if(np > dataLength || np < valuePos) break;
					p = (int) np;
				}
				continue;
			}

			if(len == UNDEFINED_LENGTH){
				// Sequence (or item) of undefined length: record the tag, then
				// skip its content up to the matching delimitation item.
				elements.put(Integer.valueOf(key(group,element)),
				             new Element(vr, valuePos, -1));
				p = skipUndefinedLength(valuePos);
			} else {
				int l = (int) len;
				if(l < 0 || valuePos + (long) l > dataLength){
					// Truncated or implausible length: record what we can and stop.
					int avail = dataLength - valuePos;
					if(avail < 0) avail = 0;
					elements.put(Integer.valueOf(key(group,element)),
					             new Element(vr, valuePos, avail));
					break;
				}
				elements.put(Integer.valueOf(key(group,element)),
				             new Element(vr, valuePos, l));
				p = valuePos + l;
			}
		}
		return p;
	}

/**
*	Skip the content of an undefined-length sequence. Items inside are
*	(FFFE,E000) with their own length (possibly undefined -> recurse); the
*	sequence ends at (FFFE,E0DD). Returns the offset just past the end.
*/
	private int skipUndefinedLength(int p){
		while(p + 8 <= dataLength){
			int group   = u16(p);
			int element = u16(p + 2);
			long len    = u32(p + 4);   // Item / delimitation tags carry no VR.
			p += 8;
			if(group == 0xFFFE && element == 0xE0DD) return p;   // sequence end
			if(group == 0xFFFE && element == 0xE000){            // item
				if(len == UNDEFINED_LENGTH){
					p = skipItem(p);     // undefined-length item: walk its content
				} else {
					long np = p + len;   // defined-length item: jump over it
					if(np > dataLength || np < p) return dataLength;
					p = (int) np;
				}
			} else {
				// Not an item where one was expected: bail out tolerantly.
				return dataLength;
			}
		}
		return dataLength;
	}

/**
*	Walk the content of an undefined-length item, element by element and
*	VR-aware, until the Item Delimitation (FFFE,E00D). The dataset inside an
*	item uses the SAME VR encoding as the enclosing dataset, so it cannot be
*	skipped with the implicit-VR shortcut used for items themselves.
*/
	private int skipItem(int p){
		while(p + 8 <= dataLength){
			int group   = u16(p);
			int element = u16(p + 2);
			if(group == 0xFFFE && element == 0xE00D) return p + 8;   // item end (+len 0)
			int np = skipElement(p);
			if(np <= p) return dataLength;   // malformed: stop cleanly
			p = np;
		}
		return dataLength;
	}

/**
*	Skip a single data element (VR-aware) and return the offset of the next
*	element. Recurses through nested undefined-length sequences. Returns -1
*	on malformed / truncated input.
*/
	private int skipElement(int p){
		if(p + 8 > dataLength) return -1;
		int group = u16(p);
		boolean isItemTag = (group == 0xFFFE);
		p += 4;

		int vr = 0;
		long len;
		if(explicitVR && !isItemTag){
			if(p + 2 > dataLength) return -1;
			int c1 = data[p] & 0xff, c2 = data[p+1] & 0xff;
			vr = (c1 << 8) | c2;
			p += 2;
			if(isLongFormVR(vr)){
				p += 2;                          // 2 reserved bytes
				if(p + 4 > dataLength) return -1;
				len = u32(p); p += 4;
			} else {
				if(p + 2 > dataLength) return -1;
				len = u16(p); p += 2;
			}
		} else {
			if(p + 4 > dataLength) return -1;
			len = u32(p); p += 4;
		}

		if(len == UNDEFINED_LENGTH) return skipUndefinedLength(p);
		long np = p + len;
		if(np > dataLength || np < p) return -1;
		return (int) np;
	}

	private static boolean isLongFormVR(int vr){
		// VRs encoded as VR(2) + reserved(2) + length(4).
		switch(vr){
			case ('O'<<8)|'B': // OB
			case ('O'<<8)|'W': // OW
			case ('O'<<8)|'F': // OF
			case ('O'<<8)|'D': // OD
			case ('O'<<8)|'L': // OL
			case ('S'<<8)|'Q': // SQ
			case ('U'<<8)|'T': // UT
			case ('U'<<8)|'N': // UN
			case ('U'<<8)|'C': // UC
			case ('U'<<8)|'R': // UR
				return true;
			default:
				return false;
		}
	}

/**	Guess the encoding of a preamble-less dataset by sniffing the 1st tag. */
	private void detectEncoding(int p){
		bE = false;                       // legacy default: little endian
		explicitVR = false;
		if(p + 6 <= dataLength){
			int c1 = data[p+4] & 0xff, c2 = data[p+5] & 0xff;
			// Two upper-case ASCII letters at the VR slot => explicit VR.
			if(c1 >= 'A' && c1 <= 'Z' && c2 >= 'A' && c2 <= 'Z') explicitVR = true;
		}
	}

/**	Set explicitVR / bE from the transfer syntax UID of the meta group. */
	private void deriveEncoding(String uid){
		if(uid == null){ explicitVR = false; bE = false; return; }
		uid = uid.trim();
		if(uid.equals(ExplicitVRLittleEndian)){ explicitVR = true;  bE = false; }
		else if(uid.equals(ExplicitVRBigEndian)){ explicitVR = true;  bE = true;  }
		else if(uid.startsWith(JPEGCompression)){ explicitVR = true;  bE = false; }
		else if(uid.startsWith(RLECompression)) { explicitVR = true;  bE = false; }
		else { explicitVR = false; bE = false; } // implicit VR little endian
	}

/**	Resolve the transfer syntax into the legacy VR enum + label. */
	protected void getVR(){
		transfertSyntaxUID = rawString(key(0x0002,0x0010));
		if(transfertSyntaxUID != null && !transfertSyntaxUID.equals("Unknown")){
			transfertSyntaxUID = transfertSyntaxUID.trim();
			if(transfertSyntaxUID.equals(ImplicitVRLittleEndian)) VR = _ImplicitVRLittleEndian;
			else if(transfertSyntaxUID.equals(ExplicitVRLittleEndian)) VR = _ExplicitVRLittleEndian;
			else if(transfertSyntaxUID.equals(ExplicitVRBigEndian)) VR = _ExplicitVRBigEndian;
			else if(transfertSyntaxUID.startsWith(JPEGCompression)) VR = _JPEGCompression;
			else if(transfertSyntaxUID.startsWith(RLECompression)) VR = _RLECompression;
			else VR = _notUnderstood;

			switch (VR){
				case _ImplicitVRLittleEndian : VRString = "ImplicitVRLittleEndian"; break;
				case _ExplicitVRLittleEndian : VRString = "ExplicitVRLittleEndian"; break;
				case _ExplicitVRBigEndian    : VRString = "ExplicitVRBigEndian"; break;
				case _JPEGCompression        : VRString = "JPEGCompression"; break;
				case _RLECompression         : VRString = "RLECompression"; break;
				case _notUnderstood          : VRString = "not understood"; break;
				default                      : VRString = "Something curious happened !";
			}
		} else {
			transfertSyntaxUID = "Transfer syntax UID tag not found";
			VRString = explicitVR ? "Explicit VR little endian (sniffed)"
			                      : "Default VR implicit little endian";
			if(VR == _ImplicitVRLittleEndian && explicitVR) VR = _ExplicitVRLittleEndian;
		}
	}

/**	Fill the legacy scalar/string fields from the parsed map. */
	protected void getEssentialData(){
		imageType            = getaString(0x0008,0x0008);
		studyDate            = getaString(0x0008,0x0020);
		modality             = getaString(0x0008,0x0060);
		manufacturer         = getaString(0x0008,0x0070);
		institution          = getaString(0x0008,0x0080);
		physician            = getaString(0x0008,0x0090);
		patientName          = getaString(0x0010,0x0010);
		patientID            = getaString(0x0010,0x0020);
		patientBirthdate     = getaString(0x0010,0x0030);
		sex                  = getaString(0x0010,0x0040);
		sliceThickness       = getaString(0x0018,0x0050);
		spacingBetweenSlices = getaString(0x0018,0x0088);
		sliceLocation        = getaString(0x0020,0x1041);
		pixelSpacing         = getaString(0x0028,0x0030);

		h = getAnInt(0x0028, 0x0010);
		w = getAnInt(0x0028, 0x0011);
		bitsAllocated = getAnInt(0x0028, 0x0100);
		bitsStored    = getAnInt(0x0028, 0x0101);
		highBit       = getAnInt(0x0028, 0x0102);
		signed        = getAnInt(0x0028, 0x0103);

		samplesPerPixel = getAnInt(0x0028, 0x0002);
		if(samplesPerPixel < 0 || samplesPerPixel > 3) samplesPerPixel = 1;
		oneSamplePerPixel = (samplesPerPixel == 1);

		try { numberOfFrames = Integer.parseInt(getaString(0x0028,0x0008).trim()); }
		catch(NumberFormatException nfe){ numberOfFrames = 1; }
		if(numberOfFrames < 1) numberOfFrames = 1;
		oneFramePerFile = (numberOfFrames == 1);

		size = (pixelDataLength >= 0) ? pixelDataLength : getFileDataLength();
		n = (bitsAllocated > 0) ? (bitsAllocated / 8) : -1 ;

		// Consistency check (kept for getPixelDataSize()).
		if(w > 0 && h > 0 && bitsAllocated > 0 && pixelDataLength > 0){
			int ba = (bitsAllocated % 8 == 0) ? bitsAllocated/8 : (bitsAllocated+8)/8;
			int tSize = samplesPerPixel * w * h * ba * numberOfFrames;
			errorDetector = pixelDataLength - tSize;
		} else {
			errorDetector = (pixelDataPos >= 0) ? 0 : -1;
		}

		debug("TransfertSyntaxUID : " + transfertSyntaxUID);
		debug("Value representation : " + VRString);
		debug("ImageType : " + imageType);
		debug("h=" + h + " w=" + w + " bitsAllocated=" + bitsAllocated
		      + " bitsStored=" + bitsStored + " highBit=" + highBit + " signed=" + signed);
	}

// ===================================================================
//  LOW-LEVEL READS (endian aware)
// ===================================================================
	private int u16(int i){
		int a = data[i] & 0xff, b = data[i+1] & 0xff;
		return bE ? ((a << 8) | b) : ((b << 8) | a);
	}
	private long u32(int i){
		long a = data[i] & 0xff, b = data[i+1] & 0xff,
		     c = data[i+2] & 0xff, d = data[i+3] & 0xff;
		return bE ? ((a<<24)|(b<<16)|(c<<8)|d) : ((d<<24)|(c<<16)|(b<<8)|a);
	}

	/** Raw value of an element as a String (no "Unknown" fallback handling). */
	private String rawString(int k){
		Element e = elements.get(Integer.valueOf(k));
		if(e == null) return "Unknown";
		int len = e.valueLen;
		if(len <= 0) return "Unknown";
		if(e.valuePos + len > dataLength) len = dataLength - e.valuePos;
		if(len <= 0) return "Unknown";
		if(len > 1024) len = 1024;                 // strings in the header are short
		StringBuffer sb = new StringBuffer(len);
		for(int i = 0; i < len; i++) sb.append((char)(data[e.valuePos + i] & 0xff));
		int end = sb.length();
		while(end > 0){
			char c = sb.charAt(end - 1);
			if(c == '\0' || c == ' ') end--; else break;
		}
		return sb.substring(0, end);
	}

// ===================================================================
//  PUBLIC ACCESSORS  (unchanged signatures)
// ===================================================================
	public String  getPatientName(){ 	return patientName; 		}
	public String  getPatientBirthdate(){ return patientBirthdate; 	}
	public String  getManufacturer(){ 	return manufacturer ;		}
	public String  getPatientID(){ 		return patientID 	; 		}
		public String  getImageType(){ 		return imageType; 			}
		public String  getStudyDate(){ 		return studyDate; 			}
		public String  getModality(){ 		return modality; 			}
		public String  getSliceThickness(){ 	return sliceThickness; 			}
		public String  getSpacingBetweenSlices(){ return spacingBetweenSlices; 		}
		public String  getSliceLocation(){ 	return sliceLocation; 			}
		public String  getPixelSpacing(){ 	return pixelSpacing; 			}
		public double getSliceThicknessValue(){ return parseDecimal(sliceThickness, 0); }
		public double getSpacingBetweenSlicesValue(){ return parseDecimal(spacingBetweenSlices, 0); }
		public double getSliceLocationValue(){ return parseDecimal(sliceLocation, 0); }
		public double getPixelSpacingRowValue(){ return parseDecimal(pixelSpacing, 0); }
		public double getPixelSpacingColumnValue(){ return parseDecimal(pixelSpacing, 1); }

/** Retrieves a String when you know the tags. */
	public String  getaString(int groupNumber, int elementNumber){
		return getaString( groupNumber, elementNumber, 0) ;
	}

	private String  getaString(int groupNumber, int elementNumber, int j ){
		// j is obsolete (was the scan start offset); kept for source compatibility.
		String s = rawString(key(groupNumber, elementNumber));
		return (s == null) ? "Unknown" : s;
	}

		private double parseDecimal(String value, int fieldIndex){
			if(value == null) return -1 ;
			value = value.trim();
			if(value.length() == 0 || value.equals("Unknown")) return -1 ;
			StringTokenizer tokenizer = new StringTokenizer(value, "\\");
			for(int i = 0; tokenizer.hasMoreTokens(); i++){
				String token = tokenizer.nextToken().trim();
				if(i == fieldIndex){
					try{
						return Double.valueOf(token).doubleValue();
					}catch(NumberFormatException nfe){
						return -1 ;
					}
				}
			}
			return -1 ;
		}

/**	Length declared by the pixel-data element (-1 if undefined/compressed). */
        public int  getFileDataLength(){
		Element e = elements.get(Integer.valueOf(key(0x7Fe0,0x0010)));
		return (e == null) ? -1 : e.valueLen ;
	}

/**
*	Returns the raw pixels of the DICOM image, provided it is uncompressed
*	and a known format. Throws IOException otherwise.
*/
	public byte[] getPixels() throws IOException{
		if (VR == _JPEGCompression)
			throw new IOException("DICOM JPEG compression not yet supported ") ;
		if (VR == _RLECompression)
			throw new IOException("DICOM RLE compression not yet supported ") ;

		int rows = getRows() ;
		if (rows == -1) throw new IOException("Format not recognized") ;
		int cols = getColumns();
		if (cols == -1) throw new IOException("Format not recognized") ;

		int ba = getBitAllocated();
		ba = (ba % 8 == 0) ? ba/8 : (ba+8)/8 ;
		int fileLength = rows * cols * ba ;

		int pos = pixelDataPos ;
		if(pos < 0) throw new IOException("Pixel data not found") ;
		if(pixelDataLength > 0 && pixelDataLength < fileLength) fileLength = pixelDataLength ;
		if(pos + fileLength > dataLength) fileLength = dataLength - pos ;
		if(fileLength <= 0) throw new IOException("Format not recognized") ;

		byte[] pixData = new byte[ fileLength ];
		System.arraycopy (data, pos, pixData, 0, fileLength );
		return pixData ;
	}

	public byte[] getPixels(int number) throws IOException{
		if( number > numberOfFrames ) throw new IOException( "Doesn't have such a frame ! ") ;
		if (VR == _JPEGCompression)
			throw new IOException("DICOM JPEG compression not yet supported ") ;
		if (VR == _RLECompression)
			throw new IOException("DICOM RLE compression not yet supported ") ;

		int rows = getRows() ;
		if (rows == -1) throw new IOException("Format not recognized") ;
		int cols = getColumns();
		if (cols == -1) throw new IOException("Format not recognized") ;

		int ba = getBitAllocated();
		ba = (ba % 8 == 0) ? ba/8 : (ba+8)/8 ;
		int frameLength = rows * cols * ba ;

		int pos = pixelDataPos + (number - 1) * frameLength ;
		if(pos < 0 || pos + frameLength > dataLength) throw new IOException("Frame out of range") ;

		byte[] pixData = new byte[ frameLength ];
		System.arraycopy (data, pos, pixData, 0, frameLength );
		return pixData ;
	}

	public String[] getInfo(){
			String [] info = new String[20];
		info[0]  = "Patient 's name              : " + getPatientName();
		info[1]  = "Patient 's ID                : " + getPatientID();
		info[2]  = "Patient 's birthdate         : " + getPatientBirthdate();
		info[3]  = "Patient 's sex               : " + sex;
		info[4]  = "Study Date                   : " + getStudyDate();
		info[5]  = "Modality                     : " + getModality();
		info[6]  = "Manufacturer                 : " + getManufacturer();
		info[7]  = "Number of frames             : " + getNumberOfFrames();
		info[8]  = "Width                        : " + getColumns();
		info[9]  = "Height                       : " + getRows();
		info[10] = "Bits allocated               : " + getBitAllocated();
		info[11] = "Bits stored                  : " + getBitStored();
		info[12] = "Sample per pixels            : " + getSamplesPerPixel();
			info[13] = "Physician                    : " + physician;
			info[14] = "Institution                  : " + institution;
			info[15] = "Transfert syntax UID         : " + transfertSyntaxUID ;
			info[16] = "Slice thickness              : " + getSliceThickness();
			info[17] = "Spacing between slices       : " + getSpacingBetweenSlices();
			info[18] = "Slice location               : " + getSliceLocation();
			info[19] = "Pixel spacing                : " + getPixelSpacing();
	return info ;
	}

// ===================================================================
//  FULL-HEADER ACCESS  (every element parsed, not just the curated set)
// ===================================================================
/** Number of distinct elements parsed in the whole file. */
	public int getElementCount(){ return elements.size(); }

/** True if the element is present in the file. */
	public boolean hasTag(int group, int element){
		return elements.containsKey(Integer.valueOf(key(group, element)));
	}

/** Human-readable name from the DICOM dictionary, or "" if unknown. */
	public String getTagName(int group, int element){
		String name = dict().get(hex8(group, element));
		if(name == null){
			// Repeating groups (50xx, 60xx, 7Fxx ...) are stored with an "XX" joker.
			String hi = Integer.toHexString((group >> 8) & 0xff).toUpperCase();
			while(hi.length() < 2) hi = "0" + hi;
			name = dict().get(hi + "XX" + hex4(element));
		}
		return (name == null) ? "" : name.trim();
	}

/** Value of any element, formatted as text according to its VR. */
	public String getElementValue(int group, int element){
		Element e = elements.get(Integer.valueOf(key(group, element)));
		return (e == null) ? "" : formatValue(e);
	}

/** Sorted list of every parsed tag, as "GGGG,EEEE" strings. */
	public String[] getTagList(){
		Integer[] keys = elements.keySet().toArray(new Integer[0]);
		java.util.Arrays.sort(keys);
		String[] out = new String[keys.length];
		for(int i = 0; i < keys.length; i++){
			int k = keys[i].intValue();
			out[i] = tag(k >>> 16, k & 0xffff);
		}
		return out;
	}

/**
*	Full header dump: one line per element,
*	"GGGG,EEEE  [VR]  Name                         value".
*/
	public String[] getAllElements(){
		Integer[] keys = elements.keySet().toArray(new Integer[0]);
		java.util.Arrays.sort(keys);
		String[] out = new String[keys.length];
		for(int i = 0; i < keys.length; i++){
			int k = keys[i].intValue();
			int g = k >>> 16, e = k & 0xffff;
			Element el = elements.get(keys[i]);
			String name = getTagName(g, e);
			if(name.length() == 0) name = "?";
			String vr = (el.vr != 0)
				? ("" + (char)((el.vr >> 8) & 0xff) + (char)(el.vr & 0xff))
				: "  ";
			out[i] = tag(g, e) + "  [" + vr + "]  " + pad(name, 32) + " " + formatValue(el);
		}
		return out;
	}

// --- formatting helpers --------------------------------------------------
	private static String hex4(int v){
		String s = Integer.toHexString(v & 0xffff).toUpperCase();
		while(s.length() < 4) s = "0" + s;
		return s;
	}
	private static String hex8(int g, int e){ return hex4(g) + hex4(e); }
	private static String tag(int g, int e){ return hex4(g) + "," + hex4(e); }
	private static String pad(String s, int n){
		StringBuffer b = new StringBuffer(s);
		while(b.length() < n) b.append(' ');
		return b.toString();
	}

	private long readU64(int i){
		long v = 0;
		if(bE) for(int k = 0; k < 8; k++) v = (v << 8) | (data[i+k] & 0xff);
		else   for(int k = 7; k >= 0; k--) v = (v << 8) | (data[i+k] & 0xff);
		return v;
	}

	private String text(int pos, int len){
		if(len > 256) len = 256;
		StringBuffer b = new StringBuffer(len);
		for(int i = 0; i < len && pos + i < dataLength; i++){
			int c = data[pos+i] & 0xff;
			b.append(((c >= 32 && c < 127) || c == 9) ? (char)c : '.');
		}
		int end = b.length();
		while(end > 0 && (b.charAt(end-1) == ' ' || b.charAt(end-1) == '.')) end--;
		return b.substring(0, end);
	}

	private boolean looksPrintable(int pos, int len){
		int n = Math.min(len, 16);
		for(int i = 0; i < n && pos + i < dataLength; i++){
			int c = data[pos+i] & 0xff;
			if(!((c >= 32 && c < 127) || c == 0 || c == 9 || c == 10 || c == 13)) return false;
		}
		return true;
	}

	private static boolean isBinaryVR(int vr){
		switch(vr){
			case ('O'<<8)|'B': case ('O'<<8)|'W': case ('O'<<8)|'F':
			case ('O'<<8)|'D': case ('O'<<8)|'L': case ('U'<<8)|'N':
			case ('S'<<8)|'Q':
				return true;
			default: return false;
		}
	}

	private String formatValue(Element e){
		if(e.valueLen < 0) return "(sequence / undefined length)";
		if(e.valueLen == 0) return "";
		if(e.valuePos + e.valueLen > dataLength) return "<" + e.valueLen + " bytes, truncated>";
		switch(e.vr){
			case ('U'<<8)|'S': return Integer.toString(u16(e.valuePos));
			case ('S'<<8)|'S': return Integer.toString((short) u16(e.valuePos));
			case ('U'<<8)|'L': return Long.toString(u32(e.valuePos) & 0xffffffffL);
			case ('S'<<8)|'L': return Integer.toString((int) u32(e.valuePos));
			case ('F'<<8)|'L': return Float.toString(Float.intBitsToFloat((int) u32(e.valuePos)));
			case ('F'<<8)|'D': return Double.toString(Double.longBitsToDouble(readU64(e.valuePos)));
			case ('A'<<8)|'T': return tag(u16(e.valuePos), u16(e.valuePos + 2));
		}
		if(isBinaryVR(e.vr)) return "<" + e.valueLen + " bytes>";
		// String VRs, or implicit VR: render as text when it looks printable.
		if(e.vr != 0 || looksPrintable(e.valuePos, e.valueLen)) return text(e.valuePos, e.valueLen);
		if(e.valueLen == 2) return Integer.toString(u16(e.valuePos));
		if(e.valueLen == 4) return Long.toString(u32(e.valuePos) & 0xffffffffL);
		return "<" + e.valueLen + " bytes>";
	}

	public Hashtable getMedicalInfos() {
		Hashtable<String,String> table = new Hashtable<String,String>(8);
		table.put("patient.name",getPatientName());
		table.put("patient.id",getPatientID());
		table.put("patient.birthdate",getPatientBirthdate());
		table.put("sex",sex);
		table.put("study.date",getStudyDate());
			table.put("physician",physician);
			table.put("institution",institution);
			table.put("transfert.syntax.uid",transfertSyntaxUID);
			table.put("slice.thickness",getSliceThickness());
			table.put("spacing.between.slices",getSpacingBetweenSlices());
			table.put("slice.location",getSliceLocation());
			table.put("pixel.spacing",getPixelSpacing());
			return table;
		}

/**	This method gets an integer when you give it the tags. */
	public int  getAnInt(int groupElement, int dataElement){
		return getAnInt( groupElement, dataElement, 0);
	}

	private int  getAnInt(int groupElement, int dataElement, int j){
		Element e = elements.get(Integer.valueOf(key(groupElement, dataElement)));
		if(e == null || e.valueLen <= 0) return -1 ;
		if(e.valuePos + e.valueLen > dataLength) return -1 ;
		if(e.valueLen == 2) return u16(e.valuePos);
		if(e.valueLen == 4) return (int) u32(e.valuePos);
		// e.g. an IS (Integer String) value:
		String s = rawString(key(groupElement, dataElement));
		try { return Integer.parseInt(s.trim()); }
		catch(NumberFormatException nfe){ return -1; }
	}

// --- legacy helpers kept for API compatibility (little-endian) ---------
	public int readMessageLength(int i){
		int i0 = data[i]   & 0xff;
		int i1 = data[i+1] & 0xff;
		int i2 = data[i+2] & 0xff;
		int i3 = data[i+3] & 0xff;
		return i3<<24 | i2<<16 | i1<<8 | i0 ;
	}

	public int readVRMessageLength(int tagPos){
		if(!explicitVR){
			bytesFromTagToValue = 8 ;
			return readMessageLength(tagPos + 4) ;
		}
		String VRTypeOf = new String(new byte[]{data[tagPos+4], data[tagPos+5]});
		if(VRTypeOf.equals("OB") || VRTypeOf.equals("OW")
		   || VRTypeOf.equals("SQ") || VRTypeOf.equals("UN")){
			bytesFromTagToValue = 12;
			return readMessageLength(tagPos + 8);
		} else {
			bytesFromTagToValue = 8 ;
			int a = data[tagPos+6] & 0xff, b = data[tagPos+7] & 0xff;
			return (b<<8) | a ;
		}
	}

	private void skip (int length){ index += length; }

/**
*	Major data accessors. Always check that width, height and bitsAllocated
*	are consistent with the file size.
*/
	public int getSize()	{ return dataLength;}
	public int getNumberOfFrames(){ return numberOfFrames ;}
	public int getSamplesPerPixels() { return samplesPerPixel ;}
	public int getPixelDataSize(){
		if (errorDetector == 0 ) return size ;
		else return -1 ;
	}

/**	The height : */
	public int  getRows()	{return h ;}
/**	The width : */
	public int  getColumns(){ return w;}
/**	The bits allocated per pixel of image */
	public int  getBitAllocated(){return bitsAllocated;}
/**	The bits stored per pixel of image */
	public int  getBitStored(){return bitsStored;}
/**	Other values : */
	public int  getHighBit(){return	highBit;}
	public int  getSamplesPerPixel(){return	samplesPerPixel;}
	public int  getPixelRepresentation(){return	signed;}

	protected  final static String author(){ return ("Serge Derhy") ; }

	private void  debug(String message){
		if (DEBUG) System.out.println(message);
	}
}//endOfClass
