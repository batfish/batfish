package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.PeerAddressNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetNextHopPeerAddress extends RouteMapSetLine {

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetNextHop(PeerAddressNextHop.getInstance(), false));
  }

  @Override
  public RouteMapSetType getType() {
    return RouteMapSetType.NEXT_HOP_PEER_ADDRESS;
  }
}
