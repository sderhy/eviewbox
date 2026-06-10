/**
*	PrefsDialog : the Preferences window.
*	Opened from the application menu on macOS (EViewBox > Settings...)
*	or from Edit > Preferences... on platforms without an application menu.
*	Changes apply immediately and are persisted through Prefs.
*/
package com.sderhy ;
import java.awt.* ;
import java.awt.event.* ;

public class PrefsDialog extends Dialog implements ItemListener {

	static final String[] SIZE_LABELS = { "Small", "Medium", "Large", "Extra Large" } ;
	static final int[]    SIZE_VALUES = {  80,      100,      140,     200 } ;

	private MainClass frame ;
	private CheckboxGroup group = new CheckboxGroup() ;
	private Checkbox[] boxes = new Checkbox[SIZE_VALUES.length] ;

	public PrefsDialog(MainClass aFrame){
		super(aFrame, "Preferences", false) ;
		this.frame = aFrame ;
		setLayout(new BorderLayout(8,8)) ;

	// icon size : radio buttons , current size preselected
		Panel sizes = new Panel(new GridLayout(0,1,0,2)) ;
		sizes.add(new Label("Icon size :")) ;
		int current = PixObject.getStampSize() ;
		for(int i = 0 ; i < SIZE_VALUES.length ; i++){
			boxes[i] = new Checkbox(SIZE_LABELS[i]+"  ("+SIZE_VALUES[i]+" px)",
						group, SIZE_VALUES[i] == current) ;
			boxes[i].addItemListener(this) ;
			sizes.add(boxes[i]) ;
		}
		Panel center = new Panel(new FlowLayout(FlowLayout.LEFT,12,8)) ;
		center.add(sizes) ;
		add(center, BorderLayout.CENTER) ;

	// background color + close
		Panel south = new Panel(new FlowLayout(FlowLayout.RIGHT)) ;
		Button bg = new Button("Change Background...") ;
		bg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ frame.canvas.changeBackground() ; }
		}) ;
		Button close = new Button("Close") ;
		close.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ dispose() ; }
		}) ;
		south.add(bg) ;
		south.add(close) ;
		add(south, BorderLayout.SOUTH) ;

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){ dispose() ; }
		}) ;
		pack() ;
		setResizable(false) ;
		setLocationRelativeTo(frame) ;
	}

	public void itemStateChanged(ItemEvent e){
		if(e.getStateChange() != ItemEvent.SELECTED) return ;
		for(int i = 0 ; i < boxes.length ; i++)
			if(e.getSource() == boxes[i]) frame.canvas.changeSize(SIZE_VALUES[i]) ;
	}
}
