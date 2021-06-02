package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
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
          ImmutableMap.Builder<OspfNeighborConfigId, OspfNeighborConfig> neighborsById =
              ImmutableMap.builder();

          // Iterate over all OSPF areas
          for (Entry<Long, OspfArea> ospfAreaEntry : proc.getAreas().entrySet()) {
            OspfArea area = ospfAreaEntry.getValue();

            // All interfaces in this area
            for (String ifaceName : area.getInterfaces()) {
              Interface iface = config.getAllInterfaces().get(ifaceName);

              // Skip any interface without OSPF settings
              if (iface.getOspfSettings() == null) {
                continue;
              }
              String hostname = config.getHostname();
              String vrfName = vrf.getName();
              firstNonNull(
                      Optional.ofNullable(iface.getOspfSettings().getOspfAddresses())
                          .map(OspfAddresses::getAddresses)
                          .orElse(null),
                      iface.getAllConcreteAddresses())
                  .forEach(
                      concreteAddress -> {
                        OspfNeighborConfigId id =
                            new OspfNeighborConfigId(
                                hostname,
                                vrfName,
                                iface.getOspfProcess(),
                                ifaceName,
                                concreteAddress);
                        neighborsById.put(
                            id,
                            OspfNeighborConfig.builder()
                                .setArea(area.getAreaNumber())
                                .setHostname(hostname)
                                .setInterfaceName(ifaceName)
                                .setIp(concreteAddress.getIp())
                                .setVrfName(vrfName)
                                .setPassive(iface.getOspfPassive())
                                .build());
                      });
            }
          }
          proc.setOspfNeighborConfigs(neighborsById.build());
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
      for (Vrf vrf : config.getVrfs().values()) {
        for (OspfProcess proc : vrf.getOspfProcesses().values()) {
          proc.getOspfNeighborConfigs()
              .forEach(
                  (neighborId, neighbor) -> {
                    if (neighbor.isPassive()) {
                      return;
                    }
                    // Check if the interface is up
                    Optional<Interface> iface =
                        configurations.getInterface(
                            config.getHostname(), neighbor.getInterfaceName());
                    if (!iface.isPresent()) {
                      return;
                    }
                    if (!iface.get().getActive()) {
                      return;
                    }
                    graph.addNode(neighborId);
                  });
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

        networkConfigurations
            .getOspfNeighborConfigs(
                remoteNodeInterface.getHostname(), remoteNodeInterface.getInterface())
            .get()
            .forEach(
                remoteConfigId -> {
                  if (configId.getAddress().getIp().equals(remoteConfigId.getAddress().getIp())) {
                    return;
                  }
                  OspfSessionStatus status =
                      getSessionStatus(configId, remoteConfigId, networkConfigurations);
                  if (status != OspfSessionStatus.NO_SESSION) {
                    graph.putEdgeValue(configId, remoteConfigId, status);
                  }
                });
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
    @Nullable OspfNetworkType assumedNetworkType = null;
    if (localNetworkType != null && remoteNetworkType != null) {
      if (localNetworkType != remoteNetworkType) {
        return OspfSessionStatus.NETWORK_TYPE_MISMATCH;
      }
      assumedNetworkType = localNetworkType;
    } else if (localNetworkType != null) {
      assumedNetworkType = localNetworkType;
    } else if (remoteNetworkType != null) {
      assumedNetworkType = remoteNetworkType;
    }
    // Skip prefix check for P2P sessions
    if (assumedNetworkType != null
        && assumedNetworkType != OspfNetworkType.POINT_TO_POINT
        && !localConfigId
            .getAddress()
            .getPrefix()
            .equals(remoteConfigId.getAddress().getPrefix())) {
      // If not P2P and prefixes do not match, the session should not come up. This can commonly
      // occur even when things are properly configured, so just silently throw out this edge with
      // NO_SESSION.
      return OspfSessionStatus.NO_SESSION;
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

    if (!checkNBMANeighorValidation(localOspf, remoteConfigId.getAddress().getIp())) {
      return OspfSessionStatus.NO_SESSION;
    }
    /*
     * TODO: check MTU matches; This is complicated because frame/packet MTU support not fully there
     * TODO: check reachability (Make sure ACLs/ARP allow communication)
     * TODO: take into account adjacency types (multi-access/p2p/p2mp, broadcast/non-broadcast) when
     * supported
     */

    return OspfSessionStatus.ESTABLISHED;
  }

  /** For NBMA interface, ensure the neighbor's IP is in the local config */
  private static boolean checkNBMANeighorValidation(OspfInterfaceSettings localOspf, Ip remoteIp) {
    if (localOspf.getNetworkType() != OspfNetworkType.NON_BROADCAST_MULTI_ACCESS) {
      // non-NBMA type is handled elsewhere
      return true;
    }

    return Optional.ofNullable(localOspf.getNbmaNeighbors())
        .map(x -> x.contains(remoteIp))
        .orElse(false);
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
