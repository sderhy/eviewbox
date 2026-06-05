/**
*	A color chooser 
* 	@ author : written by Serge Derhy 
*	@ date  :  April 9th  1998
*/
package com.sderhy ;
import java.awt.* ;
import java.awt.event.*;
import com.sderhy.* ;

public class  ColorBox extends java.awt.Dialog implements ActionListener{
	com.sderhy.InsetPanel ip , ip2;
	private static Color defaultColor =  Color.gray;//Color.getColor() ;
	private Color previous , color ;
	private static java.awt.Color[] colors={
			Color.white,Color.lightGray,Color.gray,Color.darkGray,
			Color.black,Color.red,Color.pink,Color.orange,Color.yellow,
			Color.green,Color.magenta,Color.cyan ,Color.blue,
	// some customized colors  ;-)
			new Color(0x035fff) ,//g
			new Color(0x0f058f),//r
			new Color(0x0879ed6)// b
			
	 };//end of ColorArray
 	private Canvas canvas ;
	private Button[] checkers ;
	private Button choose , cancel ; 
	private int selected = -1 ;
/**
*  first constructor :
*	@param Frame the frame from which it comes
*	@param color == the actual color to display and to change
*/
	public ColorBox( java.awt.Frame f, Color color ) {
		super(f, "Choose your color " );
			this.color = color ;
			previous = color ;
		setLayout(new BorderLayout());
		ip = new InsetPanel(Color.gray);//bgcolor ==black 
		ip.setEtched(true);
		//ip = new Panel() ;
		checkers  = new Button[16];
		ip.setLayout(new GridLayout(4,4,4,4 )) ;
		for (int i = 0 ; i < colors.length ;i++ ){
			checkers[i] = new Button() ;
			checkers[i].setBackground(colors[i]);
			checkers[i].addActionListener(this) ;
			checkers[i].setActionCommand(String.valueOf(i) ) ;
			ip.add(checkers[i]) ;
		}//endOfFor 
	
		ip2 = new InsetPanel(Color.gray);
		ip2.setEtched(true, false) ;
		canvas = new Canvas() ;
		canvas.setBackground(color) ;
		canvas.setSize(100,20);
		ip2.add(canvas) ;
		canvas.show();
		choose = new Button("Okay" ) ;
		choose.addActionListener(this);
		choose.setActionCommand("Okay");
		cancel = new Button("Cancel") ;
		cancel.addActionListener(this);
		ip2.add(choose) ;
		ip2.add(cancel) ;
		this.add("Center",ip);
		this.add("South",ip2);
		this.setSize(250,180);	
		this.setLocation(200,100) ;//ex moveTo (deprecated )
		this.setModal(true);
		this.setResizable(false);
		this.addWindowListener(new WindowAdapter(){
				public void  windowClosing(WindowEvent e){
						dismiss();
						}
			});
	}
/**
*  Second constructor to fit in a bean box 
*/
	public ColorBox(){
		this(new Frame(),Color.white);
	}	
	public void show(){
		super.show() ;
		canvas.repaint();
		this.repaint();
		setModal(true);
	}
	
	public Color getColor(){ return color;}
	public void setColor(Color couleur){ color = couleur ;}
	public void actionPerformed(ActionEvent e){
		String s = e.getActionCommand();
		int col = -1 ;
		try{col = Integer.parseInt(s) ;}
			catch(Exception xption) {col =-1;} 	
		if( col > -1 && col < 16){	
			selected = col ;
			color = colors[col] ;
			repaint();
		}
		if(s == "Okay") {
			if(selected >= 0 )color =  colors[selected] ;
			dismiss() ;
		}
	
		if (s == "Cancel"){
			color = previous ;
			dismiss();
		}
			
	}//end of actionPerformed
	
	public void paint( Graphics g){
		super.paint(g) ;
		Graphics gr = canvas.getGraphics();
		gr.setColor(color);
		gr.fillRect(1,1,canvas.getSize().width, canvas.getSize().height) ;
		gr.setColor(Color.black);
		gr.draw3DRect(0,0,canvas.getSize().width, canvas.getSize().height,true) ;
	 }
	
	public void dismiss(){
		hide();
	//	System.exit(0);
		
	}
	public  static void main (String[] args) {
		tools.Tools.debug(" Go on !");
		java.awt.Frame  f = new Frame("hello bachaumont !") ;
		f.setSize(300,50);f.show() ;
		java.awt.Color  color  = Color.pink;
		tools.Tools.debug(" Frame good !");
		ColorBox  c  = new ColorBox((Frame)f,color) ;
				tools.Tools.debug(" CB...good !");
		
		c.show();
		
		tools.Tools.debug("la couleur choisie est " + c.getColor()) ;
		c.dispose() ;	
		System.exit(0);
	}
}//end of class
