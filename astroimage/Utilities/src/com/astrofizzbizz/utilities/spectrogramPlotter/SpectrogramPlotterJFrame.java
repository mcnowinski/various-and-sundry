package com.astrofizzbizz.utilities.spectrogramPlotter;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;

public class SpectrogramPlotterJFrame extends JFrame 
{
	private static final long serialVersionUID = -6165604043529116140L;

	JTabbedPane jtab = new JTabbedPane();
	SpectrogramPlotter[] specPlot;
	ChartPanel[] chartPanel;
	public SpectrogramPlotterJFrame(String jFrameTitle, SpectrogramPlotter[] sp)
	{
		super(jFrameTitle);
		specPlot = sp;
        setJMenuBar(addMenu());
		int nplots = specPlot.length;
		chartPanel = new ChartPanel[nplots];
		for (int ip = 0; ip < nplots; ++ip)
		{
			chartPanel[ip]  = new ChartPanel(specPlot[ip].createSpectrogramChart());
			jtab.addTab(specPlot[ip].getChartTitle(), chartPanel[ip]);
		}
		getContentPane().add(jtab, BorderLayout.CENTER);
        try 
        {
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
	}
	private  JMenuBar addMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        String menuText[] = {"Set Plot Type", "Set Z Scale"};
        String itemText[][] = {
        		{"Color Spectrum", "Color Gray", "BW Gray"},
        		{"Set Zmax", "Set Zmin"}
        };

        for (int i = 0; i < menuText.length; i++)
        {
            JMenu menu = new JMenu(menuText[i]);
            menuBar.add (menu);
            
            for (int j = 0; j < itemText[i].length; j++)
            {
                JMenuItem item = new JMenuItem(itemText[i][j]);
                menu.add (item);
                item.addActionListener(new SpectrogramPlotterJFrameActionListeners( menuText[i] + "." +itemText[i][j], this));
           }
        }
        
        return menuBar;
    }
	class SpectrogramPlotterJFrameActionListeners implements ActionListener
	{
		String actionString = "";
		JFrame parentJFrame;
		SpectrogramPlotterJFrameActionListeners(String actionString, JFrame parentJFrame)
		{
			this.actionString = actionString;
			this.parentJFrame = parentJFrame;
		}
		public void actionPerformed(ActionEvent e) 
		{
	        if ("Set Plot Type.Color Spectrum".equals(actionString)) 
	        {
	        	int itab = jtab.getSelectedIndex();
	        	specPlot[itab].setColorSpectrumScale(true);
				chartPanel[itab]  = new ChartPanel(specPlot[itab].createSpectrogramChart());
				jtab.remove(itab);
				jtab.insertTab(specPlot[itab].getChartTitle(), null, chartPanel[itab], "", itab);
				jtab.setSelectedIndex(itab);
	        }
	        if ("Set Plot Type.Color Gray".equals(actionString)) 
	        {
	        	int itab = jtab.getSelectedIndex();
	        	specPlot[itab].setColorGrayScale(true);
				chartPanel[itab]  = new ChartPanel(specPlot[itab].createSpectrogramChart());
				jtab.remove(itab);
				jtab.insertTab(specPlot[itab].getChartTitle(), null, chartPanel[itab], "", itab);
				jtab.setSelectedIndex(itab);
	        }
	        if ("Set Plot Type.BW Gray".equals(actionString)) 
	        {
	        	int itab = jtab.getSelectedIndex();
	        	specPlot[itab].setColorGrayScale(false);
	        	specPlot[itab].setColorSpectrumScale(false);
				chartPanel[itab]  = new ChartPanel(specPlot[itab].createSpectrogramChart());
				jtab.remove(itab);
				jtab.insertTab(specPlot[itab].getChartTitle(), null, chartPanel[itab], "", itab);
				jtab.setSelectedIndex(itab);
	        }
	        if ("Set Z Scale.Set Zmax".equals(actionString)) 
	        {
	        	String s = (String)JOptionPane.showInputDialog(
	        						parentJFrame,
	        	                    "Enter Zmax",
	        	                    "Zmax",
	        	                    JOptionPane.PLAIN_MESSAGE,
	        	                    null,
	        	                    null,
	        	                    "");

	        	if ((s != null) && (s.length() > 0)) {
				double zmax = Double.valueOf(s).doubleValue();
	        	//If a string was returned, say so.
	        	    System.out.println(s);
		        	int itab = jtab.getSelectedIndex();
		        	specPlot[itab].setZmax(zmax);
					chartPanel[itab]  = new ChartPanel(specPlot[itab].createSpectrogramChart());
					jtab.remove(itab);
					jtab.insertTab(specPlot[itab].getChartTitle(), null, chartPanel[itab], "", itab);
					jtab.setSelectedIndex(itab);
					return;
	        	}
	        }
	        if ("Set Z Scale.Set Zmin".equals(actionString)) 
	        {
	        	String s = (String)JOptionPane.showInputDialog(
	        						parentJFrame,
	        	                    "Enter Zmin",
	        	                    "Zmin",
	        	                    JOptionPane.PLAIN_MESSAGE,
	        	                    null,
	        	                    null,
	        	                    "");

	        	if ((s != null) && (s.length() > 0)) {
				double zmin = Double.valueOf(s).doubleValue();
	        	//If a string was returned, say so.
	        	    System.out.println(s);
		        	int itab = jtab.getSelectedIndex();
		        	specPlot[itab].setZmin(zmin);
					chartPanel[itab]  = new ChartPanel(specPlot[itab].createSpectrogramChart());
					jtab.remove(itab);
					jtab.insertTab(specPlot[itab].getChartTitle(), null, chartPanel[itab], "", itab);
					jtab.setSelectedIndex(itab);
					return;
	        	}
	        }
		}
	}

	public static void main(String[] args) 
	{
   		double[] xseries = new double[10000];
   		double[] yseries = new double[10000];
   		double[] zseries = new double[10000];
  		double dx = 0.02;
  		double dy = 0.02;
  		double zmax = 0.0;
  		int ic = 0;
  		for (int ix = 0; ix < 100; ++ix)
  		{
  			for (int iy = 0; iy < 100; ++iy)
  			{
  	  			xseries[ic] = -1.0 + 2.0 * ((double) ix) / 99.0;
  	  			yseries[ic] = -1.0 + 2.0 * ((double) iy) / 99.0;
  				double r = Math.sqrt(xseries[ic] * xseries[ic] + yseries[ic] * yseries[ic]);
  				zseries[ic] = Math.exp(-r);
  				if (zmax < zseries[ic]) zmax = zseries[ic];
  				ic = ic + 1;
  			}
  		}
   		SpectrogramPlotter sp = new SpectrogramPlotter();
   		sp.setChartPixelHeight(600);
   		sp.setChartPixelWidth(800);
   		sp.setFileName("Test.png");
   		sp.setChartTitle("Test");
   		sp.setXAxisTitle("X axis");
   		sp.setYAxisTitle("Y axis");
   		sp.setZAxisTitle("Z axis");
  		sp.setDx(dx);
   		sp.setDy(dy);
   		sp.setZmax(zmax);
  		sp.setXseries(xseries);
   		sp.setYseries(yseries);
   		sp.setZseries(zseries);
   		SpectrogramPlotter[] spVec = new SpectrogramPlotter[3];
   		spVec[0] = sp;
   		spVec[1] = sp;
   		spVec[2] = sp;
   		
   		new SpectrogramPlotterJFrame("Test", spVec);
	}

}
