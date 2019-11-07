package org.batfish.representation.cisco_xr;

import org.batfish.datamodel.Prefix6;

public class BgpAggregateIpv6Network extends BgpAggregateNetwork {

  private Prefix6 _prefix6;

  public BgpAggregateIpv6Network(Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAggregateIpv6Network)) {
      return false;
    }
    BgpAggregateIpv6Network rhs = (BgpAggregateIpv6Network) o;
    return _prefix6.equals(rhs._prefix6);
  }

  public Prefix6 getPrefix6() {
    return _prefix6;
  }

  @Override
  public int hashCode() {
    return _prefix6.hashCode();
  }
}
