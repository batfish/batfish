package org.batfish.common.topology.bridge_domain.node;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;

/**
 * A (physical, logical, computational) location in the network that a frame in the process of
 * transmission may reach.
 *
 * <p>Note: Each {@link Node} keeps track of its {@link Edge edges} to other {@link Node nodes}, but
 * these are <strong>not</strong> used in {@link Node#equals(Object)} or {@link Node#hashCode()}.
 */
public interface Node {
  @Nonnull
  Map<Node, Edge> getOutEdges();
}
