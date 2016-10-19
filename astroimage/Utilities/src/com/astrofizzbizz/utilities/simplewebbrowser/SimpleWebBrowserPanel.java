package com.astrofizzbizz.utilities.simplewebbrowser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class SimpleWebBrowserPanel 	extends JPanel implements HyperlinkListener,ActionListener
{
	private static final long serialVersionUID = -1025941314111863887L;
	private JButton homeButton;
	private JButton backButton;
	private JButton forwardButton;
	private JTextField urlField;
	private JEditorPane htmlPane;
	private String currentUrl;
	private String initialUrl;
	private JFrame parentFrame;
	private ArrayList<String> urlHistory = new ArrayList<String>(20);
	private int historyIndex =  0;

	public SimpleWebBrowserPanel(String initialUrl, JFrame parentFrame) 
	{
		super();
		this.currentUrl = initialUrl;
		this.initialUrl = initialUrl;
		this.parentFrame = parentFrame;
		urlHistory.add(initialUrl);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.lightGray);
		homeButton = new JButton("Home");
		homeButton.addActionListener(this);
		JLabel urlLabel = new JLabel("URL:");
		urlField = new JTextField(50);
		urlField.setText(initialUrl);
		urlField.addActionListener(this);
		
		backButton = new JButton("Back");
		backButton.addActionListener(this);
		forwardButton = new JButton("Forward");
		forwardButton.addActionListener(this);

		topPanel.add(backButton);
		topPanel.add(forwardButton);
		topPanel.add(homeButton);
		topPanel.add(urlLabel);
		topPanel.add(urlField);
		
		setNavButtonVisibility();
//		add(topPanel, BorderLayout.NORTH);
		
		topPanel.setMaximumSize(new Dimension(3000,40));
		add(topPanel);

		try 
		{
			htmlPane = new JEditorPane(initialUrl);
			htmlPane.setEditable(false);
			htmlPane.addHyperlinkListener(this);
			JScrollPane scrollPane = new JScrollPane(htmlPane);
//			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
			add(scrollPane);
		} 
		catch(IOException ioe) 
		{
			warnUser("Can't build HTML pane for " + initialUrl + ": " + ioe);
		}
	}
	public void actionPerformed(ActionEvent event) 
	{
		if (event.getSource() == urlField) 
		{
			currentUrl = urlField.getText();
			historyIndex = historyIndex + 1;
			urlHistory.add(historyIndex, currentUrl);
		}
		if (event.getSource() == homeButton)  // Clicked "home" button instead of entering URL
		{
			currentUrl = initialUrl;
			historyIndex  = historyIndex + 1;
			urlHistory.add(historyIndex, currentUrl);
		}
		if (event.getSource() == backButton)  // Clicked "back" button instead of entering URL
		{
			historyIndex  = historyIndex - 1;
			if (historyIndex < 0) historyIndex = 0;
			currentUrl = urlHistory.get(historyIndex);
		}
		if (event.getSource() == forwardButton)  // Clicked "back" button instead of entering URL
		{
			historyIndex  = historyIndex + 1;
			if (historyIndex >= urlHistory.size()) historyIndex = urlHistory.size() - 1;
			currentUrl = urlHistory.get(historyIndex);
		}
		setNavButtonVisibility();
		try 
		{
			htmlPane.setPage(new URL(currentUrl));
			urlField.setText(currentUrl);
		} 
		catch(IOException ioe) 
		{
			warnUser("Can't follow link to " + currentUrl + ": " + ioe);
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent event) 
	{
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
		{
			try 
			{
				htmlPane.setPage(event.getURL());
				currentUrl = event.getURL().toExternalForm();
				urlField.setText(currentUrl);
				historyIndex = historyIndex + 1;
				urlHistory.add(historyIndex,currentUrl);
				setNavButtonVisibility();
			} 
			catch(IOException ioe) 
			{
				warnUser("Can't follow link to "  + event.getURL().toExternalForm() + ": " + ioe);
			}
		}
	}
	private void setNavButtonVisibility()
	{
		if (historyIndex == 0)
		{
			backButton.setEnabled(false);
		}
		else
		{
			backButton.setEnabled(true);
		}
		if (historyIndex == urlHistory.size() - 1)
		{
			forwardButton.setEnabled(false);
		}
		else
		{
			forwardButton.setEnabled(true);
		}

	}
	private void warnUser(String message) 
	{
		JOptionPane.showMessageDialog(parentFrame, message, "Error", 
                                  JOptionPane.ERROR_MESSAGE);
	}

}
