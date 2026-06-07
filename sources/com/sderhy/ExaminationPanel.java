/**
*	ExaminationPanel — a Swing panel that shows an Examination as a collapsible
*	Patient/Study/Series tree (built from a DICOMDIR or from a scanned folder).
*	It is embedded directly in the main window (left side) rather than living in
*	its own floating window.
*
*	Selecting a node and pressing "Load" (or double-clicking) clears the canvas
*	then loads every image held by that node and its descendants, reusing
*	OpenGif.fromFile (which transparently handles DICOM, JPEG, GIF, BMP, ...).
*	Loading runs on a background thread so the panel stays responsive; the final
*	canvas refresh is marshalled back onto the event thread.
*
*	@author Serge Derhy
*/
package com.sderhy ;
import java.awt.* ;
import java.awt.event.* ;
import java.io.File ;
import java.util.List ;
import javax.swing.* ;
import javax.swing.tree.* ;

public class ExaminationPanel extends JPanel {

	// Dark theme : same gray as the main window, light text.
	private static final Color PANEL_BG = Color.gray.darker().darker() ;  // ~ (62,62,62)
	private static final Color PANEL_FG = new Color(0xDDDDDD) ;           // light font
	private static final Color SEL_BG   = Color.gray.darker() ;           // selected row
	private static final int   MIN_W = 160, MAX_W = 900 ;                 // resize bounds

	private final MainClass mc ;
	private final JTree tree ;
	private final JButton loadButton ;
	private final JLabel status ;
	private volatile boolean loading = false ;

	public ExaminationPanel(Examination exam, MainClass mc){
		this.mc = mc ;
		setLayout(new BorderLayout()) ;
		setPreferredSize(new Dimension(300, 400)) ;
		setBackground(PANEL_BG) ;

		DefaultMutableTreeNode root = toTreeNode(exam.root) ;
		tree = new JTree(new DefaultTreeModel(root)) ;
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ;
		styleTree(tree) ;
		expandFirstLevels(tree, root) ;

		// Header : title on the left, a close button on the right.
		JPanel header = new JPanel(new BorderLayout()) ;
		header.setBackground(PANEL_BG) ;
		JLabel title = new JLabel(exam.fromDicomDir ? "  DICOMDIR" : "  Examination (no DICOMDIR)") ;
		title.setForeground(PANEL_FG) ;
		header.add(title, BorderLayout.CENTER) ;
		JButton close = new JButton("✕") ;
		close.setMargin(new Insets(0,5,0,5)) ;
		close.setToolTipText("Close the examination panel") ;
		close.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ mc.closeExamination() ; }
		}) ;
		header.add(close, BorderLayout.EAST) ;
		add(header, BorderLayout.NORTH) ;

		JPanel center = new JPanel(new BorderLayout()) ;
		center.setBackground(PANEL_BG) ;
		if(exam.warning != null){
			JLabel banner = new JLabel("<html>&#9888; " + exam.warning + "</html>") ;
			banner.setOpaque(true) ;
			banner.setBackground(new Color(0xFFF3CD)) ;
			banner.setForeground(new Color(0x8A6D3B)) ;
			banner.setBorder(BorderFactory.createEmptyBorder(6,8,6,8)) ;
			center.add(banner, BorderLayout.NORTH) ;
		}
		JScrollPane sp = new JScrollPane(tree) ;
		sp.getViewport().setBackground(PANEL_BG) ;
		sp.setBackground(PANEL_BG) ;
		sp.setBorder(BorderFactory.createEmptyBorder()) ;
		center.add(sp, BorderLayout.CENTER) ;
		add(center, BorderLayout.CENTER) ;

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)) ;
		south.setBackground(PANEL_BG) ;
		loadButton = new JButton("Load series") ;
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ loadSelected() ; }
		}) ;
		south.add(loadButton) ;
		status = new JLabel(" ") ;
		status.setForeground(PANEL_FG) ;
		south.add(status) ;
		add(south, BorderLayout.SOUTH) ;

		// Draggable divider on the right edge to adjust the panel width.
		add(makeResizeGripper(), BorderLayout.EAST) ;

		// Double-click on a node loads it.
		tree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() == 2) loadSelected() ;
			}
		}) ;
	}

	// Apply the dark theme (gray background, light font) to the tree.
	private static void styleTree(JTree tree){
		tree.setBackground(PANEL_BG) ;
		tree.setForeground(PANEL_FG) ;
		DefaultTreeCellRenderer r = new DefaultTreeCellRenderer() ;
		r.setBackgroundNonSelectionColor(PANEL_BG) ;
		r.setTextNonSelectionColor(PANEL_FG) ;
		r.setBackgroundSelectionColor(SEL_BG) ;
		r.setTextSelectionColor(Color.white) ;
		r.setBorderSelectionColor(SEL_BG) ;
		tree.setCellRenderer(r) ;
	}

	// A thin vertical strip the user drags to resize the panel.
	private JComponent makeResizeGripper(){
		final JPanel grip = new JPanel() ;
		grip.setPreferredSize(new Dimension(6, 10)) ;
		grip.setBackground(Color.gray) ;
		grip.setToolTipText("Drag to resize") ;
		grip.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) ;
		MouseAdapter drag = new MouseAdapter(){
			private int startScreenX, startWidth ;
			public void mousePressed(MouseEvent e){
				startScreenX = e.getXOnScreen() ;
				startWidth   = getWidth() ;
			}
			public void mouseDragged(MouseEvent e){
				int w = startWidth + (e.getXOnScreen() - startScreenX) ;
				if(w < MIN_W) w = MIN_W ;
				if(w > MAX_W) w = MAX_W ;
				setPreferredSize(new Dimension(w, getHeight())) ;
				if(mc != null){ mc.invalidate() ; mc.validate() ; }
			}
		} ;
		grip.addMouseListener(drag) ;
		grip.addMouseMotionListener(drag) ;
		return grip ;
	}

	private static DefaultMutableTreeNode toTreeNode(Examination.Node n){
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(n) ;
		for(int i=0;i<n.children.size();i++) t.add(toTreeNode(n.children.get(i))) ;
		return t ;
	}

	private static void expandFirstLevels(JTree tree, DefaultMutableTreeNode root){
		// Expand patients and studies so series are visible at a glance.
		tree.expandRow(0) ;
		for(int r=0; r<tree.getRowCount() && r<40; r++) tree.expandRow(r) ;
	}

	private Examination.Node selectedNode(){
		TreePath path = tree.getSelectionPath() ;
		if(path == null) return null ;
		Object last = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject() ;
		return (last instanceof Examination.Node) ? (Examination.Node)last : null ;
	}

	private void loadSelected(){
		if(loading) return ;
		Examination.Node node = selectedNode() ;
		if(node == null){ status.setText("Select a series first.") ; return ; }
		final List<File> files = node.collect() ;
		if(files.isEmpty()){ status.setText("Nothing to load here.") ; return ; }

		loading = true ;
		loadButton.setEnabled(false) ;
		status.setText("Loading " + files.size() + " image(s)…") ;

		// Clear the canvas first so series are never mixed together.
		mc.canvas.clearAll() ;

		Thread worker = new Thread(new Runnable(){
			public void run(){
				final int[] ok = {0} ;
				for(int i=0;i<files.size();i++){
					final int n = i + 1 ;
					try {
						if(OpenGif.fromFile(files.get(i).getAbsolutePath(), mc)) ok[0]++ ;
					} catch(Throwable t){ /* skip a bad file, keep going */ }
					EventQueue.invokeLater(new Runnable(){
						public void run(){ status.setText("Loaded " + n + " / " + files.size() + "…") ; }
					}) ;
				}
				EventQueue.invokeLater(new Runnable(){
					public void run(){
						mc.canvas.refresh() ;
						status.setText("Done : " + ok[0] + " / " + files.size() + " image(s) loaded.") ;
						loadButton.setEnabled(true) ;
						loading = false ;
					}
				}) ;
			}
		}) ;
		worker.setDaemon(true) ;
		worker.start() ;
	}
}
