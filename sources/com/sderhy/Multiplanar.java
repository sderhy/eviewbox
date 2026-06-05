

package com.sderhy ;
import java.awt.*  ;
import java.net.*  ;
import java.util.* ;
/*import PixCanvas ;
import PixObject ;
*/
public class Multiplanar   {
	PixCanvas canvas ;
	Vector vimages ;
	boolean dental = false ;
	Dimension imageDimension ;
	
		public Multiplanar(PixCanvas canvas, boolean  curve ){
			
			this.vimages = canvas.vimages ;
			this.dental = curve ;
			if(verify()){
				letTheUserDraw() ;
			}
		
		}
	
	
		Vector getVector(){ return vimages; }	
	
		public void letTheUserDraw(){
			if(dental){
				DrawJaws dJ = new DrawJaws(this);
				dJ.show();
			}
			else{
			DrawableFrame dF = new DrawableFrame( this ) ;
			dF.show() ;
			}
		}
	
	
///////////////////////////////////Verification//////////////////////////////////////////	
	
	
	
		private boolean verify(){
			if(isVectorEmpty())  return false ;
			if(	!verifyImageSize() ) {
				AlertBox AB = new AlertBox(new Frame(),"","Choose equal sized images !!!") ;
				return	false;
	   		}
	   		return true ;
	   }
	
	
		private boolean  isVectorEmpty(){
			if( vimages.isEmpty()){
				AlertBox AB = new AlertBox(new Frame(), "" ,"Please choose some images !!!") ;
				return true ;
			}
				
			if( vimages.size()<2){
				AlertBox AB = new AlertBox(new Frame(), "" ,"Please choose more than one image !!") ;
				return true ;
			}
		
			return false ;
		}
		
		
		private boolean verifyImageSize(){
		 PixObject po = (PixObject)vimages.lastElement() ;
			imageDimension = po.getSize() ;
			for(int i =0 ; i < vimages.size() ; i++){
	 			 po = (PixObject)vimages.elementAt(i);
	 			 if( !po.getSize().equals(imageDimension)) return false ;
	
			}
		return true ;
		}	
//////////////////////////////////End of verification//////////////////////////////////////////	

	
	}
