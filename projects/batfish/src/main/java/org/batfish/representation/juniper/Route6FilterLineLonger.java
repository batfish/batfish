package org.batfish.representation.juniper;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.SubRange;

public class Route6FilterLineLonger extends Route6FilterLine {

  public Route6FilterLineLonger(Prefix6 prefix) {
    super(prefix);
  }

  @Override
  public void applyTo(Route6FilterList rfl) {
    int prefixLength = _prefix6.getPrefixLength();
    if (prefixLength >= Prefix6.MAX_PREFIX_LENGTH) {
      throw new BatfishException(
          "Route filter prefix length cannot be 'longer' than " + Prefix6.MAX_PREFIX_LENGTH);
    }
    org.batfish.datamodel.Route6FilterLine line =
        new org.batfish.datamodel.Route6FilterLine(
            LineAction.PERMIT, _prefix6, new SubRange(prefixLength + 1, Prefix6.MAX_PREFIX_LENGTH));
    rfl.addLine(line);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineLonger)) {
      return false;
    }

    Route6FilterLineLonger rhs = (Route6FilterLineLonger) o;
    return _prefix6.equals(rhs._prefix6);
  }

  @Override
  public int hashCode() {
    return _prefix6.hashCode();
  }
}
