package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public abstract class RoutePolicyNextHop implements Serializable {

  public abstract Optional<NextHopExpr> toNextHopExpr(
      CiscoXrConfiguration cc, Configuration c, Warnings w);
}
