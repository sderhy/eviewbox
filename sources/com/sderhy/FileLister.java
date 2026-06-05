package com.sderhy ;
import com.sderhy.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DicomDecoder.DicomHeaderReader;

public class FileLister extends java.awt.Dialog  
	implements	ActionListener, ItemListener , KeyListener {

  private BufList list;                   // To display the directory contents in
  private TextField detailsTF;           // To display detail info in.
  private InsetPanel buttons, textfieldPanel; 
  private Button 	buttonUp, 
  						buttonClose , 
  						buttonSelectDir, //select Directory
  						selectCurrent,  //select all
 					 	buttonMultiple, 
  					//	buttonSelect,		//" select "
  					//	buttonNumerical, 
  	 					buttonChosenDir, 
  	 					buttonListDicom; 
  	 				
  
  private static  File currentDir;
  private static boolean firstPassage = true ;
  private boolean choosen ;
  private boolean multipleSelect;
  private FilenameFilter filter;       // An optional filter for the directory
  private String[] files;              // The directory contents
  java.awt.Frame parentFrame ;
  
/***
*	ChosenFiles[] is a table containing the files that are choosen
*	during this procedure.
*	Accessible through the getChosenFile() method.
*********************************/
  private File [] chosenFiles;// contains the path to the choosen files.
  private boolean controlPressed = false;
  private boolean shiftPressed = false;
  private int lastSelection = -1;
  private int singleSelection = -1;

//         ***** constructor *****
 
  public FileLister(String directory, FilenameFilter filter,java.awt.Frame f) { 
	super(f,"File Lister", true);              // Create the window
	this.parentFrame = f ;
	this.filter = filter;              // Save the filter, if any
	list = new BufList(12, false);        // Set up the list
	 
	list.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
	list.addActionListener(this);
	list.addItemListener(this);
	list.setBackground(Color.white);
	list.addKeyListener(this);	// Added by Leon Bailey and M. Pasternak
	detailsTF = new TextField(60);   
	detailsTF.setFont(new Font("TimesRoman", Font.PLAIN, 12));
 	detailsTF.addActionListener(this) ;
 	detailsTF.setEditable(true); 
	
	buttons = new InsetPanel(Color.lightGray);    
	buttons.setEtched(true) ;         // Set up the button box
	buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 5));
	buttons.setFont(new Font("SansSerif", Font.BOLD, 10));

	buttonChosenDir = new Button("...");
	buttonChosenDir.addActionListener(this);

//	buttonSelect = new Button("Select");
//	buttonSelect.addActionListener(this);

	buttonListDicom = new Button("List DICOM");
	buttonListDicom.addActionListener(this);

//	buttonNumerical = new Button("List Numerical");
//	buttonNumerical.addActionListener(this);

   textfieldPanel = new InsetPanel(Color.lightGray) ;
	textfieldPanel.setEtched(true);
	textfieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT)) ;
	textfieldPanel.add(detailsTF);
	textfieldPanel.add(buttonChosenDir);
//	textfieldPanel.add(buttonSelect);	
	textfieldPanel.add(buttonListDicom);
//	textfieldPanel.add(buttonNumerical);
	
	
   buttons = new InsetPanel(Color.lightGray);    
   buttons.setEtched(true) ;         // Set up the button box
   buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 5));
   buttons.setFont(new Font("SansSerif", Font.BOLD, 10));

	buttonSelectDir = new Button("Select Directory"); 
	buttonSelectDir.disable() ;
	buttonSelectDir.addActionListener(this);
	
	selectCurrent = new Button("Select all"); 
	selectCurrent.addActionListener(this);
	
	buttonUp = new Button("Up a Directory"); // Set up the two buttons
	buttonUp.addActionListener(this);
	buttonClose = new Button("Close");
	buttonClose.addActionListener(this);
	buttonMultiple = new Button("Select Images");
	buttonMultiple.addActionListener(this);
	
	buttons.add(buttonMultiple);
	buttons.add(selectCurrent);
	buttons.add(buttonSelectDir);
	buttons.add(buttonUp);                   // Add buttons to button box
	buttons.add(buttonClose);
	
	this.add(list, "Center");          // Add stuff to the window
	this.add(textfieldPanel, "North");
	this.add(buttons, "South");
	this.setSize(700, 350);
	this.setLocation(150,150);
	this.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
			dispose() ;
			}});
		
	choosen =false ;	
	if(firstPassage || currentDir== null){ 
		firstPassage = false ;
		listDirectory(directory); 
	}
	else {
		directory = currentDir.toString() ;
		listDirectory(directory);
	} 
	this.setResizable(true) ;
  }//end of constructor
/* ******************************************************************************************/ 
  /**
  * Allows access to the chosen files 
  **/
  public File [] getChosenFiles()  {
  	if (choosen) return chosenFiles;
	else return null;
  }
  /**
  *	Allows access to the chosen directory 
  */
  public File getChoosenDirectory(){
  	if(choosen) return currentDir ;
  	else return null ;
  }
  
  /**
   * This method uses the list() method to get all entries in a directory
   * and then displays them in the List component. 
   **/
   
  public void listDirectory(String directory) {
	File dir = new File(directory);
	if (!dir.isDirectory()) 
	  throw new IllegalArgumentException("No such directory");
	  
	files = dir.list(filter);        
   list.removeAll();
	list.addItem("..   [Up to Parent Directory ]");  // A special case entry
	String newItem ="";
	for(int i = 0; i < files.length; i++) {
			File f = new File(dir, files[i]);
			if(  f.isDirectory() ){
				newItem = "[DIR] " ; //Hugly
			}else{
				//if (f.canRead()) newItem += "R";
				//if (f.canWrite())newItem += "W";
			 	newItem = "      " ;
			}
			newItem += cleaner(files[i]) ;// cleaner makes the pathname more human readable
			list.addItem(newItem);
	}
	
	this.setTitle(directory);// Display directory name in window titlebar
	detailsTF.setText(cleaner(directory));
	currentDir = dir;// Remember this directory for later.
  }
 /**
 *	This method list the directory and the files, along with the dicom name 
 *	attribute, that is retrieve through the getDicomInfo() method ;
 ********************************************************************/ 

public void listDicomDirectory(String directory) {
   File dir = new File(directory);
   if (!dir.isDirectory()) 
		throw new IllegalArgumentException("No such directory");
	files = dir.list(filter);        
   list.removeAll();
	list.addItem("..   [Up to Parent Directory]");  // A special case entry
    String newItem ="";
    for(int i = 0; i < files.length; i++) {
    		File f = new File(dir, files[i]);
			if(f.isDirectory()){
				  	newItem = "[DIR] " ;
			}else newItem = "      " ;
    		list.addItem( newItem + getDicomInfo(directory, files[i], true));
    }
    this.setTitle(directory);// Display directory name in window titlebar
    detailsTF.setText(cleaner(directory));
    currentDir = dir;
 }//end of method listDicomDirectory.


/** ******************************************************************************** 
* 	This method give the name of the patient.
*	it calls itself recursivly so the argument useFileName is set to false
*	in this case 
*/
  public String getDicomInfo(String directoryName, String fileName,boolean useFileName){
    
    File fileObj = new File(directoryName + File.separatorChar + fileName);
    String fileInfo;
    
    if (fileObj.isDirectory()) {
      String [] dirFiles = fileObj.list();
      String dirPath = fileObj.getPath();
      String testFile = null;
      // Test the first found file to see if it is DICOM 
      for (int i = 0 ; i < dirFiles.length ; i++) {
        fileObj = new File(dirPath + File.separatorChar + dirFiles[i]);
        if (!fileObj.isDirectory()) {
          testFile = dirFiles[i];
          break; //we have found a file  !
        }
      }

      if (testFile != null) { // a file has been found and a DICOM attribute will be returned.
        fileInfo = getDicomInfo(dirPath, testFile, false);
      }
      else { // no file has been found and we will return the file name .
        if (useFileName) {
          fileInfo = fileName;
        }
        else {// This is for the recursive approach :
          fileObj = new File(directoryName);
          fileInfo = fileObj.getName();
        }
      }
    }//end if (fileObj.isDirectory()) 
    
    else {  /// fileObj is not a directory !
      byte [] headerData = new byte[DicomHeaderReader.MAX_HEADER_SIZE];
      FileInputStream inp = null;
      
      try {
        inp = new FileInputStream(fileObj);
        inp.read(headerData);
        inp.close();
        inp = null;
      }
      catch (Exception e) {
        e.printStackTrace();
        if (inp != null) {try {inp.close();}catch (Exception e1) {}}
        if (useFileName) {fileInfo = fileName;}
        else {
          fileObj = new File(directoryName);
          fileInfo = fileObj.getName();
        }
        return  fileInfo;
      }//end catch
      
      DicomHeaderReader header = new DicomHeaderReader(headerData);
      String patientName = header.getPatientName();
    	//  String studyDate = header.getStudyDate();
      //studyDate = studyDate.substring(0,4)+ ":" + studyDate.substring(4,6) +":" +studyDate.substring(6,8) ; 
      patientName = patientName.replace('^',' ');
      if (useFileName) {
        fileInfo = fileName;
      }
      else {
        fileObj = new File(directoryName);
        fileInfo = fileObj.getName();
      }

      if (!patientName.equals("Unknown")) {
        fileInfo = fileInfo +  " ("  +   patientName  +   ")"   ;
      }
    }
    return fileInfo;
  }


 /* *********************************************************************************/ 

  public void itemStateChanged(ItemEvent e) {
    System.out.println(e.paramString());
    int i = ((Integer) e.getItem()).intValue();
	 if (controlPressed) {	  	//The user is holding down the control Key.  See if we 
      								//are selecting a new file or clearing one.
      System.out.println(" Control key pressed  ") ; 
      if (e.getStateChange() == e.SELECTED) {
      	System.out.println(" Control key pressed  ") ;
      	if (i > 0) {				//We are selecting a new one, so save this for a range.
          lastSelection = i;
          if (list.isIndexSelected(0)) {  list.deselect(0);} //Clear out the previous directory.
        }else {						//This is the previous directory, so clear it all out 
        								//and select this.
          clearSelections();
          list.select(i);
          lastSelection = -1;
        }
      }//end if (controlPressed)
      else {
        //We are clearing an item, so get rid fo the last selection.
        lastSelection = -1;
      }
    }
    else if (shiftPressed) {
    	System.out.println( " ShiftPressed" ) ;
      //We are creating a range, so always clear everything.
      clearSelections();
      if (i == 0) {
        //This is the previous directory, so only select it.
        list.select(i);
        lastSelection = -1;
      }
      else if (lastSelection == -1) {
        //There was no last selection, so just select this.
        list.select(i);
        lastSelection = i;
      }
      else if (i > lastSelection) {
        //Select a range from the last selection down to this one.
        for (int j = lastSelection ; j <= i ; j++) {
          list.select(j);
        }
      }
      else {
        //Select a range from the last selection up to this one.
        for (int j = i ; j <= lastSelection ; j++) {
          list.select(j);
        }
      }
    }//end if(shiftPressed )
    else {
      //This is just a single selection, so make sure everything is cleared
      //out.  Save the index for the next time.
      clearSelections();
      list.select(i);
      if (i == 0) {
        lastSelection = -1;
      }
      else {
        lastSelection = i;
      }
    }//end else
    //If we only have one selection, it will be i.

    if (list.getSelectedIndexes().length == 1) {
      singleSelection = list.getSelectedIndexes()[0];
      if (singleSelection > 0) {
        String filename = files[singleSelection - 1]; // Get the selected entry 
        File f = new File(currentDir, filename);  // Convert to a File
        if (!f.exists())                          // Confirm that it exists
          throw new IllegalArgumentException("FileLister: " +
                                             "no such file or directory");
    
        String info = cleaner(filename);
        if (f.isDirectory()){
          buttonSelectDir.enable();
        }
        else buttonSelectDir.disable();
        
      }
    }// end if (list.getSelectedIndexes().length == 1)
    else {
      buttonSelectDir.disable();
      singleSelection = -1;
    }
  }// end of itemStateChanged(..)
/* *************************************************************************************/
	  private void clearSelections(){
    		for (int i = 0 ; i < list.getItemCount() ; i++) {
      		if (list.isIndexSelected(i)) {
        			list.deselect(i);
      		}
    		}
  		}
  
  /*
   * This ItemListener method uses various File methods to obtain information
   * about a file or directory. Then it displays that info in the detailsTF box.
   *
  
	public void itemStateChanged(ItemEvent e) {
		int i = list.getSelectedIndex() - 1;      // minus 1 for Up To Parent entry
		if (i < 0) return;
		String filename = files[i];               // Get the selected entry 
		File f = new File(currentDir, filename);  // Convert to a File
		if (!f.exists())                          // Confirm that it exists
	  	throw new IllegalArgumentException("FileLister: " +
	                                     "no such file or directory");
	
		String info = cleaner(filename);
		if (f.isDirectory()){
			 info += File.separator;
				buttonSelectDir.enable();
		}else buttonSelectDir.disable();
		info += " " + f.length() + " bytes ";
		if (f.canRead()) info += " Read";
		if (f.canWrite()) info += " Write";
		detailsTF.setText(info);
  }//end of itemStateChanged(..)
*/

/**
*						Action performed method ..... 
**********************************************************************************/

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();	
		if (obj == buttonClose) this.dispose(); 								// Button close
		else if (obj == buttonUp) { up(); }										// Button up		
		
		else if (obj == list) {  // Double click on an item		// The list 
	  		int i = list.getSelectedIndex(); // Check which item
	  		if (i == 0) up();                // Handle first Up To Parent item
	  		else{  
	                         // Otherwise, get filename
	   		String name = files[i-1]; 
	   		File f = new File(currentDir, name);    // Convert to a File
	    		String fullname = f.getAbsolutePath();
	    		if (f.isDirectory()) {
	    			buttonSelectDir.disable();  
	    			listDirectory(fullname);  // List a directory
	     		}else return;          // or display a file
	  		}//end else ;
		}//end if( obj == list )
   	
   	else if (obj == buttonSelectDir){
   		int i = list.getSelectedIndex();
			String name = files[i-1]; 
	   	File f = new File(currentDir, name);    // Convert to a File
	   	String fullname = f.getAbsolutePath();
	  		if (f.isDirectory()) {
	    		currentDir = f;
	    		choosen = true ;
	    		this.dispose() ;
	    	}else return;          // or display a file
  		}
  		else if (obj == selectCurrent){	
  			choosen = true ;
  			this.dispose() ;	
  		}
  		
  		else if (obj == buttonListDicom) {
  			File file = new File(detailsTF.getText().trim());
			String fullname = file.getAbsolutePath();
        	if (file.isDirectory()) {
				listDicomDirectory(fullname);
        	}
    	}
    	else if (obj == buttonChosenDir ){
    		String newDir = Futil.openDialog(parentFrame) ;
    		File file = new File(newDir);
    		String parentName = file.getParent();
    		detailsTF.setText(parentName) ;
    		file  = new File( parentName.trim()) ;
    		if (file.isDirectory()) {
				listDirectory(parentName);
       	}
    	}
    	else if (obj == detailsTF ){
    		File file = new File(detailsTF.getText().trim());
			String fullname = file.getAbsolutePath();
        	if (file.isDirectory()) {
				listDirectory(fullname);
       	}	
    	}
    	else if (obj == buttonMultiple ){
			int [] selected = list.getSelectedIndexes();
			chosenFiles = new File[selected.length];		
		
			int j=0;
			for(int i=0; i<selected.length; i++){
				if (selected[i] != 0) {				
					chosenFiles[j] = new File(currentDir, files[selected[i] - 1]);
					j++;
				}
			}
		
			currentDir = null;
			choosen = true;
			this.dispose();
		}//end else if ( obj == buttonMultiple ) 
		
		
  }//end of actionPerformed
 /* *********************************************************************************/ 
	protected void up() {
		String parent = currentDir.getParent();
		if (parent == null) return ;
		listDirectory(parent);
	}
  
	private String cleaner(String s){
		String cleaned = "" ;
//		java.util.StringTokenizer st = new java.util.StringTokenizer(s,"%20") ;
//		while(st.hasMoreElements())
//		cleaned += st.nextToken() +" " ;
		cleaned = s ;
		return cleaned;
	} 
  
 /* *****  Key listener :   keyTyped, keyPressed, keyReleased ***          
 *	 needed for multiple selection from the list.
 */ 
  	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e){	
		int keyCode = e.getKeyCode();
   	if (keyCode == KeyEvent.VK_CONTROL) {  controlPressed = true;}
   	else if (keyCode == KeyEvent.VK_SHIFT) { shiftPressed = true;}
	}	

	public void keyReleased(KeyEvent e){			
		int keyCode = e.getKeyCode();
    	if (keyCode == KeyEvent.VK_CONTROL) {controlPressed = false;}
    	else if (keyCode == KeyEvent.VK_SHIFT) {shiftPressed = false;}
	}
  
  
  
  
}//////END OF CLASS FileLister

class BufList extends java.awt.List {
	public BufList(int n, boolean b){
	 	super(n,b);
	 	setMultipleMode(true);// Don't forget this !
	}
	public void update (Graphics g){
	 		paint(g) ;
	}
}
