package com.astrofizzbizz.pixie;

public class SimpleImageProcess 
{
	private PixieImage piRed = null;
	private PixieImage piGreen = null;
	private PixieImage piBlue = null;
	
	public SimpleImageProcess(String redFitsFileName, String greenFitsFileName, String blueFitsFileName) throws PixieImageException
	{
		piRed = new PixieImage(redFitsFileName);
		piGreen = new PixieImage(greenFitsFileName);
		piBlue = new PixieImage(blueFitsFileName);
	}
	public void subtractDarkImage(String darkFitsFileName) throws PixieImageException
	{
		PixieImage dark = new PixieImage(darkFitsFileName);
		int nrows = dark.getRowCount();
		int ncols = dark.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				piRed.getPix()[ir][ic] = piRed.getPix()[ir][ic] - dark.getPix()[ir][ic];
				piGreen.getPix()[ir][ic] = piGreen.getPix()[ir][ic] - dark.getPix()[ir][ic];
				piBlue.getPix()[ir][ic] = piBlue.getPix()[ir][ic] - dark.getPix()[ir][ic];
			}
		}
	}
	public void normalize(boolean subtractMean, boolean normalize)
	{
		if (!subtractMean && !normalize) return;
		double[] meanSigmaRed = piRed.findNoiseMeanSigma();
		double[] meanSigmaGreen = piGreen.findNoiseMeanSigma();
		double[] meanSigmaBlue = piBlue.findNoiseMeanSigma();
		if (subtractMean)
		{
			piRed.rescalePixels(1.0, -meanSigmaRed[0]);
			piGreen.rescalePixels(1.0, -meanSigmaGreen[0]);
			piBlue.rescalePixels(1.0, -meanSigmaBlue[0]);
		}

		if (normalize)
		{
			piGreen.rescalePixels(meanSigmaRed[1] / meanSigmaGreen[1], 0.0);
			piBlue.rescalePixels(meanSigmaRed[1] / meanSigmaBlue[1], 0.0);
		}
		
		double maxRed = piRed.findMinMaxSumMean()[1];
		double maxGreen = piGreen.findMinMaxSumMean()[1];
		double maxBlue = piBlue.findMinMaxSumMean()[1];
		
		double top = maxRed;
		if (top > maxGreen) top = maxGreen;
		if (top > maxBlue) top = maxBlue;

		int nrows = piRed.getRowCount();
		int ncols = piRed.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (piRed.getPix()[ir][ic] < 0) piRed.getPix()[ir][ic] = 0.0;
				if (piGreen.getPix()[ir][ic] < 0) piGreen.getPix()[ir][ic] = 0.0;
				if (piBlue.getPix()[ir][ic] < 0) piBlue.getPix()[ir][ic] = 0.0;
				if (piRed.getPix()[ir][ic] > top) piRed.getPix()[ir][ic] = top;
				if (piGreen.getPix()[ir][ic] > top) piGreen.getPix()[ir][ic] = top;
				if (piBlue.getPix()[ir][ic] > top) piBlue.getPix()[ir][ic] = top;
			}
		}
	}
	public void gainAdjust(String imageName, double gain)
	{
		if (gain == 1.0) return;
		PixieImage refImage = null;
		if (imageName.equals("red")) refImage = piRed;
		if (imageName.equals("green")) refImage = piGreen;
		if (imageName.equals("blue")) refImage = piBlue;

		double max = refImage.findMinMaxSumMean()[1];
		int nrows = refImage.getRowCount();
		int ncols = refImage.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				refImage.getPix()[ir][ic] = refImage.getPix()[ir][ic] * gain;
				if (refImage.getPix()[ir][ic] < 0) refImage.getPix()[ir][ic] = 0.0;
				if (refImage.getPix()[ir][ic] > max) refImage.getPix()[ir][ic] = max;
			}
		}
	}
	public void writeOutFitsFiles(String redFitsFileName, String greenFitsFileName, String blueFitsFileName) throws PixieImageException
	{
		piRed.writeToFitsFile(redFitsFileName);
		piGreen.writeToFitsFile(greenFitsFileName);
		piBlue.writeToFitsFile(blueFitsFileName);
	}
	public void fitsToPng(String pngFileName, int scaleType, boolean plotRed, boolean plotGreen, boolean plotBlue) 
	{
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		PixieImage piBlank = new PixieImage(piRed.getRowCount(), piRed.getColCount());
		PixieImage piRedPlot = piBlank;
		PixieImage piGreenPlot = piBlank;
		PixieImage piBluePlot = piBlank;
		if (plotRed) piRedPlot = piRed;
		if (plotGreen) piGreenPlot = piGreen;
		if (plotBlue) piBluePlot = piBlue;
		plotter.setImages(piRedPlot, piGreenPlot, piBluePlot);

		double maxRed = piRedPlot.findMinMaxSumMean()[1];
		double maxGreen = piGreenPlot.findMinMaxSumMean()[1];
		double maxBlue = piBluePlot.findMinMaxSumMean()[1];
		double top = maxRed;
		if (top < maxGreen) top = maxGreen;
		if (top < maxBlue) top = maxBlue;
		
		plotter.setPixelValueLimits(0.0, top);
		if (plotRed) 
		{
			plotter.setScaleTable(piRedPlot);
		}
		else
		{
			if (plotGreen)
			{
				plotter.setScaleTable(piGreenPlot);
			}
			else
			{
				plotter.setScaleTable(piBluePlot);
			}
		}
		plotter.toPNGFile(pngFileName);
	}
	public void shiftImage(String imageName, int ix, int iy)
	{
		PixieImage refImage = null;
		if (imageName.equals("red")) refImage = piRed;
		if (imageName.equals("green")) refImage = piGreen;
		if (imageName.equals("blue")) refImage = piBlue;
		
		int nrows = refImage.getRowCount();
		int ncols = refImage.getColCount();
		
		PixieImage shiftImage = new PixieImage(nrows, ncols);
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
						shiftImage.getPix()[ir][ic] = refImage.getPix()[ir2][ic2];
					}
				}
			}
		}
		if (imageName.equals("red")) piRed = shiftImage;
		if (imageName.equals("green")) piGreen = shiftImage;
		if (imageName.equals("blue")) piBlue = shiftImage;
	}
	public static double starSize(String fitsFileName)
	{
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		plotter.setScaleType("log");
		plotter.setAutoScale(true);
		PixieImage pi = null;
		try {
			pi = new PixieImage(fitsFileName);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		double[] noiseMeanSigma = pi.findNoiseMeanSigma();
		pi.rescalePixels(1.0, -noiseMeanSigma[0]);
		int nrows = pi.getRowCount();
		int ncols = pi.getColCount();
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				if (pi.getPix()[ir][ic] < 0.5 * noiseMeanSigma[0]) pi.getPix()[ir][ic] = 0.0;
			}
		}
/*
		plotter.setImages(pi);
		plotter.setPixelValueLimits(pi);
		plotter.setScaleTable(pi);
		plotter.toPNGFile("C:\\temp\\test0.png");
*/
		int irowMax = 0;
		int icolMax = 0;
		double max = -1.0;
		double[] neighbor = new double[4];
		double center;
		for (int ir = 20; ir < nrows - 20; ++ir)
		{
			for (int ic = 20; ic < ncols - 20; ++ic)
			{
				if (max < pi.getPix()[ir][ic])
				{
					center = pi.getPix()[ir][ic];
					neighbor[0] = pi.getPix()[ir - 1][ic];
					neighbor[1] = pi.getPix()[ir + 1][ic];
					neighbor[2] = pi.getPix()[ir][ic - 1];
					neighbor[3] = pi.getPix()[ir][ic + 1];
					if (
							(neighbor[0] > 0.5 * center) && 
							(neighbor[1] > 0.5 * center) &&
							(neighbor[2] > 0.5 * center) &&
							(neighbor[3] > 0.5 * center) )
					{
						max = pi.getPix()[ir][ic];
						irowMax = ir;
						icolMax = ic;
					}
				}
			}
		}
		if (max < 0) return -1.0;
		double meanX = 0.0;
		double meanY = 0.0;
		double sum = 0.0;
		for (int ir = irowMax - 19; ir < irowMax + 19; ++ir)
		{
			for (int ic = icolMax - 19; ic < icolMax + 19; ++ic)
			{
				if (pi.getPix()[ir][ic] < max)
				{
					meanX = meanX + pi.getPix()[ir][ic] * ((double) ic);
					meanY = meanY + pi.getPix()[ir][ic] * ((double) ir);
					sum = sum + pi.getPix()[ir][ic];
				}
			}
		}
		meanX = meanX / sum;
		meanY = meanY / sum;
		
		double sigmaX = 0.0;
		double sigmaY = 0.0;
		for (int ir = irowMax - 19; ir < irowMax + 19; ++ir)
		{
			for (int ic = icolMax - 19; ic < icolMax + 19; ++ic)
			{
				if (pi.getPix()[ir][ic] < max)
				{
					sigmaX = sigmaX + pi.getPix()[ir][ic] * (((double) ic) - meanX) * (((double) ic) - meanX);
					sigmaY = sigmaY + pi.getPix()[ir][ic] * (((double) ir) - meanY) * (((double) ir) - meanY);
				}
			}
		}
		sigmaX = sigmaX / sum;
		sigmaY = sigmaY / sum;
		double sigma = Math.sqrt(sigmaX + sigmaY);
		return sigma;
/*		
		int imeanX = (int)Math.round(meanX);
		int imeanY = (int)Math.round(meanY);
		int itop = (int)Math.round(meanY + 1.0 * sigmaY);
		int ibot = (int)Math.round(meanY - 1.0 * sigmaY);
		int ilef = (int)Math.round(meanX + 1.0 * sigmaX);
		int irig = (int)Math.round(meanX - 1.0 * sigmaX);
		for (int ir = irowMax - 19; ir < irowMax + 19; ++ir)
		{
			pi.getPix()[ir][ilef] = max;
			pi.getPix()[ir][irig] = max;
		}
		System.out.println("\n\n\n");
		for (int ic = icolMax - 19; ic < icolMax + 19; ++ic)
		{
			pi.getPix()[itop][ic] = max;
			pi.getPix()[ibot][ic] = max;
		}
		plotter.setImages(pi);
		plotter.setPixelValueLimits(pi);
		plotter.setScaleTable(pi);
		plotter.toPNGFile("C:\\temp\\test1.png");
*/
	}
	public static void fitsToPng(String fitsFileName, String pngFileName, int scaleType) 
	{
		PixieImage pi = null;
		try {
			pi = new PixieImage(fitsFileName);
		} catch (PixieImageException e) {
			e.printStackTrace();
		}
		PixieImageRGBPlotterNoSwing plotter = new PixieImageRGBPlotterNoSwing();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		if (scaleType == 3) plotter.setScaleType("sqrt");
		plotter.setAutoScale(true);
		plotter.setImages(pi);
		plotter.setPixelValueLimits(pi);
		plotter.setScaleTable(pi);
		plotter.toPNGFile(pngFileName);
		
	}
	public static void main(String[] args) throws PixieImageException 
	{
//		double sigma = SimpleImageProcess.starSize("C:\\temp\\M57_red_80s.fits");
//		double sigma = SimpleImageProcess.starSize("C:\\temp\\M3340secRed.fits");
		double sigma = SimpleImageProcess.starSize("C:\\temp\\darkM13-20100811-60s-seo.fits");
		System.out.println("Sigma = " + sigma);
	}

}
