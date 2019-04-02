package org.batfish.datamodel.ospf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility functions for computing OSPF topology */
public final class OspfTopologyUtils {

  public static void initRemoteOspfNeighbors(
      Map<String, Configuration> configurations, Topology topology) {
    for (Entry<String, Configuration> e : configurations.entrySet()) {
      String hostname = e.getKey();
      Configuration c = e.getValue();
      for (Entry<String, Vrf> e2 : c.getVrfs().entrySet()) {
        Vrf vrf = e2.getValue();
        OspfProcess proc = vrf.getOspfProcess();
        if (proc != null) {
          proc.setOspfNeighbors(new TreeMap<>());
          String vrfName = e2.getKey();
          for (Entry<Long, OspfArea> e3 : proc.getAreas().entrySet()) {
            long areaNum = e3.getKey();
            OspfArea area = e3.getValue();
            for (String ifaceName : area.getInterfaces()) {
              Interface iface = c.getAllInterfaces().get(ifaceName);
              if (iface.getOspfPassive()) {
                continue;
              }
              Set<NodeInterfacePair> ifaceNeighbors =
                  topology.getNeighbors(new NodeInterfacePair(hostname, ifaceName));
              boolean hasOspfNeighbor = false;
              Ip localIp = iface.getAddress().getIp();
              if (ifaceNeighbors != null) {
                for (NodeInterfacePair neighbor : ifaceNeighbors) {
                  String remoteHostname = neighbor.getHostname();
                  String remoteIfaceName = neighbor.getInterface();
                  Configuration remoteNode = configurations.get(remoteHostname);
                  Interface remoteIface = remoteNode.getAllInterfaces().get(remoteIfaceName);
                  if (remoteIface.getOspfPassive()) {
                    continue;
                  }
                  Vrf remoteVrf = remoteIface.getVrf();
                  String remoteVrfName = remoteVrf.getName();
                  OspfProcess remoteProc = remoteVrf.getOspfProcess();
                  if (remoteProc != null) {
                    if (remoteProc.getOspfNeighbors() == null) {
                      remoteProc.setOspfNeighbors(new TreeMap<>());
                    }
                    OspfArea remoteArea = remoteProc.getAreas().get(areaNum);
                    if (remoteArea != null
                        && remoteArea.getInterfaces().contains(remoteIfaceName)) {
                      Ip remoteIp = remoteIface.getAddress().getIp();
                      IpLink localKey = new IpLink(localIp, remoteIp);
                      OspfNeighbor ospfNeighbor = proc.getOspfNeighbors().get(localKey);
                      if (ospfNeighbor == null) {
                        hasOspfNeighbor = true;

                        // initialize local ospfNeighbor
                        ospfNeighbor = new OspfNeighbor(localKey);
                        ospfNeighbor.setArea(areaNum);
                        ospfNeighbor.setVrf(vrfName);
                        ospfNeighbor.setOwner(c);
                        ospfNeighbor.setInterface(iface);
                        proc.getOspfNeighbors().put(localKey, ospfNeighbor);

                        // initialize remote ospfNeighbor
                        IpLink remoteKey = new IpLink(remoteIp, localIp);
                        OspfNeighbor remoteNeighbor = new OspfNeighbor(remoteKey);
                        remoteNeighbor.setArea(areaNum);
                        remoteNeighbor.setVrf(remoteVrfName);
                        remoteNeighbor.setOwner(remoteNode);
                        remoteNeighbor.setInterface(remoteIface);
                        remoteProc.getOspfNeighbors().put(remoteKey, remoteNeighbor);

                        // link neighbors
                        ospfNeighbor.setRemoteOspfNeighbor(remoteNeighbor);
                        remoteNeighbor.setRemoteOspfNeighbor(ospfNeighbor);
                      }
                    }
                  }
                }
              }
              if (!hasOspfNeighbor) {
                IpLink key = new IpLink(localIp, Ip.ZERO);
                OspfNeighbor neighbor = new OspfNeighbor(key);
                neighbor.setArea(areaNum);
                neighbor.setVrf(vrfName);
                neighbor.setOwner(c);
                neighbor.setInterface(iface);
                proc.getOspfNeighbors().put(key, neighbor);
              }
            }
          }
        }
      }
    }
  }

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
   * neighbors; Initialize {@link OspfProcess} map containing all local neighbor configs, obtainable
   * with {@link OspfProcess#getOspfNeighborConfigs()}.
   */
  public static void initNeighborConfigs(NetworkConfigurations configurations) {
    // Iterate over all configurations
    for (Configuration config : configurations.all()) {
      // All VRFs in the configuration
      for (Entry<String, Vrf> vrfEntry : config.getVrfs().entrySet()) {
        Vrf vrf = vrfEntry.getValue();
        OspfProcess proc = vrf.getOspfProcess();

        if (proc == null) {
          // No OSPF process, nothing to do.
          continue;
        }

        Builder<String, OspfNeighborConfig> neighborMap = ImmutableMap.builder();

        // Iterate over all OSPF areas
        for (Entry<Long, OspfArea> ospfAreaEntry : proc.getAreas().entrySet()) {
          OspfArea area = ospfAreaEntry.getValue();

          // All interfaces in this area
          for (String ifaceName : area.getInterfaces()) {
            Interface iface = config.getAllInterfaces().get(ifaceName);

            neighborMap.put(
                ifaceName,
                OspfNeighborConfig.builder()
                    .setArea(area.getAreaNumber())
                    .setHostname(config.getHostname())
                    .setInterfaceName(ifaceName)
                    .setVrfName(vrf.getName())
                    .setPassive(iface.getOspfPassive())
                    .build());
          }
        }
        proc.setOspfNeighborConfigs(neighborMap.build());
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
        OspfProcess proc = vrf.getOspfProcess();

        if (proc == null) {
          // No OSPF process, nothing to do.
          continue;
        }

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
                  config.getHostname(), vrf.getName(), proc.getProcessId(), iface.get().getName()));
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

    if (localConfig == null || remoteConfig == null) {
      return Optional.empty();
    }
    if (localConfig.isPassive() || remoteConfig.isPassive()) {
      return Optional.empty();
    }
    if (localConfig.getArea() != remoteConfig.getArea()) {
      return Optional.empty();
    }
    /*
     * TODO: check MTU matches; This is complicated because frame/packet MTU support not fully there
     * TODO: check reachability (Make sure ACLs/ARP allow communication)
     * TODO: take into account adjacency types (multi-access/p2p/p2mp, broadcast/non-broadcast) when
     * supported
     */
    Ip localIp =
        configurations
            .getInterface(localConfig.getHostname(), localConfig.getInterfaceName())
            .map(Interface::getAddress)
            .map(InterfaceAddress::getIp)
            .orElse(null);
    Ip remoteIp =
        configurations
            .getInterface(remoteConfig.getHostname(), remoteConfig.getInterfaceName())
            .map(Interface::getAddress)
            .map(InterfaceAddress::getIp)
            .orElse(null);
    if (localIp == null || remoteIp == null) {
      return Optional.empty();
    }
    // invariant localIP == ip1
    return Optional.of(
        new OspfSessionProperties(localConfig.getArea(), new IpLink(localIp, remoteIp)));
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
