package com.astrofizzbizz.numericalrecipes;

import java.io.Serializable;


/**
 * @author mcginnis
 *
 * A complex number, with a real and an imaginary part.  (Possibley to be replaced with 
 * a class that has better support for complex arithmetic and functions of a complex variable.)
 */

public class Complex implements Serializable
{
	  
	/**
	 * 
	 */
	private static final long serialVersionUID = 609325012699107013L;
	/**
	 * 
	 */
	public double re;
	/**
	 * 
	 */
	public double im;
	  
	/**
	   * Create a complex number initially equal to zero
	   */
	public Complex() 
	{
		re = 0.0;
		im = 0.0;
	}
	  
	/**
	 * Create a complex number initially equal to the real number x.
	 * @param x 
 	 */
	public Complex(double x) 
	{
		re = x;
		im = 0.0;
	}
	  
	/**
	 * Create a complex number initially equal to x + iy
	 * @param x 
	 * @param y 
	 */
	public Complex(double x, double y) 
	{
		re = x;
		im = y;
	}
	  
	/**
	 * Create a new complex number that is initially equal to a given complex number.
	 * @param c The complex number to be copied.  If null, it is treated as zero.
	 */
	  public Complex(Complex c) 
	  {
	    copy(c);
	  }
	  
	/**
	 * 
	 */
	public static final Complex ZERO_C = new Complex(0,0);
	  
	/**
	 * 
	 */
	public static final Complex ONE_C = new Complex(1,0);
	  
	/**
	 * 
	 */
	public static final Complex I_C = new Complex(0,1);
	  
	  /**
	   * Returns true if obj is equal to this complex number.  If obj is null or is not
	   * of type Complex, the return value is false.
	   */
	public boolean equals(Object obj) 
	{
		try 
		{
			Complex c = (Complex)obj;
			return c.re == re && c.im == im;
		}
		catch (Exception e) 
		{
			return false;
		}
	}
	  
	/**
	 * Computes the conjugate of a complex number.
	 * @return Complex
	 */
	public Complex conj() 
	{
		return new Complex( re, -im );
	}

	  
	/**
	 * Returns the complex number (r*cos(theta)) + i*(r*sin(theta)).
	 * @param r 
	 * @param theta 
	 * @return Complex
	 */
	public static Complex polar(double r, double theta) 
	{
		return new Complex(r*Math.cos(theta),r*Math.sin(theta));
	}
	  
	/**
	 * Sets this complex number equal to a copy of a given number.
	 * @param c The number to be copied; if null, the number is treated as zero.
	 */
	public void copy(Complex c) 
	{
		if (c == null)
			re = im = 0;
		else 
		{
			re = c.re;
			im = c.im;
		}
	}
	  
	/**
	 * Returns this + c; c must be non-null.
	 * @param c 
	 * @return Complex
	 */
	public Complex plus(Complex c) 
	{
		return new Complex(re + c.re, im + c.im);
	}
	  
	/**
	 * Returns this - c; c must be non-null.
	 * @param c 
	 * @return Complex
	 */
	public Complex minus(Complex c) 
	{
		return new Complex(re - c.re, im - c.im);
	}
	  
	/**
	 * Returns this * c; c must be non-null.
	 * @param c 
	 * @return Complex
	 */
	public Complex times(Complex c) 
	{
		return new Complex(re*c.re - im*c.im, re*c.im + im*c.re);
	}

	/**
	 * Returns this / c; c must be non-null.
	 * @param c 
	 * @return Complex
	 */
	public Complex dividedBy(Complex c) 
	{
		double denom = c.re*c.re + c.im*c.im;
		if (denom == 0)
			return new Complex(Double.NaN,Double.NaN);
		else
			return new Complex( (re*c.re+im*c.im)/denom, (im*c.re-re*c.im)/denom);
	}
	  
	/**
	 * @param x
	 * @return Complex
	 */
	public Complex times(double x) 
	{
		return new Complex(re*x, im*x);
	}
	  
	/**
	 * @param x
	 * @return Complex
	 */
	public Complex plus(double x) 
	{
		return new Complex(re+x, im);
	}
	  
	/**
	 * @param x
	 * @return Complex
	 */
	public Complex minus(double x) 
	{
		return new Complex(re-x, im);
	}
	  
	/**
	 * @param x
	 * @return Complex
	 */
	public Complex dividedBy(double x) 
	{
		return new Complex(re/x, im/x);
	}
	/**
	 * Returns the absolute value squared of this.
	 * @return real part squared plus imaginary part squared
	 */
	public double abs2() 
	{
		return (re*re + im*im);
	}
	  
	/**
	 * Returns the absolute value, "r" in polar coordinates, of this.
	 * @return the square root of (real part squared plus imaginary part squared)
	 */
	public double magnitude() 
	{
		return Math.sqrt(magnitudeSquared());
	}
	public double magnitudeSquared()
	{
		return (re*re + im*im);
	}
	  
	/**
	 * Returns arg(this), the angular polar coordinate of this complex number, in the range -pi to pi.
	 * The return value is simply Math.atan2(imaginary part, real part).
	 * @return double
	 */
	public double theta() 
	{
		return Math.atan2(im,re);
	}
	  
	/**
	 * Computes the complex exponential function, e^z, where z is this complex number.
	 * @return Complex
	 */
	public Complex exponential() 
	{
		double length = Math.exp(re);
		return new Complex( length*Math.cos(im), length*Math.sin(im) );
	}
	  
	/**
	 * Computes the complex reciprocal function, 1/z, where z is this complex number.
	 * @return Complex
	 */
	public Complex inverse() 
	{
		double length = re*re+im*im;
		return new Complex( re/length, -im/length );
	}
	  
	/**
	 * @return Complex
	 */
	public Complex log() 
	{
		double modulus = Math.sqrt(re*re + im*im);
		double arg = Math.atan2(im,re);
		return new Complex(Math.log(modulus), arg);
	}
	  	  
	/**
	 * @param x
	 * @return double
	 */
	public double sinh(double x) 
	{
		return (Math.exp(x) - Math.exp(-x))/2;
	}
	   
	/**
	 * @param x
	 * @return double
	 */
	public double cosh(double x) 
	{
		return (Math.exp(x) + Math.exp(-x))/2;
	}
	  
	/**
	 * @return complex
	 */
	public Complex sine() 
	{
		double x, y;
		Complex z = new Complex(0.0,0.0);
		x = re;
		y = im;
		z.re = Math.sin(x) * cosh(y);
		z.im = Math.cos(x) * sinh(y);
		return z;
	}
	  
	/**
	 * @param x
	 * @return Complex
	 */
	public Complex power(double x) 
	{
		double modulus = Math.sqrt(re*re + im*im);
		double arg = Math.atan2(im,re);
		double log_re = Math.log(modulus);
		double log_im = arg;
		double x_log_re = x * log_re;
		double x_log_im = x * log_im;
		double modulus_ans = Math.exp(x_log_re);
		return new Complex(modulus_ans*Math.cos(x_log_im), modulus_ans*Math.sin(x_log_im));
	}
	  
	/**
	 * Returns a complex k-th root of this complex number.  The root that is returned is 
	 * the one with the smallest positive arg.
	 * (If k is 0, the return value is 1.  If k is negative, the value is 1/integerRoot(-k).)
	 * @param k 
	 * @return Complex
	 */
	public Complex integerRoot(int k) 
	{
		double a,b;
		boolean neg = false;
		if (k < 0) 
		{
			k = -k;
			neg = true;
		}
		if (k == 0) 
		{ 
			a = 1;
			b = 0;
		}
	    else if (k == 1) 
	    {
	    	a = re;
	    	b = im;
	    }
	    else 
	    {
	    	double length = magnitude();
	    	double angle = theta();
	    	if (angle < 0)
	    		angle += Math.PI*2;
	    	length = Math.pow(length,1.0/k);
	    	angle = angle / k;
	    	a = length*Math.cos(angle);
	    	b = length*Math.sin(angle);
	    }
		if (neg) 
		{
			double denom = a*a + b*b;
			a = a/denom;
			b = -b/denom;
		}
		return new Complex(a,b);
	}
	/**
	 * @return String
	 * 
	 */
	public String dumpData()
	{
		return " re = " + re + " im = " + im + " ";
	}
	  
}
