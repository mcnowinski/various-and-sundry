package com.astrofizzbizz.utilities.spectrogramPlotter;

import org.jfree.data.xy.AbstractXYZDataset;

public class SpectrogramXYZDataset extends AbstractXYZDataset
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4172334269759724916L;
	private double[] xseries;
	private double[] yseries;
	private double[] zseries;
	SpectrogramXYZDataset(double[] xseries, double[] yseries, double[] zseries)
	{
		this.xseries = xseries;
		this.yseries = yseries;
		this.zseries = zseries;
	}
	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable<String> getSeriesKey(int series) 
	{
		return "myXYZDataset";
	}

	public Number getZ(int series, int item) 
	{
			return new Double(zseries[item]);
	}

	public int getItemCount(int series) 
	{
		return zseries.length;
	}

	public Number getX(int series, int item) {
		return new Double(xseries[item]);
	}

	public Number getY(int series, int item) {
		return  new Double(yseries[item]);
	}

}
