package org.batfish.datamodel.ospf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Map.Entry;
import java.util.Optional;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility functions for computing OSPF topology */
public final class OspfTopologyUtils {

  /** Initialize an OSPF topology. */
  public static OspfTopology computeOspfTopology(
      NetworkConfigurations configurations, Topology l3Topology) {

    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        collectNodes(configurations);
    establishLinks(configurations, graph, l3Topology);
    trimLinks(graph);

    return new OspfTopology(ImmutableValueGraph.copyOf(graph));
  }

  /**
   * Examine {@link Configuration} objects to compute and generate IDs for all available OSPF
   * neighbors on all OSPF processes; initialize {@link OspfProcess} map containing all local
   * neighbor configs, obtainable with {@link OspfProcess#getOspfNeighborConfigs()}.
   */
  public static void initNeighborConfigs(NetworkConfigurations configurations) {
    // Iterate over all configurations
    for (Configuration config : configurations.all()) {
      // All VRFs in the configuration
      for (Entry<String, Vrf> vrfEntry : config.getVrfs().entrySet()) {
        Vrf vrf = vrfEntry.getValue();
        for (OspfProcess proc : vrf.getOspfProcesses().values()) {
          Builder<String, OspfNeighborConfig> neighborMap = ImmutableMap.builder();

          // Iterate over all OSPF areas
          for (Entry<Long, OspfArea> ospfAreaEntry : proc.getAreas().entrySet()) {
            OspfArea area = ospfAreaEntry.getValue();

            // All interfaces in this area
            for (String ifaceName : area.getInterfaces()) {
              Interface iface = config.getAllInterfaces().get(ifaceName);
              // only checking and adding the concrete (primary) address of iface
              // TODO: check if secondary addresses also participate in OSPF neighbor relationships
              if (iface.getConcreteAddress() == null) {
                continue;
              }
              neighborMap.put(
                  ifaceName,
                  OspfNeighborConfig.builder()
                      .setArea(area.getAreaNumber())
                      .setHostname(config.getHostname())
                      .setInterfaceName(ifaceName)
                      .setIp(iface.getConcreteAddress().getIp())
                      .setVrfName(vrf.getName())
                      .setPassive(iface.getOspfPassive())
                      .build());
            }
          }
          proc.setOspfNeighborConfigs(neighborMap.build());
        }
      }
    }
  }

  private static MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> collectNodes(
      NetworkConfigurations configurations) {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();

    // Iterate over all configurations
    for (Configuration config : configurations.all()) {
      // All VRFs in the configuration
      for (Entry<String, Vrf> vrfEntry : config.getVrfs().entrySet()) {
        Vrf vrf = vrfEntry.getValue();
        for (OspfProcess proc : vrf.getOspfProcesses().values()) {
          for (OspfNeighborConfig neighbor : proc.getOspfNeighborConfigs().values()) {
            if (neighbor.isPassive()) {
              continue;
            }

            // Check if the interface is up
            Optional<Interface> iface =
                configurations.getInterface(config.getHostname(), neighbor.getInterfaceName());
            if (!iface.isPresent()) {
              continue;
            }
            if (!iface.get().getActive()) {
              continue;
            }

            graph.addNode(
                new OspfNeighborConfigId(
                    config.getHostname(),
                    vrf.getName(),
                    proc.getProcessId(),
                    iface.get().getName()));
          }
        }
      }
    }
    return graph;
  }

  /** For each compatible neighbor relationship, add a link to the graph */
  private static void establishLinks(
      NetworkConfigurations networkConfigurations,
      MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph,
      Topology l3topology) {

    for (OspfNeighborConfigId configId : ImmutableSet.copyOf(graph.nodes())) {
      for (NodeInterfacePair remoteNodeInterface :
          l3topology.getNeighbors(configId.getNodeInterfacePair())) {
        Interface remoteInterface =
            networkConfigurations
                .getInterface(remoteNodeInterface.getHostname(), remoteNodeInterface.getInterface())
                .orElse(null);

        if (remoteInterface == null
            || !remoteInterface.getActive()
            || remoteInterface.getOspfProcess() == null) {
          continue;
        }

        OspfNeighborConfigId remoteConfigId =
            new OspfNeighborConfigId(
                remoteNodeInterface.getHostname(),
                remoteInterface.getVrfName(),
                remoteInterface.getOspfProcess(),
                remoteNodeInterface.getInterface());
        getSessionIfCompatible(configId, remoteConfigId, networkConfigurations)
            .ifPresent(s -> graph.putEdgeValue(configId, remoteConfigId, s));
      }
    }
  }

  /**
   * Perform neighbor compatibility checks and return an OSPF session.
   *
   * <p>Invariant: Ip address of {@code localConfigId} is the {@link IpLink#getIp1()}
   */
  @VisibleForTesting
  static Optional<OspfSessionProperties> getSessionIfCompatible(
      OspfNeighborConfigId localConfigId,
      OspfNeighborConfigId remoteConfigId,
      NetworkConfigurations configurations) {
    OspfNeighborConfig localConfig =
        configurations.getOspfNeighborConfig(localConfigId).orElse(null);
    OspfNeighborConfig remoteConfig =
        configurations.getOspfNeighborConfig(remoteConfigId).orElse(null);
    OspfProcess localProcess = configurations.getOspfProcess(localConfigId).orElse(null);
    OspfProcess remoteProcess = configurations.getOspfProcess(remoteConfigId).orElse(null);

    if (localConfig == null
        || remoteConfig == null
        || localProcess == null
        || remoteProcess == null) {
      return Optional.empty();
    }

    long localAreaNum = localConfig.getArea();
    long remoteAreaNum = remoteConfig.getArea();
    OspfArea localArea = localProcess.getAreas().get(localAreaNum);
    OspfArea remoteArea = remoteProcess.getAreas().get(remoteAreaNum);
    if (localArea == null || remoteArea == null) {
      return Optional.empty();
    }

    if (localConfig.isPassive() || remoteConfig.isPassive()) {
      return Optional.empty();
    }
    if (localAreaNum != remoteAreaNum) {
      return Optional.empty();
    }
    if (localProcess.getRouterId().equals(remoteProcess.getRouterId())) {
      return Optional.empty();
    }
    if (localArea.getStubType() != remoteArea.getStubType()) {
      return Optional.empty();
    }
    /*
     * TODO: check MTU matches; This is complicated because frame/packet MTU support not fully there
     * TODO: check reachability (Make sure ACLs/ARP allow communication)
     * TODO: take into account adjacency types (multi-access/p2p/p2mp, broadcast/non-broadcast) when
     * supported
     */

    // invariant localIP == ip1
    return Optional.of(
        new OspfSessionProperties(
            localConfig.getArea(), new IpLink(localConfig.getIp(), remoteConfig.getIp())));
  }

  /** Ensure links in the graph are bi-directional */
  @VisibleForTesting
  static void trimLinks(MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph) {
    for (EndpointPair<OspfNeighborConfigId> edge : ImmutableSet.copyOf(graph.edges())) {
      // Reverse edge must exist, otherwise remove existing edge
      if (!graph.hasEdgeConnecting(edge.target(), edge.source())) {
        graph.removeEdge(edge.source(), edge.target());
      }
    }
  }

  private OspfTopologyUtils() {}
}
