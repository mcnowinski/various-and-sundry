package com.astrofizzbizz.numericalrecipes;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/*************************************************************************
 *  Compilation:  javac FFT.java
 *  Execution:    java FFT N
 *  Dependencies: Complex.java
 *
 *  Compute the FFT and inverse FFT of a length N complex sequence.
 *  Bare bones implementation that runs in O(N log N) time. Our goal
 *  is to optimize the clarity of the code, rather than performance.
 *
 *  Limitations
 *  -----------
 *   -  assumes N is a power of 2
 *
 *   -  not the most memory efficient algorithm (because it uses
 *      an object type for representing complex numbers and because
 *      it re-allocates memory for the subarray, instead of doing
 *      in-place or reusing a single temporary array)
 *  
 *************************************************************************/

public class FFT {
	
	public static double PI = 3.14159265358979323846;

// fft is e^(-1j * K * Theta)
    // compute the FFT of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
    public static Complex[] fft(double[] x) 
    {
    	int kmax = x.length;
    	Complex[] cx = new Complex[kmax];
    	for (int ii = 0; ii < kmax; ++ii)
    	{
    		cx[ii] = new Complex(x[ii], 0.0);
    	}
    	Complex[] f = fft(cx);
    	return f;
    }


 // ifft is (1/N) * e^(+1j * K * Theta)
 // compute the inverse FFT of x[], assuming its length is a power of 2
    /**
     * @param x
     * @return ifft
     */
    public static Complex[] ifft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];

        // take conjugate
        for (int i = 0; i < N; i++) {
            y[i] = x[i].conj();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < N; i++) {
            y[i] = y[i].conj();
        }

        // divide by N
        for (int i = 0; i < N; i++) {
            y[i] = y[i].times(1.0 / N);
        }

        return y;

    }
    public static Complex[] dft(Complex[] x)
    {
    	int N = x.length;
    	Complex[] y = new Complex[N];
    	for (int kk = 0; kk < N; ++kk)
    	{
    		y[kk] = new Complex();
	    	for (int ii = 0; ii < N; ++ii)
	    	{
	    		double arg = 2.0 * PI * ((double) ii) * ((double) kk) / ((double) N);
	    		Complex phase = new Complex(Math.cos(arg), Math.sin(arg));
	    		y[kk] = y[kk].plus(x[ii].times(phase));
	    	}
    	}
    	return y;
    }
    public static Complex[] idft(Complex[] x)
    {
    	int N = x.length;
    	Complex[] y = new Complex[N];
    	for (int kk = 0; kk < N; ++kk)
    	{
    		y[kk] = new Complex();
	    	for (int ii = 0; ii < N; ++ii)
	    	{
	    		double arg = -2.0 * PI * ((double) ii) * ((double) kk) / ((double) N);
	    		Complex phase = new Complex(Math.cos(arg), Math.sin(arg));
	    		y[kk] = y[kk].plus(x[ii].times(phase));
	    	}
	    	y[kk] = y[kk].dividedBy((double) N);
    	}
    	return y;
    }

    // compute the circular convolution of x and y
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) { throw new RuntimeException("Dimensions don't agree"); }

        int N = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++) {
            c[i] = a[i].times(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }


    // compute the linear convolution of x and y
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = ZERO;

        return cconvolve(a, b);
    }

    // display an array of Complex numbers to standard output
    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }


   /*********************************************************************
    *  Test client and sample execution
    *
    *  % java FFT 4
    *  x
    *  -------------------
    *  -0.03480425839330703
    *  0.07910192950176387
    *  0.7233322451735928
    *  0.1659819820667019
    *
    *  y = fft(x)
    *  -------------------
    *  0.9336118983487516
    *  -0.7581365035668999 + 0.08688005256493803i
    *  0.44344407521182005
    *  -0.7581365035668999 - 0.08688005256493803i
    *
    *  z = ifft(y)
    *  -------------------
    *  -0.03480425839330703
    *  0.07910192950176387 + 2.6599344570851287E-18i
    *  0.7233322451735928
    *  0.1659819820667019 - 2.6599344570851287E-18i
    *
    *  c = cconvolve(x, x)
    *  -------------------
    *  0.5506798633981853
    *  0.23461407150576394 - 4.033186818023279E-18i
    *  -0.016542951108772352
    *  0.10288019294318276 + 4.033186818023279E-18i
    *
    *  d = convolve(x, x)
    *  -------------------
    *  0.001211336402308083 - 3.122502256758253E-17i
    *  -0.005506167987577068 - 5.058885073636224E-17i
    *  -0.044092969479563274 + 2.1934338938072244E-18i
    *  0.10288019294318276 - 3.6147323062478115E-17i
    *  0.5494685269958772 + 3.122502256758253E-17i
    *  0.240120239493341 + 4.655566391833896E-17i
    *  0.02755001837079092 - 2.1934338938072244E-18i
    *  4.01805098805014E-17i
 * @throws FileNotFoundException 
    *
    *********************************************************************/

    public static void main(String[] args) throws FileNotFoundException { 
        int N = 32;
        int K = 5;
        Complex[] w = new Complex[N];
        double phase = -1.0 * PI / 4.0;

        // original data
        
        for (int i = 0; i < N; i++) {
            w[i] = new Complex(Math.cos(2.0 * PI * ((double) i) * ((double) K) / ((double) N) + phase), 0);
            w[i] = new Complex();
        }
 
        // FFT of original data
        Complex[] x = fft(w);

        // FFT of original data
        Complex[] y = ifft(w);
 
 
        // DFT of original data
        Complex[] z = dft(w);

        // DFT of original data
        Complex[] zz = idft(w);

        PrintStream ps = new PrintStream("FFT.dat");
        for (int i = 0; i < N; i++) 
        {
            ps.println(i + "," + w[i].re + "," + w[i].im + "," + x[i].re + "," + x[i].im + "," + y[i].re + "," + y[i].im + "," + z[i].re + "," + z[i].im + "," + zz[i].re + "," + zz[i].im);
        }
        ps.close();
    }

}

