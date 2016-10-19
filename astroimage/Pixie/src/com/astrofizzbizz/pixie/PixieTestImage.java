package com.astrofizzbizz.pixie;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.astrofizzbizz.utilities.FileChooserWrapper;


/**
 * @author mcginnis
 *
 */
@SuppressWarnings("serial")
public class PixieTestImage extends JFrame implements ActionListener
{
	static JFrame frame = null;
	private String lastDirectoryPath = null;
	PixieImage pixieImage = null;
	Pixie psize = null;
	JTextField imageSizeTextField = null;
	PixieImageRGBPlotter plotter = null;
 	ImageIcon imageIcon = null;
	JLabel imageIconLabel = null;

	JTextField thetaTextField = new JTextField(8);
	JTextField intensityTextField = new JTextField(8);
	JTextField sigmaxTextField = new JTextField(8);
	JTextField sigmayTextField = new JTextField(8);
	JTextField powerTextField = new JTextField(8);
	JTextField col_pos_fracTextField = new JTextField(8);
	JTextField row_pos_fracTextField = new JTextField(8);
	double dtheta = 0.0; 
	double dintensity = 100.0; 
	double dsigmax = 30.0; 
	double dsigmay = 30.0;
	double dpower = 2.0;
	double dcol_pos_frac = 0.5; 
	double drow_pos_frac = 0.5;	
	/**
	 * @param pmax
	 */
	public PixieTestImage()
	{
		psize = new Pixie(new PixieCoord(200,200),-1.0);
	}
	/**
	 * @param dtheta
	 * @param dintensity
	 * @param dsigmax
	 * @param dsigmay
	 * @param dpower
	 * @param dcol_pos_frac
	 * @param drow_pos_frac
	 */
	private Component createComponents() 
	{
	 	imageSizeTextField = new JTextField(5);
	 	imageSizeTextField.setText(Integer.toString(psize.getRow() + 1));
	 	JLabel imageSizeLabel = new JLabel("Image Size");
	 	JPanel imageSizePanel = new JPanel();
	 	imageSizePanel.setLayout(new GridLayout(1,2,5,5));
	 	imageSizePanel.add(imageSizeLabel);
	 	imageSizePanel.add(imageSizeTextField);
	 	
	 	JPanel imageDisplayPanel = new JPanel();
	 	imageIcon = new ImageIcon();	 	
		imageIconLabel = new JLabel(imageIcon);
		imageDisplayPanel.add(imageIconLabel);
		
	 	JPanel imagePanel = new JPanel();
	 	imagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),BorderFactory.createEmptyBorder(5,5,5,5)));
	 	imagePanel.add(imageSizePanel);
	 	imagePanel.add(imageDisplayPanel);

	 	thetaTextField.setText(Double.toString(dtheta));
	 	intensityTextField.setText(Double.toString(dintensity));
	 	sigmaxTextField.setText(Double.toString(dsigmax));
	 	sigmayTextField.setText(Double.toString(dsigmay));
	 	powerTextField.setText(Double.toString(dpower));
	 	col_pos_fracTextField.setText(Double.toString(dcol_pos_frac));
	 	row_pos_fracTextField.setText(Double.toString(drow_pos_frac));
	 	JLabel thetaTextLabel = new JLabel("Theta");
	 	JLabel intensityTextLabel = new JLabel("Intensity");
	 	JLabel sigmaxTextLabel = new JLabel("Sigma X");
	 	JLabel sigmayTextLabel = new JLabel("Sigma Y");
	 	JLabel powerTextLabel = new JLabel("Power");
	 	JLabel col_pos_fracTextLabel = new JLabel("Col. Pos. Frac.");
	 	JLabel row_pos_fracTextLabel = new JLabel("Row. Pos. Frac.");

	 	JPanel ellipseSettingsPanel = new JPanel();
	 	ellipseSettingsPanel.setLayout(new GridLayout(7,2,5,5));
	 	ellipseSettingsPanel.add(thetaTextLabel);
	 	ellipseSettingsPanel.add(thetaTextField);
	 	ellipseSettingsPanel.add(intensityTextLabel);
	 	ellipseSettingsPanel.add(intensityTextField);
	 	ellipseSettingsPanel.add(sigmaxTextLabel);
	 	ellipseSettingsPanel.add(sigmaxTextField);
	 	ellipseSettingsPanel.add(sigmayTextLabel);
	 	ellipseSettingsPanel.add(sigmayTextField);
	 	ellipseSettingsPanel.add(powerTextLabel);
	 	ellipseSettingsPanel.add(powerTextField);
	 	ellipseSettingsPanel.add(col_pos_fracTextLabel);
	 	ellipseSettingsPanel.add(col_pos_fracTextField);
	 	ellipseSettingsPanel.add(row_pos_fracTextLabel);
	 	ellipseSettingsPanel.add(row_pos_fracTextField);
	 	
	 	JTabbedPane tabbedPane = new JTabbedPane();
	 	tabbedPane.addTab("Images", imagePanel);
	 	tabbedPane.addTab("Ellipse Settings", ellipseSettingsPanel);
	 	
	 	JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(1,3,5,5));
        mainPanel.setLayout(new GridLayout(1,4,5,5));
        mainPanel.add(tabbedPane);
	 		 	       
        return mainPanel;
	}
    private  JMenuBar addMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        String menuText[] = {"File", "Add", "Help"};
        String itemText[][] =
            {{"New Image", "Save Image", "Exit"},
             {"Ellipse"},
             {"Help", "About"}};

        for (int i = 0; i < menuText.length; i++)
        {
            JMenu menu = new JMenu(menuText[i]);
            menuBar.add (menu);
            
            for (int j = 0; j < itemText[i].length; j++)
            {
                JMenuItem item = new JMenuItem(itemText[i][j]);
                menu.add (item);
                item.setActionCommand(menuText[i] + "." + itemText[i][j]);
                item.addActionListener(this);
 
            }
        }
        return menuBar;
    }
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getActionCommand().equals("File.New Image")) onNewFile();
		if (arg0.getActionCommand().equals("File.Save Image")) onSaveFile();
		if (arg0.getActionCommand().equals("Add.Ellipse")) onAddEllipse();
		if (arg0.getActionCommand().equals("File.Exit"))
			System.exit(0);
		if (arg0.getActionCommand().equals("Help.Help"))
			JOptionPane.showMessageDialog(frame, "You've got to be kidding.");
		if (arg0.getActionCommand().equals("Help.About"))
			JOptionPane.showMessageDialog(frame, "McGinnis Philanthropies Foundation\nAll Rights Reserved");
	}
	private void onNewFile()
	{
		int isize = Integer.valueOf(imageSizeTextField.getText()).intValue();
		psize = new Pixie(new PixieCoord(isize - 1, isize - 1),0.0);
		pixieImage = new PixieImage(psize);
		plotter = new PixieImageRGBPlotter();
		plotter.setImages(pixieImage, pixieImage, pixieImage);
		plotter.setScaleType("linear");
		plotter.setInvertImage(false);
		plotter.setAutoScale(true);
		plotter.setColorSpectrum(true);
		plotter.setPixelValueLimits(pixieImage);
		plotter.setScaleTable(null);
		
	 	imageIcon = new ImageIcon(plotter.makeBufferedImage());
	 	imageIconLabel.setIcon(imageIcon);
        frame.pack();
	}
	private void onAddEllipse()
	{
		if (pixieImage == null) onNewFile();
	 	dtheta = Double.valueOf(thetaTextField.getText()).doubleValue();
	 	dintensity = Double.valueOf(intensityTextField.getText()).doubleValue();
	 	dsigmax = Double.valueOf(sigmaxTextField.getText()).doubleValue();
	 	dsigmay = Double.valueOf(sigmayTextField.getText()).doubleValue();
	 	dpower = Double.valueOf(powerTextField.getText()).doubleValue();
	 	dcol_pos_frac = Double.valueOf(col_pos_fracTextField.getText()).doubleValue();
	 	drow_pos_frac = Double.valueOf(row_pos_fracTextField.getText()).doubleValue();
		
		double[][] dpix = pixieImage.getPix();
		for (int ii = 0; ii <= psize.getRow(); ii++)
		{  
			for (int ij = 0; ij <= psize.getCol(); ij++) 
			{
				double dx =   (((double) ij) - dcol_pos_frac * ((double) psize.getCol()))
					* Math.cos(dtheta) 
					+  (((double) ii) - drow_pos_frac * ((double) psize.getRow()))
					* Math.sin(dtheta);
				dx = Math.pow(Math.abs(dx) / dsigmax, dpower);
				double dy =  -(((double) ij) - dcol_pos_frac * ((double) psize.getCol()))
					* Math.sin(dtheta) 
					+  (((double) ii) - drow_pos_frac * ((double) psize.getRow()))
					* Math.cos(dtheta);
				dy = Math.pow(Math.abs(dy) / dsigmay, dpower);
				dpix[ii][ij] = dpix[ii][ij] + dintensity * Math.exp(-(dx + dy) / dpower); 
			}
		}

		plotter = new PixieImageRGBPlotter();
		plotter.setImages(pixieImage, pixieImage, pixieImage);
		plotter.setScaleType("linear");
		plotter.setInvertImage(false);
		plotter.setAutoScale(true);
		plotter.setColorSpectrum(true);
		plotter.setPixelValueLimits(pixieImage);
		plotter.setScaleTable(null);
		
	 	imageIcon = new ImageIcon(plotter.makeBufferedImage());
	 	imageIconLabel.setIcon(imageIcon);
        frame.pack();
	}
	private void onSaveFile()
	{
		if (pixieImage == null) return;
	   	FileChooserWrapper fc = new FileChooserWrapper(this);
//		File saveFile = fc.fOpenFile("fit",false, lastDirectoryPath);
		File saveFile = fc.fSaveFile("fit", lastDirectoryPath);
		if (saveFile == null) return;
     	lastDirectoryPath = saveFile.getPath();
     	try 
     	{
			pixieImage.writeToFitsFile(saveFile);
		} 
     	catch (PixieImageException e) 
     	{
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
        try 
        {
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        
        //Create the top-level container and add contents to it.
        frame = new JFrame("Image Simulator");
        PixieTestImage app = new PixieTestImage();
 
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);
        frame.setJMenuBar(app.addMenu());
      
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
 	}
}
