package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;

public final class Route4FilterLineLengthRange extends Route4FilterLine {

  private final int _maxPrefixLength;

  private final int _minPrefixLength;

  public Route4FilterLineLengthRange(Prefix prefix, int minPrefixLength, int maxPrefixLength) {
    super(prefix);
    _minPrefixLength = minPrefixLength;
    _maxPrefixLength = maxPrefixLength;
  }

  @Override
  public void applyTo(RouteFilterList rfl) {
    org.batfish.datamodel.RouteFilterLine line =
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, _prefix, new SubRange(_minPrefixLength, _maxPrefixLength));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineLengthRange)) {
      return false;
    }

    Route4FilterLineLengthRange rhs = (Route4FilterLineLengthRange) o;
    return _prefix.equals(rhs._prefix)
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
    result = prime * result + _prefix.hashCode();
    return result;
  }
}
