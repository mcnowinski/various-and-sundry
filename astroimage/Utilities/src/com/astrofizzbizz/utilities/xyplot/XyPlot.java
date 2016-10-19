package com.astrofizzbizz.utilities.xyplot;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XyPlot {

	public static void makeXYChart(
			double[] xdata, 
			double[] ydata, 
			String title, 
			String horzTitle, 
			String vertTitle, 
			String fileName, 
			boolean scatterPlot, 
			boolean logPlot,
			double minYValue) throws IOException
	{
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries series = new XYSeries("Test");
		for (int ii = 0; ii < xdata.length; ++ii)
		{
			series.add(xdata[ii], ydata[ii]);
		}
		xySeriesCollection.addSeries(series);
		JFreeChart chart = null;
		if (scatterPlot)
		{
			chart = ChartFactory.createScatterPlot(
					title, 
					horzTitle, 
					vertTitle, 
					xySeriesCollection, 
					PlotOrientation.VERTICAL, 
					false, 
					false, 
					false);
		}
		else
		{
			chart = ChartFactory.createXYLineChart(
		            title,
		            horzTitle,
		            vertTitle,
		            xySeriesCollection,
		            PlotOrientation.VERTICAL,
		            true,
		            true,
		            false
	        );
		}
	    XYPlot plot = (XYPlot) chart.getPlot();
//	    ValueAxis rangeAxis = plot.getRangeAxis();
	    if (logPlot)
	    {
		    LogAxis rangeAxis = new LogAxis();
		    rangeAxis.setLabel(vertTitle);
		    rangeAxis.setSmallestValue(minYValue);
			plot.setRangeAxis(rangeAxis);
	    }
	    int chartPixelWidth = 800;
	    int chartPixelHeight = 600;
		ChartUtilities.saveChartAsPNG(new File(fileName), chart, chartPixelWidth, chartPixelHeight);
	}
	public static void main(String[] args) 
	{
	}

}
