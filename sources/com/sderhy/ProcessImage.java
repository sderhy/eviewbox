
/**
*	ProcessImage called by ImageViewer
*	@sderhy
*   @date  3/05/98
*
*/

package com.sderhy;
import java.awt.image.* ;
import java.awt.*;
import java.awt.Image ;

public class ProcessImage {
	final static int CT_BASE = 1024;
	final static int CT_BAND = 2048;
	final static int Pixel_Size = 16;
	static int contrast = 100 ; // must be declared static as the whole class is static ....
	static int brightness = 100 ; // idem .

public static Image brighten2( Component c,Image src, int dxLevel , int dyWindow){
	brightness += dxLevel ;
	contrast += dyWindow ;
	if (contrast > 255)  contrast = 255 ; // high limit for contrast can be lowered or raised
	if (contrast  < -255 ) contrast = -255 ;
	if (brightness > 255 )  brightness = 255 ;
	if (brightness  < 0 ) brightness = 0 ;
	ImageFilter  filter = new Brighten2(brightness,contrast) ; // No,  think contrast/Brightness
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;

}

public static int[] grabPixels(Component c , Image src){
	int w = 	src.getWidth(c) ;
	int h =     src.getHeight(c);
	int pixels[]  = new int[w * h] ;
	PixelGrabber pg = new PixelGrabber(src,0,0,w,h,pixels,0,w);
	try{ pg.grabPixels();} catch(InterruptedException e){;}
	return pixels ;
	}

public static void trackImage(Component c , Image src){
	MediaTracker tr = new MediaTracker(c ) ;
				 tr.addImage(src,0);
	try{tr.waitForID(0) ;} catch(InterruptedException e) {};
	if (tr.isErrorID(0)){
			tools.Tools.debug(ProcessImage.class, "Tracker error "+tr.getErrorsAny().toString() ) ;
			return ;
		}
}

public static Image invertLut( Component c,Image src) {
	ImageFilter  filter = new Invert() ;
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;
	}

public static Image invert( Component c,Image src) {
	int w = 	src.getWidth(c) ;
	int h =     src.getHeight(c);
	int[] pixels = grabPixels(c,src);
	int len = pixels.length ;
	for(int i = 0; i< len ; i++)
		pixels[i] = pixels[i] ^ 0xffffff ;
	return c.createImage(new MemoryImageSource(w,h,pixels,0,w));
	}

public static Image brighten( Component c,Image src) {
	ImageFilter  filter = new Brighten() ;
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;
	}
public static Image brighten( Component c,Image src, int br) {
	ImageFilter  filter = new Brighten() ;
	Brighten.setBrightness(br);
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;
	}



public static Image grayIt( Component c,Image src) {
	ImageFilter  filter = new GrayIt() ;
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;
	}


public static Image pseudoColor( Component c,Image src) {
	ImageFilter  filter = new PseudoColor() ;
	ImageProducer producer = new FilteredImageSource(src.getSource(),filter);
	trackImage(c, src = c.createImage(producer));
	return src;
	}

public static Image flipVertical(Component c, Image src ){
	int w = 	src.getWidth(c) ;
	int h =     src.getHeight(c);
	Image dest  = c.createImage( w , h ) ;
	Graphics g = dest.getGraphics();
	g.drawImage(src,w,h,0,0,0,0,w,h,c);
	g.dispose();
	trackImage(c, dest);
	return dest ;

}

public static Image flipHorizontal(Component c, Image src ){
	int w = 	src.getWidth(c) ;
	int h =     src.getHeight(c);
	Image dest  = c.createImage( w , h ) ;
	Graphics g = dest.getGraphics();
	g.drawImage(src,w,0,0,h,0,0,w,h,c);
	g.dispose() ;

	trackImage(c, dest);
	return dest ;
}

public static Image sharpen (Component c, Image src ){

	int width   = 	src.getWidth(c) ;
	int height =     src.getHeight(c);
	tools.Tools.gc() ;
	int[] pixels = grabPixels(c,src);

		int[] pixels2;
		int i, x, y, offset;
		int rgb, rgb1, rgb2, rgb3, rgb4, rgb5, rgb6, rgb7, rgb8, newrgb;
		int rsum = 0, gsum = 0, bsum = 0;

		pixels2 = new int[width * height];
		for (i = 0; i < width * height; i++)
			pixels2[i] = pixels[i];
		for (y = 1; y < (height - 1); y++) {
			for (x = 1; x < (width - 1); x++) {
				offset = x + y * width;
				rgb1 = pixels2[offset - width - 1];
				rgb2 = pixels2[offset - width];
				rgb3 = pixels2[offset - width + 1];
				rgb4 = pixels2[offset + 1];
				rgb5 = pixels2[offset + width + 1];
				rgb6 = pixels2[offset + width];
				rgb7 = pixels2[offset + width - 1];
				rgb8 = pixels2[offset - 1];
				rgb = pixels2[offset];
				rsum = ((rgb & 0xff0000) >> 16) * 12 - ((rgb1 & 0xff0000) >> 16) - ((rgb2 & 0xff0000) >> 16)
					 - ((rgb3 & 0xff0000) >> 16) - ((rgb4 & 0xff0000) >> 16) - ((rgb5 & 0xff0000) >> 16)
					 - ((rgb6 & 0xff0000) >> 16) - ((rgb7 & 0xff0000) >> 16) - ((rgb8 & 0xff0000) >> 16);
				rsum /= 4;
				if (rsum < 0)
					rsum = 0;
				if (rsum > 255)
					rsum = 255;
				gsum =  ((rgb & 0xff00) >> 8) * 12 - ((rgb1 & 0xff00) >> 8) - ((rgb2 & 0xff00) >> 8)
					- ((rgb3 & 0xff00) >> 8) - ((rgb4 & 0xff00) >> 8) - ((rgb5 & 0xff00) >> 8)
					- ((rgb6 & 0xff00) >> 8) - ((rgb7 & 0xff00) >> 8) - ((rgb8 & 0xff00) >> 8);
				gsum /= 4;
				if (gsum < 0)
					gsum = 0;
				if (gsum > 255)
					gsum = 255;
				bsum =  ((rgb & 0xff) * 12) - (rgb1 & 0xff) - (rgb2 & 0xff) - (rgb3 & 0xff) - (rgb4 & 0xff)
					 - (rgb5 & 0xff) - (rgb6 & 0xff) - (rgb7 & 0xff) - (rgb8 & 0xff);
				bsum /= 4;
				if (bsum < 0)
					bsum = 0;
				if (bsum > 255)
					bsum = 255;
				newrgb = ((rsum << 16) & 0xff0000) | ((gsum << 8 ) & 0xff00) | bsum;
				pixels[offset] = (rgb & 0xff000000) | (newrgb & 0xffffff);

			}
		}


	return c.createImage(new MemoryImageSource(width,height,pixels,0,width));
	}//endOfSharpenImage

public static Image blur(Component c, Image src ){

	int width = 	src.getWidth(c) ;
	int height =     src.getHeight(c);
	tools.Tools.gc() ;
	int[] pixels = grabPixels(c,src);
		int[] pixels2;
		int i, x, y, offset;
		int rgb, rgb1, rgb2, rgb3, rgb4, rgb5, rgb6, rgb7, rgb8, rgbmean;
		int rsum = 0, gsum = 0, bsum = 0;

		pixels2 = new int[width * height];
		for (i = 0; i < width * height; i++)
			pixels2[i] = pixels[i];
		for (y = 1; y < (height - 1); y++) {
			for (x = 1; x < (width - 1); x++) {
				offset = x + y * width;
				rgb1 = pixels2[offset - width - 1];
				rgb2 = pixels2[offset - width];
				rgb3 = pixels2[offset - width + 1];
				rgb4 = pixels2[offset + 1];
				rgb5 = pixels2[offset + width + 1];
				rgb6 = pixels2[offset + width];
				rgb7 = pixels2[offset + width - 1];
				rgb8 = pixels2[offset - 1];
				rgb = pixels2[offset];
				rsum = (rgb1 & 0xff0000) + (rgb2 & 0xff0000) + (rgb3 & 0xff0000) + (rgb4 & 0xff0000) + (rgb5 & 0xff0000)
					+ (rgb6 & 0xff0000) + (rgb7 & 0xff0000) + (rgb8 & 0xff0000) + (rgb & 0xff0000);
				gsum = (rgb1 & 0xff00) + (rgb2 & 0xff00) + (rgb3 & 0xff00) + (rgb4 & 0xff00) + (rgb5 & 0xff00)
					+ (rgb6 & 0xff00) + (rgb7 & 0xff00) + (rgb8 & 0xff00) + (rgb & 0xff00);
				bsum = (rgb1 & 0xff) + (rgb2 & 0xff) + (rgb3 & 0xff) + (rgb4 & 0xff) + (rgb5 & 0xff)
					+ (rgb6 & 0xff) + (rgb7 & 0xff) + (rgb8 & 0xff) + (rgb & 0xff);
				rgbmean = ((rsum/9) & 0xff0000) | ((gsum/9) & 0xff00) | (bsum/9);
				pixels[offset] = (rgb & 0xff000000) | (rgbmean & 0xffffff);
			}
		}

		pixels2 = null ;
		tools.Tools.gc() ;

		return c.createImage(new MemoryImageSource(width, height,pixels,0, width));
	}//endOfrotateImage() ;


public static Image rotateImage(Component c, Image src ){

	int w = 	src.getWidth(c) ;
	int i = 0;
	int h =     src.getHeight(c);
	tools.Tools.gc() ;
	int[] pixels = grabPixels(c,src);
	int[]  rotPixels = new int[w * h]  ;
	for (int x = 0 ; x < w ; x++ )
			for (int y=0; y < h; y++) {
				rotPixels[i]  =  pixels[ (y * w) - x  +  w-1] ;
				i++ ;
				}
	return c.createImage(new MemoryImageSource(h,w,rotPixels,0,h));
	}//endOfrotateImage() ;


public static Image rotateRight(Component c, Image src ){

	int w = 	src.getWidth(c) ;
	int i = 0;
	int h =     src.getHeight(c);
	tools.Tools.gc() ;
	int[] pixels = grabPixels(c,src);
	int[]  rotPixels = new int[w * h]  ;
	for (int x = 0 ; x < w ; x++ )
			for (int y=0; y < h; y++) {
				rotPixels[i]  =  pixels[w * (h-y) + x - w] ;
				i++ ;
				}
	return c.createImage(new MemoryImageSource(h,w,rotPixels,0,h));
	}//endOfrotateImage() ;

}//end Of Class

class Invert extends RGBImageFilter{

	public Invert(){
		canFilterIndexColorModel = true ;
		}

	public int filterRGB(int x, int y, int rgb){
		return (	(rgb & 0xff000000 )+		// alpha chanel
				(rgb & 0xffffff) ^ 0xffffff); //xOr
		}

}//endOfClassInvert

class GrayIt extends RGBImageFilter{
	public GrayIt(){
		canFilterIndexColorModel = true ;
		}
	public int filterRGB(int x, int y, int rgb){
		int r = (rgb& 0xff0000) >> 16 ;
		int g = (rgb& 0x00ff00 ) >> 8 ;
		int b = (rgb& 0x0000ff) ;
		int gray = ((r*3) + (g*4) + (b*2))/9 ;
		return  0xff000000|gray<<16|gray<<8|gray ;
		}

}//endOfClassInvert

class Brighten extends RGBImageFilter{
	public  static int  percent  = 120 ;
	public Brighten(){
		canFilterIndexColorModel = true ;
		percent =120;
	}


	public int filterRGB(int x, int y, int rgb){

		int r = (rgb& 0xff0000) >> 16 ;
		int g = (rgb& 0x00ff00 ) >> 8 ;
		int b = (rgb& 0x0000ff) ;

		r = Math.min(255, (r*percent)/100) ;
		g = Math.min(255, (g*percent)/100) ;
		b = Math.min(255, (b*percent)/100) ;

			return  0xff000000|r<<16|g<<8|b ;
		}
	public static void setBrightness( int br){
		percent = br ;
		if(percent <0 ) percent=0;
	}

}//endOfBrighten

// Here we have made a change on 17/09/00 :
/*	This class does two things :
*	First it defines a range of contrast by defining i and j as double
*	Second : it defines a brightness by multiplying the pixel value by
*	a fixed number.
*	if contrast value is 100 : nothing is changed.
*	if brightness value is 100 : nothing is changed either .
*/
class Brighten2 extends RGBImageFilter{
	int l , w;

	public  Brighten2(int brightness, int contrast){
		canFilterIndexColorModel = true ;
		if(brightness<0) brightness =0 ; // else inverse curve white/black
		l = brightness -100 ;
		w = contrast ;
		if (l > 255)  l = 255 ;
		if (l <  -255 ) l = -255 ;

	}
	// Set contrast first and brightness :
	private final int filter(int color){

		color = (int) ( color * w / 100  ) +l;
		if( color < 0) color = 0;
		if( color > 255) color = 255 ;
		return color ;
	}
	public int filterRGB(int x, int y, int rgb){

		//int r = (rgb & 0xff0000) >> 16 ;
		//int g = (rgb & 0x00ff00 ) >> 8 ;
		int b = (rgb & 0x0000ff) ;

		//r = filter(r) ; g = filter(g) ;
		 b = filter(b) ;

		/*
		 r = Math.min(  	Math.max( (int)((i+j*r)*l/100),0)		,255);
		 g = Math.min(  	Math.max( (int)((i+j*g)*l/100),0)		,255);
		 b = Math.min(  	Math.max( (int)((i+j*b)*l/100),0)		,255);
		*/
		return  0xff000000|b<<16|b<<8|b ;
	  }
}// En of Brighten3

class PseudoColor extends RGBImageFilter{
	java.util.Random rnd ;
	int ra,ga,ba ;

	public PseudoColor(){
		canFilterIndexColorModel = true ;
		rnd=new java.util.Random();
		ra = (int)( 255* rnd.nextFloat() );
		ga = (int)( 255* rnd.nextFloat() );
		ba = (int)( 255* rnd.nextFloat() );



		}

	public int filterRGB(int x, int y, int rgb){

		//tools.Tools.debug(this , "appel de filterRGB") ;
		int r = (rgb& 0xff0000) >> 16 ;
		int g = (rgb& 0x00ff00 ) >> 8 ;
		int b = (rgb& 0x0000ff) ;

		/*
		int ra = (int)(r* rnd.nextFloat() );
		int ga = (int)(g* rnd.nextFloat() );
		int ba = (int)(b* rnd.nextFloat() );
	 */
		r = Math.min(255, r^ra  ) ;
		g = Math.min(255, g^ga	) ;
		b = Math.min(255, b^ba) ;


			return  0xff000000|r<<16|g<<8|b ;
		}

}//endOfPseudoColor

class RotateFilter extends ImageFilter
{
	public RotateFilter()
	{
	}
	public void setHints(int hints)
	{
		consumer.setHints(hints & ~(ImageConsumer.COMPLETESCANLINES +
			ImageConsumer.TOPDOWNLEFTRIGHT));
	}

	public void setDimensions(int width, int height)
	{
		consumer.setDimensions(height, width);
	}

	public void setPixels(int x, int y, int width, int height,
		ColorModel model, byte[] pixels, int offset, int scansize)
	{
		byte[] rotatePixels = new byte[pixels.length];

		for (int ry=0; ry < height; ry++) {
			for (int rx=0; rx < width; rx++) {

				rotatePixels[rx*height + ry] =
					pixels[(ry+1)*scansize-rx-1+offset];
			}
		}
		consumer.setPixels(y, x, height, width, model, rotatePixels,
			0, height);
	}

	public void setPixels(int x, int y, int width, int height,
		ColorModel model, int[] pixels, int offset, int scansize)
	{

		int[] rotatePixels = new int[pixels.length];

		for (int ry=0; ry < height; ry++) {
			for (int rx=0; rx < width; rx++) {

				rotatePixels[rx*height + ry] =
					pixels[(ry+1)*scansize-rx-1+offset];
			}
		}
		consumer.setPixels(y, x, height, width, model, rotatePixels,
			0, height);
	}
}
