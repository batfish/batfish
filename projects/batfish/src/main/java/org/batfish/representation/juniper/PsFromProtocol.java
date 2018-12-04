package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

public final class PsFromProtocol extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final RoutingProtocol _protocol;

  public PsFromProtocol(RoutingProtocol protocol) {
    _protocol = protocol;
  }

  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (_protocol == RoutingProtocol.BGP) {
      return new Disjunction(
          new MatchProtocol(RoutingProtocol.BGP), new MatchProtocol(RoutingProtocol.IBGP));
    } else {
      return new MatchProtocol(_protocol);
    }
  }
}
