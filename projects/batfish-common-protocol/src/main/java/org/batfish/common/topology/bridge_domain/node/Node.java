package org.batfish.common.topology.bridge_domain.node;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;

/** A node in the broadcast domain computation graph. */
public interface Node {
  @Nonnull
  Map<Node, Edge> getOutEdges();
}
