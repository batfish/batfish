package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Vni} to a {@link
 * org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain} or a {@link
 * org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain}.
 */
public abstract class L2VniToBridgeDomain extends Edge {

  protected L2VniToBridgeDomain(StateFunction stateFunction) {
    super(stateFunction);
  }
}
