package com.astrofizzbizz.numericalrecipes;

import java.io.PrintStream;

/**
 * @author mcginnis
 *
 */
public abstract class SimplexOptimize 
{
	int		numTotalVariables;
	int		numOptVariables;
	double	simLimit;
	int		optLimitType;
	OptimizeVariable[]	variable = null;
	int[]	lookupVector = null;
	PrintStream	chatterStream = null;
	
	/**
	 * @author mcginnis
	 *
	 */
	public class OptimizeVariable
	{
		String	name;
		double  value;
		double	min;
		double	max;
		double	step;
		double	initValue;
		double	mask;
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the value
		 */
		public double getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(double value) {
			this.value = value;
		}
		/**
		 * @return the min
		 */
		public double getMin() {
			return min;
		}
		/**
		 * @param min the min to set
		 */
		public void setMin(double min) {
			this.min = min;
		}
		/**
		 * @return the max
		 */
		public double getMax() {
			return max;
		}
		/**
		 * @param max the max to set
		 */
		public void setMax(double max) {
			this.max = max;
		}
		/**
		 * @return the step
		 */
		public double getStep() {
			return step;
		}
		/**
		 * @param step the step to set
		 */
		public void setStep(double step) {
			this.step = step;
		}
		/**
		 * @return the initValue
		 */
		public double getInitValue() {
			return initValue;
		}
		/**
		 * @param initValue the initValue to set
		 */
		public void setInitValue(double initValue) {
			this.initValue = initValue;
		}
		/**
		 * @return the mask
		 */
		public double getMask() {
			return mask;
		}
		/**
		 * @param mask the mask to set
		 */
		public void setMask(double mask) {
			this.mask = mask;
		}
	}
	/**
	 * @return double
	 */
	public abstract double	fChiSquare();
	/**
	 * 
	 */
	public abstract void	setupOptimization();
	int iAmoeba(double[][] p, double[] y, int ndim, double ftol,
					int[] nfunk)
	{
		int		i;
		int		ihi;
		int		ilo;
		int		inhi;
		int		j;
		int		mpts=ndim+1;
		double	rtol;
		double	sum;
		double	ysave;
		double	ytry;
		double[]	psum = null;
		double	ftime = 0.0;
		double  dswap;
		double	TINY = 1.0e-33;
	   
		psum= new double[ndim + 1];
		nfunk[0] =0;
		for (j=1;j<=ndim;j++)
		{
			for (sum=0.0,i=1;i<=mpts;i++) sum += p[i][j];
			psum[j]=sum;
		}
		for (;;) 
		{
			ilo=1;
// converted weird NR code
			if( y[1] > y[2] )
			{
				ihi = 1;
				inhi = 2;
			}
			else
			{
				ihi = 2;
				inhi = 1;
			}
			for (i=1;i<=mpts;i++) 
			{
				if (y[i] <= y[ilo]) ilo=i;
				if (y[i] > y[ihi]) 
				{
					inhi=ihi;
					ihi=i;
				} 
				else if (y[i] > y[inhi] && i != ihi) inhi=i;
			}
			if ( (nfunk[0] > ((int) simLimit)) 
				&& ( optLimitType == 1 ) )
			{
	/* NMAX exceeded */
				dswap = y[1];
				y[1] = y[ilo];
				y[ilo] = dswap;
				for (i=1;i<=ndim;i++) 
				{
					dswap = p[1][i];
					p[1][i] = p[ilo][i];
					p[ilo][i] = dswap;
				}
				psum = null;
				return -1;
			}
			if ( (ftime > simLimit) && ( optLimitType == 2 ) )
			{
	/* Time exceeded */
				dswap = y[1];
				y[1] = y[ilo];
				y[ilo] = dswap;
				for (i=1;i<=ndim;i++) 
				{
					dswap = p[1][i];
					p[1][i] = p[ilo][i];
					p[ilo][i] = dswap;
				}
				psum = null;
				return -2;
			}
			if ( (y[ilo] < simLimit) && ( optLimitType == 3 ) )
			{
	/* Chi min exceeded */
				dswap = y[1];
				y[1] = y[ilo];
				y[ilo] = dswap;
				for (i=1;i<=ndim;i++) 
				{
					dswap = p[1][i];
					p[1][i] = p[ilo][i];
					p[ilo][i] = dswap;
				}
				psum = null;
				return -3;
			}
			rtol=  (2.0*Math.abs(y[ihi]-y[ilo])/(Math.abs(y[ihi])+Math.abs(y[ilo])+TINY));
			if (rtol < ftol) 
			{
				dswap = y[1];
				y[1] = y[ilo];
				y[ilo] = dswap;
				for (i=1;i<=ndim;i++) 
				{
					dswap = p[1][i];
					p[1][i] = p[ilo][i];
					p[ilo][i] = dswap;
				}
				break;
			}
			nfunk[0] += 2;
			ytry=amotry(p,y,psum,ndim,ihi,-1.0);
			if (ytry < 0.0)
			{
				psum = null;
				return -100;
			}
			if (ytry <= y[ilo])
			{
				ytry=amotry(p,y,psum,ndim,ihi,2.0);
				if (ytry < 0.0)
				{
					psum = null;
					return -100;
				}
			}
			else if (ytry >= y[inhi]) 
			{
				ysave=y[ihi];
				ytry=amotry(p,y,psum,ndim,ihi,0.5);
				if (ytry < 0.0)
				{
					psum = null;
					return -100;
				}
				if (ytry >= ysave) 
				{
					for (i=1;i<=mpts;i++) 
					{
						if (i != ilo) 
						{
							for (j=1;j<=ndim;j++)
								p[i][j]=psum[j]= 0.5 * (p[i][j]+p[ilo][j]);
		
							vSetupTrial(psum);
							y[i] = fChiSquare();
							if (y[i] < 0.0)
							{
								psum = null;
								return -100;
							}
						}
					}
					nfunk[0] += ndim;
					for (j=1;j<=ndim;j++)
					{
						for (sum=0.0,i=1;i<=mpts;i++) sum += p[i][j];
						psum[j]=sum;
					}
				}
			} else --(nfunk[0]);
		}
		psum = null;
		return 0;
	}
	double amotry(double[][] p, double[] y, double[] psum, int ndim,
				   int ihi, double fac)
	{
		int		j;
		double	fac1;
		double	fac2;
		double	ytry;
		double[] ptry = null;
	   
		ptry= new double[ndim + 1];
		fac1=( 1.0 - fac)/ndim;
		fac2=fac1-fac;
		for (j=1;j<=ndim;j++) ptry[j]=psum[j]*fac1-p[ihi][j]*fac2;
		
		vSetupTrial(ptry);
		ytry = fChiSquare();
//		chitChat("ChiSquare " + ytry);
		if (ytry < 0.0)
		{
			ptry = null;
			return ytry;
		}
		if (ytry < y[ihi]) 
		{
			y[ihi]=ytry;
			for (j=1;j<=ndim;j++) 
			{
				psum[j] += ptry[j]-p[ihi][j];
				p[ihi][j]=ptry[j];
			}
		}
		ptry = null;
		return ytry;
	}
	/**
	 * @param ftol
	 * @return status 
	 */
	public int Optimize(double ftol)
	{

		int		isolution;
		int		iparam;
		int[]	infunc = new int[2];
		int		istatus;
		double[]	fvary_param = null;
		double[]	fchi_square = null;
		double[][]	fsolutions = null;
		double	fchiba;

		fvary_param = new double[numOptVariables + 1];
		fchi_square = new double[numOptVariables + 1  + 1];
		fsolutions = new double[numOptVariables + 1  + 1][numOptVariables  + 1];

		fchiba = 0.0;
		for ( iparam = 1; iparam <= numOptVariables; ++iparam )
		{
			fvary_param[iparam] = 0.0;
		}

		vSetupTrial(fvary_param);
		fchiba  = fChiSquare();
		if (fchiba < 0.0)
		{
			fvary_param = null;
			fchi_square = null;
			fsolutions = null;
			return -1;
		}
		for (	isolution = 1; 
				isolution <= numOptVariables + 1; 
				++isolution )
		{
			for ( iparam = 1; iparam <= numOptVariables; ++iparam )
			{
				fvary_param[iparam] = 0.0;
				fsolutions[isolution][iparam] = 0.0;
			}
			if ( isolution > 1 ) 
			{
				fvary_param[isolution - 1] = 1.0;
				fsolutions[isolution][isolution - 1] = 1.0;
			}
			vSetupTrial(fvary_param);
			fchi_square[isolution] = fChiSquare();
			if (fchi_square[isolution] < 0.0)
			{
				fvary_param = null;
				fchi_square = null;
				fsolutions = null;
				return -1;
			}
		}
		istatus = iAmoeba(fsolutions,fchi_square,
						numOptVariables,ftol,infunc);
		
		if ( istatus == -1 )
		{
			chitChat("Exceeded maximum number of iterations " + infunc[0]);
		}
		if ( istatus == -2 )
		{
			chitChat("Exceeded Time limit of " + simLimit);
			chitChat("Number of iterations " + infunc[0]);
		}
		if ( istatus == -3 )
		{
			chitChat("Reached Chi Square of " + simLimit);
			chitChat("Number of iterations " + infunc[0]);
		}
		if ( istatus == 0 )
		{
			chitChat("Function minimized after " + infunc[0] + " iterations.");
		}
		if ( istatus == -100 )
		{
			chitChat("Minimization interrupted");
		}

		vSetupTrial(fsolutions[1]);
		fchiba = fChiSquare();
		
		fvary_param = null;
		fchi_square = null;
		fsolutions = null;
		
		if (fchiba > 0.0)
		{
			chitChat("Ending Chi Square = " + fchiba);
			return 0;
		}
		else
		{
			chitChat("Minimization interrupted");
			return -1;
		}
	}
	void vSetupTrial(double x[])
	{
		int		ii;

		for (ii = 0; ii < numOptVariables; ++ii )
		{
			variable[lookupVector[ii]].value 
				= x[ii + 1] 
				* variable[lookupVector[ii]].step
				+ variable[lookupVector[ii]].initValue;
			if (variable[lookupVector[ii]].value 
				> variable[lookupVector[ii]].max)
			{
				variable[lookupVector[ii]].value 
					= variable[lookupVector[ii]].max;
			}
			if (variable[lookupVector[ii]].value 
				< variable[lookupVector[ii]].min)
			{
				variable[lookupVector[ii]].value 
					= variable[lookupVector[ii]].min;
			}
		}
		return;
	}
	/**
	 * @param cmessage
	 */
	public void chitChat(String cmessage)
	{
		if (chatterStream == null) return;
		chatterStream.println(cmessage);
	}
	/**
	 * @return the numTotalVariables
	 */
	public int getNumTotalVariables() {
		return numTotalVariables;
	}
	/**
	 * @param numTotalVariables the numTotalVariables to set
	 */
	public void setNumTotalVariables(int numTotalVariables) {
		this.numTotalVariables = numTotalVariables;
	}
	/**
	 * @return the numOptVariables
	 */
	public int getNumOptVariables() {
		return numOptVariables;
	}
	/**
	 * @param numOptVariables the numOptVariables to set
	 */
	public void setNumOptVariables(int numOptVariables) {
		this.numOptVariables = numOptVariables;
	}
	/**
	 * @return the simLimit
	 */
	public double getSimLimit() {
		return simLimit;
	}
	/**
	 * @param simLimit the simLimit to set
	 */
	public void setSimLimit(double simLimit) {
		this.simLimit = simLimit;
	}
	/**
	 * @return the optLimitType
	 */
	public int getOptLimitType() {
		return optLimitType;
	}
	/**
	 * @param optLimitType the optLimitType to set
	 */
	public void setOptLimitType(int optLimitType) {
		this.optLimitType = optLimitType;
	}
	/**
	 * @return the variable
	 */
	public OptimizeVariable[] getVariable() {
		return variable;
	}
	/**
	 * @param variable the variable to set
	 */
	public void setVariable(OptimizeVariable[] variable) {
		this.variable = variable;
	}
	/**
	 * @return the lookupVector
	 */
	public int[] getLookupVector() {
		return lookupVector;
	}
	/**
	 * @param lookupVector the lookupVector to set
	 */
	public void setLookupVector(int[] lookupVector) {
		this.lookupVector = lookupVector;
	}
	/**
	 * @param printStream 
	 * @param printStream
	 */
	public void setPrintStream(PrintStream printStream) {
		this.chatterStream = printStream;
	}

}
