
package jdd.examples;


import jdd.bdd.DoubleCache;

/**
 * A simple example that has nothing to do with BDDs :(
 * <p>
 * This class demonstrates how the Fibonacci numbers can be computed
 * recursively but still a bit efficient by using a cache...
 * <p>
 * See this more as a (very simple) tutorial on how the caches work in JDD
 * than an example on what JDD can do.
 * <p>
 * and oh yeah...for those of you who slept during the math lessons:
 * <pre>
 *   F(0) = 0
 *   F(1) = 1
 *   F(n) = F(n-1) + F(n-2)
 * </pre>
 */


public class Fibonacci  {

	// we make it class-global so we don't have to pass it on in all recursive calls
	private static DoubleCache dc;

	/**
	 * recursively (!!!) compute the fibonacci number for <tt>n</tt>
	 *
	 */
	public static double fibonacci(int n) {
		if(n < 0) return -1; // INVALID n!

		dc = new DoubleCache("fibonacci", n + 3);
		double ret = fibonacci_rec(n);
		dc.showStats(); // DEBUG
		dc = null;
		return ret;

	}

	private static double fibonacci_rec(int n) {
		// terminate when n = either 0 or 1, you see this in the BDD code too,
		// but there, we are dealing with the terminal nodes ONE and ZERO
		if(n < 2) return n;

		if(dc.lookup(n)) return dc.answer;
		int hash = dc.hash_value;

		double ret = fibonacci_rec(n-1) + fibonacci_rec(n-2);

		dc.insert(hash, n, ret);
		return ret;
	}

	public static void main(String [] args) {
		if(args.length != 1) {
			System.err.println("Usage: Java jdd.examples.Fibonacci n");
			System.err.println("      n must be a positive integer.");
			System.err.println("      if n is too large, you will see a java.lang.StackOverflowError :(");
		} else {
			int n = Integer.parseInt( args[0] );
			long t = System.currentTimeMillis();
			double f = fibonacci(n);
			t = System.currentTimeMillis() - t;
			System.out.println("In " + t + " ms:  F(" + n + ") = " + f );
		}
	}
}
