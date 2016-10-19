package com.astrofizzbizz.numericalrecipes;

import java.io.Serializable;

import com.astrofizzbizz.numericalrecipes.jama.Matrix;
import com.astrofizzbizz.numericalrecipes.jama.SingularValueDecomposition;



public class ComplexSvd implements Serializable
{
	private static final long serialVersionUID = 2775706524807794192L;
	Matrix jamaMat;
	SingularValueDecomposition jamaSvdMat;
	public ComplexSvd(ComplexMatrix matrix)
	{
		jamaMat = new Matrix(matrix.expandMatrix().cell);
		jamaSvdMat = jamaMat.svd();
	}
	public ComplexMatrix getMatrix()
	{
		return new ComplexMatrix(new DoubleMatrix(jamaMat.getArray()));
	}
	public double[] getSingularValues()
	{
		return jamaSvdMat.getSingularValues();
	}
	public double getMaxSingularValue()
	{
		return jamaSvdMat.getSingularValues()[0];
	}
	public ComplexMatrix getInverseMatrix(double minSingularValue)
	{
		double[] singValues = getSingularValues();
		int nrows = singValues.length;
		double[][] sinverse = new double[nrows][nrows];
		for (int ir = 0; ir < nrows; ++ir)
		{
			for (int ic = 0; ic < nrows; ++ic)
			{
				sinverse[ir][ic] = 0.0;
			}
			if ( singValues[ir] > minSingularValue) sinverse[ir][ir] = 1.0 / singValues[ir];
		}
		Matrix jamaSinverse = new Matrix(sinverse);
		Matrix inverse 
			= jamaSvdMat.getV().times(
				jamaSinverse.times(
						jamaSvdMat.getU().transpose() ) );
		return new ComplexMatrix(new DoubleMatrix(inverse.getArray()) );	
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
		
		ComplexSvd csvd = new ComplexSvd(mtest);
		ComplexMatrix orig = csvd.getMatrix();
		System.out.println("Initial Matrix");
		orig.printMatrix();
		
		ComplexMatrix minv = csvd.getInverseMatrix(0.0);
		ComplexMatrix ident = minv.times(orig);
		System.out.println("Inverse Matrix");
		minv.printMatrix();
		System.out.println("Identity Matrix");
		ident.printMatrix();
		
	}

}
