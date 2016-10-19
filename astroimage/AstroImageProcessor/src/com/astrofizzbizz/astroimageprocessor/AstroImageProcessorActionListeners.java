package com.astrofizzbizz.astroimageprocessor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AstroImageProcessorActionListeners implements ActionListener, ChangeListener, ComponentListener, MouseListener, MouseMotionListener
{
	String actionString = "";
	AstroImageProcessorGui gui;
	AstroImageProcessorActionListeners(String actionString, AstroImageProcessorGui gui)
	{
		this.actionString = actionString;
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		if (actionString.equals("File.Open")) if (!gui.disableMenu) gui.openFitsFile();
		if (actionString.equals("File.View Header")) if (!gui.disableMenu) gui.viewFitsSHeader();
		if (actionString.equals("File.Save as FITS")) if (!gui.disableMenu) gui.saveFitsFile();
		if (actionString.equals("File.Save as PNG")) if (!gui.disableMenu) gui.saveFitsToPng();
		if (actionString.equals("File.Save as JPG")) if (!gui.disableMenu) gui.saveFitsToJpg();
		if (actionString.equals("linearScale"))
		{
			gui.scaleType = 0;
			gui.displayFitsFile();
		}
		if (actionString.equals("logScale"))
		{
			gui.scaleType = 1;
			gui.displayFitsFile();
		}
		if (actionString.equals("sqrtScale"))
		{
			gui.scaleType = 3;
			gui.displayFitsFile();
		}
		if (actionString.equals("histoScale")) 
		{
			gui.scaleType = 2;
			gui.displayFitsFile();
		}
		if (actionString.equals("asinhScale")) 
		{
			gui.scaleType = 4;
			gui.displayFitsFile();
		}
		if (actionString.equals("Action.Add")) 			if (!gui.disableMenu) gui.addFitsFile();
		if (actionString.equals("Action.Subtract")) 	if (!gui.disableMenu) gui.subtractFitsFile();
		if (actionString.equals("Action.Divide")) 		if (!gui.disableMenu) gui.divideFitsFile();
		if (actionString.equals("Clean.Remove Hot Spots")) 	if (!gui.disableMenu) gui.removeHotspots();
		if (actionString.equals("Clean.Remove Ruler(s)")) 	if (!gui.disableMenu) gui.removeRulers();
		if (actionString.equals("Action.Normalize")) 	if (!gui.disableMenu) gui.normalize();
		if (actionString.equals("Action.Align")) 		if (!gui.disableMenu) gui.align();
		if (actionString.equals("MoveUpButton")) 		gui.plotAlignedImages( 0, 1, 0);
		if (actionString.equals("MoveDownButton")) 		gui.plotAlignedImages( 0,-1, 0);
		if (actionString.equals("MoveLeftButton")) 		gui.plotAlignedImages(-1, 0, 0);
		if (actionString.equals("MoveRightButton")) 	gui.plotAlignedImages( 1, 0, 0);
		if (actionString.equals("rotateCCWButton")) 	gui.plotAlignedImages(-1, 0, 720);
		if (actionString.equals("rotateCWButton")) 		gui.plotAlignedImages( 1, 0, -720);
		if (actionString.equals("rotate90CCWButton")) 	gui.plotAlignedImages(-1, 0, 3600*90);
		if (actionString.equals("rotate90CWButton")) 	gui.plotAlignedImages( 1, 0, -3600*90);
		if (actionString.equals("SaveAlignButton")) 	gui.saveAlignedImages(true);
		if (actionString.equals("CloseAlignButton")) 	gui.saveAlignedImages(false);
		if (actionString.equals("RGB Plot.Create RGB Plot"))		if (!gui.disableMenu) gui.rgbPlot();
		//add callback for rgb plot wizard
		if (actionString.equals("RGB Plot.RGB Plot Wizard"))		if (!gui.disableMenu) gui.rgbPlotWizard();
		if (actionString.equals("SaveRgbButton"))		gui.saveRgb(true);
		if (actionString.equals("CloseRgbButton"))		gui.saveRgb(false);
		if (actionString.equals("File.Exit"))           System.exit(0);
		if (actionString.equals("Help.About"))
			gui.messageDialog("StoneEdge Astro Image Processor " + gui.version + "\nWritten by Dave McGinnis\nLast Updated " + gui.versionDate);
		if (actionString.equals("Help.Help"))			gui.openHelp();
		if (actionString.equals("Help.YouTube Video"))	gui.openYouTubeVideo();
		if (actionString.equals("invertImage"))			gui.flipInvertImageButton();
		if (actionString.equals("colorSpectrum"))		gui.flipColorSpectrumButton();
		if (actionString.equals("invertY"))				gui.flipInvertYButton();		
	}
	public void stateChanged(ChangeEvent arg0) 
	{
		if (actionString.equals("RGB 0 Slider Changed")) gui.rgbSliderChanged(0);
		if (actionString.equals("RGB 1 Slider Changed")) gui.rgbSliderChanged(1);
		if (actionString.equals("RGB 2 Slider Changed")) gui.rgbSliderChanged(2);
		if (actionString.equals("minMax 0 Slider Changed")) gui.minMaxSliderChanged(0);
		if (actionString.equals("minMax 1 Slider Changed")) gui.minMaxSliderChanged(1);
	}

	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentResized(ComponentEvent arg0) {
		gui.tResize.restart();
		//if (actionString.equals("frameResized")) gui.windowResized();
	}
	public void componentShown(ComponentEvent e) {}
	public void mouseClicked(MouseEvent e) {
		if (actionString.equals("mouseClickedOnImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
				gui.mouseRightClickedOnBWImage(e);				
			} else { //not right click	
				gui.mouseClickedOnBWImage(e);
			}	
		}
		if (actionString.equals("mouseClickedOnZoomedImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
				gui.mouseRightClickedOnZoomedImage(e);
			} else { //not right click	
			}			
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		if (actionString.equals("mouseClickedOnImage")) {
			gui.mouseExitedBWImage(e);
		} else  if (actionString.equals("mouseClickedOnZoomedImage")) {
			gui.mouseExitedZoomedImage(e);
		}		
	}
	public void mousePressed(MouseEvent e) {
		if (actionString.equals("mouseClickedOnImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mousePressedOnBWImage(e);
			}			
		} else if (actionString.equals("mouseClickedOnZoomedImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mousePressedOnZoomedImage(e);
			}			
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (actionString.equals("mouseClickedOnImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mouseReleasedOnBWImage(e);
			}			
		} else if (actionString.equals("mouseClickedOnZoomedImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mouseReleasedOnZoomedImage(e);
			}			
		}
	}
	public void mouseDragged(MouseEvent e) {
		if (actionString.equals("mouseMovedOnImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mouseDraggedOnBWImage(e);
			}			
		} else if (actionString.equals("mouseMovedOnZoomedImage")) {
			if(e.getButton() == MouseEvent.BUTTON3) { //right click
			} else { //not right click	
				gui.mouseDraggedOnZoomedImage(e);
			}			
		}
	}
	public void mouseMoved(MouseEvent e) {
	}
}
