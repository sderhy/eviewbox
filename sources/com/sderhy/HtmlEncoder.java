package com.sderhy ;

import java.net.* ;
import java.util.* ;


public class HtmlEncoder extends Object {

		//private  String[] urls ;
		StringBuffer htmlCode ;
		private int index = 0 ;
		private String[] names;

		public HtmlEncoder(){
			//this.urls = urls ;
			initHtmlCode() ;
			names = new String[3] ;
			//encodeEachImg( );		
			//endHtmlCode() ;
		}

		private void initHtmlCode(){
			htmlCode = new StringBuffer("<HTML><HEAD><META NAME=\"GENERATOR\" Content=\"SergeDerhy's JDViewer\"> ") ;
			htmlCode.append("\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;CHARSET=iso-8859-1\"> " ) ;
			htmlCode.append("\n<TITLE>untitled</TITLE></HEAD><BODY BGCOLOR=\"white\"><P><CENTER><TABLE BORDER=\"1\" WIDTH=\"100%\"> ") ;
		}

		public void encodeEachImg(String[] urls){
		
			for(int i = 0 ; i< urls.length; i++){
				if( index ==0)htmlCode.append("\n<TR>" ) ;		
				htmlCode.append("<TD WIDTH=\"33%\"><CENTER><A HREF=\" ") ;
				htmlCode.append( urls[i] ) ;
				htmlCode.append("\" >\n<IMG SRC=\" ");
				htmlCode.append( urls[i] ) ;
				htmlCode.append("\" WIDTH=\"100\" HEIGHT=\"100\" ALIGN=\"BOTTOM\" BORDER=\"1\"></A></CENTER></TD>");
				index = (index+1)%3;
				if( index ==0)htmlCode.append("</TR>\n\n" ) ;
			}//end for
		}//end of encodeEachImg

		public void encodeImg(String url){
		
			
				if(index ==0)htmlCode.append("\n<TR>" ) ;		
				htmlCode.append("<TD WIDTH=\"33%\"><CENTER>\n<A HREF=\" ") ;
				htmlCode.append( url ) ;
				htmlCode.append(" ><IMG SRC=\" ");
				htmlCode.append( url ) ;
				htmlCode.append(" WIDTH=\"100\" HEIGHT=\"100\" ALIGN=\"BOTTOM\" BORDER=\"2\"></A>\n</CENTER></TD>");
				
				index = (index+1)%3	;	;
				if( index ==0)htmlCode.append("</TR>\n\n" ) ;	
		}//end of encodeImg
	/*	
		public void encodeImg(String url, int w , int h){
		
				
				int W , H ;
				if (w == h) {encodeImg(url) ; return ;}	
				else if( w> h){ W = 200 ;H = 200 * h / w ;}
				else { H = 200 ; W = 200*w/h ;}			
				
				if( index ==0)htmlCode.append("<TR>\n" ) ;		
				htmlCode.append("<TD WIDTH=\"33%\"><CENTER><A HREF=\" ") ;
				htmlCode.append( url ) ;
				htmlCode.append(" \">\n<IMG SRC=\" \n");
				htmlCode.append( url ) ;
				htmlCode.append("\" \n WIDTH=\""+W+"\" HEIGHT=\" " + H+ "\" ALIGN=\"BOTTOM\" BORDER=\"2\"></A></CENTER></TD>");
				index = (index+1)%3	;
				if( index ==0)htmlCode.append("</TR>\n\n" ) ;	
		}//end of encodeImg		
		
	*/	
	
		public void encodeImg(String url, int w , int h){
				
				int W , H ;
				
				if (w == h) {encodeImg(url) ; return ;}	
				else if( w> h){ W = 200 ;H = 200 * h / w ;}
				else { H = 200 ; W = 200*w/h ;}			
				
				if( index ==0)htmlCode.append("<TR>\n" ) ;		
				htmlCode.append("<TD WIDTH=\"33%\"><CENTER><A HREF=\" ") ;
				htmlCode.append( url ) ;
				htmlCode.append(" \">\n<IMG SRC=\" \n");
				htmlCode.append( url ) ;
				htmlCode.append("\" \n WIDTH=\""+W+"\" HEIGHT=\" " + H+ "\" ALIGN=\"BOTTOM\" BORDER=\"0\"></A></CENTER></TD>");
				
				names[index] = url.substring(url.lastIndexOf("/")+1 , url.length()-4);
				tools.Tools.debug("index  : "+ index + "   url name :" + names[index] ) ;
				index++;
				if( index >=3){
						htmlCode.append("</TR><TR>\n\n" ) ;	
						for( int i=0; i< 3 ;i++ ){
							tools.Tools.debug("i : "+ i + "   url name :" + names[i] ) ;
						 	htmlCode.append("<TD><CENTER><b>"+ names[i]+"</b></CENTER></TD>") ;
						 
						 }
						htmlCode.append("</TR>\n\n<TR><TD colspan=3 >&nbsp</TD></TR>" ) ;
						index = 0 ;
				}		
		}//end of encodeImg		
		
	/*	private void endHtmlCode(){
			int j = index ;// pour le nom
			if(index%3 != 0){
				while(index%3 !=0 ){ 
					htmlCode.append("<TD WIDTH=\"33%\">&#160;</TD> ");
					index++ ;
					}
				htmlCode.append("</TR>\n\n" ) ;
			}//end if
			htmlCode.append("</TABLE></BODY></HTML>" ) ;
		
		}
		
	*/
		private void endHtmlCode(){
			int j = index ;// pour le nom
			if(index%3 != 0){
				while(index%3 !=0 ){ 
					htmlCode.append("<TD WIDTH=\"33%\">&#160;</TD> ");
					index++ ;
					}
				htmlCode.append("</TR><TR>" ) ;
				for(int i = 0; i < j ; i++)
					htmlCode.append("<TD><CENTER><b>"+ names[i]+"</b></CENTER></TD>") ;
			}//end if
			htmlCode.append("</TR>\n\n<TR><TD colspan=3 >&nbsp</TD></TR>" ) ;
			htmlCode.append("</TABLE></BODY></HTML>" ) ;
		
		}
		public String getHtmlCode(){
				endHtmlCode() ;
				return htmlCode.toString();
		}
		

}

		
		

