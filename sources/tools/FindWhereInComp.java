

package tools ;
import java.awt.*;

/**
*	A convenience class to  find where the user has clicked 
*/
public class FindWhereInComp{

	public int stampW , stampH ;
	java.awt.Component c ;
	int numCol, numRow ;
	int w , h ;
	int maxImagePerCanvas ;
	private static int lastSelection ;
	//public boolean isSelected = false ;
	
	public FindWhereInComp( Component c,int stampW,int stampH){
	this.c = c;
	this.stampW = stampW ;
	this.stampH = stampH ;
	lastSelection = -1 ; // is it necessary ?
	update();
	}//endofConstructor
	
	public void update(){
		w  = c.getSize().width ;
		h =  c.getSize().height ;
		numRow = h/stampH ;
		numCol = w/stampW;	
		maxImagePerCanvas = numRow*numCol ;
	
	}
	
	/**
	*  tell where the user clicked , return -1 if it is out
	*	of the vector of numImages
	*/
	public int findWhere(int x, int y, int numImages){
    	if(numImages <= 0) return -1 ;
    	update();
    	int colWhere  = x / stampW; // 0 est la premier colonne
    		if(colWhere >= numCol) return -1 ;// on n'est pas dans une img !
    	int rowWhere  = y / stampH ;
    		if(rowWhere >= numRow ) return -1 ;
    	int where = (rowWhere * numCol) + colWhere ;
    	
    	if (where > (numImages -1)) return -1 ;
    	if (numImages <= maxImagePerCanvas){
    		lastSelection = where ;
    	 	return where ;
    	 }
    	int displayed = numImages % maxImagePerCanvas ;
    	where += numImages -maxImagePerCanvas ;
    	lastSelection = where ;
    	return  where;
    
    }//end of find where
    
    
/*    
    public void drawRect(int x, int y,int numImages){
    	int where = findWhere(x,y,numImages) ;
    	if (where== -1 ) return;
    	int xRect = (where % numCol)* stampW ;
    	int yRect = (where / numCol);
    	yRect =  yRect * stampH ;
    	c.repaint();
    	
    	}
  */  	
  
	static final Color SELECTION = new Color(64, 156, 255) ;// accent blue

    public void drawRect(int where){
    	paintRect(where, SELECTION);
	}
	public void drawRect(){
		drawRect(lastSelection);
	}
	public void clearRect(int where){
    	paintRect(where, c.getBackground());
	}
	/** 2 pixel thick selection frame , drawn inside the icon cell. */
	private void paintRect(int where, Color color){
    	update();
    	if (where == -1) return;
    	int xRect = (where % numCol)* stampW ;
    	int yRect = (where / numCol);
    	yRect =  yRect * stampH ;
    	Graphics g = c.getGraphics() ;
    	g.setColor(color) ;
    	g.drawRect(xRect+1, yRect+1, stampW-3, stampH-3);
    	g.drawRect(xRect+2, yRect+2, stampW-5, stampH-5);
	}
		
		
	public int getLastSelection(){
		return lastSelection ;
		}
    public int setLastSelection(int last){
		
		lastSelection = last;
		if (lastSelection < -1) lastSelection = -1 ;
		
		return last;		}	
    	
 }//end of class
