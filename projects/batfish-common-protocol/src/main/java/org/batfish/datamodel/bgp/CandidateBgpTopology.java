package org.batfish.datamodel.bgp;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Set;
import org.batfish.datamodel.BgpPeerConfigId;

/** Topology representing potential BGP peerings, even if some may be misconfigured. */
public class CandidateBgpTopology {
  public static final CandidateBgpTopology EMPTY =
      new CandidateBgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build());

  private final ValueGraph<BgpPeerConfigId, Annotation> _graph;

  public CandidateBgpTopology(ValueGraph<BgpPeerConfigId, Annotation> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  public ValueGraph<BgpPeerConfigId, Annotation> getGraph() {
    return _graph;
  }

  public Set<BgpPeerConfigId> nodes() {
    return _graph.nodes();
  }

  /** Container to describe properties of a potential peering */
  // TODO: whatever session compat question does right now, can go in here.
  public static final class Annotation {
    public Annotation() {}
  }
}
