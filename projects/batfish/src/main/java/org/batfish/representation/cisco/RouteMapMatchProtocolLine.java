package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

public class RouteMapMatchProtocolLine extends RouteMapMatchLine {

  private static final long serialVersionUID = 1L;

  private List<String> _protocols;

  public RouteMapMatchProtocolLine(List<String> protocol) {
    _protocols = protocol;
  }

  public List<String> getProtocols() {
    return _protocols;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (String protocol : _protocols) {
      disjuncts.add(new MatchProtocol(RoutingProtocol.fromProtocolName(protocol)));
    }
    return d.simplify();
  }
}
