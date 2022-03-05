package org.batfish.common.topology.bridge_domain.edge;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * A directed edge between two {@link org.batfish.common.topology.bridge_domain.node.Node}s in the
 * L3 adjacencies computation graph.
 */
public abstract class Edge {

  /** A transformation on state to be performed when this edge is traversed. */
  public final @Nonnull StateFunction getStateFunction() {
    return _stateFunction;
  }

  protected Edge(StateFunction stateFunction) {
    _stateFunction = stateFunction;
  }

  private final @Nonnull StateFunction _stateFunction;
}
