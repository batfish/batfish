package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.concatBitvectors;
import static org.batfish.common.bdd.BDDUtils.swapPairing;
import static org.parboiled.common.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;

public final class BDDPairingFactory {
  private final BDD[] _domain;
  private final BDD[] _codomain;
  private final BDD _domainVars;

  // lazy init
  @Nullable BDDPairing _swapPairing;

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
  public BDDPairing getSwapPairing() {
    if (_swapPairing == null) {
      _swapPairing = swapPairing(_domain, _codomain);
    }
    return _swapPairing;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BDDPairingFactory)) {
      return false;
    }
    BDDPairingFactory that = (BDDPairingFactory) o;
    // only consider domainVars, since 1) the domain is a function of it, and 2) the codomain is a
    // function of the domain (assuming no accidental variable reuse, etc).
    assert !_domainVars.equals(that._domainVars)
        || (ImmutableSet.copyOf(_domain).equals(ImmutableSet.copyOf(that._domain))
            && ImmutableSet.copyOf(_codomain).equals(ImmutableSet.copyOf(that._codomain)));
    return _domainVars.equals(that._domainVars);
  }

  @Override
  public int hashCode() {
    return _domainVars.hashCode();
  }
}
