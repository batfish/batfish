
// TODO: optimize by not calling  universe.vectorToBDD(assignment):
//       add routines to JDD that work directly on varsets (see JDD.varset2() )
//       Add also a remove routing that removes a MINTERM from a bdd (and maybe a add routing?)

package jdd.bdd.sets;

import jdd.bdd.BDD;
import jdd.bdd.BDDUtil;
import jdd.util.Array;
import jdd.util.JDDConsole;
import jdd.util.Test;
import jdd.util.math.Digits;
import jdd.util.sets.Set;
import jdd.util.sets.Universe;


/**
 * SubDomain represents a dimension in the Universe. This class is class accessible only in package
 */

/* package */ class SubDomain {
	private BDDUniverse universe;
	/* package */ int bits, size;
	/* package */ int all;
	/* package */ int [] vars, numbers;

	/* package */ SubDomain(BDDUniverse universe, int size) {
		Test.checkInequality(size, 0, "Empty subdomain :(");

		this.universe = universe;
		this.size     = size;
		this.bits     = Digits.log2_ceil( size);

		vars = new int[bits];
		numbers  = new int[size];
		for(int i = 0; i < bits; i++)
			vars[i] = universe.createVar();

		all = 0;
		for(int i = 0; i < size; i++) {
			numbers[i] = BDDUtil.numberToBDD(universe, vars, i);

			// add to the care-set
			int tmp = universe.ref ( universe.or(all, numbers[i]) );
			universe.deref(all);
			all = tmp;
		}
	}

	// ----------------------------------------------------

	public int getSize() { return size; }


	// XXX: very inefficient!!
	public int find(int bdd) {
		if(bdd == 1 || bdd == 0 /* error! */ || bdd == all) return 0;
		for(int i = 0; i < size; i++)
			if( universe.and(bdd, numbers[i]) == bdd) return i;

		return -1; /* error */
	}
}

/**
 * Universe class for the BDD sets
 * <p> insert() and member() functions are memory but not time efficient.
 * <p>The set operators (unions, diff, etc) are however very efficient (standard BDD operations).
 */

public class BDDUniverse extends BDD implements Universe {
	private int [] int_subdomains, int_bits;
	private double domainsize;
	private int num_subdomains, all, bits;
	private SubDomain [] subdomains;

	public BDDUniverse(int [] domains) {
		super(1000,1000);

		num_subdomains = domains.length;
		int_subdomains = Array.clone(domains);
		int_bits       = new int[num_subdomains];
		subdomains     = new SubDomain[num_subdomains];

		domainsize = 1.0;
		bits = 0;
		for(int i = 0; i < num_subdomains; i++) {
			subdomains[i] = new SubDomain(this, int_subdomains[i]);
			domainsize *= int_subdomains[i];
			int_bits[i] = subdomains[i].bits;
			bits       += subdomains[i].bits;
		}
		// calc the care-set
		all = 1;
		for(int i = 0; i < num_subdomains; i++) {
			int tmp = ref( and(all, subdomains[i].all) );
			deref(all);
			all = tmp;
		}
	}

	/** cleanup before die */
	public void free() {
		cleanup();
		subdomains = null; // a good way the dsaibe further use - will throw nullpointer exception :)
	}

	/* packege */ int vectorToBDD(int [] assignments) {
		int ret = 1;
		for(int i = 0; i < num_subdomains; i++) {
			if(assignments[i] != -1) {
				int tmp = ref( and( ret,subdomains[i].numbers[ assignments[i] ] ) );
				deref(ret);
				ret = tmp;
			}
		}
		return ret;
	}


	/** XXX: this one does not handle DONT-CAREs ! */
	/* packege */ void vectorToMinterm(int [] assignments, boolean [] minterm) {
		int index = 0;
		for(int i = 0; i < num_subdomains; i++) {
			if(assignments[i] != -1) {
				// System.out.println("\nat index "  + index + ", automata " + i);
				BDDUtil.numberToMinterm(assignments[i], int_bits[i], index, minterm);
				index += int_bits[i];
			} else {
				// FIXME: ERROR inside performance critical code!!!!!
			}
		}
	}

	/*
	void BDDToVector (int bdd, int [] vec) { // XXX: very time consuming
		for(int i = 0; i < num_subdomains; i++)
			vec[i] = subdomains[i].find(bdd);
	}
	*/

	public int cardinality(int [] x) {
		int ret = 1;
		for(int i = 0; i < num_subdomains; i++)
			if(x[i] == -1)
				ret *= subdomains[i].getSize() ;
		return ret;
	}

	public Set createEmptySet() {	return new BDDSet(this, 0);	}

	public Set createFullSet() {	return new BDDSet(this, all);	}

	public Set simplify(Set s1, Set s2) {
		int new_bdd = restrict( ((BDDSet)s1).bdd, ((BDDSet)s2).bdd);
		return new BDDSet(this, new_bdd);
	}

	public double domainSize() { 	return domainsize; }

	public int subdomainCount() { return num_subdomains; }

	/** number of BDD bits (variables) allocated by this universe */
	public int numberOfBits() {
		return bits;
	}


	/* package */ int removeDontCares(int bdd) {
		return and(bdd,all);
	}

	public void print(int [] v) {
		JDDConsole.out.print("<");
		for(int i = 0; i < v.length; i++) {
			if(i > 0) JDDConsole.out.print(", ");
			if(v[i] == -1) JDDConsole.out.print("-");
			else			JDDConsole.out.print(""+v[i]);
		}
		JDDConsole.out.print(">");
	}



	// ---- random member ----------------------
	public void randomMember(int [] out) {
		for(int i = 0; i < num_subdomains; i++) out[i] = (int)(Math.random() * int_subdomains[i]);
	}

	// ---- [satOneVector] more efficient minterm extraction ----------------------
	private int [] sat_vec = null;
	private int sat_curr, sat_level, sat_next, sat_index, sat_bit;
	public void satOneVector(int bdd, int [] vec) {
		sat_vec = vec;
		sat_curr = sat_level = sat_index = sat_bit = 0;
		sat_next = subdomains[0].bits;
		satOneVector_rec(bdd);
		while(sat_index < num_subdomains) satOneVector_insert(false);	// if dont care, we choose '0'
		sat_vec = null;
	}
	private void satOneVector_insert(boolean x) {
		if(x) sat_curr |= (1 << sat_bit);
		if(++sat_level == sat_next) {
			sat_vec[sat_index++] = sat_curr;
			sat_bit = sat_curr = 0;
			if(sat_index < num_subdomains) sat_next += subdomains[sat_index].bits;
		} else sat_bit++;
	}
	private void satOneVector_rec(int bdd) {
		if(bdd < 2) return;
		while(getVar(bdd) > sat_level)	satOneVector_insert(false);	// if dont care, we choose '0'
		if(getLow(bdd) == 0) {
			satOneVector_insert(true);
			satOneVector_rec( getHigh(bdd) );
		} else {
			satOneVector_insert(false);
			satOneVector_rec( getLow(bdd) );
		}
	}
}
