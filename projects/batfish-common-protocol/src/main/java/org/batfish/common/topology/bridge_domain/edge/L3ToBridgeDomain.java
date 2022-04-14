package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.function.StateFunction;

public abstract class L3ToBridgeDomain extends Edge {

  protected L3ToBridgeDomain(StateFunction stateFunction) {
    super(stateFunction);
  }
}
