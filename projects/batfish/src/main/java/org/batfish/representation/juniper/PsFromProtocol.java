package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** Represents a "from protocol" line in a {@link PsTerm} */
public final class PsFromProtocol extends PsFrom {

  private final PsProtocol _protocol;

  public PsFromProtocol(PsProtocol protocol) {
    _protocol = protocol;
  }

  public PsProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    switch (_protocol) {
      case BGP:
        return new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case ISIS:
        return new MatchProtocol(
            RoutingProtocol.ISIS_EL1,
            RoutingProtocol.ISIS_EL2,
            RoutingProtocol.ISIS_L1,
            RoutingProtocol.ISIS_L2);
      case OSPF:
        return new MatchProtocol(
            RoutingProtocol.OSPF,
            RoutingProtocol.OSPF_IA,
            RoutingProtocol.OSPF_E1,
            RoutingProtocol.OSPF_E2);
      case AGGREGATE:
        return new MatchProtocol(RoutingProtocol.AGGREGATE);
      case DIRECT:
        return new MatchProtocol(RoutingProtocol.CONNECTED);
      case EVPN:
        return new MatchProtocol(RoutingProtocol.EVPN);
      case LDP:
        return new MatchProtocol(RoutingProtocol.LDP);
      case LOCAL:
        return new MatchProtocol(RoutingProtocol.LOCAL);
      case OSPF3:
        return new MatchProtocol(RoutingProtocol.OSPF3);
      case RSVP:
        return new MatchProtocol(RoutingProtocol.RSVP);
      case STATIC:
        return new MatchProtocol(RoutingProtocol.STATIC);
      case ACCESS_INTERNAL:
        // TODO: determine correct VI model representation for ACCESS_INTERNAL
        warnings.redFlag("'from protocol access-internal' is not yet supported");
        return BooleanExprs.FALSE;
    }
    throw new IllegalStateException("Unexpected protocol: " + _protocol);
  }
}
