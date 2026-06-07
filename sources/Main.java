public final  class Main
{
	private static final String APPLICATION_NAME = "Eviewbox";

	public static void main(String args[]){
		setApplicationName();
		com.sderhy.MainClass MC = new com.sderhy.MainClass("EViewBox");
		if(args != null && args.length > 0){
			try {
				MC.canvas.loadFileSet(new java.net.URL(args[0]));
			}catch (java.net.MalformedURLException m){
				m.printStackTrace();
			}
		}
	}
	public static void  quit(){
		com.sderhy.Winager.closeAll() ;
		//System.exit(0);
	}

	private static void setApplicationName(){
		System.setProperty("apple.awt.application.name", APPLICATION_NAME);
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", APPLICATION_NAME);
	}

	public String getAppletInfo(){ return " Eviewbox version beta";}
}
