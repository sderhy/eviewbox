

package com.sderhy ;
import java.awt.*  ;
import java.net.*  ;
import java.util.* ;
/*import PixCanvas ;
import PixObject ;
*/
public class Multiplanar   {
	static final int FRONTAL = 0 ;
	static final int SAGITTAL = 1 ;
	static final int CURVED = 2 ;
	PixCanvas canvas ;
	Vector vimages ;
	boolean dental = false ;
	int mode = FRONTAL ;
	int currentImage = -1 ;
	Dimension imageDimension ;
	boolean autoDraw = true ;
	public boolean valid = false ;
	public DrawableFrame frame ;

		public Multiplanar(PixCanvas canvas, boolean  curve ){
			this(canvas, curve ? CURVED : FRONTAL, -1);
		}

		public Multiplanar(PixCanvas canvas, int mode, int currentImage){
			this(canvas, mode, currentImage, true) ;
		}

		public Multiplanar(PixCanvas canvas, int mode, int currentImage, boolean autoDraw){
			this.vimages = canvas.vimages ;
			this.mode = mode ;
			this.dental = (mode == CURVED) ;
			this.currentImage = currentImage ;
			this.autoDraw = autoDraw ;
			valid = verify() ;
			if(valid && autoDraw) letTheUserDraw() ;
		}

		/** Build the frontal/sagittal reconstruction engine WITHOUT opening a
		*	cut-selection window. The result viewer is shown ; the caller drives the
		*	cut line itself (e.g. drawing it on the live image viewer). */
		public DrawableFrame buildFrame(){
			frame = new DrawableFrame(this, mode) ;
			return frame ;
		}


			Vector getVector(){ return vimages; }

			int getSliceSpacingInPixels(int fallback){
				if(vimages == null || vimages.isEmpty()) return fallback ;
				PixObject po = (PixObject)vimages.firstElement();
				double sliceSpacing = po.spacingBetweenSlices;
				if(sliceSpacing <= 0) sliceSpacing = po.sliceThickness;
				if(sliceSpacing <= 0 && vimages.size() > 1){
					PixObject next = (PixObject)vimages.elementAt(1);
					if(po.sliceLocation > -1 && next.sliceLocation > -1)
						sliceSpacing = Math.abs(next.sliceLocation - po.sliceLocation);
				}
				double pixelSpacing = po.pixelSpacingRow;
				if(pixelSpacing <= 0) pixelSpacing = po.pixelSpacingColumn;
				if(sliceSpacing <= 0 || pixelSpacing <= 0) return fallback ;
				return Math.max(1, (int)Math.round(sliceSpacing / pixelSpacing));
			}

			public void letTheUserDraw(){
			if(mode == CURVED){
				DrawJaws dJ = new DrawJaws(this);
				dJ.show();
			}
			else{
			DrawableFrame dF = new DrawableFrame( this, mode ) ;
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
