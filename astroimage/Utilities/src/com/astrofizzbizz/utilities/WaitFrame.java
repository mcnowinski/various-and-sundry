package com.astrofizzbizz.utilities;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WaitFrame extends JFrame
{
	private static final long serialVersionUID = -7926760418288495567L;

	public WaitFrame(String title, String infoMessage, JFrame parent)
	{
		super(title);
		ClassLoader loader  = Thread.currentThread().getContextClassLoader();
		ImageIcon  logoIcon = new ImageIcon(loader.getResource("com/astrofizzbizz/utilities/files/warning.jpg"));
		JLabel warningLabel = new JLabel();
		warningLabel.setIcon(logoIcon);
		if (parent != null)
		{
			setIconImage(parent.getIconImage());
		}
		else
		{
	        setIconImage(logoIcon.getImage());
		}
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),BorderFactory.createEmptyBorder(5,5,5,5)));
		infoPanel.add(warningLabel);
		infoPanel.add(new JLabel("\n" + infoMessage + "\n"));
		getContentPane().add(infoPanel);
		setVisible(true);
		pack();
		if (parent != null)
		{
			setLocationRelativeTo(parent);
		}
		else
		{
	        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	        int w = this.getSize().width;
	        int h = this.getSize().height;
	        int x = (dim.width-w)/2;
	        int y = (dim.height-h)/2;
	        this.setLocation(x, y);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		paint(getGraphics());
	}
	public static void main(String s[])
    {  
		new WaitFrame("Hi There", "This is a test! Please Take Shelter!", null);
    }  
}
