package org.batfish.representation.juniper;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;

public class Route4FilterLineOrLonger extends Route4FilterLine {

  public Route4FilterLineOrLonger(Prefix prefix) {
    super(prefix);
  }

  @Override
  public void applyTo(RouteFilterList rfl) {
    int prefixLength = _prefix.getPrefixLength();
    org.batfish.datamodel.RouteFilterLine line =
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, _prefix, new SubRange(prefixLength, Prefix.MAX_PREFIX_LENGTH));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineOrLonger)) {
      return false;
    }

    Route4FilterLineOrLonger rhs = (Route4FilterLineOrLonger) o;
    return _prefix.equals(rhs._prefix);
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode();
  }
}
