package org.batfish.representation.cisco_nxos;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.batfish.datamodel.RoutingProtocol;

/** NX-OS routing protocols. */
public enum NxosRoutingProtocol {
  BGP,
  DIRECT,
  EIGRP,
  ISIS,
  LISP,
  OSPF,
  OSPFv3,
  RIP,
  STATIC,
  ;

  public static List<RoutingProtocol> toRoutingProtocols(NxosRoutingProtocol protocol) {
    switch (protocol) {
      case BGP:
        return ImmutableList.of(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case DIRECT:
        return ImmutableList.of(RoutingProtocol.CONNECTED);
      case EIGRP:
        return ImmutableList.of(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX);
      case ISIS:
        return ImmutableList.of(RoutingProtocol.ISIS_ANY);
      case LISP:
        return ImmutableList.of(RoutingProtocol.LISP);
      case OSPF:
        return ImmutableList.of(
            RoutingProtocol.OSPF,
            RoutingProtocol.OSPF_E1,
            RoutingProtocol.OSPF_E2,
            RoutingProtocol.OSPF_IA);
      case OSPFv3:
        return ImmutableList.of(RoutingProtocol.OSPF3);
      case RIP:
        return ImmutableList.of(RoutingProtocol.RIP);
      case STATIC:
        return ImmutableList.of(RoutingProtocol.STATIC);
      default:
        throw new IllegalArgumentException(
            String.format("Unrecognized NxosRoutingProtocol %s", protocol));
    }
  }

  public Optional<CiscoNxosStructureType> getRouterStructureType() {
    if (this == EIGRP) {
      return Optional.of(CiscoNxosStructureType.ROUTER_EIGRP);
    } else if (this == ISIS) {
      return Optional.of(CiscoNxosStructureType.ROUTER_ISIS);
    } else if (this == OSPF) {
      return Optional.of(CiscoNxosStructureType.ROUTER_OSPF);
    } else if (this == OSPFv3) {
      return Optional.of(CiscoNxosStructureType.ROUTER_OSPFV3);
    } else if (this == RIP) {
      return Optional.of(CiscoNxosStructureType.ROUTER_RIP);
    }
    // Other structures are not reference tracked.
    return Optional.empty();
  }
}
