package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public class RoutePolicyNextHopIP6 extends RoutePolicyNextHop {

  private Ip6 _address;

  public RoutePolicyNextHopIP6(Ip6 address) {
    _address = address;
  }

  public Ip6 getAddress() {
    return _address;
  }

  @Override
  public NextHopExpr toNextHopExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return null;
  }
}
