package com.astrofizzbizz.utilities.myjframe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.astrofizzbizz.utilities.DpmSwingUtilities;
import com.astrofizzbizz.utilities.StatusPanel;


@SuppressWarnings("serial")
public class MyJFrameClass extends JFrame
{
	public static final String delim = System.getProperty("file.separator");
	public static final String newline = System.getProperty("line.separator");
	
	private JMenuBar mainMenuBar;
	private JPanel mainPane;
	private StatusPanel statusBar;
	ClassLoader loader;

	protected String version = "v1.0";
	protected String versionDate = "January 22, 2016";

	public MyJFrameClass(String frametitle, String statusBarTitle, int numStatusLines, String imageIconSource)
	{
		super(frametitle);
		statusBar = new StatusPanel(numStatusLines, statusBarTitle);
		
		loader  = Thread.currentThread().getContextClassLoader();
		ImageIcon  logoIcon = new ImageIcon(loader.getResource(imageIconSource));
        setIconImage(logoIcon.getImage());
        try 
        {
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName());
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        mainMenuBar = makeMenu();
        setJMenuBar(mainMenuBar);
       
		mainPane= new JPanel();
        setupMainPanel();
        getContentPane().setLayout(new BorderLayout(5,5));
		getContentPane().add(mainPane, BorderLayout.CENTER);
		getContentPane().add(statusBar.getScrollPane(), java.awt.BorderLayout.SOUTH);  
		statusBar.setText("Welcome");
		pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        
        // Move the window
        this.setLocation(x, y);
		setVisible(true);
		statusBar.getScrollPane().setMinimumSize(statusBar.getScrollPane().getSize());
		statusBar.getScrollPane().setPreferredSize(statusBar.getScrollPane().getSize());

        
        addWindowListener(new java.awt.event.WindowAdapter() 
        {
            public void windowClosing(WindowEvent winEvt) 
            {
            	quitProgram();            
            }
        });

	}
	protected void setupMainPanel()
	{
		
	}
	protected  JMenuBar makeMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		String menuText[] = {"File", "Help"};
        String subMenuText[][] =
        {
    		{"Open","Exit"},
    		{"Help", "About"}
    	};

        for (int i = 0; i < menuText.length; i++)
        {
            JMenu menu = new JMenu(menuText[i]);
            menuBar.add (menu);
            
            for (int j = 0; j < subMenuText[i].length; j++)
            {
                JMenuItem item = new JMenuItem(subMenuText[i][j]);
                menu.add (item);
                item.addActionListener(new MyJFrameActionListeners(menuText[i] + "." +subMenuText[i][j], this));
            }
        }
        
        return menuBar;
    }
	protected void quitProgram()
	{
		dispose();
		System.exit(0);
	}
	private class MyJFrameActionListeners implements ActionListener
	{
		MyJFrameClass myJFrameClass;
		String actionString = "";
		MyJFrameActionListeners(String actionString, MyJFrameClass myJFrameClass)
		{
			this.actionString = actionString;
			this.myJFrameClass = myJFrameClass;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (actionString.equals("File.Exit"))
			{
				quitProgram();
			}
			if (actionString.equals("Help.About"))
			{
				DpmSwingUtilities.messageDialog("MyJFrame " + version + "\n" + "Last Updated " + versionDate, myJFrameClass);
			}
		}
		
	}
	public static void main(String[] args) 
	{
		new MyJFrameClass("My JFrame", "Info", 4, "com/astrofizzbizz/utilities/myjframe/files/Ess.JPG");
	}

}
