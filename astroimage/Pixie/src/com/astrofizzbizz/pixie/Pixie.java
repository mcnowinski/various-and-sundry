package com.astrofizzbizz.pixie;

/**
 * @author mcginnis
 *
 */
public class Pixie 
{
	/**
	 * @author mcginnis
	 *
	 */
	private double	compVal = 0.0;
	private static final int NUMBER_OF_COLORS = 3;
	private double[]	colorVal  = new double[NUMBER_OF_COLORS];
	private PixieCoord	coord = null;
	/**
	 * flag for the composite value
	 */
	public static final int COMP = -1;
	/**
	 * g band
	 */
	public static final int GBAND = 0;
	/**
	 * r band
	 */
	public static final int RBAND = 1;
	/**
	 * i band
	 */
	public static final int IBAND = 2;
	/**
	 * 
	 */
	public Pixie()
	{
		coord = new PixieCoord(); 
		compVal = 0.0;
		for (int icolor = 0; icolor < NUMBER_OF_COLORS; ++icolor) colorVal[icolor] = 0.0;
	}
	/**
	 * @param row
	 * @param col
	 * @param val
	 */
/*	public Pixie(int row, int col, double val)
	{
		coord = new PixieCoord(row, col); 
		this.compVal = val;
		for (int icolor = 0; icolor < NUMBER_OF_COLORS; ++icolor) colorVal[icolor] = 0.0;
	}
*/	/**
	 * @param p
	 */
	public Pixie(Pixie p)
	{
		coord = new PixieCoord(p.coord); 
		this.compVal = p.compVal;
		for (int icolor = 0; icolor < NUMBER_OF_COLORS; ++icolor) 
		{
			this.colorVal[icolor] = p.colorVal[icolor];
		}
	}
	/**
	 * @param c
	 * @param val
	 */
	public Pixie(PixieCoord c, double val)
	{
		coord = new PixieCoord(c); 
		this.compVal = val;
		for (int icolor = 0; icolor < NUMBER_OF_COLORS; ++icolor) colorVal[icolor] = 0.0;
	}
	/**
	 * @return row
	 */
	public int getRow()
	{
		return coord.getRow();
	}
	/**
	 * @return col
	 */
	public int getCol()
	{
		return coord.getCol();
	}
	/**
	 * @return Coord
	 */
	public PixieCoord getCoord()
	{
		return coord;
	}
	/**
	 * @return value
	 */
	public double getCompVal()
	{
		return compVal;
	}
	/**
	 * @param icolor
	 * @return color value;
	 */
	public double getColorVal(int icolor)
	{
		if ((icolor < 0) || (icolor >= NUMBER_OF_COLORS)) return 0.0;
		return colorVal[icolor];
	}
	/**
	 * @param row
	 */
	public void setRow(int row)
	{
		coord.setRow(row);
	}
	/**
	 * @param col
	 */
	public void setCol(int col)
	{
		coord.setCol(col);
	}
	/**
	 * @param val
	 */
	public void setCompVal(double val)
	{
		this.compVal = val;
	}
	/**
	 * @param icolor
	 * @param val
	 */
	public void setColorVal(int icolor, double val)
	{
		if (icolor == COMP) compVal = val; 
		if (icolor == GBAND) colorVal[GBAND] = val; 
		if (icolor == RBAND) colorVal[RBAND] = val; 
		if (icolor == IBAND) colorVal[IBAND] = val; 
	}
	/**
	 * @return Integer
	 */
	public String getKey()
	{
		return coord.getKey();
	}
	/**
	 * @param skey
	 * @return boolean
	 */
	public boolean checkKey(String skey)
	{
		return coord.checkKey(skey);
	}
}
