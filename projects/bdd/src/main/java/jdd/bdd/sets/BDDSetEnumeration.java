package jdd.bdd.sets;

import jdd.util.sets.SetEnumeration;

/**
 * Enumerator for the BDD-sets.
 *
 * <p>Notice the very important <tt>free</tt> function!
 */
public class BDDSetEnumeration implements SetEnumeration {
  private BDDUniverse universe;
  private int bdd;
  private int[] vec;

  /** You should not call this constructor directly, <tt>Set</tt> should do that job for you! */
  /* package */ BDDSetEnumeration(BDDUniverse u, int bdd) {
    this.universe = u;
    this.bdd = bdd;
    this.vec = new int[universe.subdomainCount()];
    universe.ref(bdd);
  }

  /** it is very important that you call this function when you are done with the set! */
  public void free() {
    universe.deref(bdd);
    bdd = 0;
  }

  public boolean hasMoreElements() {
    return bdd != 0;
  }

  public int[] nextElement() {

    universe.satOneVector(bdd, vec);
    int sat1 = universe.ref(universe.vectorToBDD(vec));
    int not_sat1 = universe.ref(universe.not(sat1));
    universe.deref(sat1);
    int tmp = universe.ref(universe.and(not_sat1, bdd));
    universe.deref(not_sat1);
    universe.deref(bdd);
    bdd = tmp;

    return vec;
  }
}
