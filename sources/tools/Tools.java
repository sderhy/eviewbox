
/** 
 *  @author   Serge Derhy
 *  @version  1.0
 */
package tools ;
public  class  Tools {
	
	static final String[]  
	Value ={"0","1","2","3","4",
			"5","6","7","8","9","A","B","C","D","E","F"};
	
	static final boolean debug = true ;
	
	public static void debug( String message){
		if(debug)
			System.out.println(">>" + message);
		}
	public static void debug(String  message , int i){
		if( debug)
			System.out.println( message +" : "+ String.valueOf(i));
		}
	public static void debug(Object o ,String message){
		if (debug )
			System.out.println(">>from class" +o.getClass().getName()+ " >>"+ message);
		}
	public static void dbg(String message){ debug(message);	}
	public static void dbg(Object o ,String message){ debug( o, message);	}
					
	public static String toHex(int i){
		byte b1 =  (byte)( i & 0xff) ;
		byte b2 =  (byte)((i & 0xff00) >> 8) ;
		byte b3 =  (byte)((i & 0xff0000) >> 16);
		byte b4 =  (byte)((i& 0xff000000) >> 24);
		String result = "";
		if (b4 != 0 ) 
			result += toHex(b4) ;
		if ((b4 == 0 && b3 != 0 )||(b4 != 0 && b3 == 0) )
			result +=  toHex(b3) ;
		
		return ( result + toHex(b2) + toHex(b1)) ;
	}
		
	public static String toHex(short sh){
		byte b1 =  (byte)( sh & 0xff) ;
		byte b2 =  (byte)((sh & 0xff00) >> 8) ;
		return (toHex(b2) + toHex(b1)) ;
	}	
		
		
	public static String toHex( byte  b){
		int USbyte = b & 0xff ;
		return(Value[ USbyte/16 ] + Value[ USbyte%16 ]) ;
	}
		


	public static void pause(int ms){
		try{ Thread.sleep(ms) ;}
		catch(InterruptedException e){ ;}
	}


	public static void gc(String s){
		long memo =  Runtime.getRuntime().freeMemory() ;
		debug("Before garbage at "+ s +" " + memo);
		for(int i= 0; i<5 ; i++)
			{
				System.gc();
				//try {Thread.sleep(10);} catch(InterruptedException e){System.gc();}
	
			}
		freeMemory(s);
		debug ( "Memory gain :" +(Runtime.getRuntime().freeMemory()- memo)+ "bytes");
	}
	
	public static void gc(){gc("");}
	public static void  freeMemory(String s){
		if(debug){
		 float f = (float)(Runtime.getRuntime().freeMemory()/ 1000000);
		System.out.println("UsedMemory :"+s+" : "+ Runtime.getRuntime().freeMemory()+ " = " +f + " MegaByte" );
		}
	}	
}//______end of class___________________________________________________
	

