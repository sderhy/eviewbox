package com.sderhy ;
/** 
* 	Window level widget
*	to be called by ImageViewer
*	for setting level /  window values
*	@date 24 oct 99
*	@author Serge Derhy Copyright 1999
**/

import java.awt.* ;
import java.awt.event.*;
public class LWWidget extends java.awt.Frame implements ActionListener {
		/** level  l  , window , w */
		static int l ;
		static int w ;
		TextField lTF;
		TextField wTF;
		
		public LWWidget( int l , int w ){
			super( "Adjust level and window values" ) ;	
			this.l = l;
			this.w = w ;
			makeComponent() ;
			this.show() ;
		}
		
		protected void makeComponent(){ 
			InsetPanel ip = new InsetPanel(new Color(0x0879ed6)) ;
			ip.setLayout(new FlowLayout(FlowLayout.LEFT));
			ip.setEtched(true);
			Label l = new Label("Brightness :");// next will be level
			Font F = new Font("TimeRoman",Font.PLAIN ,8 ) ;
			l.setFont(F) ;
			lTF = new TextField(8);
			lTF.setBackground(Color.white);
			lTF.setFont(F);
			lTF.setText(" "+ this.l);
			lTF.addActionListener(this);
			Label w = new Label("   Contrast  :");// next will be window
			w.setFont(F);
			wTF = new TextField(8) ;
			wTF.setFont(F);
			wTF.setText(" "+ this.w) ;
			wTF.setBackground(Color.white);
			wTF.addActionListener(this);
			/*// uncomment for having a button
			Button set = new Button("set") ;
			set.setFont(F) ;
			set.addActionListener(this);
			*/
			ip.add(l);
			ip.add(lTF);
			ip.add(w);
			ip.add(wTF);
			/*ip.add(set);*/ // uncomment for having a button
			this.add(ip);
			this.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent we ){hide();}
				}) ;
			this.setSize(260,40);
			this.setResizable(false);
		}
		public void actionPerformed(ActionEvent e){
			String commande = e.getActionCommand() ;
				if(commande.equals("close"))hide();
				if(commande.equals("set")) setlw();
				
				else
				      
					tools.Tools.debug("This command "+ commande+ " is not yet implemented");			
	
		}
		
		public void setlw(){
			  try {
			  l =Integer.parseInt(lTF.getText()) ;
			  w =Integer.parseInt(wTF.getText()) ;
			  }
			  catch(NumberFormatException mne){ ;}
			
		}
		
		public void setl(int l){ 
				lTF.setText(""+l);/*
			if(l>=0) lTF.setText(""+l);
			else lTF.setText("0");
			*/
			}
		public void setw(int w){ 
		/*
			if(w>=0) wTF.setText(""+w);
			else wTF.setText("0");
		*/
		 wTF.setText(""+w);
			}
		
		
		public int getl(){ return l;}		
		public int getw(){ return w ;}
		
}
