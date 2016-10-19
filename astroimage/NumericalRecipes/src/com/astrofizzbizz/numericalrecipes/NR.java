package com.astrofizzbizz.numericalrecipes;

import java.io.FileNotFoundException;
import java.io.PrintStream;


/**
 * @author mcginnis
 *
 */
public class NR 
{
	// matrix reduction assumes double data 
	// starts at row index 0 and column index 0
	/**
	 * @param a
	 * @param n
	 * @param b
	 * @param m
	 * @return
	 */
	public static boolean 	Guassj0( double[][] a, int n, double[][] b, int m)
	{
		double[][]	da = new double[n + 1][n + 1];
		double[][]	db = new double[n + 1][m + 1];
		boolean			success;

		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				da[inn + 1][imm + 1] =  a[inn][imm];
			}
			for (int imm = 0; imm < m; ++imm)
			{
				db[inn + 1][imm + 1] =  b[inn][imm];
			}
		}
		success = Guassj( da, n, db, m);
		if (!success) return success;
		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				a[inn][imm] = da[inn + 1][imm + 1];
			}
			for (int imm = 0; imm < m; ++imm)
			{
				b[inn][imm] = db[inn + 1][imm + 1];
			}
		}
		return success;
	}
	/**
	 * @param a
	 * @param n
	 * @param b
	 * @param m
	 * @return boolean
	 */
	// matrix reduction assumes double data 
	// starts at row index 1 and column index 1
	public static boolean 	Guassj( double[][] a, int n, double[][] b, int m)
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
System.out.println("At " + i + " out of " + n);
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
				for ( l = 1; l <= m; l++ )
				{
					dswap = b[irow][l];
					b[irow][l] = b[icol][l];
					b[icol][l] = dswap;
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
			for ( l = 1; l <= m; l++ ) b[icol][l] *= pivinv;
			for( ll = 1; ll <= n; ll++ )
			{
				if ( ll != icol )
				{
					dum = a[ll][icol];
					a[ll][icol] = 0.0;
					for ( l = 1; l <= n; l++ ) a[ll][l] -= a[icol][l] * dum;
					for ( l = 1; l <= m; l++ ) b[ll][l] -= b[icol][l] * dum;
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
	/**
	 * @param k
	 * @param n
	 * @param arr
	 * @return double
	 */
	public static double select(int k, int n, double arr[])
	{
		int i,ir,j,l,mid;
		double a;
		double dswap;

		l=1;
		ir=n;
		for (;;) 
		{
			if (ir <= l+1) 
			{
				if (ir == l+1 && arr[ir] < arr[l]) 
				{
					dswap = arr[l];
					arr[l] = arr[ir];
					arr[ir] = dswap;
				}
				return arr[k];
			} 
			else 
			{
				mid=(l+ir) >> 1;
				dswap = arr[mid];
				arr[mid] = arr[l+1];
				arr[l+1] = dswap;
				if (arr[l] > arr[ir]) 
				{
					dswap = arr[l];
					arr[l] = arr[ir];
					arr[ir] = dswap;
				}
				if (arr[l+1] > arr[ir]) 
				{
					dswap = arr[l+1];
					arr[l+1] = arr[ir];
					arr[ir] = dswap;
				}
				if (arr[l] > arr[l+1]) 
				{
					dswap = arr[l];
					arr[l] = arr[l+1];
					arr[l+1] = dswap;
				}
				i=l+1;
				j=ir;
				a=arr[l+1];
				for (;;) 
				{
					do i++; while (arr[i] < a);
					do j--; while (arr[j] > a);
					if (j < i) break;
					dswap = arr[i];
					arr[i] = arr[j];
					arr[j] = dswap;
				}
				arr[l+1]=arr[j];
				arr[j]=a;
				if (j >= k) ir=j-1;
				if (j <= k) l=i;
			}
		}
	}
	/**
	 * @param n
	 * @param arr
	 */
	public static void sort(int n, double arr[])
	{
		int i,ir=n,j,k,l=1;
		int[] istack = null;
		int jstack=0;
		double a;
		int 	NSTACK = 50;
		int		M = 7;
		double	dswap;

		istack = new int[NSTACK + 1];
		for (;;) 
		{
			if (ir-l < M) 
			{
				for (j=l+1;j<=ir;j++) 
				{
					a=arr[j];
					for (i=j-1;i>=l;i--) 
					{
						if (arr[i] <= a) break;
						arr[i+1]=arr[i];
					}
					arr[i+1]=a;
				}
				if (jstack == 0) break;
				ir=istack[jstack--];
				l=istack[jstack--];
			} 
			else 
			{
				k=(l+ir) >> 1;
				dswap = arr[k];
				arr[k] = arr[l+1];
				arr[l+1] = dswap;
				if (arr[l] > arr[ir]) 
				{
					dswap = arr[l];
					arr[l] = arr[ir];
					arr[ir] = dswap;
				}
				if (arr[l+1] > arr[ir]) 
				{
					dswap = arr[l+1];
					arr[l+1] = arr[ir];
					arr[ir] = dswap;
				}
				if (arr[l] > arr[l+1]) 
				{
					dswap = arr[l];
					arr[l] = arr[l+1];
					arr[l+1] = dswap;
				}
				i=l+1;
				j=ir;
				a=arr[l+1];
				for (;;) 
				{
					do i++; while (arr[i] < a);
					do j--; while (arr[j] > a);
					if (j < i) break;
					dswap = arr[i];
					arr[i] = arr[j];
					arr[j] = dswap;
				}
				arr[l+1]=arr[j];
				arr[j]=a;
				jstack += 2;
				if (ir-i+1 >= j-l) 
				{
					istack[jstack]=ir;
					istack[jstack-1]=i;
					ir=j-1;
				} 
				else 
				{
					istack[jstack]=j-1;
					istack[jstack-1]=l;
					l=i;
				}
			}
		}
		istack = null;
	}
	/**
	 * @param a 
	 * @param n 
	 * @param b 
	 * @param m 
	 * @return */
	// Complex matrix reduction assumes complex data 
	// starts at row index 0 and column index 0

	public static boolean	xGaussj( Complex[][] a, int n, Complex[][] b, int m)
	{
		double[][]	da = new double[2 * n + 1][2 * n + 1];
		double[][]	db = new double[2 * n + 1][m + 1];
		boolean			success;

		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				da[2 * inn + 1][2 * imm + 1] =  a[inn][imm].re;
				da[2 * inn + 1][2 * imm + 2] = -a[inn][imm].im;
				da[2 * inn + 2][2 * imm + 1] =  a[inn][imm].im;
				da[2 * inn + 2][2 * imm + 2] =  a[inn][imm].re;
			}
			for (int imm = 0; imm < m; ++imm)
			{
				db[2 * inn + 1][imm + 1] =  b[inn][imm].re;
				db[2 * inn + 2][imm + 1] =  b[inn][imm].im;
			}
		}
		success = Guassj( da, 2 * n, db, m);
		if (!success) return success;
		for (int inn = 0; inn < n; ++inn)
		{
			for (int imm = 0; imm < n; ++imm)
			{
				a[inn][imm].re = da[2 * inn + 1][2 * imm + 1];
				a[inn][imm].im = da[2 * inn + 2][2 * imm + 1];
			}
			for (int imm = 0; imm < m; ++imm)
			{
				b[inn][imm].re = db[2 * inn + 1][imm + 1];
				b[inn][imm].im = db[2 * inn + 2][imm + 1];
			}
		}
		return success;
	}
	protected void testxGaussj()
	{
		Complex[][] xamat = new Complex[3][3];
		Complex[][] xbmat = new Complex[3][1];
		
		xamat[0][0] = new Complex( 1.0, 1.0);
		xamat[0][1] = new Complex( 0.0, 0.0);
		xamat[0][2] = new Complex( 1.0,-2.0);
		xamat[1][0] = new Complex( 0.0, 0.0);
		xamat[1][1] = new Complex( 1.0, 0.0);
		xamat[1][2] = new Complex( 0.0, 0.0);
		xamat[2][0] = new Complex( 1.0, 2.0);
		xamat[2][1] = new Complex( 0.0, 0.0);
		xamat[2][2] = new Complex( 1.0,-1.0);

		xbmat[0][0] = new Complex( 0.0,-0.0);
		xbmat[1][0] = new Complex( 0.0, 0.0);
		xbmat[2][0] = new Complex( 0.0, 0.0);
		xGaussj( xamat, 3, xbmat, 1);
		for (int ii = 0; ii < 3; ++ii)
		{
			System.out.println("b" + ii + " = " + xbmat[ii][0].re + " +j" + xbmat[ii][0].im);
		}
		for (int ii = 0; ii < 3; ++ii)
		{
			for (int ij = 0; ij < 3; ++ij)
			{
			System.out.println("ainv " + ii + "," + ij + " = " + xamat[ii][ij].re + " +j" + xamat[ii][ij].im);
			}
		}
	}
	protected void testGaussj0()
	{
		double[][] amat = new double[3][3];
		double[][] bmat = new double[3][1];
		
		amat[0][0] = 1.0;
		amat[0][1] = 0.0;
		amat[0][2] = 2.0;
		amat[1][0] = 0.0;
		amat[1][1] = 3.0;
		amat[1][2] = 0.0;
		amat[2][0] = 4.0;
		amat[2][1] = 0.0;
		amat[2][2] = 5.0;

		bmat[0][0] = 6.0;
		bmat[1][0] = 7.0;
		bmat[2][0] = 8.0;
		Guassj0( amat, 3, bmat, 1);
		for (int ii = 0; ii < 3; ++ii)
		{
			System.out.println("b" + ii + " = " + bmat[ii][0]);
		}
	}
	public static double gaussRandom(double mean, double stdev) 
	{
	    double u = 2*Math.random()-1;
	    double v = 2*Math.random()-1;
	    double r = u*u + v*v;
	    /*if outside interval [0,1] start over*/
	    if(r == 0 || r > 1) return gaussRandom(mean, stdev);

	    double c = Math.sqrt(-2*Math.log(r)/r);
	    return u*c * stdev + mean;

	}
	private void testgaussRandom() 
	{
		PrintStream ps;
		try {
			ps = new PrintStream("test.dat");
			for (int ii = 0; ii < 1000; ++ii) ps.println(gaussRandom(0.0, 2.0));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args)  
	{
		NR nr = new NR();
		nr.testgaussRandom();
	}
}
