package com.astrofizzbizz.numericalrecipes;

public class ComplexRunningMeanAndSigma 
{
	DoubleRunningMeanAndSigma real;
	DoubleRunningMeanAndSigma imag;
	
	public ComplexRunningMeanAndSigma()
	{
		real = new DoubleRunningMeanAndSigma();
		imag = new DoubleRunningMeanAndSigma();
	}
	public void addMeasurement(Complex measurement)
	{
		real.addMeasurement(measurement.re);
		imag.addMeasurement(measurement.im);
	}
	public Complex mean()
	{
		return new Complex(real.mean(), imag.mean());
	}
	public Complex sigma()
	{
		return new Complex(real.sigma(), imag.sigma());
	}
}
