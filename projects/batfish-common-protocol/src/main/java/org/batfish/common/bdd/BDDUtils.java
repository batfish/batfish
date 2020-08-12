package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;

/** Various utility methods for working with {@link BDD}s. */
public class BDDUtils {
  /** Create a new {@link BDDFactory} object with {@code numVariables} boolean variables. */
  public static BDDFactory bddFactory(int numVariables) {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.setCacheRatio(64);
    factory.setVarNum(numVariables); // reserve 32 1-bit variables
    return factory;
  }

  /**
   * Swap the constraints on multiple {@link BDDInteger BDDIntegers} in a {@link BDD}. Usage:
   * swap(bdd, a1, a2, b1, b2, ...). Swaps a1 and a2, b1 and b2, etc.
   */
  public static BDD swap(BDD bdd, BDDInteger... vars) {
    return bdd.replace(swapPairing(vars));
  }

  /** Create a {@link BDDPairing} for swapping variables. */
  public static BDDPairing swapPairing(BDDInteger... vars) {
    checkArgument(vars.length > 0, "Requires at least 2 variables");
    checkArgument(vars.length % 2 == 0, "Requires an even number of variables");
    BDDFactory factory = vars[0].getFactory();

    Stream.Builder<BDDInteger> left = Stream.builder();
    Stream.Builder<BDDInteger> right = Stream.builder();

    for (int i = 0; i < vars.length; i += 2) {
      checkArgument(
          vars[i].size() == vars[i + 1].size(),
          "Cannot swap variables with unequal number of bits");
      left.add(vars[i]);
      right.add(vars[i + 1]);
    }

    BDD[] bv1 = left.build().flatMap(var -> Arrays.stream(var.getBitvec())).toArray(BDD[]::new);
    BDD[] bv2 = right.build().flatMap(var -> Arrays.stream(var.getBitvec())).toArray(BDD[]::new);

    BDDPairing pairing = factory.makePair();

    for (int i = 0; i < bv1.length; i++) {
      pairing.set(bv1[i].var(), bv2[i].var());
      pairing.set(bv2[i].var(), bv1[i].var());
    }

    return pairing;
  }
}
