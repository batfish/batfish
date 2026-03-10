package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarPair;
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
  public static BDDPairing swapPairing(BDDFactory bddFactory, Set<BDDVarPair> varPairs) {
    checkArgument(!varPairs.isEmpty(), "Cannot build swapPairing for empty bitvectors");

    return bddFactory.getPair(
        varPairs.stream()
            .flatMap(pair -> Stream.of(pair, new BDDVarPair(pair.getNewVar(), pair.getOldVar())))
            .collect(ImmutableSet.toImmutableSet()));
  }

  /** Create a {@link BDDPairing} for swapping variables. */
  public static BDDPairing swapPairing(BDD[] bv1, BDD[] bv2) {
    checkArgument(bv1.length > 0, "Cannot build swapPairing for empty bitvectors");
    checkArgument(bv1.length == bv2.length, "Bitvector lengths must be equal");

    return bv1[0]
        .getFactory()
        .getPair(
            IntStream.range(0, bv1.length)
                .mapToObj(
                    i -> Stream.of(new BDDVarPair(bv1[i], bv2[i]), new BDDVarPair(bv2[i], bv1[i])))
                .flatMap(Function.identity())
                .collect(ImmutableSet.toImmutableSet()));
  }
}
