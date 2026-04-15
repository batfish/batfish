package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class Route4FilterLineOrLonger extends Route4FilterLine {

  public Route4FilterLineOrLonger(Prefix prefix) {
    super(prefix);
  }

  @Override
  public List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines() {
    int prefixLength = _prefix.getPrefixLength();
    return ImmutableList.of(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, _prefix, new SubRange(prefixLength, Prefix.MAX_PREFIX_LENGTH)));
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
