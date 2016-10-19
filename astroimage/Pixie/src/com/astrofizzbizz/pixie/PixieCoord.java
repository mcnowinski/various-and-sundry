package com.astrofizzbizz.pixie;

import nom.tam.fits.Header;

/**
 * @author mcginnis
 *
 */
public class PixieCoord 
{
	private int		row = 0;
	private int		col = 0;
	private double	ra = 0.0;
	private double	dec = 0.0;
	/**
	 * 
	 */
	public PixieCoord()
	{
		this.row = 0;
		this.col = 0;
		this.ra = 0.0;
		this.dec = 0.0;
	}
	/**
	 * @param row
	 * @param col
	 */
	public PixieCoord(int row, int col)
	{
		this.row = row;
		this.col = col;
		this.ra = 0.0;
		this.dec = 0.0;
	}
	/**
	 * @param c
	 */
	public PixieCoord(PixieCoord c)
	{
		this.row = c.row;
		this.col = c.col;
		this.ra = c.ra;
		this.dec = c.dec;
	}
	/**
	 * @return row
	 */
	public int getRow()
	{
		return row;
	}
	/**
	 * @return col
	 */
	public int getCol()
	{
		return col;
	}
	/**
	 * @return RA
	 */
	public double getRa()
	{
		return ra;
	}
	/**
	 * @return DEC
	 */
	public double getDec()
	{
		return dec;
	}
	/**
	 * @param row
	 */
	public void setRow(int row)
	{
		this.row = row;
	}
	/**
	 * @param col
	 */
	public void setCol(int col)
	{
		this.col = col;
	}
	/**
	 * @param ra
	 */
	public void setRa(double ra)
	{
		this.ra = ra;
	}
	/**
	 * @param dec
	 */
	public void setDec(double dec)
	{
		this.dec = dec;
	}
	/**
	 * @param hdr
	 */
	public void setRaDec(Header hdr)
	{
		double[] raDec = PixieImage.rowColToRaDec(hdr, row, col);
		ra = raDec[0];
		dec = raDec[1];
	}
	/**
	 * @return String
	 */
	public String getKey()
	{
		String skey = Integer.toString(row) + "," + Integer.toString(col);
		return skey;
	}
	/**
	 * @param skey
	 * @return boolean
	 */
	public boolean checkKey(String skey)
	{
		String[] scoord = skey.split(",");
		int crow = Integer.valueOf(scoord[0]);
		int ccol = Integer.valueOf(scoord[1]);
		boolean test = (row == crow) && (col == ccol);
		return test;
	}

}
