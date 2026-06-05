// Main.java
import java.util.* ;
import com.sderhy.*;

public final  class Main
{
	public static boolean isApplet = false ;
	public static java.applet.Applet applet ;
	private static String msg, leMessage ;

	
	public static void main(String args[]){
				if(Main.isApplet){
					if(args != null){
						java.net.URL url;
						try { url = new java.net.URL(args[0]) ;
							MainClass.isApplet = Main.isApplet ;	
							MainClass MC =  new MainClass(" eViewBox ");
							MC.applet =  applet ;
							MC.canvas.loadFileSet(url) ;
										
						}catch (java.net.MalformedURLException m){
							m.printStackTrace();
							MainClass.main() ; 	
						}
					}//end if args !=0	
				}else {
					
					com.sderhy.MainClass MC = new com.sderhy.MainClass("EViewBox !") ;
					 		
				}	
	}
	public static void  quit(){
		com.sderhy.Winager.closeAll() ;
		//System.exit(0);
	}
	public String getAppletInfo(){ return " Eviewbox version beta";}
}
