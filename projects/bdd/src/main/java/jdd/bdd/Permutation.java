
package jdd.bdd;

import jdd.util.Allocator;
import jdd.util.Array;
import jdd.util.JDDConsole;
import jdd.util.Test;
import jdd.util.math.HashFunctions;


/**
 * BDD permutation, a function p: N -> N
 * @see BDD
 */

// a permutations is identified with its unique ID.
// the ID is used in caching  so it is important the equal permutations are given the same ID.
// therefore, we keep track of the permutations by saving (from,to) and its hash for fatser
// comparison

public class Permutation {
	private static int id_c = 0;
	/* package */ int last, first, id, hash;
	/* package */ int [] perm, from, to;
	/* package */ Permutation next; // the next Permutation in the linked list

	// We assume from and to to be sorted,
	Permutation(int [] from, int [] to, NodeTable nt) {
		Test.check( from.length == to.length, "Permutations vectors must have equal length");
		Test.check( from.length > 0, "non empty pemuration vectors");

		this.from = Array.clone(from);
		this.to= Array.clone(to);

		int len = from.length;
		int [] f = new int[len];
		int [] t = new int[len];
		for(int i = 0; i < len; i++) {
			f[i] = nt.getVar( from[i]);
			t[i] = nt.getVar( to[i] );
		}

		first = last = f[0];
		for(int i = 1; i < len; i++) {
			if(last < f[i]) last = f[i];
			if(first > f[i]) first = f[i];
		}

		perm = Allocator.allocateIntArray(last+1);
		for(int i = 0; i < last; i++) perm[i] = i;

		for(int i = 0; i < len; i++) {
			perm[ f[i] ] = t[i];
		}
		next = null;

		hash = computeHash(from, to);

		id = id_c ++;
	}

	// ----------------------------------------------------------------
	public void show() {
		JDDConsole.out.println("-----------------------------");
		for(int i = first;  i <=last; i++)
			JDDConsole.out.println(" " + i + " --> " + perm[i] );
	}

	/**
	 * get the approximate memory usage of this object
	 */
	public long getMemoryUsage() {
		return perm.length * 4 +  from.length * 4 +  to.length * 4;
	}
	// ----------------------------------------------------------------

	/* package */ static int computeHash(int [] from, int [] to) {
		int hash1 = HashFunctions.hash_FNV(from, 0, from.length);
		int hash2 = HashFunctions.hash_FNV(to, 0, to.length);
		return HashFunctions.hash_FNV(hash1, hash2, 0);
	}
	/*package */ static Permutation findPermutation(Permutation first, int []from, int []to) {
		int new_hash = computeHash(from, to);

		while(first != null) {
			if( first.equals(new_hash, from, to)) return first;
			first = first.next;
		}
		return null; // TODO
	}
	/* package */ boolean equals(int hash, int []from, int []to) {
		if(hash != this.hash) return false;
		if(from.length != this.from.length || to.length != this.to.length) return false;
		if(!Array.equals(from, this.from, from.length)) return false;
		if(!Array.equals(to, this.to, to.length)) return false;
		return true;

	}

}
