/**
*	Examination — a neutral Patient/Study/Series tree used by the DICOMDIR
*	browser. It can be built two ways :
*	  - from a DICOMDIR file (the authoritative index), or
*	  - by scanning a folder of images when no DICOMDIR is present (e.g. a
*	    PACS export of JPEG/DICOM files laid out in sub-directories). In that
*	    case a warning is attached because the tree only reflects the folders
*	    and some series may be incomplete.
*
*	A node carries the image files to open (collected from its subtree), so the
*	browser can load a whole series/folder regardless of the source.
*
*	@author Serge Derhy
*/
package com.sderhy ;
import DicomDecoder.DicomDirReader ;
import DicomDecoder.DicomHeaderReader ;
import java.io.* ;
import java.util.* ;

public class Examination {

	public static class Node {
		public String label ;
		public String type ;                       // PATIENT/STUDY/SERIES/IMAGE/DIR/ROOT
		public final List<File> images   = new ArrayList<File>() ;   // files held directly here
		public final List<Node> children = new ArrayList<Node>() ;
		public Node(String label, String type){ this.label = label ; this.type = type ; }
		/** Files held by this node and all its descendants. */
		public List<File> collect(){ List<File> out = new ArrayList<File>() ; gather(out) ; return out ; }
		private void gather(List<File> out){
			out.addAll(images) ;
			for(int i=0;i<children.size();i++) children.get(i).gather(out) ;
		}
		public boolean isLoadable(){ return !collect().isEmpty() ; }
		public String toString(){ return label ; }
	}

	public Node root ;
	public boolean fromDicomDir ;
	public String warning ;        // null when there is nothing to warn about

	private Examination(Node root, boolean fromDicomDir, String warning){
		this.root = root ; this.fromDicomDir = fromDicomDir ; this.warning = warning ;
	}

// ===================================================================
//  Built from a DICOMDIR file
// ===================================================================
	public static Examination fromDicomDir(File dicomdir) throws IOException {
		DicomDirReader reader = new DicomDirReader(dicomdir) ;
		File base = dicomdir.getParentFile() ;
		Node root = new Node("Examination — " + (base!=null?base.getName():dicomdir.getName()), "ROOT") ;
		List<DicomDirReader.Record> roots = reader.getRoots() ;
		for(int i=0;i<roots.size();i++) root.children.add(buildRecordNode(roots.get(i), base)) ;
		String warning = reader.isValid() ? null
			: "DICOMDIR present but no records could be read." ;
		return new Examination(root, true, warning) ;
	}

	private static Node buildRecordNode(DicomDirReader.Record r, File base){
		Node node = new Node(labelFor(r), r.type) ;
		// This record's own referenced file (rare at container level, kept for safety).
		addFile(node, base, r.referencedFileId) ;
		for(int i=0;i<r.children.size();i++){
			DicomDirReader.Record c = r.children.get(i) ;
			if("IMAGE".equalsIgnoreCase(c.type) && c.children.isEmpty()){
				// Fold image records into their container : the series stays the leaf.
				addFile(node, base, c.referencedFileId) ;
			} else {
				node.children.add(buildRecordNode(c, base)) ;
			}
		}
		if("SERIES".equalsIgnoreCase(r.type) && !node.images.isEmpty())
			node.label = node.label + "  (" + node.images.size() + " images)" ;
		return node ;
	}

	private static void addFile(Node node, File base, String refId){
		if(refId == null || refId.length() == 0) return ;
		File f = resolve(base, refId) ;
		if(f != null) node.images.add(f) ;
	}

	private static String labelFor(DicomDirReader.Record r){
		String t = r.type == null ? "" : r.type.toUpperCase() ;
		StringBuffer b = new StringBuffer() ;
		if(t.equals("PATIENT")){
			b.append("Patient : ").append(nz(r.get("00100010"), "?")) ;
			add(b, "ID ", r.get("00100020")) ;
		} else if(t.equals("STUDY")){
			b.append("Study : ").append(nz(r.get("00081030"), "(no description)")) ;
			add(b, "", r.get("00080020")) ;
		} else if(t.equals("SERIES")){
			b.append("Series : ").append(nz(r.get("00080060"), "?")) ;     // modality
			add(b, "#", r.get("00200011")) ;                                // series number
			add(b, "", r.get("0008103E")) ;                                 // series description
		} else if(t.equals("IMAGE")){
			b.append("Image") ; add(b, "#", r.get("00200013")) ;
			add(b, "", r.referencedFileId) ;
		} else {
			b.append(r.type==null||r.type.length()==0 ? "Record" : r.type) ;
		}
		return b.toString() ;
	}

	/** Append "  prefix+value" only when value is meaningful. */
	private static void add(StringBuffer b, String prefix, String value){
		if(value == null) return ;
		String v = value.trim() ;
		if(v.length() == 0 || v.equals("Unknown")) return ;
		b.append("  ").append(prefix).append(v) ;
	}

	/** Resolve a backslash-separated ReferencedFileID relative to the DICOMDIR folder. */
	private static File resolve(File base, String refId){
		if(refId == null) return null ;
		File f = (base != null) ? base : new File(".") ;
		StringTokenizer st = new StringTokenizer(refId, "\\/") ;
		while(st.hasMoreTokens()) f = new File(f, st.nextToken().trim()) ;
		return f ;
	}

// ===================================================================
//  Built by scanning a folder (no DICOMDIR found)
// ===================================================================
	private static final String[] IMAGE_EXT = {
		"dcm","dic","dc3","acr","ima","jpg","jpeg","gif","png","bmp","ppm","tif","tiff"
	} ;

	public static Examination fromFolder(File dir){
		Node root = scanDir(dir, dir.getName()) ;
		if(root == null) root = new Node(dir.getName(), "DIR") ;
		String warning = hasSeries(root)
			? "DICOMDIR not found — series rebuilt from the DICOM file headers."
			: "DICOMDIR not found — tree rebuilt from the folder layout; "
			               + "some series may be incomplete." ;
		return new Examination(root, false, warning) ;
	}

	private static Node scanDir(File dir, String label){
		File[] entries = dir.listFiles() ;
		if(entries == null) return null ;
		Arrays.sort(entries) ;
		Node node = new Node(label, "DIR") ;
		int imageCount = 0 ;
		for(int i=0;i<entries.length;i++){
			File e = entries[i] ;
			if(e.getName().startsWith(".")) continue ;          // skip hidden / .DS_Store
			if(e.isDirectory()){
				Node child = scanDir(e, e.getName()) ;
				if(child != null && !child.collect().isEmpty()) node.children.add(child) ;
			} else if(isImageFile(e)){
				node.images.add(e) ; imageCount++ ;
			}
		}
		// Real DICOM files carry their series in the header : regroup them by
		// SeriesInstanceUID so a flat export shows proper series, not one heap.
		int nSeries = groupBySeries(node) ;
		if(nSeries > 0) node.label = label + "  (" + nSeries + " series)" ;
		else if(imageCount > 0) node.label = label + "  (" + imageCount + " images)" ;
		return node ;
	}

	private static boolean hasSeries(Node n){
		if("SERIES".equalsIgnoreCase(n.type)) return true ;
		for(int i=0;i<n.children.size();i++) if(hasSeries(n.children.get(i))) return true ;
		return false ;
	}

	private static boolean isImageFile(File f){
		String n = f.getName() ;
		int dot = n.lastIndexOf('.') ;
		if(dot < 0) return true ;                                // no extension : likely raw DICOM
		String ext = n.substring(dot+1).toLowerCase() ;
		for(int i=0;i<IMAGE_EXT.length;i++) if(IMAGE_EXT[i].equals(ext)) return true ;
		return false ;
	}

// ===================================================================
//  Header-based series grouping (folder without DICOMDIR)
// ===================================================================
	/** Extensions that are plain raster files : no point parsing them as DICOM. */
	private static final String[] RASTER_EXT = { "jpg","jpeg","gif","png","bmp","ppm","tif","tiff" } ;
	private static final int HEAD_BYTES = 64 * 1024 ;   // enough for any normal header

	private static class FileInfo implements Comparable<FileInfo> {
		File file ;
		String uid, modality, description ;
		int seriesNumber = Integer.MAX_VALUE ;
		int instance = Integer.MAX_VALUE ;
		public int compareTo(FileInfo o){ return instance - o.instance ; }   // stable sort keeps name order on ties
	}

	/** Regroup a folder node's direct files into SERIES children keyed by
	*	SeriesInstanceUID (0020,000E), each sorted by InstanceNumber. Non-DICOM
	*	files stay attached to the folder node. Returns the number of series. */
	private static int groupBySeries(Node node){
		if(node.images.size() < 2) return 0 ;
		LinkedHashMap<String,List<FileInfo>> map = new LinkedHashMap<String,List<FileInfo>>() ;
		List<File> rest = new ArrayList<File>() ;
		for(int i=0;i<node.images.size();i++){
			File f = node.images.get(i) ;
			FileInfo fi = readSeriesInfo(f) ;
			if(fi == null){ rest.add(f) ; continue ; }
			List<FileInfo> l = map.get(fi.uid) ;
			if(l == null){ l = new ArrayList<FileInfo>() ; map.put(fi.uid, l) ; }
			l.add(fi) ;
		}
		if(map.isEmpty()) return 0 ;
		node.images.clear() ;
		node.images.addAll(rest) ;
		List<Node> series = new ArrayList<Node>() ;
		final List<Integer> numbers = new ArrayList<Integer>() ;
		for(Iterator<List<FileInfo>> it = map.values().iterator() ; it.hasNext() ; ){
			List<FileInfo> l = it.next() ;
			Collections.sort(l) ;                       // acquisition order
			FileInfo first = l.get(0) ;
			Node s = new Node(seriesLabel(first, l.size()), "SERIES") ;
			for(int i=0;i<l.size();i++) s.images.add(l.get(i).file) ;
			series.add(s) ;
			numbers.add(Integer.valueOf(first.seriesNumber)) ;
		}
		// Show the series in SeriesNumber order (the clinical order).
		Integer[] idx = new Integer[series.size()] ;
		for(int i=0;i<idx.length;i++) idx[i] = Integer.valueOf(i) ;
		Arrays.sort(idx, new Comparator<Integer>(){
			public int compare(Integer a, Integer b){
				return numbers.get(a.intValue()).intValue() - numbers.get(b.intValue()).intValue() ;
			}
		}) ;
		for(int i=0;i<idx.length;i++) node.children.add(series.get(idx[i].intValue())) ;
		return series.size() ;
	}

	private static String seriesLabel(FileInfo fi, int count){
		StringBuffer b = new StringBuffer("Series : ") ;
		b.append(nz(fi.modality, "?")) ;
		if(fi.seriesNumber != Integer.MAX_VALUE) b.append("  #").append(fi.seriesNumber) ;
		add(b, "", fi.description) ;
		b.append("  (").append(count).append(" images)") ;
		return b.toString() ;
	}

	/** Read just enough of a file to identify its series ; null when the file
	*	is not DICOM (or carries no SeriesInstanceUID). Never throws. */
	private static FileInfo readSeriesInfo(File f){
		String n = f.getName() ;
		int dot = n.lastIndexOf('.') ;
		if(dot >= 0){
			String ext = n.substring(dot+1).toLowerCase() ;
			for(int i=0;i<RASTER_EXT.length;i++) if(RASTER_EXT[i].equals(ext)) return null ;
		}
		try {
			byte[] head = readHead(f) ;
			if(head == null || head.length < 16) return null ;
			DicomHeaderReader h = new DicomHeaderReader(head) ;
			String uid = clean(h.getaString(0x0020, 0x000E)) ;   // SeriesInstanceUID
			if(uid == null) return null ;
			FileInfo fi = new FileInfo() ;
			fi.file = f ;
			fi.uid = uid ;
			fi.modality = clean(h.getaString(0x0008, 0x0060)) ;
			fi.description = clean(h.getaString(0x0008, 0x103E)) ;
			fi.seriesNumber = parseIntSafe(h.getaString(0x0020, 0x0011)) ;
			fi.instance = parseIntSafe(h.getaString(0x0020, 0x0013)) ;
			return fi ;
		} catch(Throwable t){
			return null ;   // unreadable / not DICOM : leave the file ungrouped
		}
	}

	private static byte[] readHead(File f) throws IOException {
		long flen = f.length() ;
		int n = (int)Math.min(flen, HEAD_BYTES) ;
		byte[] b = new byte[n] ;
		DataInputStream in = new DataInputStream(new FileInputStream(f)) ;
		try { in.readFully(b) ; } finally { in.close() ; }
		return b ;
	}

	private static String clean(String s){
		if(s == null) return null ;
		s = s.trim() ;
		return (s.length() == 0 || s.equals("Unknown")) ? null : s ;
	}

	private static int parseIntSafe(String s){
		s = clean(s) ;
		if(s == null) return Integer.MAX_VALUE ;
		try { return Integer.parseInt(s) ; } catch(NumberFormatException e){ return Integer.MAX_VALUE ; }
	}

// ---- small helpers -----------------------------------------------------
	private static String nz(String s, String fallback){
		return (s == null || s.trim().length() == 0 || s.equals("Unknown")) ? fallback : s.trim() ;
	}
}
