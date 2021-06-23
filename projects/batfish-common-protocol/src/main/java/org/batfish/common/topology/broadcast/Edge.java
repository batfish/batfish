package org.batfish.common.topology.broadcast;

import java.util.Optional;

/**
 * Represents a directional edge from a {@link Node} with data {@code D1} to a {@link Node} with
 * data {@code D2}, modeling how data present in one location will be received at the other.
 */
public interface Edge<D1, D2> {

  /**
   * Returns the data that {@code to} will receive if {@code from} has the given {@code data}.
   * Returns {@link Optional#empty()} if nothing.
   */
  Optional<D2> traverse(D1 data);
}
