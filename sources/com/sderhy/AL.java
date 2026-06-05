/** 
*	This action listener is called by MainClass to make it more readable.
*	@date 8/03/98
*	@author serge derhy
*/

	package com.sderhy;
	import java.awt.event.* ;
	import tools.Tools ;
//	import MainClass ;
	
public class AL implements ActionListener{
		MainClass f ;
		public AL (MainClass f){
		this.f = f;
		}
	
		String command ;
		public void actionPerformed(ActionEvent e){
			if (e.getSource()== f.TF){
				f.doCommand("TextField") ;
				 return;
			}
			command = e.getActionCommand() ;
			f.doCommand(command ) ;
		}//endOf actionPerformed

}//endofClassAL
