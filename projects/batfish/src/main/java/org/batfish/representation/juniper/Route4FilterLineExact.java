package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public final class Route4FilterLineExact extends Route4FilterLine {

  public Route4FilterLineExact(Prefix prefix) {
    super(prefix);
  }

  @Override
  public List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines() {
    int prefixLength = _prefix.getPrefixLength();
    return ImmutableList.of(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT, _prefix, SubRange.singleton(prefixLength)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineExact)) {
      return false;
    }

    Route4FilterLineExact rhs = (Route4FilterLineExact) o;
    return _prefix.equals(rhs._prefix);
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode();
  }
}
