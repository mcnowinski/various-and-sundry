package com.astrofizzbizz.pixie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.ImageHDU;
import nom.tam.image.ImageTiler;
import nom.tam.util.BufferedFile;

/**
 * @author mcginnis
 *
 */
public class PixieImage 
{
	private double[][] pix = null;
	/**
	 * 
	 */
	private Header header = null;
	
	private int iRowCenter = 0;
	private int iColCenter = 0;
	public void setRowCenter(int rowCenter) {
		iRowCenter = rowCenter;
	}
	public void setColCenter(int colCenter) {
		iColCenter = colCenter;
	}
	public int getRowCenter() {
		return iRowCenter;
	}
	public int getColCenter() {
		return iColCenter;
	}
	
	PixieImage()
	{
		pix = null;
		header = new Header();
	}
	/**
	 * @param pi
	 */
	public PixieImage(PixieImage pi)
	{
		this();
		int	ii, ij;
		int	nrow = 0;
		int ncol = 0;
		nrow = pi.getRowCount();
		ncol = pi.getColCount();
		if ((nrow > 0) && (ncol > 0))
		{
			pix = new double[nrow][ncol];
			for (ii = 0; ii < nrow; ++ii)
			{
				for (ij = 0; ij < ncol; ++ij)
				{
					pix[ii][ij] = pi.pix[ii][ij];
				}
			}
		}
        copyHeaderCards(pi.header);

	}
	public int getBitPix()
	{
		return header.getIntValue("BITPIX");
	}
	/**
	 * @param nrow
	 * @param ncol
	 */
	public PixieImage(int nrow, int ncol)
	{
		this();
		int	ii, ij;
		if ((nrow > 0) && (ncol > 0))
		{
			pix = new double[nrow][ncol];
			for (ii = 0; ii < nrow; ++ii)
			{
				for (ij = 0; ij < ncol; ++ij)
				{
					pix[ii][ij] = 0.0;
				}
			}
		}
	}
	/**
	 * @param pmax
	 */
	public PixieImage(Pixie pmax)
	{
		this(pmax.getRow() + 1, pmax.getCol() + 1);
	}
	/**
	 * @param pmax
	 */
	public PixieImage(PixieCoord pmax)
	{
		this(pmax.getRow() + 1, pmax.getCol() + 1);
	}
	/**
	 * @param fitsFileName
	 * @throws PixieImageException
	 */
	public PixieImage(String fitsFileName) throws PixieImageException
	{
		this();
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
			Header hdr = hdu.getHeader();
			int ibitpix = hdr.getIntValue("BITPIX");
			Object kernal = hdu.getData().getKernel();
// changed from !=2 to handle greenbank NRAO files
			int naxis = hdr.getIntValue("NAXIS");
			if (naxis < 2) 
			{
				fits = null;
				hdu = null;
				hdr = null;
				throw  new FitsException(null); 
			}
			//System.out.println(hdr.getNumberOfCards());
			//for(int i=0; i<hdr.getNumberOfCards(); i++) {
			//	System.out.println(hdr.getCard(i));
			//}
//			if (hdr.getIntValue("BITPIX") == -32) throw  new PixieImageException(); 
			if (hdr.getIntValue("BITPIX") == -16) throw  new PixieImageException(); 
			int ii, ij;
			if (ibitpix == 8)
			{
				char[][] cimage = (char[][]) kernal;
				int nrow = cimage.length;
				int ncol = cimage[0].length;
				pix = new double[nrow][ncol];
				for (ii = 0; ii < nrow; ++ii)
					for (ij = 0; ij < ncol; ++ij)
						pix[ii][ij] = (double) cimage[ii][ij] + 128.0;
				cimage = null;
			}
			if (ibitpix == 16)
			{
				short[][] simage = (short[][]) kernal;
				int nrow = simage.length;
				int ncol = simage[0].length;
				pix = new double[nrow][ncol];
				for (ii = 0; ii < nrow; ++ii)
					for (ij = 0; ij < ncol; ++ij)
						pix[ii][ij] = ((double) simage[ii][ij]) + 32768.0;
//						pix[ii][ij] = ((double) simage[ii][ij]);
				simage = null;
			}
			if (ibitpix == 32)
			{
				int[][] iimage = (int[][]) kernal;
				int nrow = iimage.length;
				int ncol = iimage[0].length;
				pix = new double[nrow][ncol];
				for (ii = 0; ii < nrow; ++ii)
					for (ij = 0; ij < ncol; ++ij)
						pix[ii][ij] = (double) iimage[ii][ij] + 32768.0;// + 2147483648.0;
				iimage = null;
			}
			if (ibitpix == -32)
			{
				float[][] fimage = null;
				if (naxis == 2) fimage = (float[][]) kernal;
				if (naxis == 4)
				{
					float[][][][] kernal4D = (float[][][][]) kernal;
					fimage  = kernal4D[0][0];
				}
				int nrow = fimage.length;
				int ncol = fimage[0].length;
				pix = new double[nrow][ncol];
				for (ii = 0; ii < nrow; ++ii)
					for (ij = 0; ij < ncol; ++ij)
						pix[ii][ij] = (double) fimage[ii][ij];// + 32768.0;// + 2147483648.0;
				fimage = null;
			}
			copyHeaderCards(hdu.getHeader());
			fits = null;
			hdu = null;
			hdr = null;
		}
		catch (FitsException e)
		{
			e.printStackTrace();
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}

	}
	/**
	 * @param fitsFile
	 * @throws PixieImageException
	 */
	public PixieImage(File fitsFile) throws PixieImageException
	{
		this(fitsFile.getPath());
	}
	/**
	 * @param fitsFileName
	 * @param centerRow
	 * @param centerCol
	 * @param nrows
	 * @param ncols
	 * @throws PixieImageException
	 */
	public PixieImage(String fitsFileName, int centerRow, int centerCol, int nrows, int ncols) throws PixieImageException 
	{
		this();
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
	        Header hdr = hdu.getHeader();
			int maxRows = hdr.getIntValue("NAXIS2");
			int maxCols = hdr.getIntValue("NAXIS1");
			boolean goodParam = true;
			if (centerRow > (maxRows - 1)) goodParam = false;
			if (centerCol > (maxCols - 1)) goodParam = false;
			if (centerRow < 1) goodParam = false;
			if (centerCol < 1) goodParam = false;
			if (hdr.getIntValue("NAXIS") != 2) goodParam = false;
			int ibitpix = hdr.getIntValue("BITPIX");
			if (!((hdr.getIntValue("BITPIX") == -32) || (hdr.getIntValue("BITPIX") == 16))) goodParam = false;
			if(!goodParam)
			{
				fits = null;
				hdu = null;
				hdr = null;
				throw  new FitsException(null); 
			}
			if ((centerRow + nrows / 2) >= maxRows) nrows = 2 * (maxRows - centerRow) - 1;
			if ((centerCol + ncols / 2) >= maxCols) ncols = 2 * (maxCols - centerCol);
			int startRow = centerRow - nrows / 2;
			int startCol = centerCol - ncols / 2;
			ImageTiler imageTiler = hdu.getTiler();
			pix = new double[nrows][ncols];
			if (ibitpix == 8)
			{
				char[] ctile = new char[nrows * ncols];
				imageTiler.getTile(ctile, new int[]{startRow,startCol}, new int[]{nrows,ncols});
				for (int ii = 0; ii < ncols; ++ii) 
				    for (int ij = 0; ij < nrows; ++ij) 
				    	pix[ij][ii] = ((double) ctile[ii + ij * ncols]) + 128.0;
				ctile = null;
			}
			if (ibitpix == 16)
			{
				short[] stile = new short[nrows * ncols];
				imageTiler.getTile(stile, new int[]{startRow,startCol}, new int[]{nrows,ncols});
				for (int ii = 0; ii < ncols; ++ii) 
				    for (int ij = 0; ij < nrows; ++ij) 
				    	pix[ij][ii] = ((double) stile[ii + ij * ncols]) + 32768.0;
				stile = null;
			}
			if (ibitpix == 32)
			{
				int[] itile = new int[nrows * ncols];
				imageTiler.getTile(itile, new int[]{startRow,startCol}, new int[]{nrows,ncols});
				for (int ii = 0; ii < ncols; ++ii) 
				    for (int ij = 0; ij < nrows; ++ij) 
				    	pix[ij][ii] = ((double) itile[ii + ij * ncols]);
				itile = null;
			}
			if (ibitpix == -32)
			{
				float[] ftile = new float[nrows * ncols];
				imageTiler.getTile(ftile, new int[]{startRow,startCol}, new int[]{nrows,ncols});
				for (int ii = 0; ii < ncols; ++ii) 
				    for (int ij = 0; ij < nrows; ++ij) 
				    	pix[ij][ii] = (double) ftile[ii + ij * ncols];
				ftile = null;
			}
			if (ibitpix == -64)
			{
				double[] dtile = new double[nrows * ncols];
				imageTiler.getTile(dtile, new int[]{startRow,startCol}, new int[]{nrows,ncols});
				for (int ii = 0; ii < ncols; ++ii) 
				    for (int ij = 0; ij < nrows; ++ij) 
				    	pix[ij][ii] = (double) dtile[ii + ij * ncols];
				dtile = null;
			}
			copyHeaderCards(hdu.getHeader());
			try
			{
				int[] rowCol = new int[2];
				rowCol[0] = startRow + nrows / 2;
				rowCol[1] = startCol + ncols / 2;
				double[] raDec = rowColToRaDec(rowCol[0], rowCol[1]);
				header.addValue("CRVAL1",raDec[0],"RA at Reference Pixel");
				header.addValue("CRVAL2",raDec[1],"DEC at Reference Pixel");
				header.addValue("CRPIX1",(double) (ncols / 2),"Column Pixel Coordinate of Ref. Pixel");
				header.addValue("CRPIX2",(double) (nrows / 2),"Row Pixel Coordinate of Ref. Pixel");
			}
			catch (HeaderCardException e)
			{
	    		System.out.println("Problem changing Fits Header in PixieImage");
			}
	 		fits = null;
			hdu = null;
			hdr = null;
			imageTiler = null;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	/**
	 * @param fitsFile
	 * @param centerRow
	 * @param centerCol
	 * @param nrows
	 * @param ncols
	 * @throws PixieImageException
	 */
	public PixieImage(File fitsFile, int centerRow, int centerCol, int nrows, int ncols) throws PixieImageException 
	{
		this(fitsFile.getPath(), centerRow, centerCol, nrows, ncols);
	}
	/**
	 * @param comment
	 */
	@SuppressWarnings("rawtypes")
	public void addCommentToHeaderEnd(String comment)
	{
		Iterator it = header.iterator();
		while (it.hasNext()) 
		{
			it.next();
		}
		try
		{
			header.insertComment(comment);
		}
		catch (HeaderCardException e)
		{
    		System.out.println("Problem changing Fits Header in PixieImage");
		}
	}
	/**
	 * @return int
	 */
	public int getRowCount()
	{
		int nrow = 0;
		if (pix != null)
		{
			nrow = pix.length;
		}
		return	nrow;
	}
	/**
	 * @param fitsFileName
	 * @return int
	 * @throws PixieImageException
	 */
	public static int getRowCount(String fitsFileName) throws PixieImageException
	{
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
	        Header hdr = hdu.getHeader();
			int maxRows = hdr.getIntValue("NAXIS2");
			fits = null;
			hdu = null;
			hdr = null;
			return maxRows;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	/**
	 * @param fitsFile
	 * @return int
	 * @throws PixieImageException
	 */
	public static int getRowCount(File fitsFile) throws PixieImageException
	{
		return getRowCount(fitsFile.getPath());
	}
	/**
	 * @param fitsFileName
	 * @return int
	 * @throws PixieImageException
	 */
	public static int getColCount(String fitsFileName) throws PixieImageException
	{
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
	        Header hdr = hdu.getHeader();
			int maxCols = hdr.getIntValue("NAXIS1");
			fits = null;
			hdu = null;
			hdr = null;
			return maxCols;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	/**
	 * @param ra
	 * @param dec
	 * @return int
	 */
	public int[] rADecToRowCol(double ra, double dec) 
	{
        int[] rowCol = rADecToRowCol(header, ra, dec);
		return rowCol;
	}
	/**
	 * @param fitsFileName
	 * @param ra
	 * @param dec
	 * @return int[]
	 * @throws PixieImageException
	 */
	public static int[] rADecToRowCol(String fitsFileName, double ra, double dec) throws PixieImageException
	{
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
	        Header hdr = hdu.getHeader();
	        int[] rowCol = rADecToRowCol(hdr, ra, dec);
			fits = null;
			hdu = null;
			hdr = null;
			return rowCol;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	private static int[] rADecToRowCol(Header hdr, double ra, double dec) 
	{
        double[][] CD = new double[2][2];
        double[][] CD_INV = new double[2][2];
        double[] CRPIX = new double[2];
        double[] CRVAL = new double[2];
        double[] colRow = new double[2];
        double[] raDec = new double[2];
        int[] rowCol = new int[2];
        getRaDecMatrix(hdr, CD, CD_INV, CRPIX, CRVAL);
        
        raDec[0]= (ra - CRVAL[0]) * Math.cos(3.141592654 * dec / 180.0);
        raDec[1] = (dec - CRVAL[1]);
        
        for (int ii = 0; ii < 2; ++ii)
        {
        	colRow[ii] = CRPIX[ii];
        	for (int ij = 0; ij < 2; ++ij)
        	{
        		colRow[ii] = colRow[ii] + CD_INV[ii][ij] * raDec[ij];
        	}
        }
        
        rowCol[0] = (int) colRow[1];
        rowCol[1] = (int) colRow[0];
        return rowCol;
	}
	/**
	 * @param row
	 * @param col
	 * @return double[]
	 */
	public double[] rowColToRaDec(int row, int col) 
	{
        double[] raDec = rowColToRaDec(header, row, col);
 		return raDec;
	}
	/**
	 * @param fitsFileName
	 * @param row
	 * @param col
	 * @return double[]
	 * @throws PixieImageException
	 */
	public static double[] rowColToRaDec(String fitsFileName, int row, int col) throws PixieImageException
	{
		try
		{
			Fits fits = new Fits(fitsFileName);
			ImageHDU hdu = (ImageHDU) fits.readHDU();
	        Header hdr = hdu.getHeader();
	        double[] raDec = rowColToRaDec(hdr, row, col);
	        
			fits = null;
			hdu = null;
			hdr = null;
			return raDec;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	/**
	 * @param hdr
	 * @param row
	 * @param col
	 * @return ra and dec in an array
	 */
	public static double[] rowColToRaDec(Header hdr, int row, int col) 
	{
        double[][] CD = new double[2][2];
        double[][] CD_INV = new double[2][2];
        double[] CRPIX = new double[2];
        double[] CRVAL = new double[2];
        double[] colRow = new double[2];
        double[] raDec = new double[2];
        getRaDecMatrix(hdr, CD, CD_INV, CRPIX, CRVAL);
        
        colRow[0] = (double) col;
        colRow[1] = (double) row;
        
        for (int ii = 0; ii < 2; ++ii)
        {
        	raDec[ii] = 0.0;
         	for (int ij = 0; ij < 2; ++ij)
        	{
        		raDec[ii] = raDec[ii] + CD[ii][ij]*(colRow[ij] - CRPIX[ij]);
        	}
        }
        raDec[1] = CRVAL[1] + raDec[1];
        raDec[0] = CRVAL[0] + raDec[0] / Math.cos(3.141592654 * raDec[1] / 180.0);
      	return raDec;
	}
	private static void getRaDecMatrix(Header hdr, double[][] CD, double[][] CD_INV, double[] CRPIX, double[] CRVAL)
	{
        CD[0][0] = hdr.getDoubleValue("CD1_1");
        CD[0][1] = hdr.getDoubleValue("CD1_2");
        CD[1][0] = hdr.getDoubleValue("CD2_1");
        CD[1][1] = hdr.getDoubleValue("CD2_2");
        CRPIX[0] = hdr.getDoubleValue("CRPIX1");
        CRPIX[1] = hdr.getDoubleValue("CRPIX2");
        CRVAL[0] = hdr.getDoubleValue("CRVAL1");
        CRVAL[1] = hdr.getDoubleValue("CRVAL2");
        
        CD_INV[0][0] =  CD[1][1] / (CD[0][0] * CD[1][1] - CD[1][0] * CD[0][1]);
        CD_INV[1][1] =  CD[0][0] / (CD[0][0] * CD[1][1] - CD[1][0] * CD[0][1]);
        CD_INV[1][0] = -CD[1][0] / (CD[0][0] * CD[1][1] - CD[1][0] * CD[0][1]);
        CD_INV[0][1] = -CD[0][1] / (CD[0][0] * CD[1][1] - CD[1][0] * CD[0][1]);
       
	}
	/**
	 * @param fitsFile
	 * @return int
	 * @throws PixieImageException
	 */
	public static int getColCount(File fitsFile) throws PixieImageException
	{
		return getColCount(fitsFile.getPath());
	}
	/**
	 * @return int
	 */
	public int getColCount()
	{
		int ncol = 0;
		if (pix != null)
		{
			ncol = pix[0].length;
		}
		return	ncol;
	}
	/**
	 * @param pi
	 * @return boolean
	 */
	public boolean iValidCoord(Pixie pi)
	{
		boolean		iok;
		Pixie pmax = getURHCPixie();
	
		iok = false;
		if ( (0 <= pi.getRow()) && ( pi.getRow() <= pmax.getRow()) )
		{
			if ( (0 <= pi.getCol()) && ( pi.getCol() <= pmax.getCol()) )
			{
				iok = true;
			}
		}
		return iok;
	}
	/**
	 * returns the upper right hand pixie
	 * @return Pixie
	 */
	public Pixie getURHCPixie()
	{
		Pixie pmax = new Pixie();
		pmax.setRow(getRowCount() - 1);
		pmax.setCol(getColCount() - 1);
		pmax.setCompVal(pix[pmax.getRow()][pmax.getCol()]);
		return pmax;
	}
	/**
	 * Finds the pixie in the image with the maximum image value
	 * @return Pixie
	 */
	public Pixie getMaxValPixie()
	{
		double	max = pix[0][0];
		int		imax = 0;
		int		jmax = 0;
		for (int ii = 0; ii < getRowCount(); ++ii)
		{
			for (int ij = 0; ij < getColCount(); ++ij)
			{
				if (max < pix[ii][ij])
				{
					imax = ii;
					jmax = ij;
					max = pix[ii][ij];
				}
			}
		}
		Pixie pmaxVal = new Pixie(new PixieCoord(imax,jmax), max);
		return pmaxVal;
	}
	/**
	 * @return double[][]
	 */
	public double[][] getPix()
	{
		return pix;
	}
	/**
	 * @param p
	 * @return double
	 */
	public double getPixieValue(Pixie p)
	{
		double val = -1.0;
		if ( (0 <= p.getRow()) && (p.getRow() <= getRowCount()) )
		{
			if ( (0 <= p.getCol()) && (p.getCol() <= getColCount()) )
			{
				val = pix[p.getRow()][p.getCol()];
			}
		}
		return val;
	}
	/**
	 * @param p
	 */
	public void setPixieValue(Pixie p)
	{
		if ( (0 <= p.getRow()) && (p.getRow() <= getRowCount()) )
		{
			if ( (0 <= p.getCol()) && (p.getCol() <= getColCount()) )
			{
				pix[p.getRow()][p.getCol()] = p.getCompVal();
			}
		}
	}
	/**
	 * @param headerIn
	 */
	@SuppressWarnings("rawtypes")
	public void copyHeaderCards(Header headerIn) 
	{
		Iterator it = headerIn.iterator();
		while (it.hasNext()) 
		{
			HeaderCard headerCard = (HeaderCard) it.next();
			header.addLine(headerCard);
		}
	}
	@SuppressWarnings("rawtypes")
	private void appendHeaderCards(Header headerOut, Header headerIn)
	{
		Iterator it = headerIn.iterator();
		boolean okayCardToAdd = true;
		while (it.hasNext()) 
		{
			HeaderCard headerCard = (HeaderCard) it.next();
			okayCardToAdd = true;
			if( headerCard.getKey().equals("SIMPLE")) okayCardToAdd = false;
			if( headerCard.getKey().equals("BITPIX")) okayCardToAdd = false;
			if( headerCard.getKey().equals("NAXIS")) okayCardToAdd = false;
			if( headerCard.getKey().equals("NAXIS1")) okayCardToAdd = false;
			if( headerCard.getKey().equals("NAXIS2")) okayCardToAdd = false;
			if( headerCard.getKey().equals("EXTEND")) okayCardToAdd = false;
			if (okayCardToAdd) headerOut.addLine(headerCard);
		}
	}
	/**
	 * @param key
	 * @param value
	 * @param comment
	 * @throws PixieImageException
	 */
	public void addHeadercard(String key, double value, String comment) throws PixieImageException
	{
		try
		{
			header.addLine(new HeaderCard(key,value,comment));
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
	}
	public void writeToFitsFile(String fitsFileName)  throws PixieImageException
	{
		try
		{
			if (pix == null) throw new FitsException(null);
			int ibitpix = header.getIntValue("BITPIX");
//			if (ibitpix == -32) throw  new PixieImageException(); 
			if (ibitpix == -16) throw  new PixieImageException(); 
//Make all images save in int form
// changed my mind for NRAO
			if (ibitpix > 0) ibitpix = 32;
			int nrow = pix.length;
			int ncol = pix[0].length;
			Fits fits = new Fits();
			int[][] iimage = null;
			ImageHDU hdu = null;
			double pixVal;
			if(ibitpix == 32)
			{
				iimage = new int[nrow][ncol];
				for (int ii = 0; ii < nrow; ++ii)
				{
					for (int ij = 0; ij < ncol; ++ij)
					{
//to make it display right with DS9
						pixVal = pix[ii][ij] - 32768.0;// - 2147483648.0;
						if (pixVal < -2147483648.0) pixVal = -2147483648.0;
						if (pixVal > 2147483647.0) pixVal = 2147483647.0;
						iimage[ii][ij] = (int) (pixVal);
					}
				}
				hdu = (ImageHDU) Fits.makeHDU(iimage);
			}
//  for NRAO
			if(ibitpix == -32)
			{
				int naxis = header.getIntValue("NAXIS");
				if (naxis == 2)
				{
					float[][] fimage = null;
					fimage = new float[nrow][ncol];
					for (int ii = 0; ii < nrow; ++ii)
					{
						for (int ij = 0; ij < ncol; ++ij)
						{
							pixVal = pix[ii][ij];
							fimage[ii][ij] = (float) (pixVal);
						}
					}
					hdu = (ImageHDU) Fits.makeHDU(fimage);
				}
				if (naxis == 4)
				{
					float[][][][] fimage = null;
					fimage = new float[1][1][nrow][ncol];
					for (int ii = 0; ii < nrow; ++ii)
					{
						for (int ij = 0; ij < ncol; ++ij)
						{
							pixVal = pix[ii][ij];
							fimage[0][0][ii][ij] = (float) (pixVal);
						}
					}
					hdu = (ImageHDU) Fits.makeHDU(fimage);
				}

			}
			Header hdr = hdu.getHeader();
			appendHeaderCards(hdr, header);
			fits.addHDU(hdu);
			BufferedFile bf = new BufferedFile(fitsFileName, "rw");
			fits.write(bf);
			bf.flush();
			bf.close();
			bf = null;
			fits = null;
			iimage = null;
			hdu = null;
		}
		catch (FitsException e)
		{
			throw new PixieImageException(e);
		}
		catch (IOException e)
		{
			throw new PixieImageException(e);
		}
	}
	/**
	 * @param fitsFile
	 * @param shortFormat
	 * @throws PixieImageException
	 */
	public void writeToFitsFile(File fitsFile)  throws PixieImageException
	{
		writeToFitsFile(fitsFile.getPath());
	}
	/**
	 * @return double[]
	 */
	public 	double[] findMinMaxSumMean()
	{
		int			totpix;
		int			ii, ij;
		int		nrows;
		int		ncols;
		double[][]		pix = null;
		double				imageMin;
		double				imageMax;
		double				imageMean = 0.0;
		double				imageSum;
// Used for keeping away from margins when coadding images
		int edge = 0;

		nrows = getRowCount();
		ncols = getColCount();
		pix = getPix();

		totpix = nrows * ncols;
		imageSum = 0.0;
		imageMin =  1.E33;
		imageMax = -1.E33;

		for (ii = edge; ii < nrows - edge; ii++)
		{  
			for (ij = edge; ij < ncols - edge; ij++) 
			{
				if (pix[ii][ij] > 0.1)
				{
					imageSum += pix[ii][ij];                      
					if (pix[ii][ij] < imageMin) imageMin = pix[ii][ij]; 
					if (pix[ii][ij] > imageMax) imageMax = pix[ii][ij];  
				}
				else
				{
					pix[ii][ij] = 0.0;
				}
			}
		}
		if (totpix > 0) imageMean = imageSum / ((double) totpix);
		double[] minMaxSumMean = new double[4];
		minMaxSumMean[0] = imageMin;
		minMaxSumMean[1] = imageMax;
		minMaxSumMean[2] = imageSum;
		minMaxSumMean[3] = imageMean;
		return minMaxSumMean;
	}
	/**
	 * @return double[]
	 */
	public double[] findNoiseMeanSigma()
	{
		int				ii;
		int				ij;
		double			count;
		int				itry;
		double			sigmaOld;
// Used for keeping away from margins when coadding images
		int edge = 25;

		int nrows = getRowCount();
		int ncols = getColCount();
		double[][] pix = getPix();

		double[] minMaxSumMean = findMinMaxSumMean();
		double imageMin = minMaxSumMean[0];
		double imageMax = minMaxSumMean[1];
		double imageMean = minMaxSumMean[3];
		
		if ((imageMean - imageMin) > 100.0) imageMin = imageMean - 100;
		if (imageMin < 1.0 ) imageMin = 1.0;

		sigmaOld = 1.0;
		double imageNoiseSigma = 2000.0;
		double imageNoiseMean = 0.0;
		itry = 0;
		while ((itry < 20) 
			&& (Math.abs(sigmaOld - imageNoiseSigma) > (0.02 * sigmaOld)))
		{
			sigmaOld = imageNoiseSigma;
			imageNoiseMean = 0.0;
			count = 0.0;
			for ( ii = edge; ii < nrows - edge; ++ii)
			{
				for (ij= edge; ij < ncols - edge; ++ij)
				{
					if ( (imageMin <= pix[ii][ij]) && (pix[ii][ij] <= imageMax) )
					{
						imageNoiseMean = imageNoiseMean + pix[ii][ij];
						count = count + 1.0;
					}
				}
			}
			imageNoiseMean = imageNoiseMean / count;
			imageNoiseSigma = 0.0;
			for ( ii = edge; ii < nrows - edge; ++ii)
			{
				for (ij= edge; ij < ncols - edge; ++ij)
				{
					if ( (imageMin <= pix[ii][ij]) && (pix[ii][ij] <= imageMax) )
					{
						imageNoiseSigma = imageNoiseSigma 
							+ (pix[ii][ij] - imageNoiseMean)
							* (pix[ii][ij] - imageNoiseMean);
					}
				}
			}
			imageNoiseSigma = Math.sqrt(imageNoiseSigma / count);
			imageMin = imageNoiseMean - 3.0 * imageNoiseSigma;
			if (imageMin < 1.0 ) imageMin = 1.0;
			imageMax = imageNoiseMean + 3.0 * imageNoiseSigma;
			itry = itry + 1;
		}
		double[] noiseMeanSigma = new double[2];
		noiseMeanSigma[0] = imageNoiseMean;
		noiseMeanSigma[1] = imageNoiseSigma;
		return noiseMeanSigma;
	}
	/**
	 * @param scale
	 * @param offset
	 */
	public void rescalePixels(double scale, double offset)
	{
		int		ii;
		int		ij;

		int nrows = getRowCount();
		int ncols = getColCount();
		double[][] pix = getPix();

		for (ii = 0; ii < nrows; ii++)
		{  
			for (ij = 0; ij < ncols; ij++) 
			{
				pix[ii][ij] = scale * (pix[ii][ij] + offset);
			}
		}		
	}
	/**
	 * @param fileName
	 * @param colorChar 
	 * @return int
	 */
	static public int findColorCharIndexInFileName(String fileName, char colorChar)
	{
		int		ii;
		int		colorCharIndex;
		colorCharIndex = 0;
		for (ii = 0; ii < fileName.length(); ++ii)
		{
			if(fileName.charAt(ii) == colorChar) colorCharIndex = ii;
		}
		return colorCharIndex;
	}
	/**
	 * @param fileName1
	 * @param fileName2
	 * @return int
	 */
	static public int findColorCharIndexInFileName(String fileName1, String fileName2)
	{
		int		ii;
		int		colorCharIndex;
		int		difCount;
		char	ctest1;
		char	ctest2;
		
		colorCharIndex = 0;
		difCount = 0;
		
		for (ii = 0; ii < fileName1.length(); ++ii)
		{
			ctest1 = fileName1.charAt(ii);
			ctest2 = fileName2.charAt(ii);
			
			if ( ctest1 != ctest2) 
			{
				difCount = difCount + 1;
				colorCharIndex = ii;
			}
		}
		if(difCount > 1) colorCharIndex = 0;
		return colorCharIndex;
	}
	/**
	 * @param inputFileName
	 * @param colorChar
	 * @param colorCharIndex
	 * @return String
	 */
	static public String writeColorCharIntoFileName(String inputFileName, String colorChar, int colorCharIndex)
	{
		String 	outputFileName = null;
		int		fileNameLength;
		String[]	fileNamePart = new String[3];
		fileNameLength = inputFileName.length();
		fileNamePart[0] = inputFileName.substring(0, colorCharIndex);
		fileNamePart[1] = colorChar;
		fileNamePart[2] = inputFileName.substring(colorCharIndex + 1, fileNameLength);
		outputFileName = fileNamePart[0] + fileNamePart[1] + fileNamePart[2];
		return outputFileName;
	}
	/**
	 * @return fits header
	 */
	public Header getHeader()
	{
		return header;
	}
	/**
	 * @param inputFileName
	 * @param outputFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void unGzipFile(String inputFileName, String outputFileName) throws FileNotFoundException, IOException
	{
		// Open the gzip file
		GZIPInputStream gzipInputStream =
			new GZIPInputStream(new FileInputStream(inputFileName));
		// Open the output file
		OutputStream out = new FileOutputStream(outputFileName);

		// Transfer bytes from the compressed file to the output file
		byte[] buf = new byte[1024];
		int len;
		while ((len = gzipInputStream.read(buf)) > 0) 
		{
			out.write(buf, 0, len);
		}
		// Close the file and stream
		gzipInputStream.close();
		out.close();
	}
	public static void gzipFile(String inFilename, String gzipFileName) throws FileNotFoundException, IOException
	{
		// Specify gzip file name
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipFileName));
		// Specify the input file to be compressed
		FileInputStream in = new FileInputStream(inFilename);
		// Transfer bytes from the input file 
		// to the gzip output stream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) 
		{
			out.write(buf, 0, len);
		}
		in.close();
		// Finish creation of gzip file
		out.finish();
		out.close();
	}
	/**
	 * @param file
	 * @return
	 */
	public static String getFileExtension(File file)
	{
		return getFileExtension(file.getName());
	}
	/**
	 * @param fileName
	 * @return
	 */
	public static String getFileExtension(String fileName)
	{
		int ilen = fileName.length();
		int ii = ilen - 1;
		int iperiod = -1;
		boolean periodFound = false;
		while ((ii >= 0) && !periodFound)
		{
			if (fileName.charAt(ii) == '.')
			{
				iperiod = ii;
				periodFound = true;
			}
			else
			{
				ii = ii - 1;
			}
		}
		if (iperiod < 0) return null;
		String extension = fileName.substring(iperiod + 1, ilen);
		return extension;
	}
	public static void fitsToPng(String fitsFileName, String pngFileName, int scaleType) throws PixieImageException
	{
		PixieImage pi = new PixieImage(fitsFileName);
		PixieImageRGBPlotter plotter = new PixieImageRGBPlotter();
		if (scaleType == 0) plotter.setScaleType("linear");
		if (scaleType == 1) plotter.setScaleType("log");
		if (scaleType == 2) plotter.setScaleType("hist");
		plotter.setAutoScale(true);
		plotter.setImages(pi);
		plotter.setPixelValueLimits(pi);
		plotter.setScaleTable(pi);
		plotter.toPNGFile(pngFileName);
		
	}
	public static void main(String[] args) throws PixieImageException 
	{
		int iarg = 0;
		String command = args[iarg++];
		if (command.equals("fitsToPng"))
		{
			String inputFile = args[iarg++];
			String outputFile = args[iarg++];
			int itype = Integer.parseInt(args[iarg++]);
			PixieImage.fitsToPng(inputFile, outputFile, itype);
		}
	}
}
