package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** Represents a "from protocol" line in a {@link PsTerm} */
public final class PsFromProtocol extends PsFrom {

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
      return new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
    } else if (_protocol == RoutingProtocol.ISIS_ANY) {
      return new MatchProtocol(
          RoutingProtocol.ISIS_EL1,
          RoutingProtocol.ISIS_EL2,
          RoutingProtocol.ISIS_L1,
          RoutingProtocol.ISIS_L2);
    } else if (_protocol == RoutingProtocol.OSPF) {
      return new MatchProtocol(
          RoutingProtocol.OSPF,
          RoutingProtocol.OSPF_IA,
          RoutingProtocol.OSPF_E1,
          RoutingProtocol.OSPF_E2);
    } else {
      return new MatchProtocol(_protocol);
    }
  }
}
