package com.astrofizzbizz.numericalrecipes;

import java.io.FileNotFoundException;

public class TestSimplexOptimizer extends SimplexOptimize
{
	private int numVariables = 3;
	double[] coeff = {0.0, 0.0, 0.0};
	double[] x = {-1.0, 0.0, 1.0};
	double[] ymeas = { 1.0, 0.3, 6.3333333333};

	@Override
	public double fChiSquare() 
	{
		for (int ii = 0; ii < 3; ++ii)
		{
			coeff[ii] = getVariable()[ii].getValue();
		}
		double chi = 0.0;
		for (int ii = 0; ii < 3; ++ii)
		{
			double y = polyfit(x[ii]);
			chi = chi + (y - ymeas[ii]) * (y - ymeas[ii]);
		}
		return chi;
	}
	public double polyfit(double x)
	{
		double y = 0.0;
		double xpow = 1.0;
		for (int ii = 0; ii < numVariables; ++ii)
		{
			y = y + coeff[ii] * xpow;
			xpow = xpow * x;
		}
		return y;
	}

	@Override
	public void setupOptimization() 
	{
		setNumTotalVariables(numVariables);
		setNumOptVariables(numVariables);
		setVariable(new OptimizeVariable[getNumTotalVariables()]);
		setLookupVector(new int[getNumTotalVariables()]);
		
		for (int ii = 0; ii < numVariables; ++ii)
		{
			getVariable()[ii] = new OptimizeVariable();
			getVariable()[ii].setInitValue(0.0);
			getVariable()[ii].setMin(-100.0);
			getVariable()[ii].setMax( 100.0);
			getVariable()[ii].setStep(0.01);
			getVariable()[ii].setValue(0.0);
			getLookupVector()[ii] = ii;
		}

	/* Iterations */
		/*	m_iopt_limit_type = 1;
	/* Time       */
	/*	m_iopt_limit_type = 2;
	/* Chi Square */
	/*	m_iopt_limit_type = 3; */

//		setSimLimit(optLimit);
		return;
		
	}
	public static void main(String[] args) throws FileNotFoundException
	{
		TestSimplexOptimizer test = new TestSimplexOptimizer();
		test.setPrintStream(System.out);
		test.setupOptimization();
		test.setOptLimitType(1);  // 1 = iterations
		test.setSimLimit(100000);
		test.Optimize(1.0e-24);
		for (int ii = 0; ii < 3; ++ii)
		{
			System.out.println(ii + " " + test.coeff[ii]);
		}
	}
	

}
