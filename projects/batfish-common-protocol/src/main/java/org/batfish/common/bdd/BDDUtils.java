package org.batfish.common.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

/** Various utility methods for working with {@link BDD}s. */
public class BDDUtils {
  /** Create a new {@link BDDFactory} object with {@code numVariables} boolean variables. */
  public static BDDFactory bddFactory(int numVariables) {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.disableReorder();
    factory.setCacheRatio(64);
    factory.setVarNum(numVariables); // reserve 32 1-bit variables
    return factory;
  }

  /**
   * Check if this BDD represents a single assignment, i.e. if each node has only 1 path to the leaf
   * node "one" in the DAG. If this is true, either the high or low child node will be the leaf node
   * "zero", due to reduction. Specifically, one of the children must only lead to zero, and
   * reduction recursively removes nodes whose high and low children are the same node.
   */
  public static boolean isAssignment(BDD orig) {
    BDD bdd = orig;
    if (bdd.isZero()) {
      return false;
    }
    while (!bdd.isOne()) {
      if (bdd.low().isZero()) {
        // this node looks good; check its high child.
        bdd = bdd.high();
      } else if (bdd.high().isZero()) {
        // this node looks good; check its low child.
        bdd = bdd.low();
      } else {
        // one of the branches must be zero. not an assignment
        return false;
      }
    }
    return true;
  }
}
