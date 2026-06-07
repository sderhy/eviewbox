/**
*	Cette classe utilise une méthode statique ; setMainLayout ,  à partir
*	d'objets instanciés dans la classe MainClass ,
*	@date le  10/03/98
*	@author Serge Derhy 18. rue Bachaumont , 75002 Paris France
*	Called by 	MainClass
*	modif :210498/1430
*/

package com.sderhy ;
import java.awt.*;
import java.awt.event.*;
//import MainClass ;
//import PixCanvas ;

public class MainLayout
{
	static MenuBar mbar;
	static Menu file ,  edit , windows, help ;
	static Menu open; // SUBMENUS..
	static Menu multiplanar ;
//	static Menu edit ;
//	static Menu help;
	//static Frame f;
	public final static String[] commands = new String[] {
	"Open Image Folder...", "OpenFolder",
	"Open Image ...","OpenGIF",
	"Open  Dicom...","OpenDicom",
	"Parse File...", "ParseFile",
	"Open From URL...",	"OpenFromURL",
	"Print Image..." ,"PrintImage",
	"Load Images Set...",	"LoadFileSet",
	"Close",	"Close",
	"Save Canvas Image..." ,	 "SaveAsGIF" ,
	"Save All Images In a Folder...", "batch",
	"Save All Small Images...", "batchSmall",
	"Save Images Set...",	"SaveFileSet",
	"Generate Html...", "GenerateHtml" ,
	"Quit" ,"Quit" ,
	"Undo", "Undo",
	"Cut" , "Cut",
	"Copy","Copy" ,
	"Past","Past" ,
	"Clear Icons" , "clearAll" ,
	"Change Background","bg",
	"Change Size","size",
	"Next Window","NextWindow" ,
	"Previous Window",	"LastWindow" ,
	"Slide Show" , "Diaporama" ,
	"Frontal Reconstruction",	"linear" ,
	"Sagittal Reconstruction",	"sagittal" ,
	"Curve Reconstruction",	"curve" ,

	"About...","About",
	" extra1","extra2"
	};
	//public static MainClass f;


	public static void setLayout(MainClass f){
		TextField TF = f.TF ;
//		TextArea  TA  = f.TA ;
		PixCanvas canvas = f.canvas ;
///////////new.....
		ScrollPane pane ;

		    int index = 0 ;
//AL Action listener :
		AL  al = new AL(f) ;//AL is ActionListener
//closeable window
		f.addWindowListener(	new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				// à remplacer par ThisAppExit ou doCommaand...
				System.exit(0) ;
				}
			}
		);
		f.setLayout(new BorderLayout()) ;
		f.setBounds(10,10,440,440);
		//f.setBackground(Color.lightGray);
		f.setBackground(new Color(0x0879ed6));
//North panel : supPanel :
		InsetPanel supPanel = new InsetPanel(Color.gray.darker().darker());
		supPanel.setEtched(true);
		supPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		TF.setFont(new Font("Courier", Font.PLAIN, 10));
		TF.setBackground(Color.white);
//Pixcanvas for displaying images
		//canvas.setBackground(Color.lightGray);
		canvas.setBackground(new Color(0x0879ed6));
		//canvas.addMouseMotionListener(al);
		//canvas.addMouseListener(al) ;

//////////////////////////////Scroll Pane//////////////////////////////
		pane = new ScrollPane() ;
		pane.setBackground(Color.gray) ;
		pane.setForeground(Color. white);
		pane.add(canvas) ;
		pane.addKeyListener(canvas);
		f.add("Center",pane);

// au lieu de f.add("Center", canvas) ;
/////////////////////////////////////////////////////
		//	TF.setText("Largeur de fen\u00eatre :"+ f.bounds().width);
		supPanel.add(TF);
		f.add("North", supPanel);
//TextArea for the ??
//		TA.setFont(new Font("Courier", Font.PLAIN, 10));
		//f.add("Center",TA);
		f.setResizable(true);
		TF.setText ("Java version :" + System.getProperty("java.version"));
		TF.addActionListener(al) ;
//Setting the MenuBar :
		mbar = new MenuBar();
		f.setMenuBar(mbar);
	//menu file
		file = new Menu("File");
		MenuItem m;//  m est utilisée pour souscrire aux ActionListener;
		// Open Examination first : it is the everyday entry point (the rest is for power users).
		// Explicit strings so it does not disturb the commands[++index] sequence.
		file.add(m = new MenuItem("Open Examination...", new MenuShortcut(KeyEvent.VK_E)));
			m.addActionListener(al); m.setActionCommand("OpenExamination");
		file.addSeparator();
		file.add(m=new MenuItem(commands[index]));// OpenFolder
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		file.add(m=new MenuItem("Clear Icons"));
			m.addActionListener(al);m.setActionCommand("clearAll") ;
		file.addSeparator();
		file.add(m = new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_O)));//OpenGIF
			m.addActionListener(al);
			m.setActionCommand(commands[++index]) ;
			open = new Menu("Import Image");
					open.add(m=new MenuItem(commands[++index]));
					m.addActionListener(al);m.setActionCommand(commands[++index]) ;//OpenDicom
					open.add(m = new MenuItem(commands[++index]));
					m.addActionListener(al);m.setActionCommand(commands[++index]) ;//ParseFile
					open.add(m=new MenuItem(commands[++index]));
					m.addActionListener(al);m.setActionCommand(commands[++index]) ;//OpneFromURL

				file.add(open);

		file.addSeparator();
		file.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_P)));//PrintIma
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		file.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_Y)));//LoadFileSet
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		file.addSeparator();

		file.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_W)));//Close
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;

		file.addSeparator();

		file.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_S)));//SaveAsGif
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
/*****************************************
		file.add(m=new MenuItem(commands[++index]));// batch saving...
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
//  ****************************************	*/

	open = new Menu("Batch Saving");
				//	open.add(m=new MenuItem(commands[++index]));
				//	m.addActionListener(al);m.setActionCommand(commands[++index]) ;//OpenDicom
					open.add(m = new MenuItem(commands[++index]));
					m.addActionListener(al);m.setActionCommand(commands[++index]) ;//Save All Images In a Folder ...
					open.add(m=new MenuItem(commands[++index]));
					m.addActionListener(al);m.setActionCommand(commands[++index]) ;//Save As Small Images ....

				file.add(open);

//  ****************************************	*/

		file.add(m=new MenuItem(commands[++index]));//SaveFileSet
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		//		file.add(new MenuItem("Revert"));

		file.add(m=new MenuItem(commands[++index]));//GenerateHtml
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;


//		file.add(m=new MenuItem(commands[++index],new MenuShortcut( KeyEvent.VK_T)));//SaveFileSet
//		m.addActionListener(al);m.setActionCommand(commands[++index]) ;





		file.addSeparator();
		file.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_Q)));//Quit
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;

		mbar.add(file);
//Menu Edit
		edit = new Menu("Edit");
		edit.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
			m.enable(false ) ;
		edit.addSeparator();
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_X)));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_C)));
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_V)));
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		edit.addSeparator();
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_T)));
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_B)));
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		edit.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_K)));
		m.addActionListener(al);m.setActionCommand(commands[++index]) ;

		mbar.add(edit);
//Menu Window
		windows = new Menu( "Windows" );
		windows.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_N)));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		windows.addSeparator();
		windows.add(m=new MenuItem(commands[++index], new MenuShortcut(KeyEvent.VK_L)));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;

		windows.addSeparator();
		//diaporama
		windows.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		mbar.add(windows) ;
		windows.setEnabled(true);

// Multiplanar reconstruction :
		 Menu multiplanar = new Menu( "Multiplanar" );
		multiplanar.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		multiplanar.addSeparator();
		multiplanar.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		multiplanar.addSeparator();
		multiplanar.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;

		mbar.add(multiplanar) ;
		multiplanar.setEnabled(true);


//Menu Help
		help = new Menu("Help");
		help.add(m=new MenuItem(commands[++index]));
			m.addActionListener(al);m.setActionCommand(commands[++index]) ;
		help.addSeparator();
		help.add(new Menu("help"));
		mbar.add(help);

//for MW VM
		f.setLocation(10,10) ;

		//f.pack() ;
		// menu ShortCut


	}
//  public static void initMenus(MainClass MC)


}
