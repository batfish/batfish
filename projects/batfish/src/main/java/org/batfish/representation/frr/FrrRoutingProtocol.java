package org.batfish.representation.frr;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.RoutingProtocol;

public enum FrrRoutingProtocol {
  OSPF,
  BGP,
  CONNECTED,
  STATIC;

  /** Maps each {@link FrrRoutingProtocol} to the set of {@link RoutingProtocol}s it includes. */
  public static final Map<FrrRoutingProtocol, Set<RoutingProtocol>> VI_PROTOCOLS_MAP =
      ImmutableMap.of(
          BGP,
          ImmutableSet.of(RoutingProtocol.BGP, RoutingProtocol.IBGP),
          OSPF,
          ImmutableSet.of(
              RoutingProtocol.OSPF,
              RoutingProtocol.OSPF_E1,
              RoutingProtocol.OSPF_E2,
              RoutingProtocol.OSPF_IA),
          CONNECTED,
          ImmutableSet.of(RoutingProtocol.CONNECTED),
          STATIC,
          ImmutableSet.of(RoutingProtocol.STATIC));
}
