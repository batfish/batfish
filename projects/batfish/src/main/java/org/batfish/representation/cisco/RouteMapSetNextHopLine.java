package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetNextHopLine extends RouteMapSetLine {

  private static final long serialVersionUID = 1L;

  private List<Ip> _nextHops;

  public RouteMapSetNextHopLine(List<Ip> nextHops) {
    _nextHops = nextHops;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    // TODO: something with destination-vrf
    statements.add(new SetNextHop(new IpNextHop(_nextHops), false));
  }

  public List<Ip> getNextHops() {
    return _nextHops;
  }

  @Override
  public RouteMapSetType getType() {
    return RouteMapSetType.NEXT_HOP;
  }
}
