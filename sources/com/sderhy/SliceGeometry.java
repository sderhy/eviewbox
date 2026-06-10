package com.sderhy ;
import java.util.* ;

/**
*	Through-plane geometry of a slice stack : every slice's physical position
*	(SliceLocation 0020,1041) converted to in-plane pixels and sorted, so the
*	reconstructions can resample the slice axis by PHYSICAL position instead of
*	assuming one uniform thickness. This is what makes the stack height correct
*	on non-uniform / overlapping acquisitions.
*
*	build() returns null when the stack has no usable per-slice positions
*	(missing SliceLocation, missing PixelSpacing, zero span...) ; callers then
*	keep their legacy uniform-spacing path.
**/
public final class SliceGeometry {
	/** order[k] = index in vimages of the k-th slice along the stack. The
	*	direction follows the LOAD order (first loaded slice maps to row 0), so
	*	fixing the spacing does not vertically flip the reconstruction. */
	public final int[] order ;
	/** pos[k] = through-plane position of slice order[k], in pixels of the
	*	in-plane pixel spacing. Ascending, pos[0] == 0. */
	public final double[] pos ;
	/** Mean center-to-center spacing in mm (used to map the Thickness menu to
	*	a vertical zoom factor). */
	public final double meanSpacingMm ;

	private SliceGeometry(int[] order, double[] pos, double meanSpacingMm){
		this.order = order ;
		this.pos = pos ;
		this.meanSpacingMm = meanSpacingMm ;
	}

	public int count(){ return pos.length ; }

	/** Physical height of the stack in pixels (distance first-to-last center). */
	public double spanPx(){ return pos[pos.length - 1] ; }

	public static SliceGeometry build(Vector vimages){
		int n = (vimages == null) ? 0 : vimages.size() ;
		if(n < 2) return null ;
		PixObject first = (PixObject)vimages.firstElement() ;
		double pxSpacing = first.pixelSpacingRow ;
		if(pxSpacing <= 0) pxSpacing = first.pixelSpacingColumn ;
		if(pxSpacing <= 0) return null ;

		// SliceLocation must be present on EVERY slice (NaN = not set ; real
		// locations are routinely negative) ; otherwise positions are meaningless.
		double[] loc = new double[n] ;
		for(int i = 0 ; i < n ; i++){
			PixObject po = (PixObject)vimages.elementAt(i) ;
			if(Double.isNaN(po.sliceLocation)) return null ;
			loc[i] = po.sliceLocation ;
		}
		return build(loc, pxSpacing) ;
	}

	/** Core of build(), on raw locations (mm) : kept separate so the geometry
	*	can be exercised without constructing PixObjects (which need an Image). */
	static SliceGeometry build(double[] loc, double pxSpacing){
		int n = loc.length ;
		if(n < 2 || pxSpacing <= 0) return null ;
		for(int i = 0 ; i < n ; i++) if(Double.isNaN(loc[i])) return null ;

		// Sort slice indices by location (insertion sort : stacks are small and
		// usually already sorted, and stability keeps duplicates in load order).
		int[] order = new int[n] ;
		for(int i = 0 ; i < n ; i++) order[i] = i ;
		for(int i = 1 ; i < n ; i++){
			int v = order[i] ;
			int j = i - 1 ;
			while(j >= 0 && loc[order[j]] > loc[v]){ order[j + 1] = order[j] ; j-- ; }
			order[j + 1] = v ;
		}
		double spanMm = loc[order[n - 1]] - loc[order[0]] ;
		if(spanMm <= 0) return null ;   // all slices at the same position

		// Sanity check on the SORTED steps : a coherent stack has roughly
		// uniform spacing (median ≈ max). A folder mixing several series
		// interleaves locations (median step ≪ max step), and a stack with a
		// huge acquisition gap would smear an interpolation across it — in both
		// cases positions are not trustworthy, fall back to uniform spacing.
		double[] steps = new double[n - 1] ;
		for(int k = 1 ; k < n ; k++) steps[k - 1] = loc[order[k]] - loc[order[k - 1]] ;
		double[] sorted = (double[])steps.clone() ;
		java.util.Arrays.sort(sorted) ;
		double median = sorted[(n - 1) / 2] ;
		if(median <= 0 || sorted[n - 2] > 4 * median) return null ;

		// Keep the load direction : if the files were loaded high-to-low
		// location, reverse so order[0] is still the first loaded end.
		if(loc[0] > loc[n - 1]){
			for(int a = 0, b = n - 1 ; a < b ; a++, b--){
				int t = order[a] ; order[a] = order[b] ; order[b] = t ;
			}
		}
		double[] pos = new double[n] ;
		double loc0 = loc[order[0]] ;
		for(int k = 0 ; k < n ; k++) pos[k] = Math.abs(loc[order[k]] - loc0) / pxSpacing ;
		return new SliceGeometry(order, pos, spanMm / (n - 1)) ;
	}

	/**
	*	Map outH output rows onto a stack of count slices at ascending (or
	*	descending-by-abs, see build) positions pos[]. For output row r :
	*	k0[r] = index of the slice at or below the row's physical position,
	*	frac[r] = interpolation weight toward slice k0[r]+1 (0..1).
	*	Works on any positions array, so callers may pass a decimated subset.
	**/
	public static void resampleMap(double[] pos, int count, int outH, int[] k0, float[] frac){
		if(count < 2){
			for(int r = 0 ; r < outH ; r++){ k0[r] = 0 ; frac[r] = 0f ; }
			return ;
		}
		double z0 = pos[0] ;
		double span = pos[count - 1] - z0 ;
		int k = 0 ;
		for(int r = 0 ; r < outH ; r++){
			double z = (outH == 1) ? z0 : z0 + span * r / (outH - 1) ;
			while(k < count - 2 && pos[k + 1] <= z) k++ ;
			double d = pos[k + 1] - pos[k] ;
			float f = (d <= 0) ? 0f : (float)((z - pos[k]) / d) ;
			if(f < 0f) f = 0f ; else if(f > 1f) f = 1f ;
			k0[r] = k ;
			frac[r] = f ;
		}
	}
}
