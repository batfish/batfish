
package jdd.util;



// TODO:
// the following functions have no testbed yet:
// clone(), count, reverse, shuffle, disturb, permutation, equals


import jdd.util.math.FastRandom;

/**
 * some common operations involving arrays has been gathered here.
 * The idea is to concenterate all loops in the same class so it can
 * be optimized by the JVM (loop unrolloing, SIMD etc).
 *
 * <p> we used to have all the loops unrolled here, but it turns out that
 * the latest JVMs (1.4.2_02 and above) do a much better job.
 */

public final class Array {


	// [resize]------------------------------------------------
	/**
	 * this is an implementation of realloc() for integers.
	 * the <tt>old_size</tt> old members are copied to the new array
	 */
	public static final int [] resize(final int [] old, int old_size, int new_size) {
		int [] ret = Allocator.allocateIntArray(new_size);
		if(old_size > new_size) old_size = new_size;
		fast_copy(old, 0, ret, 0, old_size);
		return ret;
	}


	/**
	 * this is an implementation of realloc() for short integers.
	 * the <tt>old_size</tt> old members are copied to the new array
	 */
	public static final short [] resize(final short [] old, int old_size, int new_size) {
		short [] ret = Allocator.allocateShortArray(new_size);
		if(old_size > new_size) old_size = new_size;
		fast_copy(old, 0, ret, 0, old_size);
		return ret;
	}

	// [ copy ] -------------------------------------------------------------

	/**
	 * a fast (?) function for copying chunks of an array to another (possibly same) array.
	 * <p>
	 * BEWRAE: stupid System.arraycopy() cant hande overlapping arrays correctly
	 */
	public static final void copy(int [] from, int []to, int len, int from_offset, int to_offset) {
		if(from == to) {
			if(from_offset < to_offset &&( from_offset + len >= to_offset )) {
				fast_copy_backward(from, from_offset, to, to_offset, len);
				return;
			}
			// XXX: 1. do we need to repeat this when its the other way around?
			// XXX: 2. how about very small diffrences (like 1 or 2 elements) ???
		}
		fast_copy(from, from_offset, to, to_offset, len);
	}

	// ----------------------------------------------------------------------------------------

	/** fast copy of arrays of integers */
	private static final void fast_copy(int [] y, int o1, int []x, int o2, int len) {
		System.arraycopy(y,o1,x,o2, len);
	}

	/** fast copy of arrays of short integers */
	private static final void fast_copy( short [] y, int o1, short []x, int o2, int len) {
			System.arraycopy(y,o1,x,o2, len);
	}

	// ----------------------------------------------------------------------------------------
	/**
	 * unrolled code for copying in an array of integers where the source and destination
	 * may/may not overlap without creating any problems
	 */
	private static final void fast_copy_forward( int [] y, int o1, int []x, int o2, int len) {
		for(int i = 0; i < len; i++)
			x[o2+i] = y[o1+i];
	}


	/**
	 * unrolled code for copying in an array of short integers, where the source and destination
	 * may/may not overlap without creating any problems
	 */
	private static final void fast_copy_forward( short [] y, int o1, short []x, int o2, int len) {
		for(int i = 0; i < len; i++)
			x[o2+i] = y[o1+i];
	}
	// -------------------------------------------------------------------------------------------

	// XXX: there is probably an error in this VERY critical function that i cant see right now!
	/**
	 * unrolled code for copying in an array of short integers, where the source and destination
	 * overlaps. to avoid overwriting the yet-to-be-copied members, we have to do the copy in
	 * backward direction.
	 */
	private static final void fast_copy_backward( int [] y, int o1, int []x, int o2, int len) {

		// while(len-- != 0) x[o2+len] = y[o1+len] ;
		while(len != 0) {
			len--;
			x[o2+len] = y[o1+len] ;
		}
	}


	// [clone ]------------------------------------------------

	/** cone an array of integers and its members */
	public static final int [] clone(int [] old) {
		int [] ret = Allocator.allocateIntArray(old.length);
		fast_copy(old, 0, ret, 0, old.length);
		return ret;
	}

	public static final boolean [] clone(boolean [] old) {
		boolean [] ret =new boolean[old.length];
		System.arraycopy(old, 0, ret, 0, old.length);
		return ret;
	}

	// [set]------------------------------------------------

	/** set the elements of "x" to "val" (faster than  for(...) x[i] = val; )*/
	public static final void set(int [] x, int val) {
		set(x, val, x.length);
	}


	/** set the first "length" elements of "x" to "val" (faster than  for(...) x[i] = val; )*/
	public static final void set(int [] x, int val, int length) {
		for(int i = length; i != 0; ) x[--i] = val; // BACKWARD
	}
	// --------------------------------------------------

	/** set the members of the array x to val */
	public static final void set(boolean [] x, boolean val) {
		for(int i = x.length; i != 0; ) x[--i] = val; // BACKWARD
	}

	// [ count ]------------------------------------------------
	/** count the number of times <tt>val</tt> is seen in the array x */
	public static final int count(final int [] x, int val) {
		int ret = 0;
		for(int i = x.length; i != 0; ) if(x[--i] == val) ret ++;
		return ret;
	}
	/** count the number of times <tt>val</tt> is seen in the array x */
	public static final int count(final boolean [] x, boolean val) {
		int len = x.length, ret = 0;
		for(int i = 0; i < len; i++) if(x[i] == val) ret ++;
		return ret;
	}
	// ---- [reverse]-------------------------------------------------
	/** reverse some list */
	public static void reverse(Object [] variables, int size) {
		int len = size / 2;
		size--;
		for(int j = 0; j < len; j++) {
			int i = size - j;
			Object tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}

	/** reverse an array of ints */
	public static void reverse(int [] variables, int size) {
		int len = size / 2;
		size--;
		for(int j = 0; j < len; j++) {
			int i = size - j;
			int tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}
	/** reverse an array of doubles */
	public static void reverse(double[] variables, int size) {
		int len = size / 2;
		size--;
		for(int j = 0; j < len; j++) {
			int i = size - j;
			double tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}
	// ---- [shuffle]-------------------------------------------------
	/** shuffle the members of an array */
	public static final void shuffle(int x[]) {
		shuffle(x, x.length);
	}
	/** shuffle the first <tt>len</tt> members of an array */
	public static final void shuffle(int x[], int len) {
		for(int i = 0; i < len; i++) {
			int j = FastRandom.mtrand() % len;
			int tmp = x[i]; x[i] = x[j]; x[j] = tmp;
		}
	}

	/**
	 * disturb the current order, but not a full shuffle (to save time)
	 * @see #shuffle(int[], int)
	 */
	public static final void disturb(int []x, int len) {
		if(len < 16) shuffle(x, len);
		else {
			// just swap "times" number of elements
			int times = Math.max(4, len / 20); // 5%, at least 4
			while( times-- > 0) {
				int j = FastRandom.mtrand() % len;
				int i = FastRandom.mtrand() % len;
				int tmp = x[i]; x[i] = x[j]; x[j] = tmp;
			}
		}
	}

	// ---- [permutation]-------------------------------------------------
	/**
	 * create a permutation from {0, ... , size-1} to {0, ... , size-1}.
	 */
	public static final int [] permutation(int size) {
		int [] ret = new int[size];
		for(int i = 0; i < size; i++) ret[i] = i;
		shuffle(ret);
		return ret;
	}

	// [ equal ?]------------------------------------------------

	/**
	 * compare to vectors
	 * @return true of the first <tt>len</tt> members of v1 and v2 are equal
	 */
	public static final boolean equals(boolean []v1, boolean []v2, int len) {
		for(int i = 0; i < len; i++) if(v1[i] != v2[i]) return false;
		return true;
	}

	/**
	 * compare to vectors
	 * @return true of the first <tt>len</tt> members of v1 and v2 are equal
	 */
	public static final boolean equals(short []v1, short []v2, int len) {
			for(int i = 0; i < len; i++) if(v1[i] != v2[i]) return false;
			return true;
	}

	/**
	 * compare to vectors
	 * @return true of the first <tt>len</tt> members of v1 and v2 are equal
	 */
	public static final boolean equals(byte []v1, byte []v2, int len) {
			for(int i = 0; i < len; i++) if(v1[i] != v2[i]) return false;
			return true;
	}

	/**
	 * compare to vectors
	 * @return true of the first <tt>len</tt> members of v1 and v2 are equal
	 */
	public static final boolean equals(int []v1, int []v2, int len) {
			for(int i = 0; i < len; i++) if(v1[i] != v2[i]) return false;
			return true;
	}
}
