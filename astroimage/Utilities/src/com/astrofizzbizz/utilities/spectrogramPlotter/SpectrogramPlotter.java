package com.astrofizzbizz.utilities.spectrogramPlotter;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class SpectrogramPlotter 
{
	private Font titleFont = new Font("SansSerif", Font.BOLD, 16);
	private double[] xseries;
	private double[] yseries;
	private double[] zseries; 
	private double dx;
	private double dy;
	private double zmax;
	private double zmin;
	private String sfileName  = "test.png";
	private String xAxisTitle;
	private String yAxisTitle;
	private String zAxisTitle;
	private String chartTitle;
	private int chartPixelWidth = 800;
	private int chartPixelHeight = 600;
	private int numZscaleTicks = 256;
	boolean colorSpectrumScale = true;
	boolean colorGrayScale = false;
	double colorGrayScaleHue = 1.0;
	private boolean xaxisAutoScale = true;
	private double[] xaxisRange = {-1.0, 1.0};
	private boolean yaxisAutoScale = true;
	private double[] yaxisRange = {-1.0, 1.0};
	private boolean displayLegend = true;
	private boolean zaxisClip = false;
	
	public SpectrogramPlotter()
	{
		
	}
	public void makePngChart(double[] xseries, double[] yseries, double[] zseries,
			double dx, double dy, double zmax, 
			String sfileName, String xAxisTitle, String yAxisTitle, String chartTitle,
			int chartPixelWidth, int chartPixelHeight) throws IOException
	{
   		setChartPixelHeight(chartPixelHeight);
   		setChartPixelWidth(chartPixelWidth);
   		setFileName(sfileName);
   		setChartTitle(chartTitle);
   		setXAxisTitle(xAxisTitle);
   		setYAxisTitle(yAxisTitle);
   		setDx(dx);
   		setDy(dy);
   		setZmax(zmax);
  		setXseries(xseries);
   		setYseries(yseries);
   		setZseries(zseries);
   		makePngChart();
 	}

	public void makePngChart() throws IOException
	{
		JFreeChart chart  = createSpectrogramChart();
		ChartUtilities.saveChartAsPNG(new File(sfileName), chart, chartPixelWidth, chartPixelHeight);
	}
	public JFreeChart createSpectrogramChart()
	{
		SpectrogramXYZDataset myXYZDataset = new SpectrogramXYZDataset(xseries, yseries, zseries);
		JFreeChart chart  = createSpectrogramChart(myXYZDataset);
		chart.getTitle().setFont(titleFont);
		return chart;
	}
	private LookupPaintScale spectrumPaintScale(boolean clip)
	{
		LookupPaintScale scale = null;
		if (clip) 
		{
			scale = new LookupPaintScale(-1e+33, 1e+33,Color.black);
		}
		else
		{
			scale = new LookupPaintScale(zmin, zmax,Color.black);
		}
		if (clip) scale.add(-1e+33, Color.getHSBColor((float)0.72, (float)1.0, (float)1.0));
        for (int ii = 0; ii < numZscaleTicks; ++ii)
        {
        	double dc = ((double) ii) / ((double) (numZscaleTicks - 1));
        	double hue = (1.0 - dc) * 0.72;
        	scale.add(zmin + (zmax - zmin) * dc, Color.getHSBColor((float)hue, (float)1.0, (float)1.0));
        }
        if (clip) scale.add(1e+33, Color.getHSBColor((float)0.0, (float)1.0, (float)1.0));
        return scale;
	}
	private LookupPaintScale colorGrayScale()
	{
		LookupPaintScale scale = new LookupPaintScale(zmin, zmax,Color.black);
        for (int ii = 0; ii < numZscaleTicks; ++ii)
        {
        	double dc = ((double) ii) / ((double) (numZscaleTicks - 1));
        	scale.add(zmin + (zmax - zmin) * dc, Color.getHSBColor((float) colorGrayScaleHue, (float)1.0, (float)dc));
        }
        return scale;
	}
	public double[] findZminZmax()
	{
		int npts = zseries.length;
		double[] zminZmax = new double[2];
		zminZmax[0] = zseries[0];
		zminZmax[1] = zseries[0];
		for (int ipt = 1; ipt < npts; ++ipt)
		{
			if (zminZmax[0] > zseries[ipt]) zminZmax[0] = zseries[ipt];
			if (zminZmax[1] < zseries[ipt]) zminZmax[1] = zseries[ipt];
		}
		return zminZmax;
	}
	public void setZminZmax(double[] zminZmax)
	{
		setZmin(zminZmax[0]);
		setZmax(zminZmax[1]);
	}
	private int[] binZSeries()
	{
		int[] 	bins = new int[numZscaleTicks];
		double 	binSize;
		double bin;
		
		binSize = (zmax - zmin) / ((double) numZscaleTicks);
		for (int ii = 0; ii < numZscaleTicks; ++ii) bins[ii] = 0;
		int npts = zseries.length;
		for (int ipt = 0; ipt < npts; ipt++)
		{  
			bin = zseries[ipt] - zmin;
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
			if ( bin > ((double)(numZscaleTicks - 1)) ) 
				bin = (double) (numZscaleTicks - 1);
			bins[(int) bin] = bins[(int) bin] + 1;
		}
		return bins;
	}
	private int[] makeHistEqScaleTable()
	{
		int			ii;
		double[]	transfer = new double[numZscaleTicks];
		int[]		bins = binZSeries();
		int[] scaleTable = new int[numZscaleTicks];
		
		transfer[0] = (double) bins[0];
		for (ii = 1; ii < numZscaleTicks; ++ii) 
			transfer[ii] = transfer[ii - 1] + ((double) bins[ii]);
		for (ii = 0; ii < numZscaleTicks; ++ii) 
			transfer[ii] = (numZscaleTicks - 1) * transfer[ii] / transfer[numZscaleTicks - 1];
		for (ii = 0; ii < numZscaleTicks; ++ii)
		{
			scaleTable[ii] = (int) transfer[ii];
			if (scaleTable[ii] < 0) scaleTable[ii] = 0;
			if (scaleTable[ii] >= numZscaleTicks) scaleTable[ii] = numZscaleTicks - 1;
		}
		return scaleTable;
	}
	public void histEqZseries()
	{
		int[] scaleTable = makeHistEqScaleTable();
		int npts = zseries.length;
		double bin;
		double binSize = (zmax - zmin) / ((double) numZscaleTicks);
		for (int ipt = 0; ipt < npts; ipt++)
		{  
			bin = zseries[ipt] - zmin;
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
			if ( bin > ((double)(numZscaleTicks - 1)) ) 
				bin = (double) (numZscaleTicks - 1);
			zseries[ipt] = (double) scaleTable[(int) bin];
		}
		zmin = scaleTable[0];
		zmax = scaleTable[numZscaleTicks - 1];
	}
    private JFreeChart createSpectrogramChart(XYZDataset dataset) 
    {
        NumberAxis xAxis = new NumberAxis(xAxisTitle);
        if (!xaxisAutoScale) xAxis.setRange(xaxisRange[0], xaxisRange[1]);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        xAxis.setAxisLinePaint(Color.white);
        xAxis.setTickMarkPaint(Color.white);

        NumberAxis yAxis = new NumberAxis(yAxisTitle);
        if (!yaxisAutoScale) yAxis.setRange(yaxisRange[0], yaxisRange[1]);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setAxisLinePaint(Color.white);
        yAxis.setTickMarkPaint(Color.white);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(dx);
        renderer.setBlockHeight(dy);
        PaintScale scaleLegend = null;
        PaintScale scaleRender = null;
        if (colorSpectrumScale)
        {
        	scaleRender = spectrumPaintScale(zaxisClip);
        	scaleLegend = spectrumPaintScale(false);
        }
        else 
        {
        	if (colorGrayScale)
        	{
        		scaleRender = colorGrayScale();
            	scaleLegend = colorGrayScale();
        	}
        	else 
        	{
        		scaleRender = new GrayPaintScale(zmin, zmax);
        		scaleLegend = new GrayPaintScale(zmin, zmax);
        	}
        }
        renderer.setPaintScale(scaleRender);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        JFreeChart chart = new JFreeChart(chartTitle, plot);
        chart.removeLegend();

        NumberAxis scaleAxis = new NumberAxis(zAxisTitle);
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
        PaintScaleLegend legend = new PaintScaleLegend(scaleLegend, scaleAxis);
        legend.setSubdivisionCount(50);
        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        legend.setAxisOffset(5.0);
        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
        legend.setFrame(new BlockBorder(Color.red));
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(10);
        legend.setPosition(RectangleEdge.RIGHT);
        if (!displayLegend) legend.setVisible(false);
        chart.addSubtitle(legend);
        ChartUtilities.applyCurrentTheme(chart);
        plot.setBackgroundPaint(Color.WHITE);
        return chart;
    }
	/**
	 * @return the titleFont
	 */
	public Font getTitleFont() {
		return titleFont;
	}
	/**
	 * @param titleFont the titleFont to set
	 */
	public void setTitleFont(Font titleFont) {
		this.titleFont = titleFont;
	}
	/**
	 * @return the xseries
	 */
	public double[] getXseries() {
		return xseries;
	}
	/**
	 * @param xseries the xseries to set
	 */
	public void setXseries(double[] xseries) {
		this.xseries = xseries;
	}
	/**
	 * @return the yseries
	 */
	public double[] getYseries() {
		return yseries;
	}
	/**
	 * @param yseries the yseries to set
	 */
	public void setYseries(double[] yseries) {
		this.yseries = yseries;
	}
	/**
	 * @return the zseries
	 */
	public double[] getZseries() {
		return zseries;
	}
	/**
	 * @param zseries the zseries to set
	 */
	public void setZseries(double[] zseries) {
		this.zseries = zseries;
	}
	/**
	 * @return the dx
	 */
	public double getDx() {
		return dx;
	}
	/**
	 * @param dx the dx to set
	 */
	public void setDx(double dx) {
		this.dx = dx;
	}
	/**
	 * @return the dy
	 */
	public double getDy() {
		return dy;
	}
	/**
	 * @param dy the dy to set
	 */
	public void setDy(double dy) {
		this.dy = dy;
	}
	public void setDisplayLegend(boolean displayLegend) {
		this.displayLegend = displayLegend;
	}
	/**
	 * @return the zmax
	 */
	public double getZmax() {
		return zmax;
	}
	public void setZmax(double zmax) {
		this.zmax = zmax;
	}
	public String getSfileName() {
		return sfileName;
	}
	/**
	 * @param sfileName the sfileName to set
	 */
	public void setFileName(String sfileName) {
		this.sfileName = sfileName;
	}
	/**
	 * @return the xAxisTitle
	 */
	public String getXAxisTitle() {
		return xAxisTitle;
	}
	/**
	 * @param axisTitle the xAxisTitle to set
	 */
	public void setXAxisTitle(String axisTitle) {
		xAxisTitle = axisTitle;
	}
	public void setXAxisRage(double minValue, double maxValue)
	{
		xaxisAutoScale = false;
		xaxisRange[0] = minValue;
		xaxisRange[1] = maxValue;
	}
	/**
	 * @return the yAxisTitle
	 */
	public String getYAxisTitle() {
		return yAxisTitle;
	}
	/**
	 * @param axisTitle the yAxisTitle to set
	 */
	public void setYAxisTitle(String axisTitle) {
		yAxisTitle = axisTitle;
	}
	public void setYAxisRage(double minValue, double maxValue)
	{
		yaxisAutoScale = false;
		yaxisRange[0] = minValue;
		yaxisRange[1] = maxValue;
	}
	public void setZAxisTitle(String axisTitle) {
		zAxisTitle = axisTitle;
	}
	public void setZmin(double zmin) {
		this.zmin = zmin;
	}
	public void setNumZscaleTicks(int numZscaleTicks) {
		this.numZscaleTicks = numZscaleTicks;
	}
	public void setColorSpectrumScale(boolean colorSpectrumScale) {
		this.colorSpectrumScale = colorSpectrumScale;
		if (colorSpectrumScale) colorGrayScale = false;
	}
	public void setColorGrayScale(boolean colorGrayScale) {
		this.colorGrayScale = colorGrayScale;
		if (colorGrayScale) colorSpectrumScale = false;
	}
	public void setColorGrayScaleHue(double colorGrayScaleHue) {
		this.colorGrayScaleHue = colorGrayScaleHue;
	}
	public void setZaxisClip(boolean zaxisClip) {
		this.zaxisClip = zaxisClip;
	}
	/**
	 * @return the chartTitle
	 */
	public String getChartTitle() {
		return chartTitle;
	}
	/**
	 * @param chartTitle the chartTitle to set
	 */
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}
	/**
	 * @return the chartPixelWidth
	 */
	public int getChartPixelWidth() {
		return chartPixelWidth;
	}
	/**
	 * @param chartPixelWidth the chartPixelWidth to set
	 */
	public void setChartPixelWidth(int chartPixelWidth) {
		this.chartPixelWidth = chartPixelWidth;
	}
	/**
	 * @return the chartPixelHeight
	 */
	public int getChartPixelHeight() {
		return chartPixelHeight;
	}
	/**
	 * @param chartPixelHeight the chartPixelHeight to set
	 */
	public void setChartPixelHeight(int chartPixelHeight) {
		this.chartPixelHeight = chartPixelHeight;
	}
	public static void main(String[] args) throws IOException  
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
//  		zseries[0] = -1;
//  		zseries[1] = 1000;
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
   		sp.setZaxisClip(false);
   		sp.setDisplayLegend(false);
   		sp.setZminZmax(sp.findZminZmax());
   		sp.histEqZseries();
   		sp.makePngChart();
	}

}
