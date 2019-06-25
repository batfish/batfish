package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;

public final class Route4FilterLineUpTo extends Route4FilterLine {

  private final int _maxPrefixLength;

  public Route4FilterLineUpTo(Prefix prefix, int maxPrefixLength) {
    super(prefix);
    _maxPrefixLength = maxPrefixLength;
  }

  @Override
  public void applyTo(RouteFilterList rfl) {
    int prefixLength = _prefix.getPrefixLength();
    org.batfish.datamodel.RouteFilterLine line =
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, _prefix, new SubRange(prefixLength, _maxPrefixLength));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineUpTo)) {
      return false;
    }

    Route4FilterLineUpTo rhs = (Route4FilterLineUpTo) o;
    return _prefix.equals(rhs._prefix) && _maxPrefixLength == rhs._maxPrefixLength;
  }

  public int getMaxPrefixLength() {
    return _maxPrefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _maxPrefixLength;
    result = prime * result + _prefix.hashCode();
    return result;
  }
}
