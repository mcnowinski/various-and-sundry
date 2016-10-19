package com.astrofizzbizz.pixie;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author mcginnis
 *
 */
public class PixieImageRGBPlotterNoSwing 
{
	private PixieImage redImage = null;
	private PixieImage greenImage = null;
	private PixieImage blueImage = null;
	private double minPixelValue = 0.0;
	private double maxPixelValue = 255.0;
	private boolean invertImage = false;
	private boolean colorSpectrum = false;
	private boolean autoScale = false;
	public int[] scaleTable = new int[256];
	private String scaleType = "linear";
	public PixieImageRGBPlotterNoSwing()
	{
		int		ii;
		for (ii = 0; ii < 256; ++ii)
		{
			scaleTable[ii] = ii;
		}
	}
	public PixieImageRGBPlotterNoSwing(ImageIcon imageIcon)
	{
		this();
		setImages(imageIcon);
	}
	/**
	 * @return BufferedImage
	 */
	public BufferedImage makeBufferedImage()
	{
		int	nrows = redImage.getRowCount();
		int ncols = redImage.getColCount();
		double[][] redPixelArray = redImage.getPix();
		double[][] greenPixelArray = greenImage.getPix();
		double[][] bluePixelArray = blueImage.getPix();
		
		//BufferedImage bi = new BufferedImage(ncols,nrows,BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi = new BufferedImage(ncols,nrows,BufferedImage.TYPE_INT_RGB);		
		int	ii;
		int	ij;
		for (ii = 0; ii < nrows; ++ii)
		{
			for (ij = 0; ij < ncols; ++ij)
			{
				bi.setRGB(ij, nrows - 1 - ii, getRGBValue(redPixelArray[ii][ij], greenPixelArray[ii][ij], bluePixelArray[ii][ij]));
			}
		}
		return bi;
	}
	/**
	 * @param outputFile
	 */
	public void toPNGFile(File outputFile)
	{	
		BufferedImage bi = makeBufferedImage();
		try 
        {
	        ImageIO.write(bi, "png", outputFile);
	    } 
        catch (IOException e)
        {
        	System.out.println("Problems writing PNG file");
        }
	}
	/**
	 * @param outputFileName
	 */
	public void toPNGFile(String outputFileName)
	{	
		File outputFile = new File(outputFileName);
		toPNGFile(outputFile);
	}
	/**
	 * @param scaleType
	 * @param minPixelValue
	 * @param maxPixelValue
	 * @param invertImage
	 * @param autoScale 
	 * @param pi
	 */
	public void setScaleTable(PixieImage pi)
	{
		double	dindex;
		int		iindex;
		int		ii;
		if (scaleType.equals("linear"))
		{
			for (ii = 0; ii < 256; ++ii)
			{
				scaleTable[ii] = ii;
			}
		}
		if (scaleType.equals("log"))
		{
			for (ii = 0; ii < 256; ++ii)
			{
				dindex = (double) ii;
				dindex = 1.0 + 47.0 * dindex / 255.0;
				dindex = 255.0 * Math.log(dindex) / Math.log(48.0);
				iindex = (int) dindex;
				if (iindex < 0) iindex = 0;
				if (iindex > 255) iindex = 255;
				scaleTable[ii] = iindex;
			}
		}
		if (scaleType.equals("asinh"))
		{
			for (ii = 0; ii < 256; ++ii)
			{
				dindex = (double) ii;
				dindex = 255.0 * asinh(47.0 * dindex / 255.0) / asinh(47.0);
				iindex = (int) dindex;
				if (iindex < 0) iindex = 0;
				if (iindex > 255) iindex = 255;
				scaleTable[ii] = iindex;
			}
		}
		if (scaleType.equals("sqrt"))
		{
			for (ii = 0; ii < 256; ++ii)
			{
				dindex = (double) ii;
				dindex = 255.0 * Math.sqrt(dindex / 255.0);
				iindex = (int) dindex;
				if (iindex < 0) iindex = 0;
				if (iindex > 255) iindex = 255;
				scaleTable[ii] = iindex;
			}
		}
		if (scaleType.equals("hist") && (pi != null))
		{
			makeHistEqScaleTable(pi);
		}
	}
	double asinh(double x)
	{
		return Math.log(x + Math.sqrt(x*x + 1.0));
	}

	/**
	 * @param steps
	 */
	public void quantizeScaleTable(int steps)
	{
		if (steps < 2) return;
		int[] xbreak = new int[steps];
		int[] ybreak = new int[steps];
		int icount = 0;
		for (int ii = 0; ii < steps; ++ii)
		{
			double yb = 255.0 * ((double) (ii + 1)) / ((double) steps);
			ybreak[ii] = (int) yb;
			boolean ibreakFound = false;
			while (!ibreakFound && (icount < 256))
			{
				if (scaleTable[icount] > ybreak[ii])
				{
					ibreakFound = true;
					xbreak[ii] = icount;
					icount = icount + 1;
				}
				else
				{
					icount = icount + 1;
				}
			}
		}
		xbreak[steps - 1] = 255;
		int ystep = 255 / (2 * steps);
		int istep = 255 / steps;
		icount = 0;
		for (int ii = 0; ii < steps; ++ii)
		{
			for ( int ij = icount; ij <= xbreak[ii]; ++ij)
			{
				scaleTable[ij] = ystep;
			}
			icount = xbreak[ii] + 1;
			ystep = ystep + istep;
		}
	}
	private int getRGBValue(double redPixValue, double greenPixValue, double bluePixValue)
	{
		Color	clr  = null;
		if (colorSpectrum)
		{
			double hue = (int) convertTo256(redPixValue);
			hue = 0.75 - 0.75 * (hue / 255.0);
			clr = Color.getHSBColor((float) hue, (float) 1.0, (float) 1.0);
		}
		else
		{
			clr =  new Color(convertTo256(redPixValue),convertTo256(greenPixValue),convertTo256(bluePixValue));
		}
		return clr.getRGB();
	}
	private int convertTo256(double pixValue)
	{
		double dindex;
		int i256;
		dindex = 255.0 * (pixValue - minPixelValue) / (maxPixelValue - minPixelValue);
		dindex = Math.floor(dindex);
		
		i256 = (int) dindex;
		if (i256 < 0) i256 = 0;
		if (i256 > 255) i256 = 255;
		i256 = scaleTable[i256];
		if (invertImage) i256 = 255 - i256;
		return i256;
	}
	private int[] binPixels(PixieImage pi, int numBins)
	{
		int[] 	bins = new int[numBins];
		double 	binSize;
		int		ii;
		int		ij;
		int	nrows = pi.getRowCount();
		int ncols = pi.getColCount();
		double[][]	pix = pi.getPix();
		double		bin;
		
		binSize = (maxPixelValue - minPixelValue) / ((double) numBins);
		for (ii = 0; ii < numBins; ++ii) bins[ii] = 0;
		for (ii = 0; ii < nrows; ii++)
		{  
			for (ij = 0; ij < ncols; ij++) 
			{
				bin = pix[ii][ij] - minPixelValue;
				bin = bin  / binSize;
				if ( (bin - Math.floor(bin)) > 0.5 )
				{
					bin = Math.floor(bin) + 1.0;
				}
				else
				{
					Math.floor(bin);
				}
				if (bin < 0.0) bin = 0.0;
				if ( bin > ((double)(numBins - 1)) ) 
					bin = (double) (numBins - 1);
				bins[(int) bin] = bins[(int) bin] + 1;
			}
		}
		
		return bins;
	}
	private void makeHistEqScaleTable(PixieImage pi)
	{
		int			ii;
		double[]	transfer = new double[256];
		int[]		bins = binPixels(pi, 256);
		
		transfer[0] = (double) bins[0];
		for (ii = 1; ii < 256; ++ii) 
			transfer[ii] = transfer[ii - 1] + ((double) bins[ii]);
		for (ii = 0; ii < 256; ++ii) 
			transfer[ii] = 255 * transfer[ii] / transfer[255];
		for (ii = 0; ii < 256; ++ii)
		{
			scaleTable[ii] = (int) transfer[ii];
			if (scaleTable[ii] < 0) scaleTable[ii] = 0;
			if (scaleTable[ii] > 255) scaleTable[ii] = 255;
		}
		return;
	}
	/**
	 * @param pixelImageRGBPlotter
	 */
	public void copyScaling(PixieImageRGBPlotterNoSwing pixelImageRGBPlotter)
	{
		int		ii;
		this.minPixelValue = pixelImageRGBPlotter.minPixelValue;
		this.maxPixelValue = pixelImageRGBPlotter.maxPixelValue;
		this.invertImage = pixelImageRGBPlotter.invertImage;
		this.colorSpectrum = pixelImageRGBPlotter.colorSpectrum;
		this.autoScale = pixelImageRGBPlotter.autoScale;
		for (ii = 0; ii < 256; ++ii)
		{
			this.scaleTable[ii] = pixelImageRGBPlotter.scaleTable[ii];
		}
		this.scaleType = new String(pixelImageRGBPlotter.scaleType);
	}
	/**
	 * @param redImage
	 * @param greenImage
	 * @param blueImage
	 */
	public void setImages(PixieImage redImage,PixieImage greenImage,PixieImage blueImage)
	{
		this.redImage = redImage;
		this.greenImage = greenImage;
		this.blueImage = blueImage;
	}
	/**
	 * @param redImage
	 */
	public void setImages(PixieImage redImage)
	{
		this.redImage = redImage;
		this.greenImage = redImage;
		this.blueImage = redImage;
	}
	public void setImages(ImageIcon imageIcon)
	{
	    Image image = imageIcon.getImage();
		BufferedImage bi = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
		
	 	Graphics2D graphics2D = bi.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	    			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    graphics2D.drawImage(image, 0, 0,imageIcon.getIconWidth(), imageIcon.getIconHeight(), null);
	    setImages(bi);
	    setPixelValueLimits(0.0, 255.0);
	}
	/**
	 * @param bi
	 */
	public void setImages(BufferedImage bi)
	{
		int height = bi.getHeight();
		int width = bi.getWidth();
		int minX = bi.getMinX();
		int minY = bi.getMinY();
		
		redImage = new PixieImage(height,width);
		greenImage = new PixieImage(height,width);
		blueImage = new PixieImage(height,width);
		
		for (int ii = 0; ii < height; ++ii)
		{
			for (int ij = 0; ij < width; ++ij)
			{
				int irgb = bi.getRGB(minX + ij, minY + ii);
				Color clr = new Color(irgb);
				Pixie pix = new Pixie();
				pix.setRow(height - 1 - ii);
				pix.setCol(ij);
				pix.setCompVal((double) clr.getBlue());
				blueImage.setPixieValue(pix);
				pix.setCompVal((double) clr.getGreen());
				greenImage.setPixieValue(pix);
				pix.setCompVal((double) clr.getRed());
				redImage.setPixieValue(pix);
			}
		}
	}
	/**
	 * 
	 */
	public void colorToBw()
	{
		int	nrows = redImage.getRowCount();
		int ncols = redImage.getColCount();
		double[][]	redPixelArray = redImage.getPix();
		double[][]	greenPixelArray = greenImage.getPix();;
		double[][]	bluePixelArray = blueImage.getPix();;
		
		int		ii;
		int		ij;
		double	dval;
		for (ii = 0; ii < nrows; ++ii)
		{
			for (ij = 0; ij < ncols; ++ij)
			{
				dval = redPixelArray[ii][ij] + greenPixelArray[ii][ij] + bluePixelArray[ii][ij];
				dval = dval / 3.0;
				redPixelArray[ii][ij] = dval;
				greenPixelArray[ii][ij] = dval;
				bluePixelArray[ii][ij] = dval;
			}
		}
		redPixelArray = null;
		greenPixelArray = null;
		bluePixelArray = null;
	}
	public void setPixelValueLimits(double minPixelValue, double maxPixelValue)
	{
		this.minPixelValue = minPixelValue;
		this.maxPixelValue = maxPixelValue;
	}
	/**
	 * 
	 */
	/**
	 * @param pi 
	 * 
	 */
	public void setPixelValueLimits(PixieImage pi)
	{
		if (!autoScale) return;
		double[] minMaxSumMean = pi.findMinMaxSumMean();
		minPixelValue = minMaxSumMean[0];
		maxPixelValue = minMaxSumMean[1];
		
	// Finds the 99.8% range using histogram
		int[] pixelBin = binPixels(pi, 256);
		int		i2p5 = pi.getRowCount() * pi.getColCount() * 1;
		i2p5 = i2p5 / 1000;
		int	imax = 255;
		int isum = pixelBin[imax];
		while (isum < i2p5)
		{
			imax = imax - 1;
			isum = isum + pixelBin[imax];
		}
		int imin = 0;
		isum = pixelBin[imin];
		while (isum < i2p5)
		{
			imin = imin + 1;
			isum = isum + pixelBin[imin];
		}
		double dmax;
		double dmin;
		dmin = minPixelValue + (maxPixelValue - minPixelValue) * ((double) imin) / 255.0;
		dmax = minPixelValue + (maxPixelValue - minPixelValue) * ((double) imax) / 255.0;
		minPixelValue = dmin;
		maxPixelValue = dmax;
	}
	/**
	 * @param invertImage
	 */
	public void setInvertImage(boolean invertImage)
	{
		this.invertImage = invertImage;
	}
	/**
	 * @return boolean
	 */
	public boolean getInvertImage()
	{
		return invertImage;
	}
	/**
	 * @param autoScale 
	 */
	public void setAutoScale(boolean autoScale)
	{
		this.autoScale = autoScale;
	}
	/**
	 * @return boolean
	 */
	public boolean getColorSpectrum()
	{
		return colorSpectrum;
	}
	/**
	 * @param colorSpectrum
	 */
	public void setColorSpectrum(boolean colorSpectrum)
	{
		this.colorSpectrum = colorSpectrum;
	}
	/**
	 * @return boolean
	 */
	public boolean getAutoScale()
	{
		return autoScale;
	}
	/**
	 * @param scaleType
	 */
	public void setScaleType(String scaleType)
	{
		this.scaleType = new String(scaleType);
	}
	/**
	 * @return double
	 */
	public double getMinPixelValue()
	{
		return minPixelValue;
	}
	/**
	 * @return double
	 */
	public double getMaxPixelValue()
	{
		return maxPixelValue;
	}
	/**
	 * @return PixieImage
	 */
	public PixieImage getRedImage()
	{
		PixieImage pi = new PixieImage(redImage); 
		return pi;
	}
	/**
	 * @return PixieImage
	 */
	public PixieImage getBlueImage()
	{
		PixieImage pi = new PixieImage(blueImage); 
		return pi;
	}
	/**
	 * @return PixieImage
	 */
	public PixieImage getGreenImage()
	{
		PixieImage pi = new PixieImage(greenImage); 
		return pi;
	}
}
