package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.UndirectedEdge;

/** VXLAN topology with edges for each compatible VNI-endpoint pair */
public final class VxlanTopology {

  public static final VxlanTopology EMPTY =
      new VxlanTopology(GraphBuilder.undirected().allowsSelfLoops(false).build());
  private static final String PROP_EDGES = "edges";
  private static final String PROP_NODES = "nodes";

  @JsonCreator
  private static @Nonnull VxlanTopology create(
      @JsonProperty(PROP_EDGES) @Nullable List<UndirectedEdge<VxlanNode>> edges,
      @JsonProperty(PROP_NODES) @Nullable Set<VxlanNode> nodes) {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    if (nodes != null) {
      nodes.forEach(graph::addNode);
    }
    if (edges != null) {
      edges.forEach(edge -> graph.putEdge(edge.getNodeU(), edge.getNodeV()));
    }
    return new VxlanTopology(graph);
  }

  private final Graph<VxlanNode> _graph;

  public VxlanTopology(Graph<VxlanNode> graph) {
    _graph = ImmutableGraph.copyOf(graph);
  }

  @JsonProperty(PROP_EDGES)
  private @Nonnull List<UndirectedEdge<VxlanNode>> getEdges() {
    return _graph.edges().stream()
        .map(endpointPair -> new UndirectedEdge<>(endpointPair.nodeU(), endpointPair.nodeV()))
        .collect(ImmutableList.toImmutableList());
  }

  @JsonIgnore
  public @Nonnull Stream<EndpointPair<VxlanNode>> getLayer2VniEdges() {
    return _graph.edges().stream()
        .filter(
            endpointPair ->
                endpointPair.nodeU().getVniLayer() == LAYER_2
                    && endpointPair.nodeV().getVniLayer() == LAYER_2);
  }

  @JsonIgnore
  public @Nonnull Stream<EndpointPair<VxlanNode>> getLayer3VniEdges() {
    return _graph.edges().stream()
        .filter(
            endpointPair ->
                endpointPair.nodeU().getVniLayer() == LAYER_3
                    && endpointPair.nodeV().getVniLayer() == LAYER_3);
  }

  @JsonIgnore
  public @Nonnull Graph<VxlanNode> getGraph() {
    return _graph;
  }

  @JsonProperty(PROP_NODES)
  private @Nonnull Set<VxlanNode> getNodes() {
    return _graph.nodes();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VxlanTopology)) {
      return false;
    }
    return _graph.equals(((VxlanTopology) obj)._graph);
  }

  @Override
  public int hashCode() {
    return _graph.hashCode();
  }
}
