package org.batfish.datamodel.interface_dependency;

import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.Interface.DependencyType.BIND;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

public class InterfaceDependencies {
  private static final NodeInterfacePair NON_EXISTENT_INTERFACE =
      NodeInterfacePair.of("non existent device", "non existent interface");

  public static Set<NodeInterfacePair> getInterfacesToDeactivate(
      Map<String, Configuration> configs, L3Adjacencies l3Adjacencies) {
    return computeInterfacesToDeactivate(
        computeInitiallyInactiveInterfaces(configs),
        computeDependencyGraph(configs, l3Adjacencies));
  }

  private static Set<NodeInterfacePair> computeInitiallyInactiveInterfaces(
      Map<String, Configuration> configs) {
    return configs.values().stream()
        .flatMap(config -> config.getAllInterfaces().values().stream())
        .filter(iface -> !iface.getActive())
        .map(NodeInterfacePair::of)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static Set<NodeInterfacePair> computeInterfacesToDeactivate(
      Set<NodeInterfacePair> initiallyInactiveInterfaces,
      Graph<NodeInterfacePair, DependencyEdge> depGraph) {
    Set<NodeInterfacePair> interfacesToDeactivate = new HashSet<>();
    List<NodeInterfacePair> workList = new LinkedList<>();
    workList.add(NON_EXISTENT_INTERFACE);
    initiallyInactiveInterfaces.stream().filter(depGraph::containsVertex).forEach(workList::add);

    Predicate<NodeInterfacePair> isInactive =
        (iface) ->
            iface == NON_EXISTENT_INTERFACE
                || initiallyInactiveInterfaces.contains(iface)
                || interfacesToDeactivate.contains(iface);
    Consumer<NodeInterfacePair> deactivate =
        (iface) -> {
          assert !isInactive.test(iface) : "deactivating already-inactive iface";
          interfacesToDeactivate.add(iface);
          workList.add(iface);
        };

    while (!workList.isEmpty()) {
      NodeInterfacePair inactiveIface = workList.remove(0);
      assert isInactive.test(inactiveIface) : "interface in workList has not been deactivated";

      for (DependencyEdge outEdge : depGraph.outgoingEdgesOf(inactiveIface)) {
        NodeInterfacePair tgtIface = depGraph.getEdgeTarget(outEdge);
        if (isInactive.test(tgtIface)) {
          // already deactivated
          continue;
        }
        switch (outEdge.getDependencyType()) {
          case BIND:
            deactivate.accept(tgtIface);
            break;
          case AGGREGATE:
            if (depGraph.incomingEdgesOf(tgtIface).stream()
                .filter(edge -> edge.getDependencyType() == AGGREGATE)
                .map(depGraph::getEdgeSource)
                .allMatch(isInactive)) {
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
    return interfacesToDeactivate;
  }

  private static Graph<NodeInterfacePair, DependencyEdge> computeDependencyGraph(
      Map<String, Configuration> configs, L3Adjacencies l3Adjacencies) {
    Graph<NodeInterfacePair, DependencyEdge> graph =
        new SimpleDirectedGraph<>(DependencyEdge.class);
    graph.addVertex(NON_EXISTENT_INTERFACE);

    configs
        .values()
        .forEach(
            config -> {
              Map<String, Interface> configIfaces = config.getAllInterfaces();
              configIfaces
                  .values()
                  .forEach(
                      iface -> {
                        NodeInterfacePair nip1 = NodeInterfacePair.of(iface);
                        graph.addVertex(nip1);

                        InterfaceType ifaceType = iface.getInterfaceType();

                        // handle aggregate interfaces with no AGGREGATE dependencies
                        if ((ifaceType == InterfaceType.REDUNDANT
                                || ifaceType == InterfaceType.AGGREGATED)
                            && iface.getDependencies().stream()
                                .noneMatch(dep -> dep.getType() == AGGREGATE)) {
                          // interface cannot come up without any AGGREGATE deps. add an edge that
                          // will cause iface to be deactivated
                          graph.addEdge(
                              NON_EXISTENT_INTERFACE, nip1, new DependencyEdge(AGGREGATE));
                        }

                        // add local dependencies
                        iface
                            .getDependencies()
                            .forEach(
                                dep -> {
                                  // if the depended-upon interface does not exist, the dependency
                                  // is not satisfied
                                  NodeInterfacePair nip2 =
                                      Optional.ofNullable(configIfaces.get(dep.getInterfaceName()))
                                          .map(NodeInterfacePair::of)
                                          .orElse(NON_EXISTENT_INTERFACE);
                                  graph.addVertex(nip2);
                                  graph.addEdge(nip2, nip1, new DependencyEdge(dep.getType()));
                                });

                        // add non-local dependencies for PHYSICAL and AGGREGATED/REDUNDANT
                        // interfaces only.
                        switch (ifaceType) {
                          case PHYSICAL:
                          case AGGREGATED:
                          case REDUNDANT:
                            /* Add a BIND dependency to the (single) l3 neighbor (if any). If either interface is down, the
                             * other will be too.
                             */
                            try {
                              l3Adjacencies
                                  .pairedPointToPointL3Interface(nip1)
                                  .ifPresent(
                                      nip2 -> {
                                        graph.addVertex(nip2);
                                        graph.addEdge(nip1, nip2, new DependencyEdge(BIND));
                                        graph.addEdge(nip2, nip1, new DependencyEdge(BIND));
                                      });
                            } catch (IllegalArgumentException e) {
                              // iface is not an L3 interface.
                            }
                            break;
                          default:
                            break;
                        }
                      });
            });
    return graph;
  }

  private InterfaceDependencies() {}
}
