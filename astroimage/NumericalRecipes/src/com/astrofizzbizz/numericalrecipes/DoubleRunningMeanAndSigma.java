package com.astrofizzbizz.numericalrecipes;

public class DoubleRunningMeanAndSigma 
{
	private double measSum = 0.0;
	private double measSquareSum = 0.0;
	int inumMeas = 0;
	
	public DoubleRunningMeanAndSigma()
	{
		measSum = 0.0;
		measSquareSum = 0.0;
		inumMeas = 0;
	}
	public void addMeasurement(double measurement)
	{
		measSum = measSum + measurement;
		measSquareSum = measSquareSum + measurement * measurement;
		inumMeas = inumMeas + 1;
	}
	public double mean()
	{
		double mean = measSum /  ((double) inumMeas);
		return mean;
	}
	public double sigma()
	{
		double mean = mean();
		double sigma = measSquareSum /  ((double) inumMeas);
		sigma = sigma - mean * mean;
		sigma = Math.sqrt(sigma);
		return sigma;
	}
}
