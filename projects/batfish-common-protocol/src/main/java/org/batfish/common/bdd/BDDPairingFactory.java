package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarPair;

public final class BDDPairingFactory implements Serializable {
  private final BDDFactory _bddFactory;
  private final Set<BDDVarPair> _varPairs;

  // lazy init
  @Nullable BDDPairing _swapPairing;
  @Nullable BDDPairing _primeToUnprimePairing;
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

  private void initDomainVars() {
    _domainVars =
        _bddFactory.andAll(
            _varPairs.stream()
                .map(varPair -> _bddFactory.ithVar(varPair.getOldVar()))
                .toArray(BDD[]::new));
  }

  /** Create a {@link BDDPairing} that swaps domain and codomain variables. */
  public BDDPairing getSwapPairing() {
    if (_swapPairing == null) {
      _swapPairing = swapPairing(_bddFactory, _varPairs);
    }
    return _swapPairing;
  }

  /** Create a {@link BDDPairing} that maps codomain variables to domain variables. */
  public BDDPairing getPrimeToUnprimePairing() {
    if (_primeToUnprimePairing == null) {
      _primeToUnprimePairing =
          _bddFactory.getPair(
              _varPairs.stream()
                  .map(p -> new BDDVarPair(p.getNewVar(), p.getOldVar()))
                  .collect(ImmutableSet.toImmutableSet()));
    }
    return _primeToUnprimePairing;
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
   * Return the identity function on this' domain, encoded as a relation between unprimed and primed
   * variables.
   */
  public BDD identityRelation() {
    return identityRelation(Predicates.alwaysTrue());
  }

  /**
   * Return the identity function on a subset of this' domain, encoded as a relation between
   * unprimed and primed variables.
   */
  public BDD identityRelation(Predicate<BDD> includeDomainVar) {
    return _bddFactory.andAllAndFree(
        _varPairs.stream()
            .map(
                varPair -> {
                  BDD oldVar = _bddFactory.ithVar(varPair.getOldVar());
                  if (includeDomainVar.test(oldVar)) {
                    return oldVar.biimpWith(_bddFactory.ithVar(varPair.getNewVar()));
                  }
                  oldVar.free();
                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }

  public boolean domainIncludes(BDD var) {
    if (_domainVars == null) {
      initDomainVars();
    }
    return _domainVars.testsVars(var);
  }

  public BDDPairingFactory union(BDDPairingFactory other) {
    if (this.includes(other)) {
      return this;
    }
    return new BDDPairingFactory(
        _bddFactory, ImmutableSet.copyOf(Sets.union(_varPairs, other._varPairs)));
  }

  public static BDDPairingFactory union(List<BDDPairingFactory> factories) {
    checkArgument(!factories.isEmpty(), "factories cannot be empty");
    BDDFactory bddFactory = factories.iterator().next()._bddFactory;
    Set<BDDVarPair> varPairs =
        factories.stream()
            .flatMap(factory -> factory._varPairs.stream())
            .collect(ImmutableSet.toImmutableSet());
    return new BDDPairingFactory(bddFactory, varPairs);
  }

  /**
   * Return a {@link BDD} of the variables in the pairing's domain, suitable for use with {@link
   * BDD#exist(BDD)}. The caller does not own the {@link BDD} and must not mutate or free it.
   */
  public BDD getDomainVarsBdd() {
    if (_domainVars == null) {
      initDomainVars();
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

  public boolean includes(BDDPairingFactory other) {
    checkArgument(_bddFactory == other._bddFactory, "BDD factories must be identical");
    return this._varPairs.containsAll(other._varPairs);
  }
}
