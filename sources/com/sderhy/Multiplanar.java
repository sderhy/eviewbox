

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

			private SliceGeometry geometry ;
			private boolean geometryBuilt = false ;

			/** Per-slice through-plane geometry (sorted physical positions), or
			*	null when the stack carries no usable SliceLocation : the
			*	reconstructions then fall back to uniform spacing. */
			SliceGeometry getSliceGeometry(){
				if(!geometryBuilt){
					geometry = SliceGeometry.build(vimages) ;
					geometryBuilt = true ;
				}
				return geometry ;
			}

			/** Median |Δ SliceLocation| between consecutive loaded slices, in mm ;
			*	-1 when fewer than one usable pair. */
			private double medianLocationStep(){
				int n = vimages.size() ;
				double[] steps = new double[Math.max(0, n - 1)] ;
				int count = 0 ;
				for(int i = 1 ; i < n ; i++){
					double a = ((PixObject)vimages.elementAt(i - 1)).sliceLocation ;
					double b = ((PixObject)vimages.elementAt(i)).sliceLocation ;
					if(Double.isNaN(a) || Double.isNaN(b)) continue ;
					steps[count++] = Math.abs(b - a) ;
				}
				if(count == 0) return -1 ;
				java.util.Arrays.sort(steps, 0, count) ;
				return steps[count / 2] ;
			}

			int getSliceSpacingInPixels(int fallback){
				if(vimages == null || vimages.isEmpty()) return fallback ;
				PixObject po = (PixObject)vimages.firstElement();
				double sliceSpacing = po.spacingBetweenSlices;
				// Center-to-center distance BEFORE SliceThickness (0018,0050 is the
				// slab thickness : on overlapping acquisitions it over-talls the
				// stack ; with gaps it under-talls it). Use the MEDIAN of the
				// consecutive |Δlocation| over the whole stack : the first pair may
				// straddle a series boundary (mixed-series folders) and its jump
				// would wildly over-stretch everything.
				if(sliceSpacing <= 0) sliceSpacing = medianLocationStep();
				if(sliceSpacing <= 0) sliceSpacing = po.sliceThickness;
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
