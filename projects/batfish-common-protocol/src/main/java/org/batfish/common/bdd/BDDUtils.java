package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
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

  public static BDD[] bitvector(BDDFactory factory, int length, int start, boolean reverse) {
    checkArgument(factory.varNum() >= start + length, "Not enough variables to create bitvector");
    BDD[] bitvec = new BDD[length];
    for (int i = 0; i < length; i++) {
      int idx;
      if (reverse) {
        idx = start + length - i - 1;
      } else {
        idx = start + i;
      }
      bitvec[i] = factory.ithVar(idx);
    }
    return bitvec;
  }

  public static BDD[] concatBitvectors(BDD[]... arrays) {
    return Arrays.stream(arrays).flatMap(Arrays::stream).toArray(BDD[]::new);
  }

  /** Create a {@link BDDPairing} for swapping variables. */
  public static BDDPairing swapPairing(BDD[] bv1, BDD[] bv2) {
    checkArgument(bv1.length > 0, "Cannot build swapPairing for empty bitvectors");
    checkArgument(bv1.length == bv2.length, "Bitvector lengths must be equal");

    int l = bv1.length;
    BDD[] oldvars = new BDD[l * 2];
    BDD[] newvars = new BDD[l * 2];
    for (int i = 0; i < l; i++) {
      // forward
      oldvars[i] = bv1[i];
      newvars[i] = bv2[i];
      // reverse
      oldvars[l + i] = bv2[i];
      newvars[l + i] = bv1[i];
    }

    return bv1[0].getFactory().getPair(oldvars, newvars);
  }
}
