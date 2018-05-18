package org.batfish.symbolic.ainterpreter;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public class LocatedAP {

  private BitSet _predicates;

  private BitSet[] _destinationLocations;

  public LocatedAP(BitSet predicates, BitSet[] destinationLocations) {
    this._predicates = predicates;
    this._destinationLocations = destinationLocations;
  }

  private BitSet or(BitSet x, BitSet y) {
    BitSet b = (BitSet) x.clone();
    b.or(y);
    return b;
  }

  public BitSet getPredicates() {
    return _predicates;
  }

  public BitSet[] getDestinationLocations() {
    return _destinationLocations;
  }

  public LocatedAP or(LocatedAP other) {
    BitSet[] locs = new BitSet[_destinationLocations.length];
    for (int i = 0; i < _destinationLocations.length; i++) {
      locs[i] = or(_destinationLocations[i], other._destinationLocations[i]);
    }
    return new LocatedAP(or(this._predicates, other._predicates), locs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocatedAP locatedAP = (LocatedAP) o;
    return Objects.equals(_predicates, locatedAP._predicates)
        && Arrays.equals(_destinationLocations, locatedAP._destinationLocations);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(_predicates);
    result = 31 * result + Arrays.hashCode(_destinationLocations);
    return result;
  }

  @Override
  public String toString() {
    return "LocatedAP{"
        + "_atoms="
        + _predicates
        + ", _locs="
        + Arrays.toString(_destinationLocations)
        + '}';
  }
}
