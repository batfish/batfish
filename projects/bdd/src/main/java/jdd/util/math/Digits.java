
package jdd.util.math;


/**
 * some common simple operations involving numbers are gathered here
 *
 */

public class Digits {

	public static int log2_ceil(int x) {
		int ret = 1;
		while( (1L << ret) < x)
			ret++;
		return ret;
	}


	public static int closest_log2(int x) {
		int lg2 = log2_ceil(x);
		long d1 = (1L << lg2) - x;
		long d2 = x - (1L << (lg2 -1));
		return d1 < d2 ? lg2 : lg2-1;
	}



	/** given a set of n elements, return the number of unique pairs */
	public static int maxUniquePairs(int n) {
		if(n == 0 || n == 1) return 0;
		if(n == 2) return 1;
		return (n-1) + maxUniquePairs(n-1);
	}


	/** return a number [0..1] as a percent value xx.yy */
	public static double getPercent(double x) {
		return getWithDecimals(100.0 * x, 2);
	}

	/** get number x with n decimals */
	public static double getWithDecimals(double x, int n) {
		double dec = Math.pow(10, n);
		return Math.round( x * dec) / dec;
	}


	public static String prettify(long n) {
		return prettifyWith(n, 1000);
	}
	public static String prettify1024(long n) {
		return prettifyWith(n, 1024);
	}
	private static String prettifyWith(long n, long k_) {
		final long m_ =  k_ * k_;
		final long g_ =  k_ * m_;
		final long t_ =  k_ * g_;

		if(n > t_)  return String.format("%.2fT", n / (double)t_);
		if(n > g_)  return String.format("%.2fG", n / (double)g_);
		if(n > m_)  return String.format("%.2fM", n / (double)m_);
		if(n > k_)  return String.format("%.2fK", n / (double)k_);
		return n + "";
	}
}
