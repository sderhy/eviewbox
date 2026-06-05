
import java.awt.*;
import java.applet.Applet;
import java.net.* ;

public class e_ViewBox_Applet extends Applet
{
	public static final String name    = "eViewBox , copyright sderhy@imaginet.fr";
	public static final String version = "0.9 Alpha";
	public boolean isOk = false ;


	public void init() {

		String fileSet  = getParameter("FileSet");
		if(fileSet != null){
			java.net.URL  url = getCodeBase();
			try{
				java.net.URL u = new java.net.URL(url, fileSet );
				String[] args = new String[1] ;
				args[0] = u.toString();
				isOk = true ;
				Main.main(args);
			}catch(MalformedURLException mue){Main.main(null) ;}
		}//endif

		else{
			//setSize(200, 200 );

		}
	}
	public void destroy()
	{

	}
		public String getAppletInfo()
	{
		return name + " version " + version;
	}


}
