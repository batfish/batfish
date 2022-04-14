package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.function.StateFunction;

public abstract class L2ToBridgeDomain extends Edge {

  protected L2ToBridgeDomain(StateFunction stateFunction) {
    super(stateFunction);
  }
}
