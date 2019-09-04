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
        convertToEstablishedGraph(
            computeCandidateOspfTopologyGraph(configurations, l3Topology), configurations);
    trimLinks(graph);

    return new OspfTopology(ImmutableValueGraph.copyOf(graph));
  }

  /** Compute candidate OSPF topology, including incompatible/unestablished links. */
  public static CandidateOspfTopology computeCandidateOspfTopology(
      NetworkConfigurations configurations, Topology l3Topology) {
    return new CandidateOspfTopology(computeCandidateOspfTopologyGraph(configurations, l3Topology));
  }

  /**
   * Helper to convert the specified graph of candidate OSPF sessions and their statuses into a
   * graph of established sessions and their session properties
   */
  private static MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties>
      convertToEstablishedGraph(
          MutableValueGraph<OspfNeighborConfigId, OspfSessionStatus> candidateGraph,
          NetworkConfigurations configurations) {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    for (EndpointPair<OspfNeighborConfigId> i : candidateGraph.edges()) {
      OspfNeighborConfigId localConfigId = i.nodeU();
      OspfNeighborConfigId remoteConfigId = i.nodeV();
      OspfNeighborConfig localConfig =
          configurations.getOspfNeighborConfig(localConfigId).orElse(null);
      OspfNeighborConfig remoteConfig =
          configurations.getOspfNeighborConfig(remoteConfigId).orElse(null);

      if (localConfig == null
          || remoteConfig == null
          || getSessionStatus(localConfigId, remoteConfigId, configurations)
              != OspfSessionStatus.ESTABLISHED) {
        continue;
      }
      graph.putEdgeValue(
          localConfigId,
          remoteConfigId,
          new OspfSessionProperties(
              localConfig.getArea(), new IpLink(localConfig.getIp(), remoteConfig.getIp())));
    }
    return graph;
  }

  /** Helper to compute candidate OSPF topology graph including incompatible/unestablished links */
  private static MutableValueGraph<OspfNeighborConfigId, OspfSessionStatus>
      computeCandidateOspfTopologyGraph(NetworkConfigurations configurations, Topology l3Topology) {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph = collectNodes(configurations);
    establishCandidateLinks(configurations, graph, l3Topology);
    return graph;
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
              // Skip any interface without OSPF settings
              if (iface.getOspfSettings() == null) {
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

  private static <T> MutableValueGraph<OspfNeighborConfigId, T> collectNodes(
      NetworkConfigurations configurations) {
    MutableValueGraph<OspfNeighborConfigId, T> graph =
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

  /** For each candidate neighbor relationship, add a link to the graph */
  private static void establishCandidateLinks(
      NetworkConfigurations networkConfigurations,
      MutableValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph,
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
        OspfSessionStatus status =
            getSessionStatus(configId, remoteConfigId, networkConfigurations);
        if (status != OspfSessionStatus.NO_SESSION) {
          graph.putEdgeValue(configId, remoteConfigId, status);
        }
      }
    }
  }

  /** Perform neighbor compatibility checks and return an OSPF session status. */
  @VisibleForTesting
  static OspfSessionStatus getSessionStatus(
      OspfNeighborConfigId localConfigId,
      OspfNeighborConfigId remoteConfigId,
      NetworkConfigurations configurations) {
    OspfNeighborConfig localConfig =
        configurations.getOspfNeighborConfig(localConfigId).orElse(null);
    OspfNeighborConfig remoteConfig =
        configurations.getOspfNeighborConfig(remoteConfigId).orElse(null);
    OspfProcess localProcess = configurations.getOspfProcess(localConfigId).orElse(null);
    OspfProcess remoteProcess = configurations.getOspfProcess(remoteConfigId).orElse(null);
    Interface localIface =
        configurations
            .getInterface(localConfigId.getHostname(), localConfigId.getInterfaceName())
            .orElse(null);
    Interface remoteIface =
        configurations
            .getInterface(remoteConfigId.getHostname(), remoteConfigId.getInterfaceName())
            .orElse(null);

    if (localProcess == null || remoteProcess == null) {
      return OspfSessionStatus.PROCESS_INVALID;
    }

    if (localConfig == null || remoteConfig == null || localIface == null || remoteIface == null) {
      // This probably shouldn't ever happen...but handle it just in case
      return OspfSessionStatus.UNKNOWN_COMPATIBILITY_ISSUE;
    }

    long localAreaNum = localConfig.getArea();
    long remoteAreaNum = remoteConfig.getArea();
    OspfArea localArea = localProcess.getAreas().get(localAreaNum);
    OspfArea remoteArea = remoteProcess.getAreas().get(remoteAreaNum);
    if (localArea == null || remoteArea == null) {
      return OspfSessionStatus.AREA_INVALID;
    }

    if (localConfig.isPassive() && remoteConfig.isPassive()) {
      return OspfSessionStatus.NO_SESSION;
    }

    if (localConfig.isPassive() != remoteConfig.isPassive()) {
      return OspfSessionStatus.PASSIVE_MISMATCH;
    }
    if (localAreaNum != remoteAreaNum) {
      return OspfSessionStatus.AREA_MISMATCH;
    }
    if (localProcess.getRouterId().equals(remoteProcess.getRouterId())) {
      return OspfSessionStatus.DUPLICATE_ROUTER_ID;
    }
    if (localArea.getStubType() != remoteArea.getStubType()) {
      return OspfSessionStatus.AREA_TYPE_MISMATCH;
    }

    // Optimistically assume unspecified network types match and therefore are compatible
    OspfNetworkType localNetworkType = localIface.getOspfNetworkType();
    OspfNetworkType remoteNetworkType = remoteIface.getOspfNetworkType();
    if ((localNetworkType != null && remoteNetworkType != null)
        && (localNetworkType != remoteNetworkType)) {
      return OspfSessionStatus.NETWORK_TYPE_MISMATCH;
    }

    OspfInterfaceSettings localOspf = localIface.getOspfSettings();
    OspfInterfaceSettings remoteOspf = remoteIface.getOspfSettings();
    // Guaranteed by initNeighborConfigs
    assert (localOspf != null);
    assert (remoteOspf != null);
    if (localOspf.getHelloInterval() != remoteOspf.getHelloInterval()) {
      return OspfSessionStatus.HELLO_INTERVAL_MISMATCH;
    }
    if (localOspf.getDeadInterval() != remoteOspf.getDeadInterval()) {
      return OspfSessionStatus.DEAD_INTERVAL_MISMATCH;
    }

    /*
     * TODO: check MTU matches; This is complicated because frame/packet MTU support not fully there
     * TODO: check reachability (Make sure ACLs/ARP allow communication)
     * TODO: take into account adjacency types (multi-access/p2p/p2mp, broadcast/non-broadcast) when
     * supported
     */

    return OspfSessionStatus.ESTABLISHED;
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
