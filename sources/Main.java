public final  class Main
{
	public static void main(String args[]){
		com.sderhy.MainClass MC = new com.sderhy.MainClass("EViewBox !");
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
	public String getAppletInfo(){ return " Eviewbox version beta";}
}
