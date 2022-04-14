package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.function.StateFunction;

public abstract class L2VniToBridgeDomain extends Edge {

  protected L2VniToBridgeDomain(StateFunction stateFunction) {
    super(stateFunction);
  }
}
