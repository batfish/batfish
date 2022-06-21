package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.L3Interface} to a {@link
 * org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain} or a {@link
 * org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain}.
 */
public abstract class L3ToBridgeDomain extends Edge {

  protected L3ToBridgeDomain(StateFunction stateFunction) {
    super(stateFunction);
  }
}
