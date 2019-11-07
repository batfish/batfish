package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetNextHop extends RoutePolicySetStatement {

  private boolean _destinationVrf;

  private RoutePolicyNextHop _nextHop;

  public RoutePolicySetNextHop(RoutePolicyNextHop nextHop, boolean destinationVrf) {
    _nextHop = nextHop;
    _destinationVrf = destinationVrf;
  }

  public boolean getDestinationVrf() {
    return _destinationVrf;
  }

  public RoutePolicyNextHop getNextHop() {
    return _nextHop;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetNextHop(_nextHop.toNextHopExpr(cc, c, w));
  }
}
