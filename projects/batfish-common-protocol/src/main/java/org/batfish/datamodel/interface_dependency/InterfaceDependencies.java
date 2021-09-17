package org.batfish.datamodel.interface_dependency;

import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.Interface.DependencyType.BIND;

import com.google.common.collect.Iterables;
import com.google.common.graph.Network;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

public class InterfaceDependencies {
  private static final NodeInterfacePair NON_EXISTENT_INTERFACE =
      NodeInterfacePair.of("non existent device", "non existent interface");

  /**
   * Compute the interfaces that should be deactivated because they depend on one or more missing or
   * inactive interfaces.
   */
  public static Set<NodeInterfacePair> getInterfacesToDeactivate(
      Map<String, Configuration> configs, Layer1Topologies layer1Topologies) {
    InterfaceDependencies deps = new InterfaceDependencies(configs, layer1Topologies);
    deps.initialize();
    deps.run();
    return deps._interfacesToDeactivate;
  }

  private final Map<String, Configuration> _configs;
  private final Layer1Topologies _layer1Topologies;
  private final Graph<NodeInterfacePair, DependencyEdge> _depGraph;
  private final Set<NodeInterfacePair> _inactiveInterfaces = new HashSet<>();
  private final Set<NodeInterfacePair> _interfacesToDeactivate = new HashSet<>();

  private InterfaceDependencies(
      Map<String, Configuration> configs, Layer1Topologies layer1Topologies) {
    _configs = configs;
    _layer1Topologies = layer1Topologies;

    _depGraph = new SimpleDirectedGraph<>(DependencyEdge.class);
    _depGraph.addVertex(NON_EXISTENT_INTERFACE);
  }

  private void initialize() {
    for (Configuration config : _configs.values()) {
      Map<String, Interface> allIfaces = config.getAllInterfaces();
      for (Interface iface : allIfaces.values()) {
        if (!iface.getActive()) {
          _inactiveInterfaces.add(NodeInterfacePair.of(iface));
          continue;
        }

        // add edges for local dependencies
        addDependencyEdges(iface);

        // add edges for type-specific dependencies
        switch (iface.getInterfaceType()) {
          case PHYSICAL:
            {
              // non-local dependencies
              @Nullable NodeInterfacePair neighbor = getL1Neighbor(iface);
              if (neighbor != null) {
                addDependencyEdge(neighbor, NodeInterfacePair.of(iface), BIND);
              }
              break;
            }
          case AGGREGATED:
          case REDUNDANT:
            {
              // if the aggregate has no AGGREGATE dependencies, deactivate it.
              if (iface.getDependencies().stream().noneMatch(dep -> dep.getType() == AGGREGATE)) {
                _interfacesToDeactivate.add(NodeInterfacePair.of(iface));
              }

              // non-local dependencies
              @Nullable NodeInterfacePair neighbor = getL1Neighbor(iface);
              if (neighbor == null) {
                /* aggregate has no neighbor. if any member interface has a neighbor, deactivate iface
                 * assumption: if none of the member interfaces has a neighbor, this is the network
                 * boundary. leave iface active so traffic can exit the network here. if any of the member
                 * interfaces has a neighbor, then this is not the network bounadary, so this aggregate
                 * needs a neighbor
                 */
                if (iface.getDependencies().stream()
                    .anyMatch(
                        dep ->
                            dep.getType() == AGGREGATE
                                && getL1Neighbor(allIfaces.get(dep.getInterfaceName())) != null)) {
                  _interfacesToDeactivate.add(NodeInterfacePair.of(iface));
                }
              } else {
                /* if the neighbor is inactive, iface must be too.
                 * e.g. if all member interfaces are up, but the neighbor is admin-down, iface will
                 * be down
                 */
                addDependencyEdge(neighbor, NodeInterfacePair.of(iface), BIND);
              }
              break;
            }
          default:
            break;
        }
      }
    }
  }

  private void addDependencyEdges(Interface iface) {
    iface.getDependencies().forEach(dep -> addDependencyEdge(iface, dep));
  }

  private void addDependencyEdge(Interface tgtIface, Interface.Dependency dep) {
    NodeInterfacePair tgt = NodeInterfacePair.of(tgtIface);
    NodeInterfacePair src =
        Optional.ofNullable(tgtIface.getOwner().getAllInterfaces().get(dep.getInterfaceName()))
            .map(NodeInterfacePair::of)
            .orElse(NON_EXISTENT_INTERFACE);
    addDependencyEdge(src, tgt, dep.getType());
  }

  private void addDependencyEdge(
      NodeInterfacePair src, NodeInterfacePair tgt, Interface.DependencyType depType) {
    _depGraph.addVertex(src);
    _depGraph.addVertex(tgt);
    _depGraph.addEdge(src, tgt, new DependencyEdge(depType));
  }

  private @Nullable Layer1Topology getLayer1Topology(Interface iface) {
    switch (iface.getInterfaceType()) {
      case PHYSICAL:
        return _layer1Topologies.getCombinedL1();
      case AGGREGATED:
      case REDUNDANT:
        return _layer1Topologies.getLogicalL1();
      default:
        return null;
    }
  }

  private @Nullable NodeInterfacePair getL1Neighbor(Interface iface) {
    @Nullable Layer1Topology l1Topology = getLayer1Topology(iface);
    if (l1Topology == null) {
      return null;
    }
    Network<Layer1Node, Layer1Edge> l1Graph = l1Topology.getGraph();
    Layer1Node layer1Node = new Layer1Node(iface.getOwner().getHostname(), iface.getName());
    if (!l1Graph.nodes().contains(layer1Node)) {
      return null;
    }
    Set<Layer1Node> neighbors = l1Graph.adjacentNodes(layer1Node);
    if (neighbors.size() == 1) {
      return Iterables.getOnlyElement(neighbors).asNodeInterfacePair();
    }
    return null;
  }

  private boolean isInactive(NodeInterfacePair nip) {
    return nip == NON_EXISTENT_INTERFACE
        || _inactiveInterfaces.contains(nip)
        || _interfacesToDeactivate.contains(nip);
  }

  private void run() {
    Queue<NodeInterfacePair> workQueue = new LinkedList<>();
    workQueue.add(NON_EXISTENT_INTERFACE);
    Stream.of(_inactiveInterfaces, _interfacesToDeactivate)
        .flatMap(Set::stream)
        .filter(_depGraph::containsVertex)
        .forEach(workQueue::add);

    Consumer<NodeInterfacePair> deactivate =
        (iface) -> {
          assert !isInactive(iface) : "deactivating already-inactive iface";
          _interfacesToDeactivate.add(iface);
          workQueue.add(iface);
        };

    while (!workQueue.isEmpty()) {
      NodeInterfacePair inactiveIface = workQueue.poll();
      assert isInactive(inactiveIface) : "interface in workList has not been deactivated";

      for (DependencyEdge outEdge : _depGraph.outgoingEdgesOf(inactiveIface)) {
        NodeInterfacePair tgtIface = _depGraph.getEdgeTarget(outEdge);
        if (isInactive(tgtIface)) {
          // already deactivated
          continue;
        }
        switch (outEdge.getDependencyType()) {
          case BIND:
            deactivate.accept(tgtIface);
            break;
          case AGGREGATE:
            if (_depGraph.incomingEdgesOf(tgtIface).stream()
                .filter(edge -> edge.getDependencyType() == AGGREGATE)
                .map(_depGraph::getEdgeSource)
                .allMatch(this::isInactive)) {
              // all the interfaces tgtIface depends upon are inactive. deactivate it.
              deactivate.accept(tgtIface);
            }
            break;
          default:
            throw new IllegalStateException(
                "Unexpected Interface.DependencyType " + outEdge.getDependencyType().name());
        }
      }
    }
  }
}
