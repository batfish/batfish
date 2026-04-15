package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;

public class Route4FilterLineLonger extends Route4FilterLine {

  public Route4FilterLineLonger(Prefix prefix) {
    super(prefix);
  }

  @Override
  public List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines() {
    int prefixLength = _prefix.getPrefixLength();
    if (prefixLength >= 32) {
      throw new BatfishException("Route filter prefix length cannot be 'longer' than 32");
    }
    return ImmutableList.of(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, PrefixRange.moreSpecificThan(_prefix)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineLonger)) {
      return false;
    }

    Route4FilterLineLonger rhs = (Route4FilterLineLonger) o;
    return _prefix.equals(rhs._prefix);
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode();
  }
}
