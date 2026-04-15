package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

/** Representation of a route-filter with a IPv4 prefix and an address mask */
public final class Route4FilterLineAddressMask extends Route4FilterLine {

  private final Ip _addressMask;

  public Route4FilterLineAddressMask(Prefix prefix, Ip addressMask) {
    super(prefix);
    _addressMask = addressMask;
  }

  @Override
  public List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines() {
    int prefixLength = _prefix.getPrefixLength();
    return ImmutableList.of(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT,
            IpWildcard.ipWithWildcardMask(
                Prefix.create(_prefix.getStartIp(), prefixLength).getStartIp(), _addressMask),
            SubRange.singleton(prefixLength)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route4FilterLineAddressMask)) {
      return false;
    }

    Route4FilterLineAddressMask rhs = (Route4FilterLineAddressMask) o;
    return _prefix.equals(rhs._prefix) && _addressMask.equals(rhs._addressMask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _addressMask);
  }
}
