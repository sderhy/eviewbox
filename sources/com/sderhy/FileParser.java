// FileParser.java

package com.sderhy ;
import java.awt.*;
import java.io.* ;
import java.util.*;
import tools.*;
//import PixObject;

/**
*		@author sderhy@imaginet.fr
*		@date 05071997
*		@version 1a </P><P>
*		FileReader a simple class to read a file , 
*		has to be extended with time , creating more constructors
*		and  letting the user browse the local files
*		or filter files
*		etc...</P>
*/

public class FileParser extends Frame
{

// Instances variables 
	final static boolean debug = false ;
	final static int MAXSIZE= 1000000 ;
	final static int FILEHEADER_MAX = 15000 ;
	final static int DATASIZE_MIN = 128*128;

	private MainClass parent ;
	private int size, height, width , highBit = 15, bitsAllocated = 8;
	private int bitsStored = bitsAllocated ;
	private int fileHeaderSize , dataSize ;
	private int byteSelected = 0;
	private boolean signed = false ;
	public boolean ignoreNegValues = true ;
 	private byte[]  data;
	private String fileName;
	private int maxSize = MAXSIZE;
	//private String errMessage = null;
	private File f ;
	private TextField  HeightTF,WidthTF,FileHeaderTF, dataSizeTF ;
	private	CheckboxGroup CBG1, CBG2 ,CBG3 ;
	private	Checkbox	LittleEnd, BigEnd, HeightPerPix , SixtPerPix;
	private  	Checkbox Signed , Unsigned ;
	private 	Checkbox	IgnoreNegVal;
	private	Button Guess, Cancel, OK ;
	private	Label l1 , canOpenLabel;
	private  boolean canOpen =  false , yes = false ;
	
	
	
public FileParser(String fileName, MainClass parent) throws IOException 
	{	
		super(/*(java.awt.Frame)parent, */"File Parser Dialog "/*, true*/) ;
		this.parent = parent ;
		this.fileName = fileName ;
		f = new File(fileName);
		size = (int) f.length();
		if ( size < maxSize ){ 
		
				
				
				int bytes_read = 0;
				FileInputStream in = new FileInputStream(f);
				data = new byte[size];
				while(bytes_read < size)
				{	
					bytes_read += in.read(data, bytes_read, size - bytes_read);
					
				}
				in.close();
				
				
				
				
				initFileDialog();// the main dialog
		}//endif(size....
		else{//file > maxSize
			cancel();
			AlertBox AB = new AlertBox(  parent , "This file is too big  for me !");
		}//endelse

	} //Endofconstructor ...
	

	
//______________________InitFileDialog______setting up the window dialog________________	
/**
*	initFileDialog : consist in a window where the user init
*	the file Dialog;
*	this is usefull when you don't know how to read the header 
*/
	 protected  void initFileDialog (){
		
			this.setBackground(Color.white);
		
// setting up the top Panel  of the default borderLayout of the frame :::
		InsetPanel top = new InsetPanel(Color.lightGray);
		top.setEtched (true,false) ;
		
			Label l0 = new Label(" File name : " + f.getName() );
			l0.setFont( new Font("Helvetica",Font.BOLD, 12));
			top.add(l0 );
		// label last modified lastMod :	
			Date  date =  new Date( f.lastModified()  );
			Label lastMod = new Label("Last modified :"+date.toString());
			lastMod.setFont( new Font("Helvetica",Font.BOLD, 12));
			top.add(lastMod);
			this.add("North",top);
		
// Setting up the center panel	for the Center of the frame in which there  will be nested panels...	
			InsetPanel Center = new InsetPanel(Color.gray);
			Center.setEtched( true,false );
			Center.setLayout(new GridLayout(4,1,5,5));

//first Panel topPanel :
		Panel topPanel = new InsetPanel(5,50,Color.lightGray);
			topPanel.setLayout(new GridLayout(3,3,0,0));
		
			CBG1 = new CheckboxGroup();
				 HeightPerPix = new Checkbox (" 8 Bits/pixel", CBG1, true );
				 SixtPerPix = new Checkbox (" 16  Bits/pixel", CBG1, false );
			CBG2 =	new CheckboxGroup();
				 LittleEnd = new Checkbox ("LittleEndian", CBG2, true );
				 BigEnd = new Checkbox ("BigEndian", CBG2, false );
			
			CBG3 = new CheckboxGroup();
				Unsigned =  new Checkbox ("Unsigned", CBG3, true );
				Signed =  new Checkbox ("Signed", CBG3, false );	
				
			//first line 	
				topPanel.add(new Label(" High bit " ));
				topPanel.add(new Label(" Bit allocated " ));
				topPanel.add(new Label(" Pixel representation" ));
				
			//Second line :	
				topPanel.add(BigEnd);
				topPanel.add(SixtPerPix);
				topPanel.add(Signed);
				
			//Third Line :
				topPanel.add(LittleEnd);
				topPanel.add(HeightPerPix);
				topPanel.add(Unsigned);
				
				
				
		Center.add(topPanel);

//Second Panel MatrixPanel :		
		Panel MatrixPanel = new InsetPanel(20,50,Color.lightGray);
			MatrixPanel.setLayout(new GridLayout(2,4,2,2) );
		//first line :	
			HeightTF = new TextField(4);
			MatrixPanel.add( new Label("Height :") );
			MatrixPanel.add(HeightTF);
			MatrixPanel.add(new Label ("Pixels") );
			MatrixPanel.add(new Label ("Negative Values") );
		//Second Line	
			WidthTF = new TextField(4);
			MatrixPanel.add( new Label(" Width :") );
			MatrixPanel.add(WidthTF);
			MatrixPanel.add(new Label ("pixels") );
				IgnoreNegVal = new Checkbox ("Ignore ");
				IgnoreNegVal.setState(true);
			MatrixPanel.add( IgnoreNegVal);
			
		Center.add(MatrixPanel);
		
//Third Panel TFPanel		
		Panel TFPanel = new InsetPanel(20,50,Color.lightGray);
			TFPanel.setLayout(new GridLayout(2,3,2,2) );
			
		//first Line :
			FileHeaderTF = new TextField(4);
			FileHeaderTF.setEditable(false);
			TFPanel.add( new Label(" File Header Size :") );
			TFPanel.add(FileHeaderTF);
			TFPanel.add(new Label (" bytes") );
			
			
 		//second line :
 			dataSizeTF = new TextField(4);
 			dataSizeTF.setEditable(false);
			TFPanel.add( new Label(" File Data   Size :") );
			TFPanel.add(dataSizeTF);
			TFPanel.add(new Label (" bytes") );
			
		Center.add(TFPanel);
		
//forth panel   fourthPanel :
		InsetPanel fourthPanel = new InsetPanel(Color.lightGray);
		fourthPanel.setEtched(true);// not quite cute ?
			fourthPanel.setLayout(new GridLayout(3,1) );
			l1 = new Label("Bytes selected : " + byteSelected + " bytes" , Label.CENTER);
			Label l2 = new Label("File size :  " + size + " bytes", Label.CENTER );
			canOpenLabel = new Label(" May  be open   :  " + canOpen , Label.CENTER );
			fourthPanel.add(l1);
			fourthPanel.add(l2);
			fourthPanel.add(canOpenLabel);
			
		Center.add(fourthPanel);
		
		
//end of Center panel add it to the frame :		
		this.add("Center", Center);
		
// Setting the South Panel
		InsetPanel southPanel = new InsetPanel(Color.pink);
		southPanel.setEtched ( true, false) ;
		 Guess = new Button( "Guess ");
		 Cancel = new Button ( "Cancel" );
		 OK = new Button ("  OK  ")		;
		 OK.disable();			
		southPanel.add(Guess);
		southPanel.add(Cancel);
		southPanel.add( OK);
		this.add("South", southPanel);
		
		this.move( 10, 20 );
		this.pack();
		this.show();
	
	}// end of initFileDialog ;
//___________________________________________________________

public boolean handleEvent(Event event){
	
	switch(event.id ){
		case Event.ACTION_EVENT :
			try {
				if (event.target == HeightTF ){
					height = Integer.parseInt(HeightTF.getText());
					updateData();
				}
				if (event.target == WidthTF ){
					width = Integer.parseInt(WidthTF.getText());
					updateData();
				}
				
////These are not editable fields :
//				if (event.target == FileHeaderTF ){
//					fileHeaderSize = Integer.parseInt(FileHeaderTF.getText());
//					updateData();
//				}
//				if( event.target == dataSizeTF ){
//					dataSize = Integer.parseInt(dataSizeTF.getText());
//					updateData();
//				}
				if(event.target == HeightPerPix || event.target == SixtPerPix){
					if(event.target == HeightPerPix ) bitsAllocated = 8 ;
					else if(event.target == SixtPerPix) bitsAllocated = 16 ;
					updateData();

				}
				if(event.target == Signed || event.target == Unsigned){
					if(event.target == Signed ) signed = true ;
					else if(event.target == Unsigned) signed = false ;
					updateData();//  not usefull

				}
				if(event.target == LittleEnd || event.target == BigEnd){
					if(event.target == BigEnd ) this.highBit= 7 ;
					else if(event.target == LittleEnd) this.highBit=15 ;
					updateData();// ! not usefull

				}
				if(event.target == IgnoreNegVal ) {
					ignoreNegValues =IgnoreNegVal.getState();
				}
			
				
			}catch(NumberFormatException NFEx)
					{ };// end of trycatch
						
			
			if(	event.target == OK ){
					okayButton();
			}
			if(	event.target == Guess ){
					guessWhat();
			}
				if(	event.target == Cancel ){
					cancel();
					}
			break;
		case Event.WINDOW_DESTROY :
					cancel();
				break;
		case Event.WINDOW_ICONIFY :
					cancel();
				break;
			
						
	}//end of switch
	return  super.handleEvent(event);
}// endof hanleEvent________________________________________________________

//_________cancel()____method called with window destroy and with cancel button_________
 	protected void cancel(){
 		this.hide();
 		this.dispose();
 		parent.canvas.repaint() ;
 		Tools.gc() ;
 		return ;
 	}// end of cancel method___

//_________OkayButton()__________________________________________

	public  synchronized boolean answer() {
		try {
				wait();
			} catch (InterruptedException e) { }
		return yes;
	}

	 synchronized protected   void okayButton(){
			
			updateData();
			
			if (canOpen){
				
				this.hide();
				yes = true ;
				openImage() ;
				dispose();
				Tools.gc() ;
				parent.canvas.repaint() ;
				//notifyAll();
				
			}
			else {
				AlertBox AB = new AlertBox(parent, "This file cannot be open !");
				cancel();
			}//end else
	return;
	
	}// end of okayButton()______________
	
/**
*		guessWhat is Public, this function try to guess what kind of data  are included in this file
*		assuming that we are dealing with a square matrix , this is usually the case in medical imaging .
*/

//---------guessButton !---------------------------- :
	public void guessWhat(){
		
		
		if(size >= 672100){
			height = size = 820 ;
			bitsAllocated  = 8 ;	
			}	
		else if (size <  672100 && size >= 524288){//case 512*512.*2
			height = width = 512 ;
			bitsAllocated = 16;
			}
		else if(size <	524288 && size >= 262144){//case 512*512*1
			height = width = 512 ;
			bitsAllocated = 8;
			}
		else if(size <262144 && size >= 204800){// case 320*320*2
			 height = width = 320 ;
			bitsAllocated = 16;
			}
		else if(size <204800 && size >= 131072){// case256*256*2
			height = width = 256 ;
			bitsAllocated = 16;
			}
		else if(size <131072 && size >= 102400){// case 320*320*1
			height = width = 320 ;
			bitsAllocated = 8;
			}
		else if(size <102400 && size >= 65536){// case 256*256*1
			height = width = 256 ;
			bitsAllocated = 8;
			}
		else if(size <65536 && size >= 32728){// case 128*128*2
			height = width = 128 ;
			bitsAllocated = 16;
			}
		else if(size < 32728  && size >= 16384){// case 128*128*1
			height = width = 128 ;
			bitsAllocated = 8;
			}
		else if (size < 16384 ){
			AlertBox AB = new AlertBox (parent , "This file is too small !");
			cancel();
		}
		updateData();

	}//end of guessWhat()

/**
*	updateData() 
*	this fonction is used to  update the datas, compute the canOpen constant and set the label canOpenLabel
*	to indicate the state of this boolean constant.
*/
	protected  void updateData()	{

		dataSize = height * width * (bitsAllocated/8);
		if ( dataSize <= size && dataSize > DATASIZE_MIN){
			fileHeaderSize = size - dataSize ;
			byteSelected = size ;
			canOpen = true ;
			OK.enable();
			setParameters(height,width,fileHeaderSize,dataSize);
			return ;
		}
		else{
			byteSelected = dataSize;
			canOpen = false ;
			OK.disable();
			setParameters(height,width,fileHeaderSize,dataSize);
		 return ;
		}	
	}// end of updateData___________________________________________________________
	
/**
* setParameters is to be used only by update fonction 	
*/
	private void  setParameters(int h, int w, int fH, int dS){
	// set the Checkboxes ...
		if (bitsAllocated == 16 )SixtPerPix.setState(true);
			else if(bitsAllocated == 8 )HeightPerPix.setState(true);
	//set Textfields		
		HeightTF.setText(String.valueOf(h));
		WidthTF.setText(String.valueOf(w));
		FileHeaderTF.setText(String.valueOf(fH));
		dataSizeTF.setText(String.valueOf(dS));
		l1.setText("Bytes selected : " + byteSelected + " bytes" );
	// setLabels
		if ( canOpen == true && fileHeaderSize > FILEHEADER_MAX)
			canOpenLabel.setText(" May  be open   :  " + canOpen + "  but not recommended !" );
		else
			canOpenLabel.setText(" May  be open   :  " + canOpen );
	
	repaint() ;
	return;
	}// endof setParameters_________________________________________________________


	
//______________ACCESSORS_________________________________	
/*
	
	public byte[] getData() 			{	return data ;		}

	public byte[] getHeader() 		{
		byte[]  header = new byte[ fileHeaderSize ];
		java.lang.System.arraycopy (data,0,header,0,fileHeaderSize );	
		return header ;	
		}// end of getHeader 
		
*/
	public byte[] getPixData()	{
		byte[]  pixData = new byte[ dataSize ];
		java.lang.System.arraycopy (data,fileHeaderSize, pixData,0, dataSize );	
		return pixData ;
	}
		
	
	public void openImage()
	{
		
  		String fileURL = "file:/"+File.separator+ fileName;
		parent.TF.setText( fileURL ) ;
		java.net.URL url = null;
		try{  url = new java.net.URL(fileURL) ;}
			catch (java.net.MalformedURLException e){
				parent.TF.setText("Not a valid File URL" );
				return ;
		}
	
	
			if(canOpen){
    		DicomDecoder.DicomReader 
    		dR = new DicomDecoder.DicomReader(
    					getPixData(),// byte[] pixels,
						width,
						height,
						highBit,	
						bitsAllocated,
						bitsAllocated,
						signed,
						1 ,//sample per pixel
						1,//int numberOfFrames
						ignoreNegValues ) ;
    		
    		Image img = dR.getImage() ;
    		MediaTracker tr = new MediaTracker(this ) ;
			tr.addImage(img,0);
    		
    		try{tr.waitForID(0) ;} catch(InterruptedException e) {};
	
    		
    		
    		PixObject ob = new PixObject(url,img , parent.canvas,false, null) ;		
    		Tools.gc();
    		parent.canvas.vimages.addElement(ob) ;
    		//parent.canvas.repaint();
    		}//endif CanOpen() ;
    	}
	
	
	
/*	
	public int 	getWidth()			{	return width;		}
	public int	getHeight()			{	return height ;	}
	public int	getHighBit()		{	return highBit ;	}
	public int 	getbitsAllocated()		{ 	return bitsAllocated;}
	public int 	getbitsStored()		{ 	return bitsAllocated;}
	public int	getFileSize()			{ 	return size ;		}
	public	int		getHeaderSize()		{return fileHeaderSize;}
	public int 	getDataSize()		{ 	return dataSize;	}
	public int 	getTheSize() 		{	return size ;		}
	public boolean getCanOpen()		{ 	return canOpen ;	}
	public int	getMaxSize()		{ 	return maxSize;	}
	public String getFileName()		{ 	return fileName;	}
	public boolean getPixelRepresentation()	{ return signed ;}
	
	
	public void   setMaxSize(int theSize){ maxSize = theSize ;}
	
	public int getByte(int Position){
		if (Position < size || Position >= 0) return (int)data[ Position ];
		else  return -1 ;
	}
	
//_________________________Main__Class____________for_testings !
/*
		public static void main(String args[]){
		// Setting a FileDialog	
		Frame  F = new Frame( " this testFrame");
		// no need to show this frame !	
		FileDialog file_dialog = new FileDialog(F, "Open File",FileDialog.LOAD);
		file_dialog.setDirectory(System.getProperty("user.dir"));// last update !
		if(debug) System.out.println(" Hello, file Dialog !");							
		file_dialog.pack();  // bug workaround
		file_dialog.show();  // blocks until user selects a file
		String fileName  = file_dialog.getDirectory()+file_dialog.getFile() ;
		try{
			FileParser FP = new FileParser(fileName,F);
			FP.guessWhat();
				if(debug){

					System.out.println(" max Size :" + FP.getMaxSize() );
					System.out.println(" can Open :" + FP.getCanOpen() );
					System.out.println(" getDataSize :" + FP.getDataSize() );
					System.out.println(" getHeaderSize:" + FP.getHeaderSize() );
					System.out.println(" getbitsAllocated:" + FP.getbitsAllocated() );
					System.out.println(" getHighBit:" + FP.getHighBit() );
					System.out.println(" getWidth:" + FP.getWidth() );
					System.out.println(" getHeight:" + FP.getHeight() );
					}// end if FP answer
		
			
			}
		catch ( Exception e ) {
			System.out.println(" Exception !!!" + e);}
			
	}
	
*/	
	
}
// end of class



