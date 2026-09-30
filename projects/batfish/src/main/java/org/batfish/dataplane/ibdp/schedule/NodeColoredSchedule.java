package org.batfish.dataplane.ibdp.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.graph.EndpointPair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.ospf.OspfTopology.EdgeId;
import org.batfish.dataplane.ibdp.Node;
import org.batfish.dataplane.ibdp.TopologyContext;
import org.jgrapht.Graph;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.alg.color.RandomGreedyColoring;
import org.jgrapht.alg.color.SaturationDegreeColoring;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Allows nodes to process/exchange routes only if they are of the same "graph color" (i.e., do not
 * have any protocol adjacencies)
 */
public final class NodeColoredSchedule extends IbdpSchedule {

  public enum Coloring {
    GREEDY,
    RANDOM,
    SATURATION
  }

  private final Iterator<Set<String>> _iterator;
  private Graph<String, DefaultEdge> _graph;

  /**
   * Create a new schedule based on existing nodes and topology
   *
   * @param nodes all nodes in the network
   * @param topologyContext the various network topologies
   */
  public NodeColoredSchedule(
      Map<String, Node> nodes, Coloring algorithm, TopologyContext topologyContext) {
    super(nodes);
    _graph = makeGraph(nodes, topologyContext);

    // Color the graph
    VertexColoringAlgorithm<String> coloringAlg = getColoringAlgorithmInstance(algorithm, _graph);
    VertexColoringAlgorithm.Coloring<String> coloring = coloringAlg.getColoring();
    List<Set<String>> colorClasses = ImmutableList.copyOf(coloring.getColorClasses());
    _iterator = colorClasses.iterator();
  }

  /**
   * Get a new instance of the coloring algorithm based
   *
   * @param type a {@link Coloring} type
   * @param graph the graph to color. Will be converted to a undirected graph
   * @return a new instance of {@link VertexColoringAlgorithm}
   */
  private static VertexColoringAlgorithm<String> getColoringAlgorithmInstance(
      Coloring type, Graph<String, DefaultEdge> graph) {
    return switch (type) {
      case GREEDY -> new GreedyColoring<>(graph);
      case RANDOM -> new RandomGreedyColoring<>(graph);
      case SATURATION -> new SaturationDegreeColoring<>(graph);
    };
  }

  /**
   * Create a graph for coloring purposes, and color it.
   *
   * @param nodes all nodes in the network
   * @param topologyContext the various network topologies
   */
  @SuppressWarnings("deprecation")
  private static Graph<String, DefaultEdge> makeGraph(
      Map<String, Node> nodes, TopologyContext topologyContext) {
    /*
     * For the purposes of coloring, two nodes are adjacent if:
     * - They have established a BGP session
     * - They have an OSPF adjacency
     */

    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    nodes.keySet().stream().sorted().forEach(graph::addVertex);

    Set<NodePair> edges = new TreeSet<>();
    for (EndpointPair<BgpPeerConfigId> edge : topologyContext.getBgpTopology().getGraph().edges()) {
      addEdge(edges, edge.source().getHostname(), edge.target().getHostname());
    }
    for (EdgeId edge : topologyContext.getOspfTopology().edges()) {
      addEdge(edges, edge.getTail().getHostname(), edge.getHead().getHostname());
    }
    edges.forEach(edge -> graph.addEdge(edge.first(), edge.second()));
    return graph;
  }

  private static void addEdge(Set<NodePair> edges, String node1, String node2) {
    if (!node1.equals(node2)) {
      edges.add(NodePair.of(node1, node2));
    }
  }

  private record NodePair(String first, String second) implements Comparable<NodePair> {
    private static NodePair of(String node1, String node2) {
      return node1.compareTo(node2) < 0 ? new NodePair(node1, node2) : new NodePair(node2, node1);
    }

    @Override
    public int compareTo(NodePair other) {
      int firstComparison = first.compareTo(other.first);
      return firstComparison != 0 ? firstComparison : second.compareTo(other.second);
    }
  }

  /**
   * Checks if unprocessed nodes are available in this schedule
   *
   * @return true if more unprocessed nodes are available
   */
  @Override
  public boolean hasNext() {
    return _iterator.hasNext();
  }

  /**
   * Get the next set of nodes that are allowed to be run in parallel during a dataplane iteration
   *
   * @return a map of nodes keyed by name, containing a subset of all network nodes
   */
  @Override
  public Map<String, Node> next() {
    Set<String> nodeNames = _iterator.next();
    return Maps.filterKeys(_nodes, nodeNames::contains);
  }
}
