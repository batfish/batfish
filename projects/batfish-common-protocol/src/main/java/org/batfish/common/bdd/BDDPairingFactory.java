package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.swapPairing;
import static org.parboiled.common.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarPair;

public final class BDDPairingFactory {
  private final BDDFactory _bddFactory;
  private final Set<BDDVarPair> _varPairs;

  // lazy init
  @Nullable BDDPairing _swapPairing;
  @Nullable BDD _domainVars;

  public BDDPairingFactory(BDDFactory bddFactory, Set<BDDVarPair> varPairs) {
    checkArgument(!varPairs.isEmpty(), "BDDPairingFactory must have at least one variable pair.");

    Set<Integer> oldVars = varPairs.stream().map(BDDVarPair::getOldVar).collect(Collectors.toSet());
    checkArgument(oldVars.size() == varPairs.size(), "domain must have distinct variables");

    Set<Integer> newVars = varPairs.stream().map(BDDVarPair::getNewVar).collect(Collectors.toSet());
    checkArgument(newVars.size() == varPairs.size(), "codomain must have distinct variables");

    checkArgument(
        Sets.intersection(oldVars, newVars).isEmpty(), "domain and codomain must be disjoint");

    _bddFactory = bddFactory;
    _varPairs = ImmutableSet.copyOf(varPairs);
  }

  /** Create a {@link BDDPairing} that swaps domain and codomain variables. */
  public BDDPairing getSwapPairing() {
    if (_swapPairing == null) {
      _swapPairing = swapPairing(_bddFactory, _varPairs);
    }
    return _swapPairing;
  }

  public BDDPairingFactory composeWith(BDDPairingFactory other) {
    checkArgument(
        _bddFactory == other._bddFactory,
        "Cannot compose with a BDDPairingFactory for a different BDDFactory");
    checkArgument(
        Sets.intersection(_varPairs, other._varPairs).isEmpty(),
        "Cannot compose two BDDPairingFactories with overlapping var pairs");
    return new BDDPairingFactory(
        _bddFactory,
        Stream.of(_varPairs, other._varPairs)
            .flatMap(Set::stream)
            .collect(ImmutableSet.toImmutableSet()));
  }

  public boolean overlapsWith(BDDPairingFactory other) {
    return !Sets.intersection(_varPairs, other._varPairs).isEmpty();
  }

  /**
   * Return a {@link BDD} of the variables in the pairing's domain, suitable for use with {@link
   * BDD#exist(BDD)}. The caller does not own the {@link BDD} and must not mutate or free it.
   */
  public BDD getDomainVarsBdd() {
    if (_domainVars == null) {
      _domainVars =
          _bddFactory.andAll(
              _varPairs.stream()
                  .map(varPair -> _bddFactory.ithVar(varPair.getOldVar()))
                  .toArray(BDD[]::new));
    }
    return _domainVars;
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
    return _varPairs.equals(that._varPairs);
  }

  @Override
  public int hashCode() {
    return _varPairs.hashCode();
  }
}
