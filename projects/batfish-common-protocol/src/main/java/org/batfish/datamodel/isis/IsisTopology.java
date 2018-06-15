package org.batfish.datamodel.isis;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;

public final class IsisTopology {

  private final Set<IsisEdge> _edges;

  public IsisTopology(Map<String, Configuration> configurations, Topology topology) {
    _edges =
        topology
            .getEdges()
            .stream()
            .map(edge -> IsisEdge.newEdge(edge, configurations))
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());
  }

  public Set<IsisEdge> getEdges() {
    return _edges;
  }
}
