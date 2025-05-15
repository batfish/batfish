package jdd.util.math;

/**
 * prime-number support, mostly used in the hash-tables
 * <p>
 * note: we consider zero to not be a prime :(
 */
public final class Prime {

	private static final int NUM_TRIALS = 8;

	private static final long witness(long a, long i, long n) {
		if(i == 0) return 1;
		long x = witness( a, i /2, n);
		if(x == 0) return 0;
		long y = (x * x) % n;
		if( y == 1 && x != 1 && x != n -1) return 0;
		if( (i %2) == 1) y = (a * y) % n;
		return y;
	}

	/**
	 * prime test
	 * @return true if "n" is a prime number
	 */
	public static final boolean isPrime(int n) {
		// small primes ?
		if(n < 20) {
			if( (n == 1) || (n == 2) || (n == 3) || (n == 5) || (n == 7) || (n == 11) || (n == 13) || (n == 17) || (n == 19))
				return true;
		}

		// multiple of small primes?
		if(( (n % 2) == 0)||  ( (n % 3) == 0)||  ( (n % 5) == 0)||  ( (n % 7) == 0)||  ( (n % 11) == 0)||  ( (n % 13) == 0)||  ( (n % 17) == 0)||  ( (n % 19) == 0))
			return false;

		// ... not? take out the big guns now:
		for(int c = 0; c < NUM_TRIALS; c++)
			if( witness( 2 + (long)( Math.random() * (n -2)), n-1, n) != 1)
				return false;

		// yes sir, we have a winner
		return true;
	}

	/**
	 * get the closest prime larger than "n", including "n" itself
	 */
	public static final int nextPrime(int n) {
		if( (n % 2) == 0) n++;
		for(;;) {
			if(isPrime(n)) return n;
			n += 2;
		}
	}


	/**
	 * get the closest prime smaller than "n", including "n" itself
	 */
	public static final int prevPrime(int n) {
		if( (n % 2) == 0) n--;
		for(;;) {
			if(isPrime(n)) return n;
			n -= 2;
		}
	}

}
