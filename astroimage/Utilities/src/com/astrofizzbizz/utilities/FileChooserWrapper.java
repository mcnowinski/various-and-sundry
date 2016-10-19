package com.astrofizzbizz.utilities;



import java.io.File;
import javax.swing.JFileChooser;
import java.awt.Component;


/**
 * @author mcginnis
 *
 */
public class FileChooserWrapper 
{
	private Component mparent;
	/**
	 * @param parent
	 */
	public FileChooserWrapper(Component parent)
	{
		mparent = parent;
	}
    /**
     * @param sfilter
     * @param directorysOnly
     * @param directoryPath
     * @return File
     */
    public File fOpenFile(String sfilter,boolean directorysOnly, String directoryPath) 
    {
    	File file = null;
    	JFileChooser fc = null;
    	if (directoryPath != null)
    	{
    		fc = new JFileChooser(directoryPath);
    	}
    	else
    	{
    		fc = new JFileChooser();
    	}
        if (directorysOnly)
        {
        	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        else
        {
        	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
    	fc.setMultiSelectionEnabled(false);
    	FileChooserFilter filter = new FileChooserFilter(sfilter);
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(mparent);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            file = fc.getSelectedFile();
        } 
        return file;
 	}
    /**
     * @param sfilter
     * @param directoryPath 
     * @return File[]
     */
    public File[] fOpenMultipleFiles(String sfilter, String directoryPath) 
    {
    	File[] file = null;
    	JFileChooser fc = null;
    	if (directoryPath != null)
    	{
    		fc = new JFileChooser(directoryPath);
    	}
    	else
    	{
    		fc = new JFileChooser();
    	}
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        FileChooserFilter filter = new FileChooserFilter(sfilter);
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(mparent);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            file = fc.getSelectedFiles();
        } 
        return file;
 	}
    /**
     * @param sfilter
     * @param directoryPath
     * @return File
     */
    public File fSaveFile(String sfilter, String directoryPath) 
    {
    	File file = null;
    	JFileChooser fc = null;
    	if (directoryPath != null)
    	{
    		fc = new JFileChooser(directoryPath);
    	}
    	else
    	{
    		fc = new JFileChooser();
    	}
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);
        FileChooserFilter filter = new FileChooserFilter(sfilter);
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(mparent);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            file = fc.getSelectedFile();
        }
        return file;
   }

}
