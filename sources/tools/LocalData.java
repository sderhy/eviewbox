package tools ;
public class LocalData {

	public static String JavaVersion = System.getProperty("java.version");
	public static String  os = System.getProperty("os.name");
	public static java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit() ;
	
	public static int  pagedpi = toolkit.getScreenResolution();
	public static java.awt.Dimension screen = toolkit.getScreenSize();
	public static int screenW =  toolkit.getScreenSize().width;
	public static int screenH = toolkit.getScreenSize().height;
	public static String sep = java.io.File.separator ;
	
}
