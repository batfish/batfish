package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;

public class BgpAggregateIpv4Network extends BgpAggregateNetwork {

  private Prefix _prefix;

  public BgpAggregateIpv4Network(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAggregateIpv4Network)) {
      return false;
    }
    BgpAggregateIpv4Network rhs = (BgpAggregateIpv4Network) o;
    return _prefix.equals(rhs._prefix);
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode();
  }
}
