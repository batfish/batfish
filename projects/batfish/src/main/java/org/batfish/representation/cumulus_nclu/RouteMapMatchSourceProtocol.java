package org.batfish.representation.cumulus_nclu;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** A {@link RouteMapMatch} that implements {@code route-map match source-protocol}. */
public final class RouteMapMatchSourceProtocol implements RouteMapMatch {

  public enum Protocol {
    BGP,
    CONNECTED,
    EIGRP,
    ISIS,
    KERNEL,
    OSPF,
    RIP,
    STATIC,
  }

  private final @Nonnull Protocol _protocol;

  public RouteMapMatchSourceProtocol(@Nonnull Protocol protocol) {
    _protocol = protocol;
  }

  @Override
  public @Nonnull BooleanExpr toBooleanExpr(
      Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    switch (_protocol) {
      case BGP:
        return new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case CONNECTED:
        // TODO: cumulus local routes?
        return new MatchProtocol(RoutingProtocol.CONNECTED);
      case EIGRP:
        // TODO: verify subsets
        return new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX);
      case ISIS:
        // TODO: verify subsets
        return new MatchProtocol(
            RoutingProtocol.ISIS_EL1,
            RoutingProtocol.ISIS_EL2,
            RoutingProtocol.ISIS_L1,
            RoutingProtocol.ISIS_L2);
      case KERNEL:
        return new MatchProtocol(RoutingProtocol.KERNEL);
      case OSPF:
        // TODO: verify subsets
        return new MatchProtocol(
            RoutingProtocol.OSPF,
            RoutingProtocol.OSPF_E1,
            RoutingProtocol.OSPF_E2,
            RoutingProtocol.OSPF_IA);
      case RIP:
        return new MatchProtocol(RoutingProtocol.RIP);
      case STATIC:
        return new MatchProtocol(RoutingProtocol.STATIC);
      default:
        w.unimplemented(String.format("Matching protocol %s", _protocol));
        return BooleanExprs.FALSE;
    }
  }

  public @Nonnull Protocol getProtocol() {
    return _protocol;
  }
}
