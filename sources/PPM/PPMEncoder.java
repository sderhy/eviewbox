package PPM;

import java.io.*;
import java.awt.*;
import java.awt.image.*;

public class PPMEncoder
{
   int width;
   int height;
   Image src ;
   

   public PPMEncoder(Image src)
   {
      		width = 	src.getWidth(null) ;
			height =    src.getHeight(null);
			this.src = src ;
	
   }

	
   private void encodeHeader(OutputStream out) throws IOException
	{
	   writeString( out, "P6\n" );
	   writeString( out, width + " " + height + "\n" );
	   // Write the maximum value of each color.
	   // i.e. r,g,b all vary between 0 and 255.
	   writeString( out, "255\n" );
	}

   private static void writeString(OutputStream out, String str) throws IOException
	{
	   int len = str.length();
	   byte[] buf = new byte[len];
	   str.getBytes( 0, len, buf, 0 );
	   out.write(buf);
	}

   public void write(java.io.OutputStream out) throws IOException{
      
      encodeHeader(out);
     //encode pixels
      int j;
      int i = 0;
      int pixels[]  = new int[width * height] ;
      int argb ;
	  PixelGrabber pg = new PixelGrabber(src,0,0,width,height,pixels,0,width);
			try{ pg.grabPixels();} catch(InterruptedException e){;}
			
	   byte[] ppmPixels = new byte[width * 3];

	   for ( int row = 0; row < height; ++row )
      {
         j = 0;
	      for ( int col = 0; col < width; ++col )
		   {
		   	  argb = pixels[i] ;
		      ppmPixels[j++] = (byte)(    (argb  & 0xff0000) >> 16 	 );//r
		      ppmPixels[j++] = (byte)(    (argb  & 0xff00) >>  8     );//g
		      ppmPixels[j++] = (byte)(     argb   & 0xff	          ) ;//b
		      i++ ;
		   }
         // Write ppm file a row at a time.
	      out.write(ppmPixels);
	   }
	}//endOfEncode();
}
