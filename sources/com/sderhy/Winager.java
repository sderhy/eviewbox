package com.sderhy ;
import java.awt.* ;
import java.util.* ;

public  class Winager {

	private static Vector windoz = new Vector();
	private static int current = -1 ;

	public static Frame getPrevious(){
		current--;
		return getCurrent() ;
	}
	public static void next(){
		Frame f = getNextImageWindow(1);
		if(f==null)return;
		f.toFront();
	}

	public static void previous(){
		Frame f = getNextImageWindow(-1);
		if(f==null)return;
		f.toFront();
	}

	public static int getIndexOf(Frame f){
	return  windoz.indexOf(f);
	}

	public  static void setCurrent(int i ) {
		current = i ;
		if(windoz.isEmpty()){
			current = -1 ;
			return ;
			}
		if( i >= windoz.size()){
			current = windoz.size()-1 ;
			return ;
			}
	}

	public static Frame getCurrent(){
		if( windoz.isEmpty()){current= -1 ;return null ;}
		if( current>= windoz.size() ){
				current = 0;
				return (Frame)windoz.firstElement();
		}
		if( current < 0){
			current = windoz.size()-1;
		 return (Frame)windoz.lastElement() ;
		}
		else return (Frame)windoz.elementAt(current ) ;
	}

	public static Frame getNext() {
		current++;
		return getCurrent() ;
	}

	private static Frame getNextImageWindow(int direction) {
		if(windoz.isEmpty()) return null ;
		for(int i = 0 ; i < windoz.size() ; i++){
			current += direction ;
			Frame f = getCurrent();
			if(!(f instanceof MainClass)) return f ;
		}
		return null ;
	}


	public static void add(Frame f){
		if(windoz.indexOf(f)>=0 ) return ;//eviter les repetitions
		windoz.addElement(f);
		//tools.Tools.debug("windoz current incremented before " + current) ;
		current = windoz.size()-1;
		//tools.Tools.debug("windoz.size() " + windoz.size()) ;
	}


	public static void remove(Frame f) {
			int i = windoz.indexOf(f);
			if(i <0)	return ;
			windoz.removeElementAt(i) ;
			current = i-1;
			//tools.Tools.debug("windoz.size() after remove  "+ windoz.size()) ;
			//tools.Tools.debug("current after remove " + current) ;
	}
	public static void closeAll(){
			for(int i = 0 ; i< windoz.size() ; i++ ){
				Frame f = (java.awt.Frame)windoz.elementAt(i) ;
				f.hide() ;
				f.dispose() ;
			 }

	}
	public static Frame getPixObject(PixObject po) {
		for(int i = 0; i< windoz.size(); i++){
		Object o  = windoz.elementAt(i) ;
			if(o instanceof PixObjectViewer){
				PixObjectViewer pov = (PixObjectViewer)o;
				if( po == pov.po ){ setCurrent(i); return pov ;}
			}
		}
		return null ;
	}
}
