package org.batfish.representation.cisco_xr;

import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public class RoutePolicyNextHopPeerAddress extends RoutePolicyNextHop {

  @Override
  public Optional<NextHopExpr> toNextHopExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return Optional.of(BgpPeerAddressNextHop.getInstance());
  }
}
