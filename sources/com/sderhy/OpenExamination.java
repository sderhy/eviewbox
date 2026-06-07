/**
*	OpenExamination — entry point for the "Open Examination…" command.
*
*	Asks the user for a folder, searches it recursively for a DICOMDIR. If one
*	is found the tree is built from it; otherwise the folder layout itself is
*	scanned and shown with a warning (some series may be incomplete). The
*	resulting Examination is shown as a tree panel embedded in the main window.
*
*	@author Serge Derhy
*/
package com.sderhy ;
import java.awt.event.* ;
import java.io.* ;
import javax.swing.* ;
import tools.Tools ;

public class OpenExamination {

	private static final int MAX_DEPTH = 8 ;

	public static void run(final MainClass mc){
		// FILES_AND_DIRECTORIES so the user can pick a DICOMDIR file directly OR a
		// folder (current / parent / child). An accessory button selects the folder
		// currently shown without having to step into it.
		final JFileChooser chooser = new JFileChooser(Futil.getLastDirectory()) ;
		chooser.setDialogTitle("Open Examination — pick a DICOMDIR file or a folder") ;
		chooser.setApproveButtonText("Open") ;
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES) ;
		chooser.setAcceptAllFileFilterUsed(true) ;

		JButton useCurrent = new JButton("Use Current Folder") ;
		useCurrent.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				chooser.setSelectedFile(chooser.getCurrentDirectory()) ;
				chooser.approveSelection() ;
			}
		}) ;
		chooser.setAccessory(useCurrent) ;

		if(chooser.showOpenDialog(mc) != JFileChooser.APPROVE_OPTION) return ;

		File chosen = chooser.getSelectedFile() ;
		if(chosen == null) chosen = chooser.getCurrentDirectory() ;
		if(chosen == null) return ;

		// Work out the DICOMDIR (if any) and the folder to fall back on.
		File dicomdir = null ;
		File dir ;
		if(chosen.isFile()){
			if(chosen.getName().equalsIgnoreCase("DICOMDIR")) dicomdir = chosen ;
			dir = chosen.getParentFile() ;          // a file was picked : use its folder
		} else {
			dir = chosen ;
		}
		if(dir != null) Futil.setLastDirectory(dir) ;
		if(dicomdir == null && dir != null) dicomdir = findDicomdir(dir, 0) ;

		Examination exam ;
		try {
			if(dicomdir != null){
				Tools.debug("DICOMDIR : " + dicomdir.getPath()) ;
				exam = Examination.fromDicomDir(dicomdir) ;
			} else if(dir != null){
				Tools.debug("No DICOMDIR under " + dir.getPath() + " — scanning folder layout.") ;
				exam = Examination.fromFolder(dir) ;
			} else {
				return ;
			}
		} catch(IOException e){
			Tools.debug("OpenExamination : " + e) ;
			if(dir == null) return ;
			exam = Examination.fromFolder(dir) ;   // degrade gracefully
		}

		final Examination toShow = exam ;
		java.awt.EventQueue.invokeLater(new Runnable(){
			public void run(){ mc.showExamination(toShow) ; }
		}) ;
	}

	/** Depth-bounded recursive search for a file named DICOMDIR (case-insensitive). */
	private static File findDicomdir(File dir, int depth){
		File[] entries = dir.listFiles() ;
		if(entries == null) return null ;
		// First pass : a DICOMDIR at this level.
		for(int i=0;i<entries.length;i++){
			File e = entries[i] ;
			if(e.isFile() && e.getName().equalsIgnoreCase("DICOMDIR")) return e ;
		}
		// Second pass : descend.
		if(depth < MAX_DEPTH){
			for(int i=0;i<entries.length;i++){
				File e = entries[i] ;
				if(e.isDirectory() && !e.getName().startsWith(".")){
					File found = findDicomdir(e, depth+1) ;
					if(found != null) return found ;
				}
			}
		}
		return null ;
	}
}
