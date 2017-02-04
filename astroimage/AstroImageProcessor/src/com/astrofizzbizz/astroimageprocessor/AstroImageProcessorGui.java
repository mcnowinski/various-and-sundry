package com.astrofizzbizz.astroimageprocessor;

import java.awt.Cursor;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import jsky.coords.WorldCoords;
import static javax.swing.ScrollPaneConstants.*;

import com.astrofizzbizz.pixie.PixieImage;
import com.astrofizzbizz.pixie.PixieImageException;
import com.astrofizzbizz.pixie.PixieImageRGBPlotterNoSwing;
import com.astrofizzbizz.pixie.SingleSimpleImageProcess;

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
//import nom.tam.fits.HeaderCardException;

public class AstroImageProcessorGui extends JFrame {
	private static final long serialVersionUID = -1433615005454840487L;
	protected String version = "v3.4.8";
	protected String versionDate = "January 26, 2017";
	private final static int RECOMMENDED_HEAP = 768;

	JList status;
	DefaultListModel statusMsgs;

	JLabel statusBar = new JLabel();
	JLabel imageIconLabel/* = new JLabel()*/;
	JLabel zoomedImageIconLabel/* = new JLabel()*/;
	JPanel settingsAndImagePanel;
	JPanel imagePanel;
	JPanel settingsPanel;
	JPanel scaleTypePanel;
	JPanel alignPanel;
	JPanel rgbPanel;
	JPanel invertAndColorPanel;
	JSlider[] rgbSlider = new JSlider[3];
	JLabel[] rgbLevelLabel = new JLabel[3];
	JPanel minMaxPanel;
	JSlider[] minMaxSlider = new JSlider[2];
	JLabel[] minMaxLevelLabel = new JLabel[2];
	JRadioButton invertImageButton;
	JRadioButton colorSpectrumButton;
	JRadioButton invertYButton;
	boolean invertImage = false;
	boolean colorSpectrum = false;
	boolean invertY = true; // default invert to true?
	boolean initializingPanels = true;

	BufferedImage fitsImage;
	int imageWidth;
	int imageHeight;
	BufferedImage fitsZoomedImage;	
	int zoomedImageWidth;
	int zoomedImageHeight;
	
	//batch align parameters
	private boolean doingBatchAlign = false;
	private String[] fitsAlignFilePaths;
	private String fitsAlignRefPath = "";
	private int fitsAlignCurrentIdx = -1;

	double[] zoomedImageScale = new double[2];
	double[] zoomedImageOffset = new double[2];
	double zoomedImageCenterRow;
	double zoomedImageCenterCol;
	PixieImage[] zoomPixieImageDisplay = new PixieImage[3];
	
	double[] imageScale = new double[2];
	double[] imageOffset = new double[2];
	int ialignLeftSum = 0;
	int ialignUpSum = 0;
	double thetaSumSeconds = 0;

	SingleSimpleImageProcess ssip = null;
	SingleSimpleImageProcess ssipAlignStart = null;
	SingleSimpleImageProcess ssipRef = null;
	SingleSimpleImageProcess[] ssipRgbOrig = { null, null, null };
	SingleSimpleImageProcess[] ssipRgbMod = { null, null, null };
	String ssipFitsFileName = null;
	String subtractFitsFileName = "";
	String normalizeFitsFileName = "";
	String alignFitsFileName = "";
	PixieImage[] pixieImageDisplay = new PixieImage[3];
	boolean disableMenu = false;
	boolean startupScreenDisplayed = true;
	ImageIcon startupDisplay = null;
	String[] fitsExtensions = { "fits", "fts", "fit" };
	Dimension mouseClick = null;
	Dimension settingsPanelMinimumSize = null;
	AstroImageProcessorHelpFrame helpFrame = null;

	DecimalFormat tp = new DecimalFormat("##.##");

	private String lastDirectoryPath = null;

	int scaleType = 3;
	
	private boolean dragging = false;
	private int ruler_x_start = 0;
	private int ruler_y_start = 0;
	private int ruler_x_end = 0;
	private int ruler_y_end = 0;

	//same as above, but for the zoomed window
	private boolean dragging_zoomed = false;
	private int ruler_x_start_zoomed = 0;
	private int ruler_y_start_zoomed = 0;
	private int ruler_x_end_zoomed = 0;
	private int ruler_y_end_zoomed = 0;	
	
	private double maxMinSliderIncrement = 0.05; //percentage increment for min max slider
	
	//delay resize of image to prevent overloading the CPU
	ActionListener resizeTimer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			windowResized();
			tResize.stop();
		}
	};
	public Timer tResize = new Timer(250, resizeTimer);

	public AstroImageProcessorGui(String jFrameTitle) {
		super(jFrameTitle);
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
		}

		setupDisplay();
		getContentPane().add(settingsAndImagePanel, BorderLayout.CENTER);
		getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
		pack();
		setVisible(true);
		toFront();
		repaint();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String iconLoc = "com/astrofizzbizz/astroimageprocessor/files/StoneEdgeTelescopeLogo.jpg";
		String startupImageLoc = "com/astrofizzbizz/astroimageprocessor/files/StoneEdgeImages.jpg";
		URL resourceURL = loader.getResource(startupImageLoc);
		resourceURL = loader.getResource(iconLoc);
		setIconImage(new ImageIcon(resourceURL).getImage());
		this.addComponentListener(new AstroImageProcessorActionListeners("frameResized", this));
		this.setMinimumSize(new Dimension(600, 400));
		int heapSizeMegs = (int) (Runtime.getRuntime().maxMemory() / 1024) / 1024;
		if (heapSizeMegs < RECOMMENDED_HEAP) {
			String message = "Max Memory size = " + heapSizeMegs + " MB\n";
			message = message + "Recommend size = " + RECOMMENDED_HEAP
					+ " MB\n";
			message = message
					+ "Might have trouble aligning or making RGB plots for large images";
			messageDialog(message);
		}
	}

	//public void paint(Graphics g) {
	//	super.paint(g);
	//}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupDisplay() {
		setJMenuBar(addMenu());

		settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Settings"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		settingsPanel.setLayout(new BoxLayout(settingsPanel,
				BoxLayout.PAGE_AXIS));
		setupScaleTypePanel();
		setupInvertAndColorPanel();
		setupAlignPanel();
		setupRgbPanel();
		setupMinMaxSliderPanel();
		settingsPanel.add(scaleTypePanel);
		settingsPanel.add(invertAndColorPanel);
		settingsPanel.add(minMaxPanel);
		settingsPanel.add(rgbPanel);
		settingsPanel.add(alignPanel);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Status"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.PAGE_AXIS));
		// statusPanel.add(zoomedImageIconLabel);
		statusMsgs = new DefaultListModel();
		status = new JList(statusMsgs);
		status.setFont(new Font("Tahoma", Font.PLAIN, 10));
		JScrollPane statusScroll = new JScrollPane(status) {
//			// don't know why I had to do this! but it helps with status window updates
//			public Dimension getPreferredSize() {
//				Dimension pref = super.getPreferredSize();
//				Dimension max = super.getMaximumSize();
//				if (pref.height > max.height)
//					pref.height = max.height;
//				if (pref.width > max.width)
//					pref.width = max.width;
//				return pref;
//			}
		};
		statusScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		statusScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//statusScroll.setMaximumSize(new Dimension(300, 100));
		statusPanel.add(statusScroll);
		settingsPanel.add(statusPanel);

		JPanel zoomedImagePanel = new JPanel();
		zoomedImagePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Zoom"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		zoomedImagePanel.setLayout(new FlowLayout());
		
		zoomedImageIconLabel = new JLabel();
		zoomedImageIconLabel.addMouseListener(new AstroImageProcessorActionListeners("mouseClickedOnZoomedImage", this));
		zoomedImageIconLabel.addMouseMotionListener(new AstroImageProcessorActionListeners("mouseMovedOnZoomedImage", this));
		zoomedImagePanel.add(zoomedImageIconLabel);
		settingsPanel.add(zoomedImagePanel);

		settingsPanelMinimumSize = new Dimension(settingsPanel.getMinimumSize());
		settingsPanel.setMaximumSize(new Dimension(
				settingsPanelMinimumSize.width, 1500));

		alignPanel.setVisible(false);
		rgbPanel.setVisible(false);

		imageIconLabel = new JLabel();
		imageIconLabel.addMouseListener(new AstroImageProcessorActionListeners("mouseClickedOnImage", this));
		imageIconLabel.addMouseMotionListener(new AstroImageProcessorActionListeners("mouseMovedOnImage", this));
		imagePanel = new JPanel();
		imagePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		imagePanel.add(imageIconLabel);

		settingsAndImagePanel = new JPanel();
		settingsAndImagePanel.setLayout(new BoxLayout(settingsAndImagePanel,BoxLayout.X_AXIS));
		settingsAndImagePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JScrollPane settingsScrollPane = new JScrollPane(settingsPanel);
		settingsScrollPane
				.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		settingsScrollPane.setMaximumSize(new Dimension(
				settingsPanelMinimumSize.width + 10, 1500));
		settingsAndImagePanel.add(settingsScrollPane);
		settingsAndImagePanel.add(imagePanel);
		statusBar.setText("Version " + version + " - " + versionDate);
		initializingPanels = false;
	}

	@SuppressWarnings("unchecked")
	private void statusMsg(String msg) {
		statusMsgs.addElement(msg);
		status.setSelectedIndex(statusMsgs.size() - 1);
		status.ensureIndexIsVisible(status.getSelectedIndex());
		status.paintImmediately(status.getBounds());
	}

	private void clearStatusMsg() {
		statusMsgs.clear();
		status.paintImmediately(status.getBounds());
	}

	private void setupScaleTypePanel() {
		scaleTypePanel = new JPanel();
		JRadioButton[] scaleTypeButton = new JRadioButton[4];
		scaleTypePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Transfer Function"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		scaleTypePanel.setLayout(new FlowLayout());
		scaleTypeButton[0] = new JRadioButton("Linear");
		scaleTypeButton[1] = new JRadioButton("Sqrt");
		scaleTypeButton[2] = new JRadioButton("Log");
		scaleTypeButton[3] = new JRadioButton("Asinh");
		scaleTypeButton[0]
				.addActionListener(new AstroImageProcessorActionListeners(
						"linearScale", this));
		scaleTypeButton[1]
				.addActionListener(new AstroImageProcessorActionListeners(
						"sqrtScale", this));
		scaleTypeButton[2]
				.addActionListener(new AstroImageProcessorActionListeners(
						"logScale", this));
		scaleTypeButton[3]
				.addActionListener(new AstroImageProcessorActionListeners(
						"asinhScale", this));
		ButtonGroup graphicsTypeButtonGroup = new ButtonGroup();
		graphicsTypeButtonGroup.add(scaleTypeButton[0]);
		graphicsTypeButtonGroup.add(scaleTypeButton[1]);
		graphicsTypeButtonGroup.add(scaleTypeButton[2]);
		graphicsTypeButtonGroup.add(scaleTypeButton[3]);
		scaleTypePanel.add(scaleTypeButton[0]);
		scaleTypePanel.add(scaleTypeButton[1]);
		scaleTypePanel.add(scaleTypeButton[2]);
		scaleTypePanel.add(scaleTypeButton[3]);
		if (scaleType == 0)
			scaleTypeButton[0].setSelected(true);
		if (scaleType == 3)
			scaleTypeButton[1].setSelected(true);
		if (scaleType == 1)
			scaleTypeButton[2].setSelected(true);
		if (scaleType == 4)
			scaleTypeButton[3].setSelected(true);

		return;
	}

	private void setupInvertAndColorPanel() {
		invertImage = false;
		colorSpectrum = false;
		invertY = false; // default invert Y to true?
		invertAndColorPanel = new JPanel();
		invertAndColorPanel.setLayout(new FlowLayout());
		invertAndColorPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		invertImageButton = new JRadioButton("Invert Color");
		invertImageButton.setSelected(false);
		invertImageButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"invertImage", this));
		invertAndColorPanel.add(invertImageButton);

		colorSpectrumButton = new JRadioButton("Color Spectrum");
		colorSpectrumButton.setSelected(false);
		colorSpectrumButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"colorSpectrum", this));
		invertAndColorPanel.add(colorSpectrumButton);

		//new invert Y button
		invertYButton = new JRadioButton("Invert Y");
		invertYButton.setSelected(invertY);
		invertYButton.addActionListener(new AstroImageProcessorActionListeners(
				"invertY", this));
		invertAndColorPanel.add(invertYButton);
	}

	private void setupAlignPanel() {
		alignPanel = new JPanel();
		alignPanel.setLayout(new BoxLayout(alignPanel, BoxLayout.Y_AXIS));
		alignPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Align"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JPanel[] buttonPanel = new JPanel[4];
		for (int ii = 0; ii < 4; ++ii) {
			buttonPanel[ii] = new JPanel();
			buttonPanel[ii].setLayout(new FlowLayout());
		}	
		
		JButton moveUpButton = new JButton(" Up ");
		moveUpButton.addActionListener(new AstroImageProcessorActionListeners(
				"MoveUpButton", this));
		buttonPanel[0].add(moveUpButton);

		JButton moveDownButton = new JButton("Down");
		moveDownButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"MoveDownButton", this));
		buttonPanel[2].add(moveDownButton);		
		
		JButton moveLeftButton = new JButton("Left");
		moveLeftButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"MoveLeftButton", this));
		buttonPanel[1].add(moveLeftButton);

		JButton moveRightButton = new JButton("Right");
		moveRightButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"MoveRightButton", this));
		buttonPanel[1].add(moveRightButton);	

		JButton rotate90CCWButton = new JButton("90CCW");
		rotate90CCWButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"rotate90CCWButton", this));
		buttonPanel[3].add(rotate90CCWButton);		
		
		JButton rotateCCWButton = new JButton("CCW");
		rotateCCWButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"rotateCCWButton", this));
		buttonPanel[3].add(rotateCCWButton);	
		
		JButton rotateCWButton = new JButton("CW");
		rotateCWButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"rotateCWButton", this));
		buttonPanel[3].add(rotateCWButton);		

		JButton rotate90CWButton = new JButton("90CW");
		rotate90CWButton
				.addActionListener(new AstroImageProcessorActionListeners(
						"rotate90CWButton", this));
		buttonPanel[3].add(rotate90CWButton);		
		
		JPanel allButtonPanel = new JPanel();
		allButtonPanel
				.setLayout(new BoxLayout(allButtonPanel, BoxLayout.Y_AXIS));
		allButtonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		allButtonPanel.add(buttonPanel[0]);
		allButtonPanel.add(buttonPanel[1]);
		allButtonPanel.add(buttonPanel[2]);
		allButtonPanel.add(buttonPanel[3]);		
		alignPanel.add(allButtonPanel);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new AstroImageProcessorActionListeners(
				"SaveAlignButton", this));

		JButton closeButton = new JButton("Close Align");
		closeButton.addActionListener(new AstroImageProcessorActionListeners(
				"CloseAlignButton", this));

		JPanel saveQuitPanel = new JPanel();
		saveQuitPanel.setLayout(new FlowLayout());
		saveQuitPanel.add(saveButton);
		saveQuitPanel.add(closeButton);
		alignPanel.add(saveQuitPanel);

	}

	void setupRgbPanel() {
		JPanel rgbSliderPanel = new JPanel();
		rgbSliderPanel.setLayout(new GridLayout(1, 3));
		AstroImageProcessorActionListeners[] rgbSliderListen = new AstroImageProcessorActionListeners[3];
		JPanel[] singleRgbSliderPanel = new JPanel[3];
		JLabel[] colorTitle = { new JLabel("Red"), new JLabel("Green"),
				new JLabel("Blue") };
		for (int ii = 0; ii < 3; ++ii) {
			singleRgbSliderPanel[ii] = new JPanel();
			singleRgbSliderPanel[ii].setLayout(new BoxLayout(
					singleRgbSliderPanel[ii], BoxLayout.Y_AXIS));
			rgbSlider[ii] = new JSlider(JSlider.VERTICAL, -100, 100, 0);
			rgbSliderListen[ii] = new AstroImageProcessorActionListeners("RGB "
					+ ii + " Slider Changed", this);
			rgbSlider[ii].addChangeListener(rgbSliderListen[ii]);
			rgbSlider[ii].setMinimumSize(new Dimension(20, 100));

			// Turn on labels at major tick marks.
			rgbSlider[ii].setMajorTickSpacing(100);
			rgbSlider[ii].setMinorTickSpacing(100);
			rgbSlider[ii].setPaintTicks(true);
			rgbSlider[ii].setPaintLabels(false);
			rgbSlider[ii].setSnapToTicks(false);
			rgbLevelLabel[ii] = new JLabel("1.0");
			// rgbSlider[ii].setSize(rgbSlider[ii].getSize().width, 1000);

			singleRgbSliderPanel[ii].add(rgbSlider[ii]);
			singleRgbSliderPanel[ii].add(colorTitle[ii]);
			singleRgbSliderPanel[ii].add(rgbLevelLabel[ii]);

			rgbSliderPanel.add(singleRgbSliderPanel[ii]);

		}
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new AstroImageProcessorActionListeners(
				"SaveRgbButton", this));

		JButton closeButton = new JButton("Close RGB");
		closeButton.addActionListener(new AstroImageProcessorActionListeners(
				"CloseRgbButton", this));

		JPanel saveQuitPanel = new JPanel();
		saveQuitPanel.setLayout(new FlowLayout());
		saveQuitPanel.add(saveButton);
		saveQuitPanel.add(closeButton);

		rgbPanel = new JPanel();
		rgbPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("RGB Colors"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		rgbPanel.setLayout(new BoxLayout(rgbPanel, BoxLayout.Y_AXIS));
		// rgbPanel.setLayout(new GridLayout(2,1));
		rgbPanel.add(rgbSliderPanel);
		rgbPanel.add(saveQuitPanel);
	}

	private void setupMinMaxSliderPanel() {
		JPanel minMaxSliderPanel = new JPanel();
		minMaxSliderPanel.setLayout(new GridLayout(2, 1));
		AstroImageProcessorActionListeners[] minMaxSliderListen = new AstroImageProcessorActionListeners[2];
		JPanel[] singleMinMaxSliderPanel = new JPanel[2];
		JLabel[] minMaxTitle = { new JLabel("Min"), new JLabel("Max"),
				new JLabel("Blue") };
		for (int ii = 0; ii < 2; ++ii) {
			singleMinMaxSliderPanel[ii] = new JPanel();
			singleMinMaxSliderPanel[ii].setLayout(new BoxLayout(
					singleMinMaxSliderPanel[ii], BoxLayout.X_AXIS));
			minMaxSlider[ii] = new JSlider(JSlider.HORIZONTAL, 0, (int)(100/maxMinSliderIncrement), 0);
			minMaxSliderListen[ii] = new AstroImageProcessorActionListeners(
					"minMax " + ii + " Slider Changed", this);
			minMaxSlider[ii].addChangeListener(minMaxSliderListen[ii]);

			// Turn on labels at major tick marks.
			minMaxSlider[ii].setMajorTickSpacing((int)(100/maxMinSliderIncrement));
			minMaxSlider[ii].setMinorTickSpacing((int)(25/maxMinSliderIncrement));
			minMaxSlider[ii].setPaintTicks(true);
			//minMaxSlider[ii].setPaintLabels(true);
			minMaxSlider[ii].setSnapToTicks(false);
			minMaxLevelLabel[ii] = new JLabel("0.0");

			singleMinMaxSliderPanel[ii].add(minMaxTitle[ii]);
			singleMinMaxSliderPanel[ii].add(minMaxSlider[ii]);
			singleMinMaxSliderPanel[ii].add(minMaxLevelLabel[ii]);
			minMaxSliderPanel.add(singleMinMaxSliderPanel[ii]);

		}
		minMaxSlider[0].setValue(0);
		minMaxSlider[1].setValue((int)(100/maxMinSliderIncrement));
		minMaxLevelLabel[1].setText("100.0");
		minMaxPanel = new JPanel();
		minMaxPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Scale"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		minMaxPanel.setLayout(new GridLayout(1, 1));
		minMaxPanel.add(minMaxSliderPanel);
	}

	private JMenuBar addMenu() {
		JMenuBar menuBar = new JMenuBar();

		String menuText[] = { "File", "Clean", "Action", "RGB Plot", "Help" };
		String itemText[][] = {
				{ "Open", "View Header", "Save as FITS", "Save as PNG", "Save as JPG", "Exit" },
				{ "Remove Hot Spots", "Remove Ruler(s)" },
				{ "Add", "Subtract", "Divide", "Normalize", "Align" },
				{ "Create RGB Plot", "RGB Plot Wizard" }, // add menu item for
															// rgb plot wizard
				{ "Help", "YouTube Video", "About" } };

		for (int i = 0; i < menuText.length; i++) {
			JMenu menu = new JMenu(menuText[i]);
			menuBar.add(menu);

			for (int j = 0; j < itemText[i].length; j++) {
				JMenuItem item = new JMenuItem(itemText[i][j]);
				menu.add(item);
				item.addActionListener(new AstroImageProcessorActionListeners(
						menuText[i] + "." + itemText[i][j], this));
				if (itemText[i][j].equals("Open")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Save as FITS")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Exit")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Add")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Subtract")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Divide")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Remove Hot Spots")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Remove Ruler(s)")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Normalize")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
							ActionEvent.CTRL_MASK));

				}
				if (itemText[i][j].equals("Align")) {
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
							ActionEvent.CTRL_MASK));

				}
			}
		}

		return menuBar;
	}

	public void openFitsFile() {
		File fitsFile = chooseFile(lastDirectoryPath, "Open Fits File", "",
				false, fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip = new SingleSimpleImageProcess(fitsFile.getPath());
			ssipFitsFileName = fitsFile.getName();
			//pixieImageDisplay = ssip.createRGBPixieImage();
			pixieImageDisplay[0] = ssip.getPixieImage();
			pixieImageDisplay[1] = ssip.getPixieImage();
			pixieImageDisplay[2] = ssip.getPixieImage();
			mouseClick = new Dimension(pixieImageDisplay[0].getColCount() / 2,
					pixieImageDisplay[0].getRowCount() / 2);
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}

		displayFitsFile();
		statusBar.setText("Opened " + ssipFitsFileName);
	}

	String removeExtension(String fileName) {
		String stripName = fileName.substring(0, (fileName.lastIndexOf(".") == -1 ? fileName.length() : fileName.lastIndexOf(".")));
		return stripName;
	}

	FileNameExtensionFilter makeFileNameExtensionFilter(String[] extensions) {
		int numExtensions = 0;
		for (int ii = 0; ii < extensions.length; ++ii)
			if (extensions[ii] != null)
				numExtensions = numExtensions + 1;
		String[] nne = new String[numExtensions];
		int iext = 0;
		String extensionDesc = "";
		for (int ii = 0; ii < extensions.length; ++ii) {
			if (extensions[ii] != null) {
				nne[iext] = extensions[ii];
				if (iext == 0)
					extensionDesc = extensionDesc + "*." + extensions[ii];
				if (iext > 0)
					extensionDesc = extensionDesc + ", *." + extensions[ii];
				iext = iext + 1;
			}
		}
		if (numExtensions == 1)
			return new FileNameExtensionFilter(extensionDesc, nne[0]);
		if (numExtensions == 2)
			return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1]);
		if (numExtensions == 3)
			return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1],
					nne[2]);
		if (numExtensions >= 4)
			return new FileNameExtensionFilter(extensionDesc, nne[0], nne[1],
					nne[2], nne[3]);
		return null;
	}

	public File chooseFile(String directoryPath, String dialogTitle,
			String selectedFileName, boolean saveDialog, String[] extensions) {
		File file = null;
		JFileChooser fc = null;
		if (directoryPath != null) {
			fc = new JFileChooser(directoryPath);
		} else {
			fc = new JFileChooser();
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setSelectedFile(new File(selectedFileName));
		// disable All Files extension
		if (!saveDialog)
			fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(makeFileNameExtensionFilter(extensions));
		fc.setDialogTitle(dialogTitle);
		int returnVal = 0;
		if (saveDialog) {
			returnVal = fc.showSaveDialog(this);
		} else {
			returnVal = fc.showOpenDialog(this);
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
		return file;
	}

	void saveFitsFile() {
		if (ssip == null) {
			messageDialog("Nothing to save");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath, "Save Fits File",
				ssipFitsFileName, true, fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip.writeOutFitsFile(fitsFile.getPath());
		} catch (PixieImageException e) {
			messageDialog("Can't Save File\nSorry!");
			return;
		}
	}

	void viewFitsSHeader() {
		if (ssip == null) {
			messageDialog("Nothing to view!");
			return;
		}		
		Header header = ssip.getPixieImage().getHeader();
		Iterator it = header.iterator();
		while (it.hasNext()) 
		{
			HeaderCard headerCard = (HeaderCard) it.next();
			statusMsg(headerCard.toString());
		}
	}
	
	void saveImageFile(String suggestedName, String extension) {
		String[] imageFileExtension = { extension };
		File imageFile = chooseFile(lastDirectoryPath, "Save ." + extension + " file",
				suggestedName, true, imageFileExtension);
		if (imageFile == null)
			return;
		lastDirectoryPath = imageFile.getPath();
		PixieImageRGBPlotterNoSwing plotter = null;
		try {
			plotter = SingleSimpleImageProcess.threeColor(pixieImageDisplay[0],
					pixieImageDisplay[1], pixieImageDisplay[2], scaleType,
					(double) minMaxSlider[0].getValue() * maxMinSliderIncrement,
					(double) minMaxSlider[1].getValue() * maxMinSliderIncrement);
		} catch (PixieImageException e) {
			messageDialog("Error creating image!");
		}
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		//plotter.toPNGFile(pngFile);
		try {
			BufferedImage bi = plotter.makeBufferedImage();
			//if inverted, translate buffered image before saving!
			if(invertY) {
			     AffineTransform tx=AffineTransform.getScaleInstance(1.0,-1.0);  //scaling
			     tx.translate(0,-bi.getHeight());  //translating
			     AffineTransformOp tr=new AffineTransformOp(tx,null);  //transforming
			     bi=tr.filter(bi, null);  //filtering
			}
			if(extension.equals("jpg")) {	//get best quality
				JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
				jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegParams.setCompressionQuality(1f);
				final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				// specifies where the jpg image has to be written
				writer.setOutput(new FileImageOutputStream(imageFile));
				// writes the file with given compression level 
				// from your JPEGImageWriteParam instance
				writer.write(null, new IIOImage(bi, null, null), jpegParams);
			} else	
				ImageIO.write(bi, extension, imageFile);
		} catch (IOException e) {
			messageDialog("Error. Could not create " + imageFile.getPath() + ".");		    
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void saveFitsToPng() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		String suggestedName = removeExtension(ssipFitsFileName) + ".png";
		saveImageFile(suggestedName, "png");
	}
	void saveFitsToJpg() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		String suggestedName = removeExtension(ssipFitsFileName) + ".jpg";
		saveImageFile(suggestedName, "jpg");
	}

	void displayStartUpScreen(ImageIcon imageIcon) {
		/*
		 * startupScreenDisplayed = true; int iheightH = this.getHeight() - 150;
		 * int iwidthH = iheightH * imageIcon.getIconWidth(); iwidthH = iwidthH
		 * / imageIcon.getIconHeight();
		 * 
		 * int iwidthW = this.getWidth() - settingsPanel.getWidth() - 50; int
		 * iheightW = iwidthW * imageIcon.getIconHeight(); iheightW = iheightW /
		 * imageIcon.getIconWidth();
		 * 
		 * int iwidth = iwidthH; int iheight = iheightH; if((iwidthW < iwidthH)
		 * || (iheightW < iheightH)) { iwidth = iwidthW; iheight = iheightW; }
		 * 
		 * BufferedImage bi = new
		 * BufferedImage(iwidth,iheight,BufferedImage.TYPE_INT_ARGB); Graphics2D
		 * graphics2D = bi.createGraphics();
		 * graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		 * RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		 * graphics2D.drawImage(imageIcon.getImage(), 0, 0, iwidth, iheight,
		 * null); imageIconLabel.setIcon(new ImageIcon(bi));
		 */
	}

	void displayFitsFile() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		if ((ssip == null) && (ssipRgbMod == null)) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return;
		}
		startupScreenDisplayed = false;
		PixieImageRGBPlotterNoSwing plotter = null;
		try {
			plotter = SingleSimpleImageProcess.threeColor(pixieImageDisplay[0],
					pixieImageDisplay[1], pixieImageDisplay[2], scaleType,
					(double) minMaxSlider[0].getValue() * maxMinSliderIncrement,
					(double) minMaxSlider[1].getValue() * maxMinSliderIncrement);
			//System.out.println(minMaxSlider[0].getValue() * maxMinSliderIncrement);
			//System.out.println(minMaxSlider[1].getValue() * maxMinSliderIncrement);			
		} catch (PixieImageException e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			e.printStackTrace();
		}
		plotter.setColorSpectrum(colorSpectrum);
		plotter.setInvertImage(invertImage);
		BufferedImage biOrig = plotter.makeBufferedImage();
		
		//call zoomed image early, get preferred width, and use that to size fits image
		makeZoomedImage(10.0, settingsPanelMinimumSize.width, plotter);
		
		int iheightH = this.getHeight() - 150;
		int iwidthH = iheightH * biOrig.getWidth();
		iwidthH = iwidthH / biOrig.getHeight();

		//use preferred width to size fits image
		int iwidthW = this.getWidth() - (int) settingsPanel.getPreferredSize().getWidth() - 50;
		int iheightW = iwidthW * biOrig.getHeight();
		iheightW = iheightW / biOrig.getWidth();

		imageWidth = iwidthH;
		imageHeight = iheightH;
		if ((iwidthW < iwidthH) || (iheightW < iheightH)) {
			imageWidth = iwidthW;
			imageHeight = iheightW;
		}
		int rows = pixieImageDisplay[0].getRowCount();
		int cols = pixieImageDisplay[0].getColCount();
		imageScale[0] = ((double) cols) / ((double) imageWidth);
		imageScale[1] = -((double) rows) / ((double) imageHeight);
		imageOffset[0] = 0;
		imageOffset[1] = -(double) imageHeight;
		
		//statusMsg("ImageScale0="+imageScale[0]);
		//statusMsg("ImageScale1="+imageScale[1]);

		fitsImage = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = fitsImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked
		graphics2D.drawImage(plotter.makeBufferedImage(), 0,
				invertY ? imageHeight : 0, imageWidth, invertY ? -imageHeight
						: imageHeight, null);
		imageIconLabel.setIcon(new ImageIcon(fitsImage));

		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void subtractFitsFile() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath,
				"Open Fits File To Subtract", subtractFitsFileName, false,
				fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip.subtractImage(fitsFile.getPath());
			ssipFitsFileName = removeExtension(ssipFitsFileName) + "_sub.fits";
			subtractFitsFileName = fitsFile.getName();
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);

	}

	void addFitsFile() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath, "Open Fits File To Add",
				"", false, fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip.addImage(fitsFile.getPath());
			ssipFitsFileName = removeExtension(ssipFitsFileName) + "_add.fits";
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void divideFitsFile() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath,
				"Open Fits File To Divide", "", false, fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip.divideImage(fitsFile.getPath());
			ssipFitsFileName = removeExtension(ssipFitsFileName) + "_div.fits";
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void removeMean() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		ssip.subtractMean();
		ssipFitsFileName = removeExtension(ssipFitsFileName) + "_meanR.fits";
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void removeHotspots() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		ssip.removeHotSpots();
		ssipFitsFileName = removeExtension(ssipFitsFileName) + "_hspotR.fits";
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void removeRulers() {
		if (ssip == null) {
			//messageDialog("Open a file first!");
			return;
		}

		//redraw image
		BufferedImage rulerImage = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = rulerImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked
		graphics2D.drawImage(fitsImage, 0, 0, imageWidth, imageHeight, null);
		imageIconLabel.setIcon(new ImageIcon(rulerImage));				
		
		//redraw zoom
		BufferedImage zoomedRulerImage = new BufferedImage(zoomedImageWidth, zoomedImageHeight,
				BufferedImage.TYPE_INT_ARGB);
		graphics2D = zoomedRulerImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked	
		graphics2D.drawImage(fitsZoomedImage, 0, 0, zoomedImageWidth, zoomedImageHeight, null);
		zoomedImageIconLabel.setIcon(new ImageIcon(zoomedRulerImage));				
	}

	void normalize() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath,
				"Open Fits File To Normalize To", normalizeFitsFileName, false,
				fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssip.normalize(fitsFile.getPath());
			ssipFitsFileName = removeExtension(ssipFitsFileName) + "_norm.fits";
			normalizeFitsFileName = fitsFile.getName();
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);

	}

	void align() {
		if (ssip == null) {
			messageDialog("Open a file first!");
			return;
		}
		File fitsFile = chooseFile(lastDirectoryPath,
				"Open Fits File To Align To", alignFitsFileName, false,
				fitsExtensions);
		if (fitsFile == null)
			return;
		lastDirectoryPath = fitsFile.getPath();
		try {
			ssipRef = new SingleSimpleImageProcess(fitsFile.getPath());
			pixieImageDisplay[0] = ssip.getPixieImage();
			pixieImageDisplay[1] = ssipRef.getPixieImage();
			pixieImageDisplay[2] = ssipRef.getPixieImage();
			ssipFitsFileName = removeExtension(ssipFitsFileName)
					+ "_align.fits";
			alignFitsFileName = fitsFile.getName();
		} catch (PixieImageException e) {
			messageDialog("Weird fits format.\nCan't open File\nSorry!");
			return;
		}
		ialignLeftSum = 0;
		ialignUpSum = 0;
		thetaSumSeconds = 0;
		try {
			ssipAlignStart = new SingleSimpleImageProcess(ssip);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		alignPanel.setVisible(true);
		disableMenu = true;
		displayFitsFile();
		statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void align(String fitsRefFilePath) {
		if (ssip == null) {
			return;
		}
		try {
			ssipRef = new SingleSimpleImageProcess(fitsRefFilePath);
			pixieImageDisplay[0] = ssip.getPixieImage();
			pixieImageDisplay[1] = ssipRef.getPixieImage();
			pixieImageDisplay[2] = ssipRef.getPixieImage();
			// ssipFitsFileName = removeExtension(ssipFitsFileName) +
			// "_align.fits";
			// alignFitsFileName = fitsFile.getName();
		} catch (PixieImageException e) {
			messageDialog("Error. Unknown FITS format.");
			return;
		}
		ialignLeftSum = 0;
		ialignUpSum = 0;
		thetaSumSeconds = 0;
		try {
			ssipAlignStart = new SingleSimpleImageProcess(ssip);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		alignPanel.setVisible(true);
		disableMenu = true;
		displayFitsFile();
		// statusBar.setText("Fits File Suggested Name " + ssipFitsFileName);
	}

	void plotAlignedImages(int ileft, int iup, double theta_seconds) {
		ialignLeftSum = ialignLeftSum + ileft;
		//adjust if inverted!
		ialignUpSum = ialignUpSum + (invertY ? -iup : iup);
		thetaSumSeconds = thetaSumSeconds +  (invertY ? -theta_seconds : theta_seconds);
		try {
			ssip = new SingleSimpleImageProcess(ssipAlignStart);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		//if(thetaSumSeconds != 0)
		ssip.rotateImage(thetaSumSeconds/3600.0);
		//if(ialignLeftSum + ialignUpSum != 0)
		ssip.shiftImage(ialignLeftSum, ialignUpSum);
		pixieImageDisplay[0] = ssip.getPixieImage();
		pixieImageDisplay[1] = ssipRef.getPixieImage();
		pixieImageDisplay[2] = ssipRef.getPixieImage();
		displayFitsFile();
	}

	void saveAlignedImages(boolean saveImages) {
		// doing batch align?
		if (doingBatchAlign) {
			if (!saveImages) {
				doingBatchAlign = false;
				statusMsg("RGB plot wizard aborted!");
				alignPanel.setVisible(false);
				disableMenu = false;
				return;
			} else {
				try {
					ssip.writeOutFitsFile(fitsAlignFilePaths[fitsAlignCurrentIdx]);
				} catch (PixieImageException e) {
					messageDialog("Error saving normalized FITS file to "
							+ fitsAlignFilePaths[fitsAlignCurrentIdx]);
					return;
				}
				fitsAlignCurrentIdx++;
			}
			alignPanel.setVisible(false);
			disableMenu = false;
			pixieImageDisplay[0] = ssip.getPixieImage();
			pixieImageDisplay[1] = ssip.getPixieImage();
			pixieImageDisplay[2] = ssip.getPixieImage();
			displayFitsFile();
			rgbPlotWizard(); // generalize callback?
			// NOT doing batch align!
		} else {
			if (saveImages)
				saveFitsFile();
			alignPanel.setVisible(false);
			disableMenu = false;
			pixieImageDisplay[0] = ssip.getPixieImage();
			pixieImageDisplay[1] = ssip.getPixieImage();
			pixieImageDisplay[2] = ssip.getPixieImage();
			displayFitsFile();
		}
	}

	void rgbPlot() {
		colorSpectrum = false;
		invertImage = false;
		colorSpectrumButton.setSelected(false);
		invertImageButton.setSelected(false);
		String[] directions = { "Open Fits Red File", "Open Fits Green File",
				"Open Fits Blue File" };
		for (int ii = 0; ii < 3; ++ii) {
			File fitsFile = chooseFile(lastDirectoryPath, directions[ii], "",
					false, fitsExtensions);
			if (fitsFile == null)
				return;
			lastDirectoryPath = fitsFile.getPath();
			try {
				ssipRgbOrig[ii] = new SingleSimpleImageProcess(
						fitsFile.getPath());
				ssip = ssipRgbOrig[ii];
			} catch (PixieImageException e1) {
				messageDialog("Weird fits format.\nCan't open File\nSorry!");
				return;
			}
			pixieImageDisplay[0] = ssipRgbOrig[ii].getPixieImage();
			pixieImageDisplay[1] = ssipRgbOrig[ii].getPixieImage();
			pixieImageDisplay[2] = ssipRgbOrig[ii].getPixieImage();
			mouseClick = new Dimension(pixieImageDisplay[0].getColCount() / 2,
					pixieImageDisplay[0].getRowCount() / 2);
			displayFitsFile();
		}
		for (int ii = 0; ii < 3; ++ii) {
			pixieImageDisplay[ii] = ssipRgbOrig[ii].getPixieImage();
			rgbSlider[ii].setValue(0);
			rgbLevelLabel[ii].setText("1.0");
		}
		rgbPanel.setVisible(true);
		disableMenu = true;
		displayFitsFile();
	}

	void rgbPlotWizard() {

		colorSpectrum = false;
		invertImage = false;
		colorSpectrumButton.setSelected(false);
		invertImageButton.setSelected(false);

		// doing batch align steps? start where we left off
		if (doingBatchAlign) {
			if (fitsAlignCurrentIdx < 3) {
				// open the color file
				try {
					ssip = new SingleSimpleImageProcess(
							fitsAlignFilePaths[fitsAlignCurrentIdx]);
					fitsAlignFilePaths[fitsAlignCurrentIdx] = removeExtension(fitsAlignFilePaths[fitsAlignCurrentIdx])
							+ "_align.fits.tmp";
					pixieImageDisplay = ssip.createRGBPixieImage();
					mouseClick = new Dimension(
							pixieImageDisplay[0].getColCount() / 2,
							pixieImageDisplay[0].getRowCount() / 2);
				} catch (PixieImageException e1) {
					doingBatchAlign = false;
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error. Unknown FITS format.");
					return;
				}
				align(fitsAlignRefPath);
				statusMsg("Align image " + (fitsAlignCurrentIdx+1) + " of 3, then click \"Save\".");
				return;
			}
			// alignment complete; last task, assign the colors
			for (int ii = 0; ii < 3; ++ii) {
				try {
					ssipRgbOrig[ii] = new SingleSimpleImageProcess(
							fitsAlignFilePaths[ii]);
				} catch (PixieImageException e1) {
					doingBatchAlign = false;
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error. Unknown FITS format.");
					return;
				}
			}
			for (int ii = 0; ii < 3; ++ii) {
				pixieImageDisplay[ii] = ssipRgbOrig[ii].getPixieImage();
				rgbSlider[ii].setValue(0);
				rgbLevelLabel[ii].setText("1.0");
			}
			doingBatchAlign = false;
			rgbPanel.setVisible(true);
			disableMenu = true;
			//guess a filename for saving
			ssipFitsFileName = fitsAlignFilePaths[0].substring(0, (fitsAlignFilePaths[0].indexOf("_") == -1 ? fitsAlignFilePaths[0].length() : fitsAlignFilePaths[0].indexOf("_")));
			displayFitsFile();
			statusMsg("Adjust RGB values, then click \"Save\".");
			// start from the beginning
		} else {

			String[] directions = { "red", "green", "blue", "reference" };
			File fitsFile;
			String[] fitsFilePaths = new String[4]; // store locations of all
													// three color fits files
													// and reference file
			String[] fitsNormalizedFilePaths = new String[4]; // store locations
																// of all three
																// NORMALIZED
																// color fits
																// files

			clearStatusMsg();
			statusMsg("Starting RGB Plot wizard...");
			// identify each of the 3 "color" images and 1 reference image
			for (int ii = 0; ii < 4; ++ii) {
				fitsFile = chooseFile(lastDirectoryPath, "Please open "
						+ directions[ii] + " .fits file.", "", false,
						fitsExtensions);
				if (fitsFile == null) {
					statusMsg("RGB plot wizard aborted!");
					return;
				}
				statusMsg("Opened " + fitsFile.getPath());
				lastDirectoryPath = fitsFilePaths[ii] = fitsFile.getPath();
			}
			statusMsg("Cleaning and normalizing images...");

			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			// clean and normalize all colors to the reference
			for (int ii = 0; ii < 3; ++ii) {
				// open the color file
				// statusMsg("Opening " + fitsFilePaths[ii] + "...");
				try {
					ssip = new SingleSimpleImageProcess(fitsFilePaths[ii]);
				} catch (PixieImageException e1) {
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error. Unknown FITS format.");
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				// clean hot spots
				try {
					ssip.removeHotSpots();
				} catch (Exception e) {
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error. Unknown FITS format.");
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				//statusMsg("Cleaned " + fitsFilePaths[ii]);
				// normalize it to the reference file
				try {
					ssip.normalize(fitsFilePaths[3]);
					fitsNormalizedFilePaths[ii] = removeExtension(fitsFilePaths[ii])
							+ "_hspotR_norm.fits.tmp";
				} catch (PixieImageException e) {
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error. Unknown FITS format.");
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				//statusMsg("Normalized " + fitsFilePaths[ii]);
				// save the normalized file
				//statusMsg("Saving " + fitsNormalizedFilePaths[ii] + "...");
				try {
					ssip.writeOutFitsFile(fitsNormalizedFilePaths[ii]);
				} catch (PixieImageException e) {
					statusMsg("RGB plot wizard aborted!");
					messageDialog("Error saving normalized FITS file to "
							+ fitsNormalizedFilePaths[ii]);
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				//statusMsg("Saved " + fitsFilePaths[ii]);
			}

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			// start batch align
			doingBatchAlign = true;
			fitsAlignCurrentIdx = 0;
			fitsAlignFilePaths = new String[3];
			for (int ii = 0; ii < 3; ii++) {
				fitsAlignFilePaths[ii] = fitsNormalizedFilePaths[ii];
			}
			fitsAlignRefPath = fitsFilePaths[3];
			// open the color file
			try {
				ssip = new SingleSimpleImageProcess(
						fitsAlignFilePaths[fitsAlignCurrentIdx]);
				fitsAlignFilePaths[fitsAlignCurrentIdx] = removeExtension(fitsAlignFilePaths[fitsAlignCurrentIdx])
						+ "_align.fits.tmp";
				pixieImageDisplay = ssip.createRGBPixieImage();
				mouseClick = new Dimension(
						pixieImageDisplay[0].getColCount() / 2,
						pixieImageDisplay[0].getRowCount() / 2);
			} catch (PixieImageException e1) {
				messageDialog("Error. Unknown FITS format.");
				doingBatchAlign = false;
				statusMsg("RGB plot wizard aborted!");
				return;
			}
			align(fitsAlignRefPath);
			statusMsg("Align image " + (fitsAlignCurrentIdx+1) + " of 3, then click \"Save\".");
			// disableMenu = true;
		}
	}

	protected void rgbSliderChanged(int islider) {
		if (rgbSlider[islider].getValueIsAdjusting())
			return;
		for (int ii = 0; ii < 3; ++ii) {
			double sliderValue = (double) rgbSlider[ii].getValue();
			sliderValue = Math.pow(10.0, sliderValue / 100.0);
			rgbLevelLabel[ii].setText(tp.format(sliderValue));
			try {
				ssipRgbMod[ii] = new SingleSimpleImageProcess(ssipRgbOrig[ii]);
				ssipRgbMod[ii].gainAdjust(sliderValue);
			} catch (PixieImageException e) {
				e.printStackTrace();
				return;
			}
			pixieImageDisplay[ii] = ssipRgbMod[ii].getPixieImage();
		}
		displayFitsFile();
	}

	protected void minMaxSliderChanged(int islider) {
		if (initializingPanels)
			return;
		if (minMaxSlider[islider].getValueIsAdjusting())
			return;
		int[] sliderValue = new int[2];
		for (int ii = 0; ii < 2; ++ii) {
			sliderValue[ii] = minMaxSlider[ii].getValue();
		}
		if (islider == 0) {
			if (sliderValue[0] > sliderValue[1])
				sliderValue[0] = sliderValue[1];
			minMaxSlider[0].setValue(sliderValue[0]);
			minMaxLevelLabel[0].setText(tp.format(sliderValue[0] * maxMinSliderIncrement));
		}
		if (islider == 1) {
			if (sliderValue[0] > sliderValue[1])
				sliderValue[1] = sliderValue[0];
			minMaxSlider[1].setValue(sliderValue[1]);
			minMaxLevelLabel[1].setText(tp.format(sliderValue[1] * maxMinSliderIncrement));
		}
		displayFitsFile();

	}

	void saveRgb(boolean saveImages) {
		if (saveImages)
			saveFitsToJpg();
		rgbPanel.setVisible(false);
		disableMenu = false;
		//ssip = null;
	}

	void windowResized() {
		if (startupScreenDisplayed) {
			displayStartUpScreen(startupDisplay);
			return;
		}
		displayFitsFile();
	}

	protected void mouseRightClickedOnBWImage(MouseEvent e) {
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		int ix = e.getX();
		int iy = e.getY();

		double x = (((double) ix) + imageOffset[0]) * imageScale[0];
		double y = (((double) iy) + imageOffset[1]) * imageScale[1];

		ix = (int) x;
		iy = (int) y;
		if (ix < 0 || iy < 0)
			return;
		
		double raDec[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy : iy, ix);

		WorldCoords wc = new WorldCoords(raDec[0], raDec[1]);
		
		statusMsg("RA (H:M:S) = "+wc.getRA().toString()+", Dec (D:M:S) = "+wc.getDec().toString()/*"Ra="+raDec[0]+", Dec="+raDec[1]*/);
	}
	
	protected void mouseRightClickedOnZoomedImage(MouseEvent e) {
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		int ix = e.getX();
		int iy = e.getY();

		double x = ((double) ix) * zoomedImageScale[0];
		double y = ((double) iy) * zoomedImageScale[1];
		//statusMsg("x="+x+",y="+y);
		
		//statusMsg("x0="+(zoomedImageCenterCol-zoomedImageOffset[0])+",y0="+(zoomedImageCenterRow-zoomedImageOffset[1]));			

		ix = (int) (x + zoomedImageCenterCol - zoomedImageOffset[0]);
		iy = (int) (-y + zoomedImageCenterRow + zoomedImageOffset[1]);
		if (ix < 0 || iy < 0)
			return;
		//statusMsg("ix="+ix+",iy="+iy);		
		
		double raDec[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy : iy, ix);

		WorldCoords wc = new WorldCoords(raDec[0], raDec[1]);
		
		statusMsg("RA (H:M:S) = "+wc.getRA().toString()+", Dec (H:M:S) = "+wc.getDec().toString()/*"Ra="+raDec[0]+", Dec="+raDec[1]*/);
	}	
	
	protected void mouseClickedOnBWImage(MouseEvent e) {
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		int ix = e.getX();
		int iy = e.getY();

		//statusMsg("ix="+ix+",iy="+iy);			
		
		double x = (((double) ix) + imageOffset[0]) * imageScale[0];
		double y = (((double) iy) + imageOffset[1]) * imageScale[1];

		////update center of zoomed image
		//zoomedImageCenterCol = x;
		//zoomedImageCenterRow = y;	
		//statusMsg("zoomedImageCenterCol="+x);		
		//statusMsg("zoomedImageCenterRow="+y);	
		
		ix = (int) x;
		iy = (int) y;
		
		//statusMsg("x="+x+",y="+y);
		
		if (ix < 0 || iy < 0)
			return;
		// these are reversed!
		// int ired = (int) pixieImageDisplay[0].getPix()[ix][iy];
		// int igreen = (int) pixieImageDisplay[1].getPix()[ix][iy];
		// int iblue = (int) pixieImageDisplay[2].getPix()[ix][iy];
		int ired = (int) pixieImageDisplay[0].getPix()[invertY ? pixieImageDisplay[0]
				.getRowCount() - iy
				: iy][ix];
		int igreen = (int) pixieImageDisplay[1].getPix()[invertY ? pixieImageDisplay[0]
				.getRowCount() - iy
				: iy][ix];
		int iblue = (int) pixieImageDisplay[2].getPix()[invertY ? pixieImageDisplay[0]
				.getRowCount() - iy
				: iy][ix];
		//double raDec[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy : iy, ix);
		//statusMsg("Ra="+raDec[0]+", Dec="+raDec[1]);
		//correct index if image is flipped
		mouseClick.setSize(ix, invertY ? pixieImageDisplay[0].getRowCount()
				- iy : iy);
		String message = "Pixel: X = " + ix + " ("
				+ pixieImageDisplay[0].getColCount() + "), Y = "
				+ (invertY ? pixieImageDisplay[0].getRowCount() - iy : iy)
				+ " (" + pixieImageDisplay[0].getRowCount() + "); Red = "
				+ ired + ", Green = " + igreen + ", Blue = " + iblue;
		statusBar.setText(message);
		displayFitsFile();
	}

	protected void mouseDraggedOnZoomedImage(MouseEvent e) {
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		
		dragging_zoomed = true;
		
		ruler_x_end_zoomed = e.getX();
		ruler_y_end_zoomed = e.getY();
		
		BufferedImage zoomedRulerImage = new BufferedImage(zoomedImageWidth, zoomedImageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = zoomedRulerImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked	
		graphics2D.drawImage(fitsZoomedImage, 0, 0, zoomedImageWidth, zoomedImageHeight, null);
		graphics2D.setPaint(Color.RED);
		graphics2D.drawLine(ruler_x_start_zoomed, ruler_y_start_zoomed, ruler_x_end_zoomed, ruler_y_end_zoomed);
		zoomedImageIconLabel.setIcon(new ImageIcon(zoomedRulerImage));				
	}	
	
	protected void mouseDraggedOnBWImage(MouseEvent e) {
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		
		dragging = true;
		
		ruler_x_end = e.getX();
		ruler_y_end = e.getY();

		BufferedImage rulerImage = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = rulerImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked
		graphics2D.drawImage(fitsImage, 0, 0, imageWidth, imageHeight, null);
		graphics2D.setPaint(Color.RED);
		graphics2D.drawLine(ruler_x_start, ruler_y_start, ruler_x_end, ruler_y_end);
		imageIconLabel.setIcon(new ImageIcon(rulerImage));				
	}	
	
	protected void mousePressedOnBWImage(MouseEvent e) {	
		ruler_x_start = e.getX();
		ruler_y_start = e.getY();
	}

	protected void mousePressedOnZoomedImage(MouseEvent e) {
		ruler_x_start_zoomed = e.getX();
		ruler_y_start_zoomed = e.getY();
	}	
	
	protected void mouseExitedBWImage(MouseEvent e) {
		//turn off dragging	
		dragging = false;
		ruler_x_start = 0;
		ruler_y_start = 0;
	}

	protected void mouseExitedZoomedImage(MouseEvent e) {
		//turn off dragging	
		dragging_zoomed = false;
		ruler_x_start_zoomed = 0;
		ruler_y_start_zoomed = 0;
	}	
	
	protected void mouseReleasedOnBWImage(MouseEvent e) {		
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		
		if(dragging) {
			//calc end RA, DEC
			int ix_start = (int) ((((double) ruler_x_start) + imageOffset[0]) * imageScale[0]);
			int iy_start = (int) ((((double) ruler_y_start) + imageOffset[1]) * imageScale[1]);			
			if (ix_start < 0 || iy_start < 0)
				return;
			double raDec_start[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy_start : iy_start, ix_start);			
			WorldCoords wc_start = new WorldCoords(raDec_start[0], raDec_start[1]);
			statusMsg("Start : RA (H:M:S) = "+wc_start.getRA().toString()+", Dec (H:M:S) = "+wc_start.getDec().toString());			
		
			ruler_x_end = e.getX();
			ruler_y_end= e.getY();	
			
			//calc end RA, DEC
			int ix_end = (int) ((((double) ruler_x_end) + imageOffset[0]) * imageScale[0]);
			int iy_end = (int) ((((double) ruler_y_end) + imageOffset[1]) * imageScale[1]);			
			if (ix_end < 0 || iy_end < 0)
				return;
			double raDec_end[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy_end : iy_end, ix_end);			
			WorldCoords wc_end = new WorldCoords(raDec_end[0], raDec_end[1]);
			statusMsg("End : RA (H:M:S) = "+wc_end.getRA().toString()+", Dec (H:M:S) = "+wc_end.getDec().toString());		
			
			statusMsg("Distance (arcmin) = " + wc_end.dist(wc_start));
			
			double PA = Math.toDegrees(Math.atan2(wc_end.getDecDeg()-wc_start.getDecDeg(), wc_end.getRaDeg()-wc_start.getRaDeg()));
			statusMsg("Position Angle (deg) = " + PA);
			double PlA = Math.toDegrees(Math.atan2(iy_end-iy_start, ix_end-ix_start));			
			statusMsg("Plate Angle (deg) = " + PlA);
			
			dragging = false;
		}	
	}	

	protected void mouseReleasedOnZoomedImage(MouseEvent e) {		
		if ((ssip == null) && (ssipRgbMod == null))
			return;
		
		if(dragging_zoomed) {		
			//calc end RA, DEC
			double x = ((double) ruler_x_start_zoomed) * zoomedImageScale[0];
			double y = ((double) ruler_y_start_zoomed) * zoomedImageScale[1];
			int ix_start = (int) (x + zoomedImageCenterCol - zoomedImageOffset[0]);
			int iy_start = (int) (-y + zoomedImageCenterRow + zoomedImageOffset[1]);
			if (ix_start < 0 || iy_start < 0)
				return;	
			double raDec_start[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy_start : iy_start, ix_start);			
			WorldCoords wc_start = new WorldCoords(raDec_start[0], raDec_start[1]);
			statusMsg("Start : RA (H:M:S) = "+wc_start.getRA().toString()+", Dec (H:M:S) = "+wc_start.getDec().toString());			
		
			ruler_x_end_zoomed = e.getX();
			ruler_y_end_zoomed = e.getY();	
			
			//calc end RA, DEC
			x = ((double) ruler_x_end_zoomed) * zoomedImageScale[0];
			y = ((double) ruler_y_end_zoomed) * zoomedImageScale[1];
			int ix_end = (int) (x + zoomedImageCenterCol - zoomedImageOffset[0]);
			int iy_end = (int) (-y + zoomedImageCenterRow + zoomedImageOffset[1]);
			if (ix_end < 0 || iy_end < 0)
				return;	
			double raDec_end[] = pixieImageDisplay[0].rowColToRaDec(invertY ? pixieImageDisplay[0].getRowCount() - iy_end : iy_end, ix_end);			
			WorldCoords wc_end = new WorldCoords(raDec_end[0], raDec_end[1]);
			statusMsg("End : RA (H:M:S) = "+wc_end.getRA().toString()+", Dec (H:M:S) = "+wc_end.getDec().toString());		

			statusMsg("Distance (arcmin) = " + wc_end.dist(wc_start));			

			double PA = Math.toDegrees(Math.atan2(wc_end.getDecDeg()-wc_start.getDecDeg(), wc_end.getRaDeg()-wc_start.getRaDeg()));
			statusMsg("Position Angle (deg) = " + PA);
			double PlA = Math.toDegrees(Math.atan2(iy_end-iy_start, ix_end-ix_start));			
			statusMsg("Plate Angle (deg) = " + PlA);			
			
			dragging_zoomed = false;
		}	
	}	
	
	protected void makeZoomedImage(double zoom, int displayWidth,
			PixieImageRGBPlotterNoSwing origPlotter) {
		//zoomedImageCenterRow = mouseClick.height;
		//zoomedImageCenterCol= mouseClick.width;	
		
		//statusMsg("mousex="+mouseClick.width+",mousey="+mouseClick.height);
		
		zoomPixieImageDisplay = SingleSimpleImageProcess
				.makeZoomedPixieImage(pixieImageDisplay, zoom, mouseClick.height, mouseClick.width);
		
		zoomedImageCenterCol = zoomPixieImageDisplay[0].getColCenter();
		zoomedImageCenterRow = invertY ? pixieImageDisplay[0].getRowCount() - zoomPixieImageDisplay[0].getRowCenter() : zoomPixieImageDisplay[0].getRowCenter();
		//statusMsg("zoomedImageCenterColmz="+zoomedImageCenterCol);		
		//statusMsg("zoomedImageCenterRow="+zoomedImageCenterRow);			
		
		//zoomedImageCenterCol = (int) (((double) mouseClick.width) + imageOffset[0]) * imageScale[0];
		//zoomedImageCenterRow = (int) (((double) mouseClick.height) + imageOffset[1]) * imageScale[1];
		
		PixieImageRGBPlotterNoSwing plotter = null;
		try {
			plotter = SingleSimpleImageProcess.threeColor(
					zoomPixieImageDisplay[0], zoomPixieImageDisplay[1],
					zoomPixieImageDisplay[2], scaleType,
					(double) minMaxSlider[0].getValue() * maxMinSliderIncrement,
					(double) minMaxSlider[1].getValue() * maxMinSliderIncrement);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		plotter.setAutoScale(false);
		plotter.setPixelValueLimits(origPlotter.getMinPixelValue(),
				origPlotter.getMaxPixelValue());
		plotter.setColorSpectrum(colorSpectrum);
		plotter.setInvertImage(invertImage);
		BufferedImage biOrig = plotter.makeBufferedImage();
		zoomedImageWidth = displayWidth;
		zoomedImageHeight = zoomedImageWidth * biOrig.getHeight();
		zoomedImageHeight = zoomedImageHeight / biOrig.getWidth();

		//statusMsg("displayHeight="+displayHeight+",displayWidth="+displayWidth);
		
		int rows = zoomPixieImageDisplay[0].getRowCount();
		int cols = zoomPixieImageDisplay[0].getColCount();
		zoomedImageScale[0] = ((double) cols) / ((double) zoomedImageWidth);
		zoomedImageScale[1] = ((double) rows) / ((double) zoomedImageHeight);
		zoomedImageOffset[0] = ((double) cols) / 2.0;
		zoomedImageOffset[1] = ((double) rows) / 2.0;
		
		//statusMsg("zoomedImageScale0="+zoomedImageScale[0]);
		//statusMsg("zoomedImageScale1="+zoomedImageScale[1]);
		//statusMsg("zoomedImageOffset0="+zoomedImageOffset[0]);
		//statusMsg("zoomedImageOffset1="+zoomedImageOffset[1]);		
		
		fitsZoomedImage = new BufferedImage(zoomedImageWidth, zoomedImageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = fitsZoomedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//flip if InvertY is checked
		graphics2D.drawImage(plotter.makeBufferedImage(), 0,
				invertY ? zoomedImageHeight : 0, zoomedImageWidth,
				invertY ? -zoomedImageHeight : zoomedImageHeight, null);
		zoomedImageIconLabel.setIcon(new ImageIcon(fitsZoomedImage));
	}

	void flipInvertImageButton() {
		if (invertImage) {
			invertImageButton.setSelected(false);
			invertImage = false;
		} else {
			invertImageButton.setSelected(true);
			invertImage = true;
		}
		if (!startupScreenDisplayed)
			displayFitsFile();
	}

	void flipColorSpectrumButton() {
		if (colorSpectrum) {
			colorSpectrumButton.setSelected(false);
			colorSpectrum = false;
		} else {
			colorSpectrumButton.setSelected(true);
			colorSpectrum = true;
		}
		if (!startupScreenDisplayed)
			displayFitsFile();

	}

	void flipInvertYButton() {
		if (invertY) {
			invertYButton.setSelected(false);
			invertY = false;
		} else {
			invertYButton.setSelected(true);
			invertY = true;
		}
		if (!startupScreenDisplayed)
			displayFitsFile();
	}

	protected void openHelp() {
		if (helpFrame != null) {
			if (!helpFrame.frameClosed) {
				helpFrame.setState(JFrame.NORMAL);
				return;
			}
		}
		try {
			helpFrame = new AstroImageProcessorHelpFrame(this);
		} catch (IOException e) {
			messageDialog("Can't open help file: " + e.getMessage());
		}

	}

	protected void openYouTubeVideo() {
		try {
			URL imageLinkUrl = new URL(
					"http://www.youtube.com/watch?v=dE_-yH0GfJg");
			Desktop.getDesktop().browse(imageLinkUrl.toURI());
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (URISyntaxException e) {
		}
	}

	protected void messageDialog(String string) {
		JOptionPane.showMessageDialog(this, string);
	}

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.out.println(args[0]);
		}
		new AstroImageProcessorGui("StoneEdge Astro Image Processor");
	}

}
