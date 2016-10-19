package com.astrofizzbizz.utilities;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CodeUpdateInformer 
{
	private JFrame infoJFrame;
	private String downloadURL; 
	private boolean newCodeDownloaded = false;
	private boolean waitForMe = false;
	
	public CodeUpdateInformer(String codeURL, String downloadURL, JFrame parent)
	{
		this.downloadURL = downloadURL;
		WaitFrame waitFrame  = new WaitFrame("Please Wait", "Checking for new code. Please Wait", parent);
		int iexists = urlExists(codeURL);
		waitFrame.dispose();
		if (iexists < 0)
		{
			waitForMe = true;
			infoJFrame = makeInfoJFrame(parent);
			while (waitForMe)
			{
				try {Thread.sleep(250);} catch (InterruptedException e) {}
			}

		}
	}
	private JFrame makeInfoJFrame(JFrame parent)
	{
		JFrame infoJFrame = new JFrame("New Code");
        try 
        {
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
		
		ClassLoader loader  = Thread.currentThread().getContextClassLoader();
		ImageIcon  logoIcon = new ImageIcon(loader.getResource("com/astrofizzbizz/utilities/files/information.jpg"));
		if (parent != null)
		{
			infoJFrame.setIconImage(parent.getIconImage());
		}
		else
		{
			infoJFrame.setIconImage(logoIcon.getImage());
		}
		JButton infoLabel      = new JButton("   New Code Available   ");
		infoLabel.setIcon(logoIcon);

		JButton downloadButton = new JButton("Download new code.     ");
		JButton cancelButton   = new JButton("Continue with old code.");
        downloadButton.addActionListener(new CodeUpdateInformerActionListeners("downloadButton", this));
        cancelButton.addActionListener(new CodeUpdateInformerActionListeners("cancelButton", this));
        downloadButton.setIcon(new ImageIcon(loader.getResource("com/astrofizzbizz/utilities/files/download.jpg")));
        cancelButton.setIcon(new ImageIcon(loader.getResource("com/astrofizzbizz/utilities/files/continue.jpg")));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(infoLabel);
		mainPanel.add(downloadButton);
		mainPanel.add(cancelButton);
		
		infoJFrame.getContentPane().add(mainPanel);
		infoJFrame.setVisible(true);
		infoJFrame.pack();
		if (parent != null)
		{
			infoJFrame.setLocationRelativeTo(parent);
		}
		else
		{
	        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	        int w = infoJFrame.getSize().width;
	        int h = infoJFrame.getSize().height;
	        int x = (dim.width-w)/2;
	        int y = (dim.height-h)/2;
	        infoJFrame.setLocation(x, y);
	        infoJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		infoJFrame.paint(infoJFrame.getGraphics());
		
		return infoJFrame;
	}
	public int  urlExists(String URLName)
	{
		int result = 0;
		URL url;
		try 
		{
			url = new URL(URLName);
			InputStream is  = url.openStream();
			result = 1;
			is.close();
		} catch (MalformedURLException e) 
		{
			System.err.println("Bad URL");
			result = 0;
		} catch (java.io.FileNotFoundException e) {
			result = -1;
		} catch (java.net.UnknownHostException e) {
			System.err.println("Bad Internet");
			result = 0;
		} catch (IOException e) {
			System.err.println("Bad Internet");
			result = 0;
		}
		catch (java.lang.NullPointerException e) {
			System.err.println("Bad URL");
			result = 0;
		}
		return result;
    }    
	public boolean isNewCodeDownloaded() {return newCodeDownloaded;}
	private class CodeUpdateInformerActionListeners implements ActionListener
	{
		String actionString = "";
		CodeUpdateInformer codeUpdateInformer;
		private CodeUpdateInformerActionListeners(String actionString, CodeUpdateInformer codeUpdateInformer)
		{
			this.actionString = actionString;
			this.codeUpdateInformer = codeUpdateInformer;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (actionString.equals("downloadButton"))
			{
				try 
				{
					URL imageLinkUrl = new URL(codeUpdateInformer.downloadURL);
			    	Desktop.getDesktop().browse(imageLinkUrl.toURI());
					codeUpdateInformer.newCodeDownloaded = true;
					codeUpdateInformer.waitForMe = false;
					codeUpdateInformer.infoJFrame.dispose();
				} 
				catch (Exception e) 
				{
					DpmSwingUtilities.messageDialog(e.getMessage(), codeUpdateInformer.infoJFrame);
					codeUpdateInformer.waitForMe = false;
					codeUpdateInformer.infoJFrame.dispose();
				} 
			}
			if (actionString.equals("cancelButton"))
			{
				codeUpdateInformer.waitForMe = false;
				codeUpdateInformer.infoJFrame.dispose();
			}
		}
		
	}
	public static void main(String s[])
    {  
        String codeURL = "https://stone-edge-iii.googlecode.com/files/AstroImageProcessorV3.jar";  
        String downloadURL = "https://code.google.com/p/stone-edge-iii/downloads/list";  
 		new CodeUpdateInformer(codeURL, downloadURL, null);
    }  
}  
