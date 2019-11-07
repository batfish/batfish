package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public abstract class RoutePolicyNextHop implements Serializable {

  public abstract NextHopExpr toNextHopExpr(CiscoXrConfiguration cc, Configuration c, Warnings w);
}
