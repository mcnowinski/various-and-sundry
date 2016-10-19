package com.astrofizzbizz.numericalrecipes;

import java.io.Serializable;

public class PolyFit implements Serializable
{
	private static final long serialVersionUID = 4197949090190409609L;
	private double xMin;
	private double xMax;
	private double[] coeff;
	private int fitOrder;
	
	public PolyFit (double[] xData, double[] yData, double[] sigma, int fitOrder) throws NumericalRecipesException
	{
		int numDataPts = xData.length;
		if (yData.length != numDataPts) 
			throw new NumericalRecipesException("Y data vector length does not match X Vector");
		if (sigma != null)
		{
			if (sigma.length != numDataPts) 
				throw new NumericalRecipesException("Sigma data vector length does not match X Vector");
		}
		this.fitOrder = fitOrder;
		this.xMin = xData[0];
		this.xMax = xData[0];
		for (int ii = 0; ii < numDataPts; ++ii)
		{
			if (this.xMin > xData[ii]) this.xMin = xData[ii];
			if (this.xMax < xData[ii]) this.xMax = xData[ii];
		}
		DoubleMatrix fitMatrixCalc = new DoubleMatrix(fitOrder, fitOrder);
		double[] fitVec = new double[fitOrder];
		for (int ii = 0; ii < fitOrder; ++ii)
		{
			fitVec[ii] = 0.0;
		}
		double matElemCont;
		double vecElemCont;
		for (int im = 0; im < numDataPts; ++im)
		{
			for (int ii = 0; ii < fitOrder; ++ii)
			{
				vecElemCont = yData[im] * xPow(xNorm(xData[im]), ii) / ((double) numDataPts);
				if (sigma != null) vecElemCont = vecElemCont / (sigma[im] * sigma[im]);
				fitVec[ii] = fitVec[ii] + vecElemCont;
				for (int ij = 0; ij < fitOrder; ++ij)
				{
					matElemCont = xPow(xNorm(xData[im]), ii) * xPow(xNorm(xData[im]), ij) / ((double) numDataPts);
					if (sigma != null) matElemCont = matElemCont / (sigma[im] * sigma[im]);
					fitMatrixCalc.cell[ii][ij] = fitMatrixCalc.cell[ii][ij] + matElemCont;
				}
			}
		}
		DoubleMatrix fitMatrixInv = fitMatrixCalc.invert();
		coeff = fitMatrixInv.times(fitVec);
	}
	private double xNorm(double x)
	{
		double xnorm = (x - xMin) / (xMax - xMin);
		return xnorm;
	}
	private double xPow(double x, int pow)
	{
		double xpow = 1;
		if (pow > 0)
		{
			for (int ipow = 1; ipow <= pow; ++ipow)
			{
				xpow = xpow * x;
			}
		}
		return xpow;
	}
	public double fit(double x) 
	{
		double yfit = 0.0;
		for (int ii = 0; ii < fitOrder; ++ii)
		{
			yfit = yfit + coeff[ii] * xPow(xNorm(x), ii);
		}
		return yfit;
	}
	public static void main(String[] args) 
	{

	}

}
