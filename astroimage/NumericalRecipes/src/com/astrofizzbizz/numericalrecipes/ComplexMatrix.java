package com.astrofizzbizz.numericalrecipes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * @author mcginnis
 *
 */
public class ComplexMatrix implements Serializable
{
	private static final long serialVersionUID = 8603095864916455077L;
	public Complex[][] cell = null;
	public ComplexMatrix(int nrow, int ncol)
	{
		cell = new Complex[nrow][ncol];
		for (int ii = 0; ii < nrow; ++ii)
		{
			for (int ij = 0; ij < ncol; ++ij)
			{
				cell[ii][ij] = new Complex();
			}
		}
	}
	public ComplexMatrix(ComplexMatrix m)
	{
		cell = new Complex[m.getRows()][m.getCols()];
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				cell[ii][ij] = new Complex(m.cell[ii][ij]);
			}
		}
	}
	public ComplexMatrix(Complex[][] cell)
	{
		if (cell != null)
		{
			this.cell = new Complex[cell.length][cell[0].length];
			for (int ii = 0; ii < getRows(); ++ii)
			{
				for (int ij = 0; ij < getCols(); ++ij)
				{
					this.cell[ii][ij] = new Complex(cell[ii][ij]);
				}
			}
		}
	}
	public ComplexMatrix(Complex[] cVector)
	{
		if (cVector != null)
		{
			this.cell = new Complex[cVector.length][1];
			for (int ii = 0; ii < getRows(); ++ii)
			{
				this.cell[ii][0] = new Complex(cVector[ii]);
			}
		}
	}
	public ComplexMatrix(DoubleMatrix matrixX)
	{
		int nrows = matrixX.getRows() / 2;
		int ncols = matrixX.getCols() / 2;
		cell= new Complex[nrows][ncols];
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < ncols; ++ic)
			{
				cell[ir][ic] = new Complex(matrixX.cell[2 * ir][2 * ic], matrixX.cell[2 * ir + 1][2 * ic]);
				
			}
		}
	}
	@SuppressWarnings("resource")
	public ComplexMatrix(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		this((ComplexMatrix) new ObjectInputStream( new FileInputStream(fileName)).readObject());
	}
	public int getRows()
	{
		if (cell == null) return 0;
		return cell.length;
	}
	public int getCols()
	{
		if (cell == null) return 0;
		return cell[0].length;
	}
	public void copyToColumn(Complex[] colVec, int icol) throws NumericalRecipesException
	{
		if (icol >= getCols()) throw new NumericalRecipesException("Column index too big");
		if (colVec.length != getRows()) throw new NumericalRecipesException("Col vec length wrong size");
		for (int ir = 0; ir < getRows(); ++ir)
		{
			cell[ir][icol] = new Complex(colVec[ir]);
		}
	}
	public void copyToRow(Complex[] rowVec, int irow) throws NumericalRecipesException
	{
		if (irow >= getRows()) throw new NumericalRecipesException("Row index too big");
		if (rowVec.length != getCols()) throw new NumericalRecipesException("Row vec length wrong size");
		for (int ic = 0; ic < getCols(); ++ic)
		{
			cell[irow][ic] = new Complex(rowVec[ic]);
		}
	}
	public Complex[] getColumn(int icol) throws NumericalRecipesException
	{
		if (icol >= getCols()) throw new NumericalRecipesException("Column index too big");
		Complex[] column = new Complex[getRows()];
		for (int ir = 0; ir < getRows(); ++ir)
		{
			column[ir] = new Complex(cell[ir][icol]);
		}
		return column;
	}
	public Complex[] getRow(int irow) throws NumericalRecipesException
	{
		if (irow >= getRows()) throw new NumericalRecipesException("Row index too big");
		Complex[] row = new Complex[getCols()];
		for (int ic = 0; ic < getCols(); ++ic)
		{
			row[ic] = new Complex(cell[irow][ic]);
		}
		return row;
	}
	public ComplexMatrix plus(ComplexMatrix m)
	{
		if ((m.getRows() != getRows()) || (m.getCols() != getCols())) return null;
		ComplexMatrix msum = new ComplexMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				msum.cell[ii][ij] = cell[ii][ij].plus(m.cell[ii][ij]);
			}
		}
		return msum;
	}
	public ComplexMatrix subtract(ComplexMatrix m)
	{
		if ((m.getRows() != getRows()) || (m.getCols() != getCols())) return null;
		ComplexMatrix msum = new ComplexMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				msum.cell[ii][ij] = cell[ii][ij].minus(m.cell[ii][ij]);
			}
		}
		return msum;
	}
	public ComplexMatrix times(ComplexMatrix m)
	{
		if (m.getRows() != getCols()) return null;
		ComplexMatrix mtimes = new ComplexMatrix(getRows(), m.getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < m.getCols(); ++ij)
			{
				mtimes.cell[ii][ij] = new Complex();
				for (int ik = 0; ik < getCols(); ++ik)
				{
					mtimes.cell[ii][ij] = mtimes.cell[ii][ij].plus(cell[ii][ik].times(m.cell[ik][ij]) );
				}
			}
		}
		return mtimes;
	}
	public ComplexMatrix times(Complex c)
	{
		ComplexMatrix mtimes = new ComplexMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				mtimes.cell[ii][ij] = cell[ii][ij].times(c);
			}
		}
		return mtimes;
	}
	public Complex[] times(Complex[] vecIn) throws NumericalRecipesException
	{
		if (vecIn.length != getCols()) throw new NumericalRecipesException("Col vec length wrong size");
		Complex[] vecOut = new Complex[getRows()];
		for (int ii = 0; ii < getRows(); ++ii)
		{
			vecOut[ii] = new Complex();
			for (int ij = 0; ij < getCols(); ++ij)
			{
				vecOut[ii] = vecOut[ii].plus(cell[ii][ij].times(vecIn[ij]));
			}
		}
		return vecOut;
	}
	public DoubleMatrix expandMatrix()
	{
		
		DoubleMatrix matrixX = new DoubleMatrix(2 * getRows(), 2 * getCols());
		for (int ir = 0; ir < getRows(); ++ir)
		{
			for (int ic = 0; ic < getCols(); ++ic)
			{
				matrixX.cell[2 * ir    ][2 * ic    ] =  cell[ir][ic].re;
				matrixX.cell[2 * ir + 1][2 * ic + 1] =  cell[ir][ic].re;
				matrixX.cell[2 * ir    ][2 * ic + 1] = -cell[ir][ic].im;
				matrixX.cell[2 * ir + 1][2 * ic    ] =  cell[ir][ic].im;
			}
		}
		return matrixX;
	}
	public ComplexMatrix invert()
	{
		if (getRows() != getCols()) return null;
		DoubleMatrix dcopy = expandMatrix();
		
		boolean isuccess = dcopy.Guassj0( dcopy.cell, 2 * getRows());
		if (!isuccess) return null;
		return new ComplexMatrix(dcopy);
		
	}
	public void printMatrix(PrintWriter pw) 
	{
		DecimalFormat formatter = new DecimalFormat("0.00000E00");
		formatter.setPositivePrefix("+");
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				pw.print(formatter.format(cell[ii][ij].re));
				pw.print('\t');
				pw.print(formatter.format(cell[ii][ij].im));
				pw.print('\t');
			}
			pw.print('\n');
		}
		pw.flush();
	}
	public void printMatrix(String fileName) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(fileName);
		PrintWriter pw = new PrintWriter(fos);
		printMatrix(pw);
		pw.close();
		fos.close();
	}
	public void printMatrix()
	{
		PrintWriter pw = new PrintWriter(System.out);
		printMatrix(pw);
	}
	public void writeToFile(String fileName) throws FileNotFoundException, IOException
	{
		System.out.println("Writing ComplexMatrix...");
		ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(fileName));
		oos.writeObject((ComplexMatrix) this);
		oos.flush();
		oos.close();
		System.out.println("Finished writing ComplexMatrix.");
	}
	public int[] locateMaxMag()
	{
		int irowMax = 0;
		int icolMax = 0;
		double dmax = cell[irowMax][icolMax].magnitude();
		for (int ir = 0; ir < getRows(); ++ir)
		{
			for (int ic = 0; ic < getCols(); ++ic)
			{
				if (dmax < cell[ir][ic].magnitude())
				{
					irowMax = ir;
					icolMax = ic;
					dmax = cell[irowMax][icolMax].magnitude();
				}
			}
		}
		int[] ivec = new int[2];
		ivec[0] = irowMax;
		ivec[1] = icolMax;
		return ivec;
	}
	public int locateMaxMagInCol(int icol)
	{
		int irowMax = 0;
		double dmax = cell[irowMax][icol].magnitude();
		for (int ir = 0; ir < getRows(); ++ir)
		{
			if (dmax < cell[ir][icol].magnitude())
			{
				irowMax = ir;
				dmax = cell[irowMax][icol].magnitude();
			}
		}
		return irowMax;
	}
	public int locateMaxMagInRow(int irow)
	{
		int icolMax = 0;
		double dmax = cell[irow][icolMax].magnitude();
		for (int ic = 0; ic < getCols(); ++ic)
		{
			if (dmax < cell[irow][icolMax].magnitude())
			{
				icolMax = ic;
				dmax = cell[irow][icolMax].magnitude();
			}
		}
		return icolMax;
	}
	public void drawEllipse(Complex value, int acol, int brow)
	{
		int irowc = getRows() / 2;
		int icolc = getCols() / 2;
		for (int ic = -acol; ic <= acol; ++ic)
		{
			double drow = ((double) ic) * ((double) brow ) / ((double) acol);
			drow = ((double) brow ) * ((double) brow ) - drow * drow;
			drow = Math.sqrt(drow);
			int icol = ic + icolc;
			if (icol < 0) icol = 0;
			if (icol >= getCols() ) icol = getCols() - 1;
			int irow = irowc + ((int) drow);
			if (irow >= getRows())  irow = getRows() - 1;
			cell[irow][icol] = new Complex(value);
			irow = irowc - ((int) drow);
			if (irow < 0)  icol = 0;
			cell[irow][icol] = new Complex(value);
		}
	}
	public ComplexMatrix transpose()
	{
		ComplexMatrix transpose = new ComplexMatrix(getCols(), getRows());
		for (int ir = 0; ir < getRows(); ++ir)
		{
			for (int ic = 0; ic < getCols(); ++ic)
			{
				transpose.cell[ic][ir] = new Complex(cell[ir][ic]);
			}
		}
		return transpose;
	}
//transpose and complex conjugate
	public ComplexMatrix dagger()
	{
		ComplexMatrix transpose = new ComplexMatrix(getCols(), getRows());
		for (int ir = 0; ir < getRows(); ++ir)
		{
			for (int ic = 0; ic < getCols(); ++ic)
			{
				transpose.cell[ic][ir] = new Complex(cell[ir][ic].conj());
			}
		}
		return transpose;
	}
	
	public static void main(String[] args) 
	{
		ComplexMatrix mtest = new ComplexMatrix(3,3);
		
		mtest.cell[0][0] = new Complex(1.0, 3.0);
		mtest.cell[0][1] = new Complex(2.0, 1.0);
		mtest.cell[0][2] = new Complex(3.0, 3.0);
		mtest.cell[1][0] = new Complex(3.0, 0.0);
		mtest.cell[1][1] = new Complex(2.0, 2.0);
		mtest.cell[1][2] = new Complex(1.0, 1.0);
		mtest.cell[2][0] = new Complex(0.0, 3.0);
		mtest.cell[2][1] = new Complex(1.0, 1.0);
		mtest.cell[2][2] = new Complex(0.0, 0.0);
		
		ComplexMatrix minv = mtest.invert();
		ComplexMatrix ident = minv.times(mtest);
		ComplexMatrix transpose = mtest.dagger();
		PrintWriter pw = new PrintWriter(System.out);
		pw.println("Initial Matrix");
		mtest.printMatrix(pw);
		pw.println("Inverse Matrix");
		minv.printMatrix(pw);
		pw.println("Identity Matrix");
		ident.printMatrix(pw);
		pw.println("Transpose Matrix");
		transpose.printMatrix(pw);
	}
}
