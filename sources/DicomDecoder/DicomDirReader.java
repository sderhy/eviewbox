/**
*	DicomDirReader — parses a DICOMDIR (Media Storage Directory) file.
*
*	A DICOMDIR is a Part-10 DICOM file (preamble + "DICM"), always Explicit VR
*	Little Endian. Its payload is the Directory Record Sequence (0004,1220):
*	a flat list of items (records), each typed by (0004,1430) DirectoryRecordType
*	(PATIENT / STUDY / SERIES / IMAGE / ...). The hierarchy is rebuilt from byte
*	offsets, all relative to the start of the file :
*	  (0004,1200) FirstDirectoryRecordOffset  -> first root record
*	  (0004,1400) OffsetOfNextDirectoryRecord -> next sibling
*	  (0004,1420) LowerLevelDirectoryEntityOffset -> first child
*	IMAGE records point to the actual file via (0004,1500) ReferencedFileID
*	(its components are separated by a backslash, relative to the DICOMDIR folder).
*
*	This reader does pure parsing : no UI, no file loading. It walks the dataset
*	VR-aware (handling items and undefined lengths) and exposes the records both
*	as a flat list and as a tree of root records.
*
*	@author Serge Derhy
*/
package DicomDecoder ;
import java.util.* ;
import java.io.* ;

public class DicomDirReader {

	private byte[] d ;
	private int len ;
	private int firstRecordOffset = 0 ;
	private final HashMap<Integer,Record> byOffset = new HashMap<Integer,Record>() ;
	private final ArrayList<Record> all   = new ArrayList<Record>() ;
	private final ArrayList<Record> roots = new ArrayList<Record>() ;

	private static final long UNDEF = 0xFFFFFFFFL ;

/** One DICOMDIR directory record. */
	public static class Record {
		public String type = "" ;              // (0004,1430)
		public String referencedFileId = "" ;  // (0004,1500), backslash-separated
		public int offset, next, lower ;
		public int inUse = 0xFFFF ;             // (0004,1410), 0 == inactive
		public final HashMap<String,String> fields = new HashMap<String,String>() ;
		public final ArrayList<Record> children = new ArrayList<Record>() ;
		/** Value of a tag given as 8 hex digits "GGGGEEEE" (upper case), or "". */
		public String get(String tag){ String v = fields.get(tag) ; return v == null ? "" : v ; }
	}

	public DicomDirReader(byte[] bytes){
		d = bytes ; len = (bytes == null) ? 0 : bytes.length ;
		parse() ; link() ;
	}
	public DicomDirReader(File f) throws IOException { this(readAll(f)) ; }

	public List<Record> getRoots(){ return roots ; }
	public List<Record> getAllRecords(){ return all ; }
	public boolean isValid(){ return !all.isEmpty() ; }

	private static byte[] readAll(File f) throws IOException {
		int n = (int) f.length() ;
		byte[] b = new byte[n] ;
		DataInputStream in = new DataInputStream(new FileInputStream(f)) ;
		try { in.readFully(b) ; } finally { in.close() ; }
		return b ;
	}

// ---- low-level Explicit VR Little Endian reads -------------------------
	private int u16(int i){ return (d[i+1]&0xff)<<8 | (d[i]&0xff) ; }
	private long u32(int i){
		return ((long)(d[i+3]&0xff)<<24)|((d[i+2]&0xff)<<16)|((d[i+1]&0xff)<<8)|(d[i]&0xff) ;
	}
	private String vr(int p){ return "" + (char)(d[p]&0xff) + (char)(d[p+1]&0xff) ; }
	private static boolean longForm(String vr){
		return vr.equals("OB")||vr.equals("OW")||vr.equals("OF")||vr.equals("OD")||vr.equals("OL")
		     ||vr.equals("SQ")||vr.equals("UT")||vr.equals("UN")||vr.equals("UC")||vr.equals("UR") ;
	}
	private static String hex4(int v){ String s = Integer.toHexString(v&0xffff).toUpperCase(); while(s.length()<4)s="0"+s; return s ; }
	private String text(int pos,int l){
		if(l < 0) return "" ;
		if(pos + l > len) l = len - pos ;
		if(l <= 0) return "" ;
		if(l > 1024) l = 1024 ;
		StringBuffer b = new StringBuffer(l) ;
		for(int i=0;i<l;i++) b.append((char)(d[pos+i]&0xff)) ;
		int end = b.length() ;
		while(end>0){ char c=b.charAt(end-1); if(c=='\0'||c==' ') end--; else break; }
		return b.substring(0,end) ;
	}

// ---- parsing -----------------------------------------------------------
	private void parse(){
		if(d == null || len < 8) return ;
		int p = 0 ;
		boolean part10 = len>=132 && d[128]=='D'&&d[129]=='I'&&d[130]=='C'&&d[131]=='M' ;
		if(part10) p = 132 ;
		walkTop(p) ;
	}

	private void walkTop(int p){
		while(p+8 <= len){
			int g = u16(p), e = u16(p+2) ;
			if(g == 0xFFFE){ long l=u32(p+4); p+=8; if(l==UNDEF)p=skipUndef(p); else p+=l; continue ; }
			p += 4 ;
			if(p+2 > len) break ;
			String vr = vr(p) ; p += 2 ;
			long l ;
			if(longForm(vr)){ p+=2; if(p+4>len)break; l=u32(p); p+=4; }
			else { if(p+2>len)break; l=u16(p); p+=2; }
			int valuePos = p ;

			if(g==0x0004 && e==0x1200) firstRecordOffset = (int)u32(valuePos) ;

			if(g==0x0004 && e==0x1220){               // Directory Record Sequence
				int seqEnd = (l==UNDEF)? len : (int)Math.min(len, valuePos + l) ;
				parseRecords(valuePos, seqEnd) ;
				if(l==UNDEF) p = skipUndef(valuePos) ; else p = valuePos + (int)l ;
				continue ;
			}
			if(vr.equals("SQ") || l==UNDEF){          // any other sequence : skip it
				if(l==UNDEF) p = skipUndef(valuePos) ;
				else { long np=valuePos+l; if(np>len||np<valuePos)break; p=(int)np; }
			} else {
				long np = valuePos + l ; if(np>len||np<valuePos) break ; p = (int)np ;
			}
		}
	}

	/** Each item (FFFE,E000) in the Directory Record Sequence is one record. */
	private void parseRecords(int p,int end){
		while(p+8 <= end){
			int itemOffset = p ;
			int g=u16(p), e=u16(p+2) ; long l=u32(p+4) ; p+=8 ;
			if(g==0xFFFE && e==0xE0DD) return ;                  // sequence delimitation
			if(g==0xFFFE && e==0xE000){
				int itemEnd = (l==UNDEF)? end : (int)Math.min(end, p + l) ;
				Record r = new Record() ; r.offset = itemOffset ;
				readRecordFields(p, itemEnd, r) ;
				byOffset.put(Integer.valueOf(itemOffset), r) ; all.add(r) ;
				if(l==UNDEF) p = skipUndef(p) ; else p = itemEnd ;
			} else return ;
		}
	}

	/** Read the top-level elements of a record into r; nested sequences are skipped. */
	private void readRecordFields(int p,int end,Record r){
		while(p+8 <= end){
			int g=u16(p), e=u16(p+2) ;
			if(g==0xFFFE && e==0xE00D) return ;                  // item delimitation
			p += 4 ;
			if(p+2 > end) return ;
			String vr = vr(p) ; p += 2 ;
			long l ;
			if(longForm(vr)){ p+=2; if(p+4>end)return; l=u32(p); p+=4; }
			else { if(p+2>end)return; l=u16(p); p+=2; }
			int valuePos = p ;

			if(vr.equals("SQ") || l==UNDEF){                     // nested sequence : skip
				if(l==UNDEF) p = skipUndef(valuePos) ;
				else { long np=valuePos+l; if(np>end||np<valuePos)return; p=(int)np; }
				continue ;
			}

			String tag = hex4(g)+hex4(e) ;
			String val ;
			if(vr.equals("UL")) val = Long.toString(u32(valuePos)&0xffffffffL) ;
			else if(vr.equals("US")) val = Integer.toString(u16(valuePos)) ;
			else val = text(valuePos,(int)l) ;
			r.fields.put(tag, val) ;

			if(g==0x0004){
				if(e==0x1430) r.type = val.trim() ;
				else if(e==0x1500) r.referencedFileId = val ;
				else if(e==0x1400) r.next  = (int)u32(valuePos) ;
				else if(e==0x1420) r.lower = (int)u32(valuePos) ;
				else if(e==0x1410) r.inUse = u16(valuePos) ;
			}
			long np = valuePos + l ; if(np>end||np<valuePos) return ; p = (int)np ;
		}
	}

// ---- VR-aware sequence skipping (items + undefined lengths) ------------
	private int skipUndef(int p){
		while(p+8 <= len){
			int g=u16(p), e=u16(p+2) ; long l=u32(p+4) ; p+=8 ;
			if(g==0xFFFE && e==0xE0DD) return p ;
			if(g==0xFFFE && e==0xE000){
				if(l==UNDEF) p = skipItem(p) ;
				else { long np=p+l; if(np>len||np<p) return len ; p=(int)np ; }
			} else return len ;
		}
		return len ;
	}
	private int skipItem(int p){
		while(p+8 <= len){
			int g=u16(p), e=u16(p+2) ;
			if(g==0xFFFE && e==0xE00D) return p+8 ;
			int np = skipElement(p) ;
			if(np <= p) return len ;
			p = np ;
		}
		return len ;
	}
	private int skipElement(int p){
		if(p+8 > len) return -1 ;
		int g = u16(p) ; boolean isItem = (g==0xFFFE) ; p += 4 ;
		long l ;
		if(!isItem){
			if(p+2 > len) return -1 ;
			String vr = vr(p) ; p += 2 ;
			if(longForm(vr)){ p+=2; if(p+4>len)return -1; l=u32(p); p+=4; }
			else { if(p+2>len)return -1; l=u16(p); p+=2; }
		} else { if(p+4>len)return -1; l=u32(p); p+=4; }
		if(l==UNDEF) return skipUndef(p) ;
		long np = p + l ; if(np>len||np<p) return -1 ;
		return (int)np ;
	}

// ---- hierarchy ---------------------------------------------------------
	private void link(){
		roots.clear() ;
		addChain(firstRecordOffset, roots, new HashSet<Integer>()) ;
		// Fallback : if the offset links were missing/zero, present every record flat.
		if(roots.isEmpty() && !all.isEmpty()){
			for(int i=0;i<all.size();i++){ Record r=all.get(i); if(r.inUse!=0) roots.add(r); }
		}
	}
	private void addChain(int offset, List<Record> into, Set<Integer> seen){
		while(offset != 0 && byOffset.containsKey(Integer.valueOf(offset)) && seen.add(Integer.valueOf(offset))){
			Record r = byOffset.get(Integer.valueOf(offset)) ;
			if(r.inUse != 0){
				into.add(r) ;
				addChain(r.lower, r.children, seen) ;
			}
			offset = r.next ;
		}
	}
}
