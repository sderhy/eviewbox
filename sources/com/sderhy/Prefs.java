/**
*	Prefs : user preferences (background color, icon size),
*	persisted in ~/.eviewbox.properties so they survive a restart.
*	Used by the Preferences menu (MainLayout) and applied at startup (MainClass).
*/
package com.sderhy ;
import java.awt.Color ;
import java.io.* ;
import java.util.Properties ;

public class Prefs {

	private static final File FILE =
		new File(System.getProperty("user.home"), ".eviewbox.properties");
	private static Properties props = load();

	private static Properties load(){
		Properties p = new Properties();
		try{
			FileInputStream in = new FileInputStream(FILE);
			p.load(in);
			in.close();
		}catch(IOException e){ /* first run : no preference file yet */ }
		return p;
	}

	private static void save(){
		try{
			FileOutputStream out = new FileOutputStream(FILE);
			props.store(out, "EViewBox user preferences");
			out.close();
		}catch(IOException e){ tools.Tools.debug("Prefs : cannot save : " + e); }
	}

	public static Color getBackground(Color def){
		String s = props.getProperty("background");
		if(s == null) return def;
		try{ return new Color(Integer.parseInt(s)); }
		catch(NumberFormatException e){ return def; }
	}

	public static void setBackground(Color c){
		if(c == null) return;
		props.setProperty("background", String.valueOf(c.getRGB()));
		save();
	}

	public static int getStampSize(int def){
		String s = props.getProperty("iconSize");
		if(s == null) return def;
		try{ return Integer.parseInt(s); }
		catch(NumberFormatException e){ return def; }
	}

	public static void setStampSize(int size){
		props.setProperty("iconSize", String.valueOf(size));
		save();
	}
}
