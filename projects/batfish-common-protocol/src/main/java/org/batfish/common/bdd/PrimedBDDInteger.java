package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/** A wrapper around an {@link ImmutableBDDInteger} with primed variables for encoding relations. */
public final class PrimedBDDInteger {
  private final ImmutableBDDInteger _var;
  private final ImmutableBDDInteger _primeVar;
  private final BDDPairingFactory _pairingFactory;

  public PrimedBDDInteger(BDDFactory factory, BDD[] bitvec, BDD[] primeBitvec) {
    _var = new ImmutableBDDInteger(factory, bitvec);
    _primeVar = new ImmutableBDDInteger(factory, primeBitvec);
    _pairingFactory = BDDPairingFactory.create(bitvec, primeBitvec);
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
    return BDDPairingFactory.create(
        Arrays.copyOf(_var._bitvec, n), Arrays.copyOf(_primeVar._bitvec, n));
  }
}
