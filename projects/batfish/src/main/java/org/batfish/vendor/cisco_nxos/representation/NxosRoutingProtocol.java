package org.batfish.vendor.cisco_nxos.representation;

import java.util.Optional;

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
