/**
*	MakeIcon : renders the EViewBox application icon (Java2D, no asset needed).
*	Design : a stylized axial CT slice (thorax : body outline, lungs,
*	vertebra) on a dark rounded plate (macOS style).
*	Usage : java MakeIcon <iconset-dir>
*	Writes : the icon_NxN[@2x].png files expected by `iconutil --convert icns`
*	(macOS), EViewBox.png (1024 , Dock / Linux packaging) and EViewBox.ico
*	(Windows packaging) next to the iconset.
*/
import java.awt.* ;
import java.awt.geom.* ;
import java.awt.image.BufferedImage ;
import java.io.* ;
import javax.imageio.ImageIO ;

public class MakeIcon {

	public static void main(String[] args) throws Exception {
		File dir = new File(args.length > 0 ? args[0] : "EViewBox.iconset") ;
		dir.mkdirs() ;
		int[][] specs = { {16,1},{16,2},{32,1},{32,2},{128,1},{128,2},{256,1},{256,2},{512,1},{512,2} } ;
		for(int[] s : specs){
			int px = s[0]*s[1] ;
			String name = "icon_" + s[0] + "x" + s[0] + (s[1]==2 ? "@2x" : "") + ".png" ;
			ImageIO.write(render(px), "png", new File(dir, name)) ;
		}
		File parent = dir.getAbsoluteFile().getParentFile() ;
		ImageIO.write(render(1024), "png", new File(parent, "EViewBox.png")) ;
		writeIco(new File(parent, "EViewBox.ico"), new int[]{16, 32, 48, 256}) ;
		System.out.println("icons written to " + dir) ;
	}

//////////////////////////////// ICO (Windows) ////////////////////////////////

	/** Writes a .ico holding the given sizes : BMP entries for the small
	*	ones , PNG entry for 256 (the layout Windows expects). */
	static void writeIco(File f, int[] sizes) throws Exception {
		byte[][] blobs = new byte[sizes.length][] ;
		for(int i = 0 ; i < sizes.length ; i++){
			ByteArrayOutputStream b = new ByteArrayOutputStream() ;
			if(sizes[i] >= 256) ImageIO.write(render(sizes[i]), "png", b) ;
			else writeBmpEntry(render(sizes[i]), b) ;
			blobs[i] = b.toByteArray() ;
		}
		ByteArrayOutputStream ico = new ByteArrayOutputStream() ;
		le16(ico, 0) ; le16(ico, 1) ; le16(ico, sizes.length) ;// ICONDIR
		int offset = 6 + 16*sizes.length ;
		for(int i = 0 ; i < sizes.length ; i++){// ICONDIRENTRY
			int s = sizes[i] ;
			ico.write(s >= 256 ? 0 : s) ;
			ico.write(s >= 256 ? 0 : s) ;
			ico.write(0) ; ico.write(0) ;
			le16(ico, 1) ; le16(ico, 32) ;
			le32(ico, blobs[i].length) ;
			le32(ico, offset) ;
			offset += blobs[i].length ;
		}
		for(int i = 0 ; i < sizes.length ; i++) ico.write(blobs[i]) ;
		FileOutputStream out = new FileOutputStream(f) ;
		out.write(ico.toByteArray()) ;
		out.close() ;
	}

	/** One BMP icon entry : BITMAPINFOHEADER (doubled height) , BGRA pixels
	*	bottom-up , then an empty AND mask (transparency comes from alpha). */
	static void writeBmpEntry(BufferedImage img, OutputStream os) throws IOException {
		int w = img.getWidth(), h = img.getHeight() ;
		int maskRow = ((w + 31)/32)*4 ;
		le32(os, 40) ; le32(os, w) ; le32(os, 2*h) ;
		le16(os, 1) ; le16(os, 32) ;
		le32(os, 0) ; le32(os, w*h*4 + maskRow*h) ;
		le32(os, 0) ; le32(os, 0) ; le32(os, 0) ; le32(os, 0) ;
		for(int y = h-1 ; y >= 0 ; y--)
			for(int x = 0 ; x < w ; x++){
				int p = img.getRGB(x, y) ;
				os.write(p & 0xff) ;        // B
				os.write((p >> 8) & 0xff) ; // G
				os.write((p >> 16) & 0xff) ;// R
				os.write((p >> 24) & 0xff) ;// A
			}
		byte[] mask = new byte[maskRow*h] ;
		os.write(mask) ;
	}

	static void le16(OutputStream os, int v) throws IOException {
		os.write(v & 0xff) ; os.write((v >> 8) & 0xff) ;
	}
	static void le32(OutputStream os, int v) throws IOException {
		os.write(v & 0xff) ; os.write((v >> 8) & 0xff) ;
		os.write((v >> 16) & 0xff) ; os.write((v >> 24) & 0xff) ;
	}

	static BufferedImage render(int n){
		BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB) ;
		Graphics2D g = img.createGraphics() ;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) ;
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE) ;
		double S = n ;

	// dark rounded plate with the standard macOS margin
		double m = 0.09*S ;
		RoundRectangle2D plate = new RoundRectangle2D.Double(m, m, S-2*m, S-2*m, 0.225*S, 0.225*S) ;
		g.setPaint(new GradientPaint(0f, (float)m, new Color(0x2a3a4c), 0f, (float)(S-m), new Color(0x10161d))) ;
		g.fill(plate) ;
		g.setClip(plate) ;

		double cx = S/2.0, cy = S/2.0 ;

	// axial CT slice of a thorax , drawn in CT grays
	// body outline (soft tissue)
		double bw = 0.68*S, bh = 0.48*S ;
		Ellipse2D body = new Ellipse2D.Double(cx-bw/2, cy-bh/2, bw, bh) ;
		g.setPaint(new GradientPaint(0f, (float)(cy-bh/2), new Color(0xaab4bc),
				0f, (float)(cy+bh/2), new Color(0x77828c))) ;
		g.fill(body) ;
		g.setColor(new Color(0xdfe7ec)) ;// skin line
		g.setStroke(new BasicStroke((float)(0.014*S), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)) ;
		g.draw(body) ;

	// lungs : two dark kidney-bean lobes , medial side hollowed by the
	// mediastinum (heart) so the slice reads as a chest CT
		Color air = new Color(0x141b22) ;
		double lw = 0.26*S, lh = 0.36*S ;
		Area lungs = new Area(new Ellipse2D.Double(cx-0.17*S-lw/2, cy-lh/2, lw, lh)) ;
		lungs.add(new Area(new Ellipse2D.Double(cx+0.17*S-lw/2, cy-lh/2, lw, lh))) ;
		lungs.subtract(new Area(new Ellipse2D.Double(cx-0.11*S, cy-0.10*S, 0.22*S, 0.42*S))) ;//heart
		lungs.intersect(new Area(body)) ;
		g.setColor(air) ;
		g.fill(lungs) ;

	// vertebra (bone , bright) with its dark spinal canal , posterior midline
		double rv = 0.045*S ;
		double vy = cy + 0.115*S ;
		g.setColor(new Color(0xf0f4f7)) ;
		g.fill(new Ellipse2D.Double(cx-rv, vy, 2*rv, 2*rv)) ;
		g.setColor(air) ;
		double rc = 0.016*S ;
		g.fill(new Ellipse2D.Double(cx-rc, vy+0.014*S, 2*rc, 2*rc)) ;

		g.dispose() ;
		return img ;
	}
}
