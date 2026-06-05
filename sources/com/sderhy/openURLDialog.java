
package com.sderhy ;
import java.awt.*;
import java.awt.event.* ;

public class openURLDialog extends java.awt.Dialog implements ActionListener{
	 InsetPanel ip , ip1 ;
	 Button  okay , cancel ;
	 TextField  TF ;
	 String URL ;
	 
	 
	 public openURLDialog(String  URL, java.awt.Frame f) {
	 	super ( f ,  "Open URL", true ) ;
	 	TF  = new TextField(80) ;
	 	TF.setBackground(Color.white);
	 	TF.setFont(new Font( "Monaco", Font.PLAIN ,10));
	 	TF.addActionListener(this) ;
	 	TF.setText( URL);
	 	
	 	ip  = new InsetPanel( Color.gray) ;
	 	ip.setEtched(true) ;
	 	ip.add(TF) ;
	 	this.add("Center",ip) ;
	 	
	 	ip1 = new InsetPanel( Color.gray ) ;
	 	ip1.setEtched(true) ;
	 	
		okay = new Button ("Okay") ;
		okay.addActionListener(this);
		
		cancel = new Button ("Cancel") ;
		cancel.addActionListener(this);
		ip1.add(okay );
		ip1.add(cancel) ;
		this.add("South" , ip1 ) ;
		 pack() ;
		}
	
	public void actionPerformed(ActionEvent e){
	
		if(e.getSource() == TF)
			URL = TF.getText() ;
		if(e.getSource() == okay){
			URL = TF.getText() ;
			this.hide();
		 	}
		if(e.getSource() == cancel ) 
			this.hide() ;		 
	
	 } ;
	
	
	public String getResult(){ return URL ;}
	
	}//end of class
