package com.astrofizzbizz.utilities.simplewebbrowser;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;


public class SimpleWebBrowserJFrame extends JFrame 
{
	private static final long serialVersionUID = -7520335325549518155L;
	SimpleWebBrowserPanel swbp = null;

	public SimpleWebBrowserJFrame(String initialURL) 
	{
		super("Simple Swing Browser");
		addWindowListener(new ExitListener());
        try 
        {
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        } 
        catch (Exception e)
        {}
        swbp = new SimpleWebBrowserPanel(initialURL, this);
		getContentPane().add(swbp);

		Dimension screenSize = getToolkit().getScreenSize();
		int width = screenSize.width * 8 / 10;
		int height = screenSize.height * 8 / 10;
		setBounds(width/8, height/8, width, height);
		setVisible(true);
	}
	public class ExitListener extends WindowAdapter 
	{
		public void windowClosing(WindowEvent event) 
		{
			System.exit(0);
		}
	}
	public static void main(String[] args) 
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL resourceURL = loader.getResource("com/astrofizzbizz/simplewebbrowser/files/homePage.html");
		new SimpleWebBrowserJFrame(resourceURL.toString());
	}

}

