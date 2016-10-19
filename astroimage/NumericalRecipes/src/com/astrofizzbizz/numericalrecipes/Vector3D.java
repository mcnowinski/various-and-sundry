package com.astrofizzbizz.numericalrecipes;

import java.io.Serializable;

/**
 * @author mcginnis
 *
 */
public class Vector3D implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5226258694020805322L;
	/**
	 * 
	 */
	public double[] vec = new double[3];
	
	/**
	 * 
	 */
	public Vector3D()
	{
		for (int ii = 0; ii < 3; ++ii)
		{
			vec[ii] = 0.0;
		}
	}
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3D(double x, double y, double z)
	{
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}
	/**
	 * @param v
	 */
	public void copy(Vector3D v) 
	{
		if (v == null)
		{
			for (int ii = 0; ii < 3; ++ii)
				vec[ii] = 0;
		}
		else
		{
			for (int ii = 0; ii < 3; ++ii)
			{
				vec[ii] = v.vec[ii];
			}
		}
	}
	/**
	 * @param v
	 */
	public Vector3D(Vector3D v)
	{
		copy(v);
	}
	/**
	 * @param v
	 * @return Vector3D
	 */
	public Vector3D plus(Vector3D v)
	{
		Vector3D sum = new Vector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii] + v.vec[ii];
		}
		return sum;
	}
	/**
	 * @param v
	 * @return Vector3D
	 */
	public Vector3D minus(Vector3D v)
	{
		Vector3D sum = new Vector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii] - v.vec[ii];
		}
		return sum;
	}
	/**
	 * @param d
	 * @return Vector3D
	 */
	public Vector3D times(double d)
	{
		Vector3D sum = new Vector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii] * d;
		}
		return sum;
	}
	/**
	 * @param v
	 * @return double
	 */
	public double dot(Vector3D v)
	{
		double	sum = 0.0;
		for (int ii = 0; ii < 3; ++ii)
		{
			sum = sum + vec[ii] * v.vec[ii];
		}
		return sum;
	}
	/**
	 * @return double
	 */
	public double magnitude()
	{
		double	sum = 0.0;
		for (int ii = 0; ii < 3; ++ii)
		{
			sum = sum + vec[ii] * vec[ii];
		}
		return Math.sqrt(sum);
	}
	/**
	 * @param v
	 * @return ComplexVector
	 */
	public Vector3D cross(Vector3D v)
	{
		Vector3D sum  = new Vector3D();
		Vector3D sum1 = new Vector3D();
		Vector3D sum2 = new Vector3D();
		
		sum1.vec[0] = vec[1];
		sum1.vec[1] = vec[2];
		sum1.vec[2] = vec[0];
		
		sum1.vec[0] = sum1.vec[0] * v.vec[2];
		sum1.vec[1] = sum1.vec[1] * v.vec[0];
		sum1.vec[2] = sum1.vec[2] * v.vec[1];

		sum2.vec[0] = vec[2];
		sum2.vec[1] = vec[0];
		sum2.vec[2] = vec[1];
		
		sum2.vec[0] = sum2.vec[0] * v.vec[1];
		sum2.vec[1] = sum2.vec[1] * v.vec[2];
		sum2.vec[2] = sum2.vec[2] * v.vec[0];
		
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = sum1.vec[ii] - sum2.vec[ii];
		}
		return sum;
	}
	/**
	 * @param cv
	 * @return ComplexVector3D
	 */
	public ComplexVector3D cross(ComplexVector3D cv)
	{
		ComplexVector3D cvthis = new ComplexVector3D(this);
		return cvthis.cross(cv);
	}
	/**
	 * @param cv
	 * @return Complex
	 */
	public Complex dot(ComplexVector3D cv)
	{
		ComplexVector3D cvthis = new ComplexVector3D(this);
		return cvthis.dot(cv);
	}
	/**
	 * @param cv
	 * @return ComplexVector3D
	 */
	public ComplexVector3D plus(ComplexVector3D cv)
	{
		ComplexVector3D cvthis = new ComplexVector3D(this);
		return cvthis.plus(cv);
	}
	/**
	 * @param cv
	 * @return ComplexVector3D
	 */
	public ComplexVector3D minus(ComplexVector3D cv)
	{
		ComplexVector3D cvthis = new ComplexVector3D(this);
		return cvthis.minus(cv);
	}
	/**
	 * @param c
	 * @return ComplexVector3D
	 */
	public ComplexVector3D times(Complex c)
	{
		ComplexVector3D cvthis = new ComplexVector3D(this);
		return cvthis.times(c);
	}
	/**
	 * @return String
	 * 
	 */
	public String dumpData()
	{
		String sdata = "";
		for (int ii = 0; ii < 3; ++ii)
		{
			sdata = sdata + "Comp " + ii + "  : " + vec[ii] + "\n";
		}
		return sdata;
	}
}
