package com.astrofizzbizz.numericalrecipes.jama.examples;

import com.astrofizzbizz.numericalrecipes.jama.Matrix;
import com.astrofizzbizz.numericalrecipes.jama.SingularValueDecomposition;

public class SvdExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		double[][] a = {{1,2,3},{3,2,1},{0,1,0}};
		Matrix amat = new Matrix(a);
		SingularValueDecomposition svdAmat = amat.svd();
//		double[][] u = svdAmat.getU().getArray();
//		double[][] v = svdAmat.getV().getArray();
//		double[] s  = svdAmat.getSingularValues();

		System.out.println("A = ");
		amat.print(10, 2);
		System.out.println("U = ");
		svdAmat.getU().print(10, 5);
		System.out.println("V = ");
		svdAmat.getV().print(10, 5);
		System.out.println("S = ");
		svdAmat.getS().print(10, 5);
		
	}

}
