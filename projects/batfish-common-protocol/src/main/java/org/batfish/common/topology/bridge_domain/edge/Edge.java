package org.batfish.common.topology.bridge_domain.edge;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.function.StateFunction;

/**
 * A directed edge between two {@link org.batfish.common.topology.bridge_domain.node.Node}s in the
 * L3 adjacencies computation graph.
 */
public abstract class Edge implements Serializable {

  /** A transformation on state to be performed when this edge is traversed. */
  public final @Nonnull StateFunction getStateFunction() {
    return _stateFunction;
  }

  @Override
  public final boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    Edge that = (Edge) o;
    return _stateFunction.equals(that._stateFunction);
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode() * 31 + _stateFunction.hashCode();
  }

  @Override
  public final String toString() {
    return toStringHelper(getClass()).add("_stateFunction", _stateFunction).toString();
  }

  protected Edge(StateFunction stateFunction) {
    _stateFunction = stateFunction;
  }

  private final @Nonnull StateFunction _stateFunction;
}
