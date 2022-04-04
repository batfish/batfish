package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.concatBitvectors;
import static org.batfish.common.bdd.BDDUtils.swapPairing;
import static org.parboiled.common.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;

public final class BDDPairingFactory {
  private final BDD[] _domain;
  private final BDD[] _codomain;
  private final BDD _domainVars;

  public BDDPairingFactory(BDD[] domain, BDD[] codomain) {
    checkArgument(domain.length == codomain.length, "domain and codomain must have equal size");
    checkArgument(domain.length > 0, "domain and codomain must contain at least one variable");
    checkArgument(hasDistinctElements(domain), "domain must have distinct variables");
    checkArgument(hasDistinctElements(codomain), "codomain must have distinct variables");
    checkArgument(
        Sets.intersection(Sets.newHashSet(domain), Sets.newHashSet(codomain)).isEmpty(),
        "domain and codomain must be disjoint");
    _domain = domain;
    _codomain = codomain;
    _domainVars = domain[0].getFactory().andAll(domain);
  }

  private static boolean hasDistinctElements(BDD[] vars) {
    return Arrays.stream(vars).distinct().count() == vars.length;
  }

  /** Create a {@link BDDPairing} that swaps domain and codomain variables. */
  public BDDPairing makeSwapPairing() {
    return swapPairing(_domain, _codomain);
  }

  public BDDPairingFactory composeWith(BDDPairingFactory other) {
    return new BDDPairingFactory(
        concatBitvectors(_domain, other._domain), concatBitvectors(_codomain, other._codomain));
  }

  /**
   * Return a {@link BDD} of the variables in the pairing's domain, suitable for use with {@link
   * BDD#exist(BDD)}. The caller owns the {@link BDD} and must free it.
   */
  public BDD getDomainVarsBdd() {
    return _domainVars.id(); // defensive copy
  }
}
