package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
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
  public @Nonnull BooleanExpr toBooleanExpr(Configuration c, FrrConfiguration vc, Warnings w) {
    return switch (_protocol) {
      case BGP -> new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case CONNECTED ->
          // TODO: cumulus local routes?
          new MatchProtocol(RoutingProtocol.CONNECTED);
      case EIGRP ->
          // TODO: verify subsets
          new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX);
      case ISIS ->
          // TODO: verify subsets
          new MatchProtocol(
              RoutingProtocol.ISIS_EL1,
              RoutingProtocol.ISIS_EL2,
              RoutingProtocol.ISIS_L1,
              RoutingProtocol.ISIS_L2);
      case KERNEL -> new MatchProtocol(RoutingProtocol.KERNEL);
      case OSPF ->
          // TODO: verify subsets
          new MatchProtocol(
              RoutingProtocol.OSPF,
              RoutingProtocol.OSPF_E1,
              RoutingProtocol.OSPF_E2,
              RoutingProtocol.OSPF_IA);
      case RIP -> new MatchProtocol(RoutingProtocol.RIP);
      case STATIC -> new MatchProtocol(RoutingProtocol.STATIC);
    };
  }

  public @Nonnull Protocol getProtocol() {
    return _protocol;
  }
}
