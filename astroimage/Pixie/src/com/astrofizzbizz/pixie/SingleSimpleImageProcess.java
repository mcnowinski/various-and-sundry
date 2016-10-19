package com.astrofizzbizz.pixie;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class SingleSimpleImageProcess 
{
	private PixieImage pixieImage = null;
	private double[] brightStarLocation = {-1.0, -1.0};
	private double brightStarSize = -1.0;
	private double maxPixelValue = -1.0;
	
	public SingleSimpleImageProcess(String fitsFileName) throws PixieImageException
	{
		pixieImage = new PixieImage(fitsFileName);
	}
	public SingleSimpleImageProcess(SingleSimpleImageProcess ssip) throws PixieImageException
	{
		pixieImage = new PixieImage(ssip.getPixieImage());
	}
	public PixieImage[] createRGBPixieImage()
	{
		PixieImage[] pi = new PixieImage[3];
		pi[0] = getPixieImage();
		pi[1] = getPixieImage();
		pi[2] = getPixieImage();
		return pi;
	}
	public void subtractImage(String subtractFitsFileName) throws PixieImageException
	{
		PixieImage subtractImage = new PixieImage(subtractFitsFileName);
		int nrows = subtractImage.getRowCount();
		int ncols = subtractImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				pixieImage.getPix()[ir][ic] = pixieImage.getPix()[ir][ic] - subtractImage.getPix()[ir][ic];
				if (pixieImage.getPix()[ir][ic] < 0.0) pixieImage.getPix()[ir][ic] = 0.0;
			}
		}
	}
	public void addImage(String addFitsFileName) throws PixieImageException
	{
		PixieImage addImage = new PixieImage(addFitsFileName);
		int nrows = addImage.getRowCount();
		int ncols = addImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if ((pixieImage.getPix()[ir][ic] > 0.1) && (addImage.getPix()[ir][ic] > 0.1))
				{
					pixieImage.getPix()[ir][ic] = pixieImage.getPix()[ir][ic] + addImage.getPix()[ir][ic];
				}
				else
				{
					pixieImage.getPix()[ir][ic] = 0.0;
				}
			}
		}
	}
	public void divideImage(String divideFitsFileName) throws PixieImageException
	{
		PixieImage divideImage = new PixieImage(divideFitsFileName);
		int nrows = divideImage.getRowCount();
		int ncols = divideImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				pixieImage.getPix()[ir][ic] = pixieImage.getPix()[ir][ic] / (divideImage.getPix()[ir][ic] + 1.0e-33);
			}
		}
	}
	public PixieImage getPixieImage()
	{
		return pixieImage;
	}
	public void subtractMean()
	{
		double[] meanSigma = pixieImage.findNoiseMeanSigma();
		pixieImage.rescalePixels(1.0, -meanSigma[0]);
	}
	public void normalize(String refFitsFileName) throws PixieImageException
	{
		PixieImage refImage = new PixieImage(refFitsFileName);
		double[] meanSigma = pixieImage.findNoiseMeanSigma();
		double[] meanSigmaRef = refImage.findNoiseMeanSigma();

		pixieImage.rescalePixels(1.0, -meanSigma[0]);
		pixieImage.rescalePixels(meanSigmaRef[1] / meanSigma[1], 0.0);
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (pixieImage.getPix()[ir][ic] < 0) pixieImage.getPix()[ir][ic] = 0.0;
			}
		}

	}
	public void gainAdjust(double gain) throws PixieImageException
	{
		if (gain == 1.0) return;

		double max = pixieImage.findMinMaxSumMean()[1];
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				pixieImage.getPix()[ir][ic] = pixieImage.getPix()[ir][ic] * gain;
				if (pixieImage.getPix()[ir][ic] < 0) pixieImage.getPix()[ir][ic] = 0.0;
				if (pixieImage.getPix()[ir][ic] > max) pixieImage.getPix()[ir][ic] = max;
			}
		}
	}
	public void writeOutFitsFile(String fitsFileName) throws PixieImageException
	{
		pixieImage.writeToFitsFile(fitsFileName);
	}
	public void fitsToPng(String pngFileName, int scaleType) 
	{
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		plotter.setImages(pixieImage, pixieImage, pixieImage);

		double max = pixieImage.findMinMaxSumMean()[1];
		double top = max;
		
		plotter.setPixelValueLimits(0.0, top);
		plotter.setScaleTable(pixieImage);
		plotter.toPNGFile(pngFileName);
	}
	public BufferedImage fitsToBufferedImage(int scaleType) 
	{
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		plotter.setImages(pixieImage, pixieImage, pixieImage);

		double max = pixieImage.findMinMaxSumMean()[1];
		double top = max;
		
		plotter.setPixelValueLimits(0.0, top);
		plotter.setScaleTable(pixieImage);
		return plotter.makeBufferedImage();
	}
	public void shiftImage(int ix, int iy)
	{		
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		
		PixieImage shiftImage = new PixieImage(nrows, ncols);
		shiftImage.copyHeaderCards(pixieImage.getHeader());
		for (int ir = 0; ir < nrows; ++ ir)
		{
			int ir2 = ir - iy;
			if ((0 <= ir2) && (ir2 < nrows) )
			{
				for (int ic  = 0; ic < ncols; ++ ic)
				{
					int ic2 = ic - ix;
					if ((0 <= ic2) && (ic2 < ncols) )
					{
						shiftImage.getPix()[ir][ic] = pixieImage.getPix()[ir2][ic2];
					}
				}
			}
		}
		pixieImage = shiftImage;
	}
	public void rotateImage(double theta_deg)
	{		
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		
		//center pixel
		int xc = (int) Math.round(ncols/2.0);
		int yc = (int) Math.round(nrows/2.0);
		
		//create space for rotated image
		PixieImage shiftImage = new PixieImage(nrows, ncols);
		shiftImage.copyHeaderCards(pixieImage.getHeader());
		for (int ir = 0; ir < nrows; ++ ir)
		{
			for (int ic  = 0; ic < ncols; ++ ic)
			{
				double distance = Math.sqrt((ic-xc)*(ic-xc)+(ir-yc)*(ir-yc));
				double angle = Math.atan2((double)(ir-yc), (double)(ic-xc));
				double angle_rotated = angle + Math.toRadians(theta_deg);
				
				int x = (int) Math.round(xc + distance*Math.cos(angle_rotated));
				int y = (int) Math.round(yc + distance*Math.sin(angle_rotated));
				
				//System.out.println("x_in,y_in="+ic+","+ir+"; x_out,y_out="+x+","+y);
				if ((0 <= y) && (y < nrows) && (0 <= x) && (x < ncols))
				{
					shiftImage.getPix()[y][x] = pixieImage.getPix()[ir][ic];
				}
			}
		}
		pixieImage = shiftImage;
	}	

	private void calcMaxPixelValue()
	{
		PixieImage pixieImageTemp = new PixieImage(pixieImage); 
		int nrows = pixieImageTemp.getRowCount();
		int ncols = pixieImageTemp.getColCount();
		double[] noiseMeanSigma = pixieImageTemp.findNoiseMeanSigma();
		pixieImageTemp.rescalePixels(1.0, -noiseMeanSigma[0]);
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (pixieImageTemp.getPix()[ir][ic] < 0.5 * noiseMeanSigma[0]) pixieImageTemp.getPix()[ir][ic] = 0.0;
			}
		}
		int irowMax = 0;
		int icolMax = 0;
		double max = -1.0;
		double[] neighbor = new double[4];
		double center;
		for (int ir = 20; ir < nrows - 20; ++ir)
		{
			for (int ic = 20; ic < ncols - 20; ++ic)
			{
				if (max < pixieImageTemp.getPix()[ir][ic])
				{
					center = pixieImageTemp.getPix()[ir][ic];
					neighbor[0] = pixieImageTemp.getPix()[ir - 1][ic];
					neighbor[1] = pixieImageTemp.getPix()[ir + 1][ic];
					neighbor[2] = pixieImageTemp.getPix()[ir][ic - 1];
					neighbor[3] = pixieImageTemp.getPix()[ir][ic + 1];
					if (
							(neighbor[0] > 0.3 * center) && 
							(neighbor[1] > 0.3 * center) &&
							(neighbor[2] > 0.3 * center) &&
							(neighbor[3] > 0.3 * center) )
					{
						max = pixieImageTemp.getPix()[ir][ic];
						irowMax = ir;
						icolMax = ic;
					}
				}
			}
		}
		maxPixelValue = 0.0;
		if (max > 0)
		{
			maxPixelValue = pixieImage.getPix()[irowMax][icolMax];
		}
		return;
	}
	public double getExposureLevel()
	{
		if (maxPixelValue < 0.0) calcMaxPixelValue();
		return 100.0 * maxPixelValue / 65535.0;
	}
	public void removeHotSpots()
	{
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		double nearNeighborValue = 0.0;
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				nearNeighborValue = nearestNeighborMaxValue(ir, ic);
				if (pixieImage.getPix()[ir][ic] > 10.0 * nearNeighborValue) 
					pixieImage.getPix()[ir][ic] = nearNeighborValue;
			}
		}
	}
	private double nearestNeighborMaxValue(int irow, int icol)
	{
		double maxValue = -1.0e+33;
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		for (int ir = (irow - 1); ir <=  (irow + 1); ++ir)
		{
			if ((ir >= 0) && (ir < nrows))
			{
				for (int ic = (icol - 1); ic <= (icol + 1); ++ic)
				{
					if ((ic >= 0) && (ic < ncols))
					{
						if (!((ir == irow) && (ic == icol)))
							if (pixieImage.getPix()[ir][ic] > maxValue) maxValue = pixieImage.getPix()[ir][ic];
					}
				}
			}
		}
		return maxValue;
	}
	public void calcBrightStarSize()
	{
		PixieImage pixieImageTemp = new PixieImage(pixieImage); 
		double[] noiseMeanSigma = pixieImageTemp.findNoiseMeanSigma();
		pixieImageTemp.rescalePixels(1.0, -noiseMeanSigma[0]);
		int nrows = pixieImageTemp.getRowCount();
		int ncols = pixieImageTemp.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (pixieImageTemp.getPix()[ir][ic] < 0.5 * noiseMeanSigma[0]) pixieImageTemp.getPix()[ir][ic] = 0.0;
			}
		}

		int irowMax = 0;
		int icolMax = 0;
		double max = -1.0;
		double[] neighbor = new double[4];
		double center;
		for (int ir = 20; ir < nrows - 20; ++ir)
		{
			for (int ic = 20; ic < ncols - 20; ++ic)
			{
				if (max < pixieImageTemp.getPix()[ir][ic])
				{
					center = pixieImageTemp.getPix()[ir][ic];
					neighbor[0] = pixieImageTemp.getPix()[ir - 1][ic];
					neighbor[1] = pixieImageTemp.getPix()[ir + 1][ic];
					neighbor[2] = pixieImageTemp.getPix()[ir][ic - 1];
					neighbor[3] = pixieImageTemp.getPix()[ir][ic + 1];
					if (
							(neighbor[0] > 0.3 * center) && 
							(neighbor[1] > 0.3 * center) &&
							(neighbor[2] > 0.3 * center) &&
							(neighbor[3] > 0.3 * center) )
					{
						max = pixieImageTemp.getPix()[ir][ic];
						irowMax = ir;
						icolMax = ic;
					}
				}
			}
		}
		if (max < 0)
		{
			maxPixelValue = 0.0;
			brightStarSize = -1.0;
			brightStarLocation[0] = -1.0;
			brightStarLocation[1] = -1.0;
			return;
		}
		else
		{
			maxPixelValue = pixieImage.getPix()[irowMax][icolMax];
		}
		double meanX = 0.0;
		double meanY = 0.0;
		double sum = 0.0;
		for (int ir = irowMax - 19; ir < irowMax + 19; ++ir)
		{
			for (int ic = icolMax - 19; ic < icolMax + 19; ++ic)
			{
				if (pixieImageTemp.getPix()[ir][ic] < max)
				{
					meanX = meanX + pixieImageTemp.getPix()[ir][ic] * ((double) ic);
					meanY = meanY + pixieImageTemp.getPix()[ir][ic] * ((double) ir);
					sum = sum + pixieImageTemp.getPix()[ir][ic];
				}
			}
		}
		meanX = meanX / sum;
		meanY = meanY / sum;
		
		brightStarLocation[0] = meanX;
		brightStarLocation[1] = meanY;

		double sigmaX = 0.0;
		double sigmaY = 0.0;
		for (int ir = irowMax - 19; ir < irowMax + 19; ++ir)
		{
			for (int ic = icolMax - 19; ic < icolMax + 19; ++ic)
			{
				if (pixieImageTemp.getPix()[ir][ic] < max)
				{
					sigmaX = sigmaX + pixieImageTemp.getPix()[ir][ic] * (((double) ic) - meanX) * (((double) ic) - meanX);
					sigmaY = sigmaY + pixieImageTemp.getPix()[ir][ic] * (((double) ir) - meanY) * (((double) ir) - meanY);
				}
			}
		}
		sigmaX = sigmaX / sum;
		sigmaY = sigmaY / sum;
		double sigma = Math.sqrt(sigmaX + sigmaY);
		
		brightStarSize = sigma;
		return;

	}
	public double getBrightStarSize()
	{
		return brightStarSize;
	}
	public boolean[][][] exposureLevelCut()
	{
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		boolean[][][] exposureLevelCut = new boolean[3][nrows][ncols];
		
		for (int ip = 0; ip < 3; ++ip)
		{
			for (int ir = 0; ir < nrows; ++ir)
			{
				for (int ic = 0; ic < ncols; ++ic)
				{
					exposureLevelCut[ip][ir][ic] = false;
				}
			}
		}
		if (maxPixelValue > 0.0)
		{
			for (int ir = 0; ir < nrows; ++ir)
			{
				for (int ic = 0; ic < ncols; ++ic)
				{
					if (pixieImage.getPix()[ir][ic] > (0.90 * maxPixelValue))
					{
						exposureLevelCut[0][ir][ic] = true;
					}
					else
					{
						if (pixieImage.getPix()[ir][ic] > (0.50 * maxPixelValue))
						{
							
							exposureLevelCut[1][ir][ic] = true;
						}
						else
						{
							exposureLevelCut[0][ir][ic] = true;
							exposureLevelCut[1][ir][ic] = true;
							exposureLevelCut[2][ir][ic] = true;
						}
					}
				}
			}
		}
		return exposureLevelCut;
	}
	public void fitsToPngWithBrightStarLocation(String pngFileName, int scaleType, boolean plotBrightStar, boolean[][][] exposureLevelCut) 
	{
		double[] minMaxSumMean = pixieImage.findMinMaxSumMean();
		double max = minMaxSumMean[1];
		double top = max;
		
		PixieImage[] colorPi = new PixieImage[3];
		colorPi[0] = new PixieImage(pixieImage);
		colorPi[1] = new PixieImage(pixieImage);
		colorPi[2] = new PixieImage(pixieImage);
		int nrows = pixieImage.getRowCount();
		int ncols = pixieImage.getColCount();
		
		if (exposureLevelCut != null)
		{
			for (int ip = 0; ip < 3; ++ip)
			for (int ir = 0; ir < nrows; ++ir)
			{
				for (int ic = 0; ic < ncols; ++ic)
				{
					if (!exposureLevelCut[ip][ir][ic]) colorPi[ip].getPix()[ir][ic] = 0.0;
				}
			}

		}
		
		double markerMult = 2.0;
		if (brightStarSize > 0.0)
		{
			int itop = (int)Math.round(brightStarLocation[1] + markerMult * brightStarSize);
			int ibot = (int)Math.round(brightStarLocation[1] - markerMult * brightStarSize);
			int ilef = (int)Math.round(brightStarLocation[0] + markerMult * brightStarSize);
			int irig = (int)Math.round(brightStarLocation[0] - markerMult * brightStarSize);
			
			if (itop >= nrows) itop = nrows - 1;
			if (ibot < 0 ) ibot = 0;
			if (ilef >= ncols) ilef = ncols - 1;
			if (irig < 0 ) irig = 0;
			
			int irowCenter = (int)Math.round(brightStarLocation[1]);
			int icolCenter = (int)Math.round(brightStarLocation[0]);
			for (int ir = irowCenter - 15; ir < irowCenter + 15; ++ir)
			{
				colorPi[1].getPix()[ir][ilef] = top;
				colorPi[1].getPix()[ir][irig] = top;
			}
			for (int ic = icolCenter - 15; ic < icolCenter + 15; ++ic)
			{
				colorPi[1].getPix()[itop][ic] = top;
				colorPi[1].getPix()[ibot][ic] = top;
			}
		}
		
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		plotter.setImages(colorPi[0], colorPi[1], colorPi[2]);
		
		plotter.setPixelValueLimits(0.0, top);
		plotter.setScaleTable(pixieImage);
		plotter.toPNGFile(pngFileName);
	}

	public static void threeColorPng(String redFitsFileName, String greenFitsFileName, String blueFitsFileName, String pngFileName, int scaleType, double minPerc, double maxPerc) throws PixieImageException 
	{
		PixieImage redPixieImage = null;
		PixieImage greenPixieImage = null;
		PixieImage bluePixieImage = null;
		try {redPixieImage = new PixieImage(redFitsFileName);} catch (PixieImageException e) {}		
		try {greenPixieImage = new PixieImage(greenFitsFileName);} catch (PixieImageException e) {}		
		try {bluePixieImage = new PixieImage(blueFitsFileName);} catch (PixieImageException e) {}	
		threeColorPng(redPixieImage, greenPixieImage, bluePixieImage, pngFileName, scaleType, minPerc, maxPerc);
	}
	public static void threeColorPng(PixieImage redPixieImage, PixieImage greenPixieImage, PixieImage bluePixieImage, String pngFileName, int scaleType, double minPerc, double maxPerc) throws PixieImageException 
	{
		PixieImageRGBPlotterNoSwing plotter = threeColor(redPixieImage, greenPixieImage, bluePixieImage, scaleType, minPerc, maxPerc);
		plotter.toPNGFile(pngFileName);
	}
	public static PixieImageRGBPlotterNoSwing threeColorOld(PixieImage redPixieImage, PixieImage greenPixieImage, PixieImage bluePixieImage, int scaleType) throws PixieImageException 
	{
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		int nrows = 0;
		int ncols = 0;
		if (redPixieImage != null)
		{
			nrows = redPixieImage.getRowCount();
			ncols = redPixieImage.getColCount();
		}
		else
		{
			if (greenPixieImage != null)
			{
				nrows = greenPixieImage.getRowCount();
				ncols = greenPixieImage.getColCount();
			}
			else
			{
				if (bluePixieImage != null)
				{
					nrows = bluePixieImage.getRowCount();
					ncols = bluePixieImage.getColCount();
				}
				else
				{
					throw new PixieImageException();
				}
			}
		}
		if (redPixieImage == null) redPixieImage = new PixieImage(nrows, ncols);
		if (greenPixieImage == null) greenPixieImage = new PixieImage(nrows, ncols);
		if (bluePixieImage == null) bluePixieImage = new PixieImage(nrows, ncols);
		
		double[] redMinMaxSumMean = redPixieImage.findMinMaxSumMean();
		double[] greenMinMaxSumMean = greenPixieImage.findMinMaxSumMean();
		double[] blueMinMaxSumMean = bluePixieImage.findMinMaxSumMean();

		double maxRed = redPixieImage.findMinMaxSumMean()[1];
		double maxGreen = greenPixieImage.findMinMaxSumMean()[1];
		double maxBlue = bluePixieImage.findMinMaxSumMean()[1];

		double top = redMinMaxSumMean[1];
		if ((redMinMaxSumMean[1] > 0.1)) top = redMinMaxSumMean[1];
		if ((greenMinMaxSumMean[1] > 0.1) && (top > greenMinMaxSumMean[1])) top = greenMinMaxSumMean[1];
		if ((blueMinMaxSumMean[1] > 0.1) && (top > blueMinMaxSumMean[1])) top = blueMinMaxSumMean[1];

		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (redPixieImage.getPix()[ir][ic] < 0) redPixieImage.getPix()[ir][ic] = 0.0;
				if (greenPixieImage.getPix()[ir][ic] < 0) greenPixieImage.getPix()[ir][ic] = 0.0;
				if (bluePixieImage.getPix()[ir][ic] < 0) bluePixieImage.getPix()[ir][ic] = 0.0;
				if (redPixieImage.getPix()[ir][ic] > top) redPixieImage.getPix()[ir][ic] = top;
				if (greenPixieImage.getPix()[ir][ic] > top) greenPixieImage.getPix()[ir][ic] = top;
				if (bluePixieImage.getPix()[ir][ic] > top) bluePixieImage.getPix()[ir][ic] = top;
			}
		}

		plotter.setImages(redPixieImage, greenPixieImage, bluePixieImage);

		plotter.setPixelValueLimits(0.0, top);
		if ((maxRed > 0.1) && (maxRed <= maxGreen) && (maxRed <= maxBlue))
		{
			plotter.setScaleTable(redPixieImage);
		}
		if ((maxGreen > 0.1) && (maxGreen <= maxRed) && (maxGreen <= maxBlue)) 
		{
			plotter.setScaleTable(greenPixieImage);
		}
		if ((maxGreen > 0.1) && (maxBlue <= maxRed) && (maxBlue <= maxGreen))
		{
			plotter.setScaleTable(bluePixieImage);
		}
		
		return plotter;

	}
	public static PixieImageRGBPlotterNoSwing threeColor(PixieImage redPixieImage, PixieImage greenPixieImage, PixieImage bluePixieImage, int scaleType, double minPerc, double maxPerc) throws PixieImageException 
	{
		PixieImage[] pi3 = new PixieImage[3];
		PixieImage[] pi3new = new PixieImage[3];
		pi3[0] = redPixieImage;
		pi3[1] = greenPixieImage;
		pi3[2] = bluePixieImage;
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		if (scaleType == 4) plotter.setScaleType("asinh");
		plotter.setAutoScale(true);
		int nrows = -1;
		int ncols = -1;
		double dnumImages = 0;
		for (int ii = 0; ii < 3; ++ii)
		{
			if (pi3[ii] != null)
			{
				nrows = pi3[ii].getRowCount();
				ncols = pi3[ii].getColCount();
				dnumImages = dnumImages + 1.0;
			}
		}
		if (nrows < 0)
		{
			throw new PixieImageException();
		}
		PixieImage piAvg = new PixieImage(nrows, ncols);
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				for (int ii = 0; ii < 3; ++ii)
				{
					if (pi3[ii] != null)
					{
						piAvg.getPix()[ir][ic] = piAvg.getPix()[ir][ic] + pi3[ii].getPix()[ir][ic];
					}
				}
				piAvg.getPix()[ir][ic] = piAvg.getPix()[ir][ic] / dnumImages;
			}
		}
		double[] minMaxSumMean = piAvg.findMinMaxSumMean();
//		double bot = minMaxSumMean[0] + (minMaxSumMean[1] - minMaxSumMean[0]) * minPerc / 100;
//		double top = minMaxSumMean[0] + (minMaxSumMean[1] - minMaxSumMean[0]) * maxPerc / 100;
		double bot = minMaxSumMean[1] * minPerc / 100;
		double top = minMaxSumMean[1] * maxPerc / 100;
		for (int ii = 0; ii < 3; ++ii)
		{
			if (pi3[ii] != null)
			{
				pi3new[ii] = new PixieImage(pi3[ii]);
			}
			else
			{
				pi3new[ii] = new PixieImage(nrows, ncols);
				for (int ir = 0; ir < nrows; ++ir)
				{
					for (int ic = 0; ic < ncols; ++ic)
					{
						pi3new[ii].getPix()[ir][ic] = bot;
					}
				}
			}
		}

		plotter.setImages(pi3new[0], pi3new[1], pi3new[2]);

		plotter.setPixelValueLimits(bot, top);
		plotter.setScaleTable(piAvg);
		
		return plotter;

	}
	public static PixieImage[] makeZoomedPixieImage(PixieImage[] pixieImageDisplay, double zoom, int iRowCenter, int iColCenter)
	{
		if (zoom < 1.0) zoom = 1.0;
		int nrows = pixieImageDisplay[0].getRowCount();
		int ncols = pixieImageDisplay[0].getColCount();
		int nrowsZoom = (int) (((double) nrows) / zoom);
		int ncolsZoom = (int) (((double) ncols) / zoom);
		int irowStart = iRowCenter - nrowsZoom / 2;
		int irowStop = irowStart + nrowsZoom - 1;
		if (irowStart < 0)
		{
			irowStart = 0;
			irowStop = irowStart + nrowsZoom - 1;
		}
		if (irowStop >= nrows)
		{
			irowStop = nrows - 1;
			irowStart = irowStop - nrowsZoom + 1;
		}
		int icolStart = iColCenter - ncolsZoom / 2;
		int icolStop = icolStart + ncolsZoom - 1;
		if (icolStart < 0)
		{
			icolStart = 0;
			icolStop = icolStart + ncolsZoom - 1;
		}
		if (icolStop >= ncols)
		{
			icolStop = ncols - 1;
			icolStart = icolStop - ncolsZoom + 1;
		}
		PixieImage[] zoomPixieImageDisplay = new PixieImage[3];		
		for (int ii = 0; ii < 3; ++ii)
		{
			zoomPixieImageDisplay[ii] = new PixieImage(nrowsZoom, ncolsZoom);
			zoomPixieImageDisplay[ii].setRowCenter(irowStart + nrowsZoom / 2);
			zoomPixieImageDisplay[ii].setColCenter(icolStart + ncolsZoom / 2);
			for (int ir = 0; ir < nrowsZoom; ++ir)
			{
				for (int ic = 0; ic < ncolsZoom; ++ic)
				{
					zoomPixieImageDisplay[ii].getPix()[ir][ic] = pixieImageDisplay[ii].getPix()[ir + irowStart][ic + icolStart];
				}
			}
		}
		return zoomPixieImageDisplay;
	}
	public static void  makeExposureImage(String[] args) throws PixieImageException
	{
		String inputFitsFileName = "";
		String outputPngFileName = "";
		boolean dark = false;
		int numArgs = args.length;
		for (int iarg = 0;  iarg < numArgs; ++iarg)
		{
			if (args[iarg].indexOf(".fits") >= 0 ) inputFitsFileName = args[iarg];
			if (args[iarg].indexOf(".png") >= 0 ) outputPngFileName = args[iarg];
			if (args[iarg].indexOf("-dark") >= 0 ) dark = true;
		}
		
		SingleSimpleImageProcess ssip = new SingleSimpleImageProcess(inputFitsFileName);
		if (!dark)
		{
			ssip.calcBrightStarSize();
			double starSize = ssip.getBrightStarSize();
			if (starSize > 0.0)
			{
				System.out.println("Star found in field");
				DecimalFormat tp = new DecimalFormat("###.##");
				System.out.println(tp.format(starSize) + "pixels star size");
				tp = new DecimalFormat("###.#");
				System.out.println(tp.format(ssip.getExposureLevel()) + "% Exposure level");
				boolean[][][] exposureLevelCut = null;
				exposureLevelCut =  ssip.exposureLevelCut();
				ssip.removeHotSpots();
				ssip.subtractMean();
				ssip.fitsToPngWithBrightStarLocation(outputPngFileName, 3, true, exposureLevelCut);
			}
			else
			{
				System.out.println("Star not found in field");
				System.out.println("0.00" + "pixels star size");
				System.out.println("0.0" + "% Exposure level");
				ssip.fitsToPng(outputPngFileName, 0);
			}
		}
		else
		{
			System.out.println("Star not found in field");
			System.out.println("0.00" + "pixels star size");
			System.out.println("0.0" + "% Exposure level");
			ssip.fitsToPng(outputPngFileName, 0);
		}
	}
	public static void main(String[] args) throws PixieImageException  
	{
		int numArgs = args.length;
		if (numArgs > 0)
		{
			for (int iarg = 0;  iarg < numArgs; ++iarg)
			{
				if (args[iarg].indexOf("-exp") >= 0) SingleSimpleImageProcess.makeExposureImage(args);
			}
		}
		

	}

}
