package org.batfish.datamodel.interface_dependency;

import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.Interface.DependencyType.BIND;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

  private final Logger _logger = LogManager.getLogger(InterfaceDependencies.class);
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
        NodeInterfacePair ifaceId = NodeInterfacePair.of(iface);
        if (!iface.getActive()) {
          _inactiveInterfaces.add(ifaceId);
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
                addDependencyEdge(neighbor, ifaceId, BIND);
              }
              break;
            }
          case AGGREGATED:
          case REDUNDANT:
            {
              // if the aggregate has no AGGREGATE dependencies, deactivate it.
              if (iface.getDependencies().stream().noneMatch(dep -> dep.getType() == AGGREGATE)) {
                _logger.warn(
                    "deactivating AGGREGATE/REDUNDANT interface {} with no dependencies", ifaceId);
                _interfacesToDeactivate.add(ifaceId);
                break;
              }

              // non-local dependencies
              Set<Layer1Node> neighbors = getL1Neighbors(iface);
              if (neighbors.size() == 1) {
                /* if the neighbor is inactive, iface must be too. e.g. if all member interfaces are up, but the
                 * neighbor is admin-down, iface will be down
                 * TODO sanity check: neighbor is a portchannel and its members are neighbors of iface's members
                 */
                addDependencyEdge(
                    Iterables.getOnlyElement(neighbors).asNodeInterfacePair(), ifaceId, BIND);
                break;
              } else if (neighbors.size() == 2) {
                // check if this is likely a virtual portchannel: assume if the neighbors are on two
                // devices, and our members have neighbors on those same two devices, it's probably
                // a virtual portchannel

                // TODO this check could be more strict, i.e. check that:
                // iface --> members --> neighbors == iface --> neighbors --> members
                Set<String> memberInterfaceNeighborNodes =
                    iface.getDependencies().stream()
                        .filter(dep -> dep.getType() == AGGREGATE)
                        .map(dep -> getL1Neighbor(allIfaces.get(dep.getInterfaceName())))
                        .filter(Objects::nonNull)
                        .map(NodeInterfacePair::getHostname)
                        .collect(ImmutableSet.toImmutableSet());
                Set<String> neighborHostnames =
                    neighbors.stream().map(Layer1Node::getHostname).collect(Collectors.toSet());

                if (neighborHostnames.size() == 2
                    && neighborHostnames.equals(memberInterfaceNeighborNodes)) {
                  // aggregate may be a virtual portchannel, which we don't support well yet. we
                  // need both VI modeling and improved dependency tracking. In particular,
                  // iface should come down if both of its neighbors are down, so we need
                  // something like a second group of AGGREGATE dependencies.
                  _logger.warn(
                      "interface {} looks like a virtual portchannel. "
                          + "Disabling dependency tracking.",
                      ifaceId);
                  break;
                }
                // fall through to catch-all below
              } else if (neighbors.isEmpty()) {
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
                  _logger.warn(
                      () -> {
                        ImmutableMap<String, NodeInterfacePair> depToNeighbor =
                            iface.getDependencies().stream()
                                .filter(
                                    dep ->
                                        dep.getType() == AGGREGATE
                                            && getL1Neighbor(allIfaces.get(dep.getInterfaceName()))
                                                != null)
                                .collect(
                                    ImmutableMap.toImmutableMap(
                                        Interface.Dependency::getInterfaceName,
                                        dep ->
                                            getL1Neighbor(allIfaces.get(dep.getInterfaceName()))));
                        return String.format(
                            "Deactivating %s interface %s with no neighbor. "
                                + "Some of its members do have neighbors: %s",
                            iface.getInterfaceType(), ifaceId, depToNeighbor);
                      });
                  _interfacesToDeactivate.add(ifaceId);
                  break;
                }

                // looks like a boundary interface; leave it up
                break;
              }

              _logger.warn(
                  "deactivating {} interface {} with neighbors {}",
                  iface.getInterfaceType(),
                  ifaceId,
                  neighbors);
              _interfacesToDeactivate.add(ifaceId);
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
    // ensure both vertices exist, otherwise addEdge will throw. it's ok to re-add a vertex.
    _depGraph.addVertex(src);
    _depGraph.addVertex(tgt);
    _depGraph.addEdge(src, tgt, new DependencyEdge(depType));
  }

  private Set<Layer1Node> getL1Neighbors(Interface iface) {
    Layer1Node layer1Node = new Layer1Node(iface.getOwner().getHostname(), iface.getName());
    switch (iface.getInterfaceType()) {
      case PHYSICAL:
        return adjacentNodes(_layer1Topologies.getCombinedL1(), layer1Node);
      case AGGREGATED:
      case REDUNDANT:
        // the logical L1 contains physical nodes and AGGREGATED/REDUNDANT nodes. filter them out.
        return adjacentNodes(_layer1Topologies.getLogicalL1(), layer1Node).stream()
            .filter(
                node ->
                    _configs
                            .get(node.getHostname())
                            .getAllInterfaces()
                            .get(node.getInterfaceName())
                            .getInterfaceType()
                        == iface.getInterfaceType())
            .collect(ImmutableSet.toImmutableSet());
      default:
        return ImmutableSet.of();
    }
  }

  private static Set<Layer1Node> adjacentNodes(Layer1Topology topology, Layer1Node node) {
    if (!topology.getGraph().nodes().contains(node)) {
      return ImmutableSet.of();
    }
    return topology.getGraph().adjacentNodes(node);
  }

  private @Nullable NodeInterfacePair getL1Neighbor(Interface iface) {
    Set<Layer1Node> neighbors = getL1Neighbors(iface);
    if (neighbors.isEmpty()) {
      return null;
    }
    if (neighbors.size() > 1) {
      _logger.warn("ambiguous l1 neighbor for {}: {}", NodeInterfacePair.of(iface), neighbors);
      return null;
    }
    return Iterables.getOnlyElement(neighbors).asNodeInterfacePair();
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
