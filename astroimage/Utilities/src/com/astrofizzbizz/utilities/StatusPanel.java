package com.astrofizzbizz.utilities;

import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class StatusPanel 
{
	private JTextArea textArea;
	private JScrollPane scrollPane;
	public StatusPanel(int numLines, String title)
	{
		textArea = new JTextArea();
		textArea.setRows(numLines);
		scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(5,5,5,5)));
	}
	public void setText(String text)
	{
		String info = new Date().toString() + " : " + text;
		textArea.insert(info + "\n", 0);
		scrollToTop();
//		System.out.println(info);
	}
	public JScrollPane getScrollPane() 
	{
		return scrollPane;
	}
	public void scrollToTop()
	{
		textArea.setCaretPosition(0);
	}
}
