package org.batfish.representation.cumulus;

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

  public Optional<CumulusStructureType> getRouterStructureType() {
      /* XXX: REMOVE ME. The following are not defined in CumulusStructureType. Hence, commenting for now.
    if (this == EIGRP) {
      return Optional.of(CumulusStructureType.ROUTER_EIGRP);
    } else if (this == ISIS) {
      return Optional.of(CumulusStructureType.ROUTER_ISIS);
    } else if (this == OSPF) {
      return Optional.of(CumulusStructureType.ROUTER_OSPF);
    } else if (this == OSPFv3) {
      return Optional.of(CumulusStructureType.ROUTER_OSPFV3);
    } else if (this == RIP) {
      return Optional.of(CumulusStructureType.ROUTER_RIP);
    }
      */
    // Other structures are not reference tracked.
    return Optional.empty();
  }
}
