package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.stream.IntStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarPair;

/** A wrapper around an {@link ImmutableBDDInteger} with primed variables for encoding relations. */
public final class PrimedBDDInteger implements Serializable {
  private final ImmutableBDDInteger _var;
  private final ImmutableBDDInteger _primeVar;
  private final BDDPairingFactory _pairingFactory;

  public PrimedBDDInteger(BDDFactory factory, BDD[] bitvec, BDD[] primeBitvec) {
    checkArgument(
        bitvec.length == primeBitvec.length,
        "Must have equal number of primed and unprimed variables");
    _var = new ImmutableBDDInteger(factory, bitvec);
    _primeVar = new ImmutableBDDInteger(factory, primeBitvec);
    _pairingFactory =
        new BDDPairingFactory(
            factory,
            IntStream.range(0, bitvec.length)
                .mapToObj(i -> new BDDVarPair(bitvec[i], primeBitvec[i]))
                .collect(ImmutableSet.toImmutableSet()));
  }

  public ImmutableBDDInteger getVar() {
    return _var;
  }

  public ImmutableBDDInteger getPrimeVar() {
    return _primeVar;
  }

  public BDDPairingFactory getPairingFactory() {
    return _pairingFactory;
  }

  /** Create a {@link BDDPairingFactory} for the input number of most significant variables. */
  public BDDPairingFactory getPairingFactory(int n) {
    checkArgument(n <= _var.size(), "Cannot get pairing factory for more vars than exist");
    if (n == _var.size()) {
      return _pairingFactory;
    }
    return new BDDPairingFactory(
        _var.getFactory(),
        IntStream.range(0, n)
            .mapToObj(i -> new BDDVarPair(_var._bitvec[i], _primeVar._bitvec[i]))
            .collect(ImmutableSet.toImmutableSet()));
  }
}
