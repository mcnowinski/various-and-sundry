package com.astrofizzbizz.utilities;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.MenuElement;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DpmSwingUtilities 
{

	public static JMenu findMenu(JMenuBar menuBar, String menuName)
	{
        MenuElement[] menus =  menuBar.getSubElements();
        for (int ii = 0; ii < menus.length; ++ii)
        {
        	JMenu menu = (JMenu) menus[ii].getComponent();
        	if (menu.getText().equals(menuName)) return menu;
        }
        return null;
	}
	public static JMenuItem findMenuItem(JMenu menu, String menuItemName)
	{
        if (menu.getItemCount() > 0)
        {
	        for (int ii = 0; ii < menu.getItemCount(); ++ii)
	        {
	        	JMenuItem menuItem = menu.getItem(ii);
	        	if (menuItem.getText().equals(menuItemName)) return menuItem;
	        }
        }
        return null;
	}
	public static void messageDialog(String string, JFrame parentFrame)
	{
		JOptionPane.showMessageDialog(parentFrame, string);
	}
	public static void messageDialog(String string, JDialog parentDialog)
	{
		JOptionPane.showMessageDialog(parentDialog, string);
	}
	public static int optionDialog(String title, String text, String option1, String option2, int defaultOption,JFrame parentFrame)
	{
		if (defaultOption < 1) defaultOption = 1;
		if (defaultOption > 2) defaultOption = 2;
		Object[] options = {option1, option2};
		int n = JOptionPane.showOptionDialog(parentFrame,
				text,
				title,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[defaultOption - 1]);
		n= n+ 1;
		return n;
	}
    public static File chooseFile(String directoryPath, String dialogTitle, String selectedFileName, boolean saveDialog, String[] extensions, JFrame parentFrame) 
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
    	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	fc.setMultiSelectionEnabled(false);
    	fc.setSelectedFile(new File(selectedFileName));
    	if (extensions != null)
    	{
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(makeFileNameExtensionFilter(extensions));
    	}
        fc.setDialogTitle(dialogTitle);
        int returnVal = 0;
        if (saveDialog)
        {
            returnVal = fc.showSaveDialog(parentFrame);
        }
        else
        {
            returnVal = fc.showOpenDialog(parentFrame);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            file = fc.getSelectedFile();
        } 
        return file;
 	}
    public static FileNameExtensionFilter makeFileNameExtensionFilter(String[] extensions)
	{
    	if (extensions == null) return null;
		int numExtensions = 0;
		for (int ii = 0; ii < extensions.length; ++ii) if (extensions[ii] != null) numExtensions = numExtensions + 1;
		String[] nne = new String[numExtensions];
		int iext = 0;
	   	String extensionDesc = "";
		for (int ii = 0; ii < extensions.length; ++ii) 
		{
			if (extensions[ii] != null)
			{
				nne[iext] = extensions[ii];
				if (iext == 0 ) extensionDesc = extensionDesc + "*." + extensions[ii];
				if (iext >  0 ) extensionDesc = extensionDesc + ", *." + extensions[ii];
				iext = iext + 1;
			}
		}
		if (numExtensions == 1) return new FileNameExtensionFilter(extensionDesc, nne[0]);
		if (numExtensions == 2) return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1]);
		if (numExtensions == 3) return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1], nne[2]);
		if (numExtensions >= 4) return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1], nne[2], nne[3]);
		return null;
	}

}
