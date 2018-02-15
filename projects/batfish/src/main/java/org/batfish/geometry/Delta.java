package org.batfish.geometry;

public class Delta {

  private EquivalenceClass _old;

  private EquivalenceClass _new;

  public Delta(EquivalenceClass o, EquivalenceClass n) {
    this._old = o;
    this._new = n;
  }

  public EquivalenceClass getOld() {
    return _old;
  }

  public EquivalenceClass getNew() {
    return _new;
  }
}
