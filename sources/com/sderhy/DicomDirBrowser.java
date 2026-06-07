/**
*	DicomDirBrowser — a Swing window that shows an Examination as a collapsible
*	Patient/Study/Series tree (built from a DICOMDIR or from a scanned folder).
*	Selecting a node and pressing "Load" (or double-clicking) loads every image
*	held by that node and its descendants into the main canvas, reusing
*	OpenGif.fromFile (which transparently handles DICOM, JPEG, GIF, BMP, ...).
*
*	Loading runs on a background thread so the browser stays responsive; the
*	final canvas refresh is marshalled back onto the event thread.
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

public class DicomDirBrowser extends JFrame {

	private final MainClass mc ;
	private final JTree tree ;
	private final JButton loadButton ;
	private final JLabel status ;
	private volatile boolean loading = false ;

	public DicomDirBrowser(Examination exam, MainClass mc){
		super(exam.fromDicomDir ? "DICOMDIR" : "Examination (no DICOMDIR)") ;
		this.mc = mc ;

		DefaultMutableTreeNode root = toTreeNode(exam.root) ;
		tree = new JTree(new DefaultTreeModel(root)) ;
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ;
		expandFirstLevels(tree, root) ;

		getContentPane().setLayout(new BorderLayout()) ;

		if(exam.warning != null){
			JLabel banner = new JLabel(" ⚠  " + exam.warning) ;
			banner.setOpaque(true) ;
			banner.setBackground(new Color(0xFFF3CD)) ;
			banner.setForeground(new Color(0x8A6D3B)) ;
			banner.setBorder(BorderFactory.createEmptyBorder(6,8,6,8)) ;
			getContentPane().add(banner, BorderLayout.NORTH) ;
		}

		getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER) ;

		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)) ;
		loadButton = new JButton("Load selected series") ;
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ loadSelected() ; }
		}) ;
		south.add(loadButton) ;
		status = new JLabel(" ") ;
		south.add(status) ;
		getContentPane().add(south, BorderLayout.SOUTH) ;

		// Double-click on a node loads it.
		tree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() == 2) loadSelected() ;
			}
		}) ;

		setSize(560, 620) ;
		setLocationRelativeTo(null) ;
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
