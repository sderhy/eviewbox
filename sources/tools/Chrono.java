package tools;

public class Chrono {
	
	private static long tzero = System.currentTimeMillis() ;
	
	private static long  timer =0 ;
	
	
	public  static void start() { 
		tzero = System.currentTimeMillis() ;
		timer =0 ;
	}

	public  static void stop() {
		if ( tzero ==0 ) return ;
		timer = System.currentTimeMillis() - tzero ;
		tools.Tools.debug( "Elapsed time  :" + timer );
	}
	
	public  static int stopIt(){
		stop();
		return elapsedTime();
	}
	
	public static int elapsedTime(){
		return (int) timer ;
	}
		
	public static int stopReset(){
		int  laps = stopIt();
		start() ;
		return laps ;
	}	
	
}//end of method
 
		
		
		
