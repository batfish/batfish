package org.batfish.vendor.arista.representation;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetNextHopPeerAddress extends RouteMapSetLine {

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetNextHop(BgpPeerAddressNextHop.getInstance()));
  }
}
