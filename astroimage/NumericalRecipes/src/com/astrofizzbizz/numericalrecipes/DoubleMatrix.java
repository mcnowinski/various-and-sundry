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
public class DoubleMatrix implements Serializable
{
	private static final long serialVersionUID = 5848612731095766726L;
	public double[][] cell = null;
	public DoubleMatrix(int nrow, int ncol)
	{
		cell = new double[nrow][ncol];
		for (int ii = 0; ii < nrow; ++ii)
		{
			for (int ij = 0; ij < ncol; ++ij)
			{
				cell[ii][ij] = 0.0;
			}
		}
	}
	@SuppressWarnings("resource")
	public DoubleMatrix(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		this((DoubleMatrix) new ObjectInputStream( new FileInputStream(fileName)).readObject());
	}

	public void makeUnity()
	{
		if ( getRows() !=  getCols()) return;
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				cell[ii][ij] = 0.0;
			}
		}
		for (int ii = 0; ii < getRows(); ++ii)
		{
			cell[ii][ii] = 1.0;
		}
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
	public DoubleMatrix(DoubleMatrix m)
	{
		cell = new double[m.getRows()][m.getCols()];
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				cell[ii][ij] = m.cell[ii][ij];
			}
		}
	}
	public DoubleMatrix(double[][] cell)
	{
		this.cell = new double[cell.length][cell[0].length];
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				this.cell[ii][ij] = cell[ii][ij];
			}
		}
	}
	public void copyToColumn(double[] colVec, int icol) throws NumericalRecipesException
	{
		if (icol >= getCols()) throw new NumericalRecipesException("Column index too big");
		if (colVec.length != getRows()) throw new NumericalRecipesException("Col vec length wrong size");
		for (int ir = 0; ir < getRows(); ++ir)
		{
			cell[ir][icol] = colVec[ir];
		}
	}
	public void copyToRow(double[] rowVec, int irow) throws NumericalRecipesException
	{
		if (irow >= getRows()) throw new NumericalRecipesException("Row index too big");
		if (rowVec.length != getCols()) throw new NumericalRecipesException("Row vec length wrong size");
		for (int ic = 0; ic < getCols(); ++ic)
		{
			cell[irow][ic] = rowVec[ic];
		}
	}
	public DoubleMatrix plus(DoubleMatrix m)
	{
		if ((m.getRows() != getRows()) || (m.getCols() != getCols())) return null;
		DoubleMatrix msum = new DoubleMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				msum.cell[ii][ij] = cell[ii][ij] + m.cell[ii][ij];
			}
		}
		return msum;
	}
	public DoubleMatrix subtract(DoubleMatrix m)
	{
		if ((m.getRows() != getRows()) || (m.getCols() != getCols())) return null;
		DoubleMatrix msum = new DoubleMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				msum.cell[ii][ij] = cell[ii][ij] - m.cell[ii][ij];
			}
		}
		return msum;
	}
	public DoubleMatrix times(DoubleMatrix m)
	{
		if (m.getRows() != getCols()) return null;
		DoubleMatrix mtimes = new DoubleMatrix(getRows(), m.getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < m.getCols(); ++ij)
			{
				mtimes.cell[ii][ij] = 0.0;
				for (int ik = 0; ik < getCols(); ++ik)
				{
					mtimes.cell[ii][ij] = mtimes.cell[ii][ij] + cell[ii][ik] * m.cell[ik][ij];
				}
			}
		}
		return mtimes;
	}
	public double[] times(double[] vecIn) throws NumericalRecipesException
	{
		if (vecIn.length != getCols()) throw new NumericalRecipesException("Col vec length wrong size");
		double[] vecOut = new double[getRows()];
		for (int ii = 0; ii < getRows(); ++ii)
		{
			vecOut[ii] = 0.0;
			for (int ij = 0; ij < getCols(); ++ij)
			{
				vecOut[ii] = vecOut[ii] + cell[ii][ij] * vecIn[ij];
			}
		}
		return vecOut;
	}
	public DoubleMatrix times(double d)
	{
		DoubleMatrix mtimes = new DoubleMatrix(getRows(), getCols());
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				mtimes.cell[ii][ij] = cell[ii][ij] * d;
			}
		}
		return mtimes;
	}
	public DoubleMatrix invert()
	{
		if (getRows() != getCols()) return null;
		DoubleMatrix mcopy = new DoubleMatrix(this);
		boolean isuccess = Guassj0( mcopy.cell, getRows());
		if (!isuccess) return null;
		return mcopy;
	}
	public void printMatrix(String fileName) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(fileName);
		PrintWriter pw = new PrintWriter(fos);
		printMatrix(pw);
		pw.close();
		fos.close();
	}
	public void writeToFile(String fileName) throws FileNotFoundException, IOException
	{
		System.out.println("Writing DoubleMatrix...");
		ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(fileName));
		oos.writeObject((DoubleMatrix) this);
		oos.flush();
		oos.close();
		System.out.println("Finished writing DoubleMatrix.");
	}
	public void printMatrix() 
	{
		PrintWriter pw = new PrintWriter(System.out);
		printMatrix(pw);
	}
	public void printMatrix(PrintWriter pw)
	{
		DecimalFormat formatter = new DecimalFormat("0.#####E0");
		for (int ii = 0; ii < getRows(); ++ii)
		{
			for (int ij = 0; ij < getCols(); ++ij)
			{
				pw.print(formatter.format(cell[ii][ij]));
				pw.print('\t');
			}
			pw.print('\n');
		}
		pw.flush();
	}
	// matrix reduction assumes double data 
	// starts at row index 1 and column index 1
	protected boolean 	Guassj1( double[][] a, int n)
	{
		double	big;
		double	dum;
		double	pivinv;
		int[]	ipiv = null;
		int[]	indxr = null;
		int[]	indxc = null;
		int		i;
		int		j;
		int		k;
		int		l;
		int		ll;
		int		irow = 0;
		int		icol = 0;
		boolean	istatus;
		double	dswap;
		
		indxc = new int[n + 1];
		indxr = new int[n + 1];
		ipiv = new int[n + 1];

		istatus = true;
		for ( j = 1; j <= n; j++ ) ipiv[j] = 0;
		for ( i = 1; i <= n; i++ )
		{
//System.out.println("At " + i + " out of " + n);
			big = 0.0;
			for ( j = 1; j <= n; j++ )
			{
				if ( ipiv[j] != 1 )
				{
					for ( k = 1; k <= n; k++ )
					{
						if ( ipiv[k] == 0 )
						{
							if ( Math.abs( a[j][k] ) >= big )
							{
								big = Math.abs( a[j][k] );
								irow = j;
								icol = k;
							}
						}
						else if ( ipiv[k] > 1 )
						{
							istatus = false;
							return istatus;
//							printf("Singular Matrix-1 in guassj. Now exiting to system\n");
//							exit(1);
						}
					}
				}
			}
			ipiv[icol] = ipiv[icol] + 1;
			if ( irow != icol )
			{
				for ( l = 1; l <= n; l++ ) 
				{
					dswap = a[irow][l];
					a[irow][l] = a[icol][l];
					a[icol][l] = dswap;
				}
			}
			indxr[i] = irow;
			indxc[i] = icol;
			if ( a[icol][icol] == 0.0 )
			{
				istatus = false;
				return false;
//				printf("Singular Matrix-2 in guassj. Now exiting to system\n");
//				exit(1);
			}
			pivinv = 1.0 / a[icol][icol];
			a[icol][icol] = 1.0;
			for ( l = 1; l <= n; l++ ) a[icol][l] *= pivinv;
			for( ll = 1; ll <= n; ll++ )
			{
				if ( ll != icol )
				{
					dum = a[ll][icol];
					a[ll][icol] = 0.0;
					for ( l = 1; l <= n; l++ ) a[ll][l] -= a[icol][l] * dum;
				}
			}
		}
		for ( l = n; l >= 1; l-- )
		{
			if (indxr[l] != indxc[l] )
			{
				for ( k = 1; k <= n; k++ ) 
				{
					dswap = a[k][indxr[l]];
					a[k][indxr[l]] = a[k][indxc[l]];
					a[k][indxc[l]] = dswap;
				}
			}
		}
		ipiv = null;
		indxr = null;
		indxc = null;
		return istatus;
	}
	// matrix reduction assumes double data 
	// starts at row index 0 and column index 0
	protected boolean 	Guassj0( double[][] a, int n)
	{
		
		double[][]	da = new double[n + 1][n + 1];
		boolean			success;

		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				da[inn + 1][imm + 1] =  a[inn][imm];
			}
		}
		success = Guassj1( da, n);
		if (!success) return success;
		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				a[inn][imm] = da[inn + 1][imm + 1];
			}
		}
		return success;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DoubleMatrix mtest = new DoubleMatrix(3,3);
		
		mtest.cell[0][0] = 1.0;
		mtest.cell[0][1] = 0.0;
		mtest.cell[0][2] = 2.0;
		mtest.cell[1][0] = 0.0;
		mtest.cell[1][1] = 3.0;
		mtest.cell[1][2] = 0.0;
		mtest.cell[2][0] = 4.0;
		mtest.cell[2][1] = 0.0;
		mtest.cell[2][2] = 5.0;
		
		DoubleMatrix minv = mtest.invert();
		DoubleMatrix ident = minv.times(mtest);
		try 
		{
			mtest.printMatrix("startMat.dat");
			minv.printMatrix("invMat.dat");
			ident.printMatrix("identMat.dat");
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
