package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.SubRange;

public final class Route6FilterLineLengthRange extends Route6FilterLine {

  private final int _maxPrefixLength;

  private final int _minPrefixLength;

  public Route6FilterLineLengthRange(Prefix6 prefix6, int minPrefixLength, int maxPrefixLength) {
    super(prefix6);
    _minPrefixLength = minPrefixLength;
    _maxPrefixLength = maxPrefixLength;
  }

  @Override
  public void applyTo(Route6FilterList rfl) {
    org.batfish.datamodel.Route6FilterLine line =
        new org.batfish.datamodel.Route6FilterLine(
            LineAction.PERMIT, _prefix6, new SubRange(_minPrefixLength, _maxPrefixLength));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineLengthRange)) {
      return false;
    }

    Route6FilterLineLengthRange rhs = (Route6FilterLineLengthRange) o;
    return _prefix6.equals(rhs._prefix6)
        && _minPrefixLength == rhs._minPrefixLength
        && _maxPrefixLength == rhs._maxPrefixLength;
  }

  public int getMaxPrefixLength() {
    return _maxPrefixLength;
  }

  public int getMinPrefixLength() {
    return _minPrefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _maxPrefixLength;
    result = prime * result + _minPrefixLength;
    result = prime * result + _prefix6.hashCode();
    return result;
  }
}
