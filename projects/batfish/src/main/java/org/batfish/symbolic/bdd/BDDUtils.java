package org.batfish.symbolic.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

/** Various utility methods for working with {@link BDD}s. */
public class BDDUtils {
  /** Create a new {@link BDDFactory} object with {@param numVariables} boolean variables. */
  public static BDDFactory bddFactory(int numVariables) {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.disableReorder();
    factory.setCacheRatio(64);
    factory.setVarNum(numVariables); // reserve 32 1-bit variables
    return factory;
  }

  public static boolean isAssignment(BDD bdd) {
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
