package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.SubRange;

public final class Route6FilterLineUpTo extends Route6FilterLine {

  private final int _maxPrefixLength;

  public Route6FilterLineUpTo(Prefix6 prefix6, int maxPrefixLength) {
    super(prefix6);
    _maxPrefixLength = maxPrefixLength;
  }

  @Override
  public void applyTo(Route6FilterList rfl) {
    int prefixLength = _prefix6.getPrefixLength();
    org.batfish.datamodel.Route6FilterLine line =
        new org.batfish.datamodel.Route6FilterLine(
            LineAction.PERMIT, _prefix6, new SubRange(prefixLength, _maxPrefixLength));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineUpTo)) {
      return false;
    }

    Route6FilterLineUpTo rhs = (Route6FilterLineUpTo) o;
    return _prefix6.equals(rhs._prefix6) && _maxPrefixLength == rhs._maxPrefixLength;
  }

  public int getMaxPrefixLength() {
    return _maxPrefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _maxPrefixLength;
    result = prime * result + _prefix6.hashCode();
    return result;
  }
}
