/**
*	AboutBox : the About dialog , credits and link to the project page.
*	Opened from Help > About... or from the application menu on macOS
*	(EViewBox > About , see MainClass).
*/
package com.sderhy ;
import java.awt.* ;
import java.awt.event.* ;

public class AboutBox extends Dialog {

	static final String SITE = "https://github.com/sderhy/eviewbox" ;

	public AboutBox(Frame parent){
		super(parent, "About EViewBox", false) ;
		setBackground(Color.lightGray) ;

		MultiLineLabel text = new MultiLineLabel(
			"EViewBox\n"
			+ "Open source DICOM viewer",
			20, 12, MultiLineLabel.CENTER) ;
		text.setFont(new Font("TimesRoman", Font.BOLD, 14)) ;
		add("North", text) ;

		MultiLineLabel disclaimer = new MultiLineLabel(
			"Not a medical device — educational and research use only.\n"
			+ "Not intended for medical diagnosis.",
			20, 4, MultiLineLabel.CENTER) ;
		disclaimer.setFont(new Font("TimesRoman", Font.PLAIN, 11)) ;
		disclaimer.setForeground(Color.darkGray) ;
		add("Center", disclaimer) ;

		Panel buttons = new Panel() ;
		Button site = new Button("Open GitHub Page") ;
		site.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ openSite() ; }
		}) ;
		Button ok = new Button("     OK     ") ;
		ok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ dispose() ; }
		}) ;
		buttons.add(site) ;
		buttons.add(ok) ;
		add("South", buttons) ;

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){ dispose() ; }
		}) ;
		pack() ;
		setResizable(false) ;
		setLocationRelativeTo(parent) ;
		toFront() ;
	}

	private void openSite(){
		try{
			if(Desktop.isDesktopSupported()
					&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
				Desktop.getDesktop().browse(new java.net.URI(SITE)) ;
				return ;
			}
		}catch(Exception e){ tools.Tools.debug("AboutBox : cannot open browser : " + e) ; }
	}
}
