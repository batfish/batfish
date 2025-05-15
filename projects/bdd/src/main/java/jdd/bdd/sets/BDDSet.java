
package jdd.bdd.sets;

import jdd.util.JDDConsole;
import jdd.util.sets.Set;
import jdd.util.sets.SetEnumeration;

/**
 * BDD representation of a set (of a product of few small subsets)
 */

public class BDDSet implements Set {
	private BDDUniverse universe;
	private boolean [] internal_minterm;
	/* package */ int bdd;

	/* package */ BDDSet(BDDUniverse u, int bdd) {
		this.universe = u;
		this.bdd = universe.ref(bdd);
		this.internal_minterm = new boolean[ universe.numberOfBits() ];
	}

	public double cardinality() { return universe.satCount(bdd); }
	public void free() { universe.deref(bdd);	}
	public boolean equals(Set s) { return bdd == ((BDDSet)s).bdd; }

	public boolean isEmpty() { return bdd == 0; }

	public void assign(Set s) {
		universe.deref(bdd);
		bdd = universe.ref(((BDDSet)s).bdd);
	}
	public void clear() {
		universe.deref(bdd);
		bdd = 0;
	}

	/* package */ void show() { universe.printSet(bdd); }

	/** returns true if assignment is in the set. allows dont cares */
	public boolean memberDC(int [] assignment) {
		int x = universe.vectorToBDD(assignment);
		int tmp = universe.or(x, bdd);
		boolean ret = (tmp == bdd);
		universe.deref(x);
		return ret;
	}

	/** fast membership test. no dont cares */
	public boolean member(int [] assignment) {
		universe.vectorToMinterm(assignment, internal_minterm);
		return universe.member(bdd, internal_minterm);
	}

	/** returns true if assignment was in the set */
	public boolean remove(int [] assignment) {
		int x = universe.vectorToBDD(assignment);
		int notx = universe.ref( universe.not( x) );
		universe.deref(x);
		int tmp = universe.ref( universe.and( bdd, notx) );
		universe.deref(notx);
		if(tmp == bdd) { // alread in there??
			universe.deref(tmp);
			return false;
		} else {
			universe.deref(bdd);
			bdd = tmp;
			return true;
		}
	}


	/** returns true if assignment was not alread in the set */
	public boolean insert(int [] assignments) {
		int x = universe.vectorToBDD(assignments);
		int tmp = universe.ref( universe.or( bdd, x) );

		if(tmp == bdd) { // alread in there??
			universe.deref(tmp);
			return false;
		} else {
			universe.deref(bdd);
			bdd = tmp;
			return true;
		}
	}

	public Set copy() { return new BDDSet(universe, bdd); }
	public Set invert() {
		int neg = universe.ref( universe.not(bdd) );
		BDDSet ret = new BDDSet(universe, universe.removeDontCares(neg) );
		universe.deref( neg);
		return ret;
	}

	public Set union(Set s) { return new BDDSet(universe, universe.or(bdd, ((BDDSet)s).bdd) ); }
	public Set intersection(Set s) { return new BDDSet(universe, universe.and(bdd, ((BDDSet)s).bdd) ); }

	public Set diff(Set s_) {
		BDDSet s = (BDDSet) s_;
		int neg = universe.ref( universe.not(s.bdd) );
		int d   = universe.and(bdd, neg);
		universe.deref(neg);
		return new BDDSet(universe, d);
	}


	/** retruns 0 if equal, -1 if this \subset s, +1 if s \subset this, Integer.MAX_VALUE otherwise */
	public int compare(Set s_) {
		BDDSet s = (BDDSet) s_;
		if(s.bdd == bdd) return 0;
		int u = universe.or(bdd, s.bdd);
		if(u == bdd) return +1;
		if(u == s.bdd) return -1;

		return Integer.MAX_VALUE; // no relation between this and s
	}

	public SetEnumeration elements() {
		return new BDDSetEnumeration(universe, bdd);
	}

	public void show(String name) {
		JDDConsole.out.print(name + " = " );
		if(bdd == 0) {	JDDConsole.out.println("empty set");	return;		}

		JDDConsole.out.print("{\n  ");
		SetEnumeration se = elements();
		int j = 0;
		for(; se.hasMoreElements();) {
			int [] x = se.nextElement();
			universe.print(x);
			j += x.length + 1;
			if(j > 20) {
				j = 0;
				JDDConsole.out.print("\n  ");
			} else 	JDDConsole.out.print(" ");
		}
		if(j != 0) JDDConsole.out.printf("\n");
		JDDConsole.out.println("\r}");
		se.free();
	}

}
