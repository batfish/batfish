package org.batfish.common.bdd;

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
    _pairingFactory = new BDDPairingFactory(bitvec, primeBitvec);
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
}
