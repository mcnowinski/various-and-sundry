package com.astrofizzbizz.numericalrecipes;

import java.io.Serializable;

/**
 * @author mcginnis
 *
 */
public class ComplexVector3D implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7471444368177319847L;
	/**
	 * 
	 */
	public Complex[] vec = new Complex[3];
	
	/**
	 * 
	 */
	public ComplexVector3D()
	{
		for (int ii = 0; ii < 3; ++ii)
		{
			vec[ii] = new Complex();
		}
	}
	/**
	 * @param cv
	 */
	public void copy(ComplexVector3D cv) 
	{
		if (cv == null)
		{
			for (int ii = 0; ii < 3; ++ii)
				vec[ii].re = vec[ii].im = 0;
		}
		else
		{
			for (int ii = 0; ii < 3; ++ii)
			{
				vec[ii].re = cv.vec[ii].re;
				vec[ii].im = cv.vec[ii].im;
			}
		}
	}
	/**
	 * @param cv
	 */
	public ComplexVector3D(ComplexVector3D cv)
	{
		copy(cv);
	}
	/**
	 * @param v
	 */
	public ComplexVector3D(Vector3D v)
	{
		for (int ii = 0; ii < 3; ++ii)
		{
			vec[ii] = new Complex(v.vec[ii]);
		}
	}
	/**
	 * @param cv
	 * @return ComplexVector
	 */
	public ComplexVector3D plus(ComplexVector3D cv)
	{
		ComplexVector3D sum = new ComplexVector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii].plus(cv.vec[ii]);
		}
		return sum;
	}
	/**
	 * @param v
	 * @return ComplexVector3D
	 */
	public ComplexVector3D plus(Vector3D v)
	{
		ComplexVector3D cv = new ComplexVector3D(v);
		return plus(cv);
	}

	/**
	 * @param cv
	 * @return ComplexVector
	 */
	public ComplexVector3D minus(ComplexVector3D cv)
	{
		ComplexVector3D sum = new ComplexVector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii].minus(cv.vec[ii]);
		}
		return sum;
	}
	/**
	 * @param v
	 * @return ComplexVector3D
	 */
	public ComplexVector3D minus(Vector3D v)
	{
		ComplexVector3D cv = new ComplexVector3D(v);
		return minus(cv);
	}
	/**
	 * @param c
	 * @return ComplexVector
	 */
	public ComplexVector3D times(Complex c)
	{
		ComplexVector3D sum = new ComplexVector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii].times(c);
		}
		return sum;
	}
	/**
	 * @param d
	 * @return ComplexVector
	 */
	public ComplexVector3D times(double d)
	{
		ComplexVector3D sum = new ComplexVector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii].times(d);
		}
		return sum;
	}
	/**
	 * @return ComplexVector
	 */
	public ComplexVector3D conj()
	{
		ComplexVector3D sum = new ComplexVector3D();
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = vec[ii].conj();
		}
		return sum;
	}
	/**
	 * @param cv
	 * @return Complex
	 */
	public Complex dot(ComplexVector3D cv)
	{
		Complex	sum = new Complex();
		for (int ii = 0; ii < 3; ++ii)
		{
			Complex itemSum = vec[ii].times(cv.vec[ii]);
			sum = sum.plus(itemSum);
		}
		return sum;
	}
	/**
	 * @param v
	 * @return Complex
	 */
	public Complex dot(Vector3D v)
	{
		ComplexVector3D cv = new ComplexVector3D(v);
		return dot(cv);
	}
	/**
	 * @return double
	 */
	public double magnitude()
	{
		ComplexVector3D convec = conj();
		Complex dotprod = dot(convec);
		return Math.sqrt(dotprod.re);
	}
	/**
	 * @param cv
	 * @return ComplexVector
	 */
	public ComplexVector3D cross(ComplexVector3D cv)
	{
		ComplexVector3D sum  = new ComplexVector3D();
		ComplexVector3D sum1 = new ComplexVector3D();
		ComplexVector3D sum2 = new ComplexVector3D();
		
		sum1.vec[0] = new Complex(vec[1]);
		sum1.vec[1] = new Complex(vec[2]);
		sum1.vec[2] = new Complex(vec[0]);
		
		sum1.vec[0] = sum1.vec[0].times(cv.vec[2]);
		sum1.vec[1] = sum1.vec[1].times(cv.vec[0]);
		sum1.vec[2] = sum1.vec[2].times(cv.vec[1]);

		sum2.vec[0] = new Complex(vec[2]);
		sum2.vec[1] = new Complex(vec[0]);
		sum2.vec[2] = new Complex(vec[1]);
		
		sum2.vec[0] = sum2.vec[0].times(cv.vec[1]);
		sum2.vec[1] = sum2.vec[1].times(cv.vec[2]);
		sum2.vec[2] = sum2.vec[2].times(cv.vec[0]);
		
		for (int ii = 0; ii < 3; ++ii)
		{
			sum.vec[ii] = sum1.vec[ii].minus(sum2.vec[ii]);
		}
		return sum;
	}
	/**
	 * @param v
	 * @return ComplexVector3D
	 */
	public ComplexVector3D cross(Vector3D v)
	{
		ComplexVector3D cv = new ComplexVector3D(v);
		ComplexVector3D sum  = cross(cv);
		return sum;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		ComplexVector3D cv1 = new ComplexVector3D();
		ComplexVector3D cv2 = new ComplexVector3D();
		
		cv1.vec[0] = new Complex(1.0,0.0);
		cv1.vec[1] = new Complex(2.0,2.0);
		cv1.vec[2] = new Complex(3.0,1.0);

		cv2.vec[0] = new Complex(4.0,6.0);
		cv2.vec[1] = new Complex(5.0,5.0);
		cv2.vec[2] = new Complex(6.0,4.0);
		
		Complex dotsum = cv1.dot(cv2);
		System.out.println("real = " + dotsum.re + " imag = " + dotsum.im);
		
		ComplexVector3D crossvec = cv1.cross(cv2);
		for (int ii = 0; ii < 3; ++ii)
		{
			System.out.println("real = " + crossvec.vec[ii].re + " imag = " + crossvec.vec[ii].im);
		}
	}
	/**
	 * @return String
	 */
	public String dumpData()
	{
		String sdata = "";
		for (int ii = 0; ii < 3; ++ii)
		{
			sdata = sdata + "Comp " + ii + "  : " + vec[ii].dumpData() + "\n";
		}
		return sdata;
	}
}
