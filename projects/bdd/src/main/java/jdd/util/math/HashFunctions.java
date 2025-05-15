
package jdd.util.math;

/**
 * This class contains some useful (?) hash functions.
 *
 * <p>Note: the hash numbers may be smaller than zero.
 * you may AND them with the mask 0x7FFFFFFF to fix that.
 *
 * <p>
 * Each hash function in good in its own place.
 * for example, pair() may give bad results in the chi^2 test,
 * but it is the better hash in some situations.
 *
 */

// TODO:
// 1. the hash functions of TiGER should be added to!
// 2. the mix functions seem good for small tables but less good when the table grows ??
//     that why we changed >>> 16 to >>> 8, but we really should have two different ones for
//     small (caches) and large (nodetable) n.

public final class HashFunctions {

	// ------- [ mix functions ] ----------------------


	/**
	 * mix the bits in <tt>i</tt> in some clever way.
	 *
	 * <p> Use this one when you dont know much about <tt>i</tt>.
	 * NOTE: it is slower then the others
	 */
	public static final int mix(int i) {
		// TODO: using a very large prime might be equally good!
		return i ^(i >>> 8);


		/*
		// my stupid mix: rotate every other bit 8 positions.
		int i1 = i & 0x55555555;
		int i2 = i & 0xAAAAAAAA;
		i2 = (i2 << 8) | (i2 >>> (32 - 8));
		return i1 | i2;
		*/
	}

	/**
	 * Thomas Wang's 32 bit mix function
	 */
	public static final int mix_wang(int i) {
		i += ~(i << 15);
		i ^=  (i >>> 10);
		i += ~(i << 3);
		i ^=  (i >>> 6);
		i += ~(i << 11);
		i ^=  (i >>> 16);
		return i;
	}

	/**
	 * Robert Jenkins' bit mix function
	 */
	public static final int mix_jenkins(int i) {
		i += (i << 12);
		i ^= (i >> 22);
		i += (i <<  4);
		i ^= (i >>  9);
		i += (i << 10);
		i ^= (i >>  2);
		i += (i <<  7);
		i ^= (i >> 12);

		return i;
	}

	// ----- [ hash functions based on pair() ]------------
	/** the pair functions itself */
	private static final long pair(long i, long j) {
		return ((( i + j) * (i + j +1)) >>>1) + 1;
	}

	/** pair-hash for two elements */
	public static final int hash_pair(int a, int b) {
		return (int)pair(a,b);
	}

	/** pair-hash for three elements */
	public static final int hash_pair(int a, int b, int c ) {
		return (int)pair(a,pair(b,c));
	}


	// ----- [ hash functions based on prime multiplication ]------------
	/**
	 * these numbers are the prime factors used int the  hash_prime() functions.
	 * <p>
	 * The hash functions based on prime numbers are not the best but probably
	 * the fastest hash functions. There are however cases when they give very
	 * bad mixing (compare to weak keys in cryptography)
	 */
	// I think these values are stolen  from CUDD:
	private static final int	DD_P1 = 12582917, DD_P2 = 4256249,
														DD_P3 = 741457, DD_P4 = 1618033999;

	/** prime-hash for two elements */
	public static final int hash_prime(int a, int b) {
		return (a * DD_P1) + (b * DD_P2);
	}

	/** prime-hash for three elements */
	public static final int hash_prime(int a, int b, int c ) {
		return (a * DD_P1) + (b * DD_P2) + (c * DD_P3);
	}


	// ------[ hash functions based on Bob Jenkins ideas ] -----------------------
	/**
	 * This hash function was suggested by Bob Jenkins (of Oracle).
	 * It performs equally well, but is a bit slower than the others.
	 */
	public static final int hash_jenkins(int a, int b, int c) {
		// we only need the mix() function. since the length is always 12,
		// the rest of Bob's functions is irrelevant
		a -= b; a -= c; a ^= (c >>> 13);
		b -= c; b -= a; b ^= (a << 8);
		c -= a; c -= b; c ^= (b >>> 13);

		a -= b; a -= c; a ^= (c >>> 12);
		b -= c; b -= a; b ^= (a << 16);
		c -= a; c -= b; c ^= (b >>> 5);

		a -= b; a -= c; a ^= (c >>> 3);
		b -= c; b -= a; b ^= (a << 10);
		c -= a; c -= b; c ^= (b >>> 15);

		return c;
	}

	// ------[ The Fowler/Noll/Vo hash function ] --------------------------
	private static final int FNV_PRIME = 16777619;		/** The magical FNV prime */
	private static final int FNV_OFFSET = 0x811C9E29; /** 2166136361, FNV offset basis */

	/** one round of FNV */
	private static final int hash_FNV_round(int init, int word) {
		init = (init * FNV_PRIME) ^ (word & 0xFF);
		init = (init * FNV_PRIME) ^ ((word >> 8)  &0xFF);
		init = (init * FNV_PRIME) ^ ((word >> 16)  &0xFF);
		init = (init * FNV_PRIME) ^ ((word >> 24)  &0xFF);
		return init;
	}

	/**
	 * 32-bit hash of FNV with 3 32-bit words input.
	 * <p>
	 * This hash functions is probably best for strings than 3-tupple integers.
	 */
	public static final int hash_FNV(int a, int b, int c) {
		int hash = FNV_OFFSET;
		hash = hash_FNV_round(hash, a);
		hash = hash_FNV_round(hash, b);
		hash = hash_FNV_round(hash, c);
		return hash;
	}

	/**
	 * hash the 32-bit array with FNV
	 */
	public static final int hash_FNV(int [] data, int offset, int len) {
		int hash = FNV_OFFSET;
		for(int i = 0; i < len; i++)
			hash = hash_FNV_round(hash, data[offset + i]);
		return hash;
	}
}
