package org.batfish.datamodel.eigrp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility functions for computing EIGRP topology and neighbors */
public class EigrpTopologyUtils {

  /** Initialize the EIGRP topology as a directed graph. */
  public static @Nonnull EigrpTopology initEigrpTopology(
      Map<String, Configuration> configurations, Topology topology) {
    NetworkConfigurations nc = NetworkConfigurations.of(configurations);
    MutableNetwork<EigrpNeighborConfigId, EigrpEdge> eigrpNetwork = collectNodes(nc);
    establishLinks(nc, eigrpNetwork, topology);
    trimLinks(eigrpNetwork);

    return new EigrpTopology(ImmutableNetwork.copyOf(eigrpNetwork));
  }

  /**
   * Add {@link EigrpNeighborConfigId}s corresponding to all {@link EigrpNeighborConfig}s across all
   * {@link Configuration}s
   */
  private static MutableNetwork<EigrpNeighborConfigId, EigrpEdge> collectNodes(
      NetworkConfigurations configurations) {
    MutableNetwork<EigrpNeighborConfigId, EigrpEdge> network =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();

    for (Configuration config : configurations.all()) {
      for (Vrf vrf : config.getVrfs().values()) {
        for (EigrpProcess proc : vrf.getEigrpProcesses().values()) {
          for (EigrpNeighborConfig neighbor : proc.getNeighbors().values()) {
            if (neighbor.isPassive()) {
              continue;
            }
            // Check if the interface is present and active
            Optional<Interface> iface =
                configurations.getInterface(config.getHostname(), neighbor.getInterfaceName());
            if (!iface.isPresent() || !iface.get().getActive()) {
              continue;
            }

            network.addNode(
                new EigrpNeighborConfigId(
                    neighbor.getAsn(),
                    neighbor.getHostname(),
                    neighbor.getInterfaceName(),
                    neighbor.getVrfName()));
          }
        }
      }
    }
    return network;
  }

  /** For each compatible neighbor relationship, add a link to the network */
  private static void establishLinks(
      NetworkConfigurations networkConfigurations,
      MutableNetwork<EigrpNeighborConfigId, EigrpEdge> eigrpNetwork,
      Topology l3topology) {

    for (EigrpNeighborConfigId configId : ImmutableSet.copyOf(eigrpNetwork.nodes())) {
      for (NodeInterfacePair remoteNodeInterface :
          l3topology.getNeighbors(configId.getNodeInterfacePair())) {
        Interface remoteInterface =
            networkConfigurations
                .getInterface(remoteNodeInterface.getHostname(), remoteNodeInterface.getInterface())
                .orElse(null);

        if (remoteInterface == null
            || !remoteInterface.getActive()
            || remoteInterface.getEigrp() == null
            || !remoteInterface.getEigrp().getEnabled()) {
          continue;
        }

        EigrpNeighborConfigId remoteConfigId =
            new EigrpNeighborConfigId(
                remoteInterface.getEigrp().getAsn(),
                remoteNodeInterface.getHostname(),
                remoteNodeInterface.getInterface(),
                remoteInterface.getVrfName());

        if (eigrpNetwork.nodes().contains(remoteConfigId)) {
          Interface iface =
              networkConfigurations
                  .getInterface(configId.getHostname(), configId.getInterfaceName())
                  .orElse(null);
          Interface remoteIface =
              networkConfigurations
                  .getInterface(remoteConfigId.getHostname(), remoteConfigId.getInterfaceName())
                  .orElse(null);
          assert iface != null
              && remoteIface != null; // since both the neighbor configs are present in the graph
          if (iface.getEigrp() != null
              && remoteIface.getEigrp() != null
              && iface.getEigrp().getAsn() == remoteIface.getEigrp().getAsn()
              && iface.getEigrp().getMetric().isCompatible(remoteIface.getEigrp().getMetric())) {
            eigrpNetwork.addEdge(configId, remoteConfigId, new EigrpEdge(configId, remoteConfigId));
          }
        }
      }
    }
  }

  /**
   * Initialize {@link EigrpNeighborConfig}s for all {@link EigrpProcess}s present under all {@link
   * Vrf}s of all {@link Configuration}s, and store under the correct {@link EigrpProcess}.
   */
  public static void initNeighborConfigs(NetworkConfigurations configurations) {
    for (Configuration config : configurations.all()) {
      for (Vrf vrf : config.getVrfs().values()) {
        for (EigrpProcess proc : vrf.getEigrpProcesses().values()) {
          ImmutableList.Builder<EigrpNeighborConfig> neighborsBuilder = ImmutableList.builder();
          for (Interface iface : config.getAllInterfaces(vrf.getName()).values()) {
            // if the interface does not belong to the current EIGRP process, skip it
            if (iface.getConcreteAddress() == null
                || iface.getEigrp() == null
                || !iface.getEigrp().getEnabled()
                || iface.getEigrp().getAsn() != proc.getAsn()
                // this shouldn't happen, but if it does, ignore the interface
                || iface.getEigrp().getMetric().getValues().getBandwidth() == null) {
              continue;
            }
            // TODO: check if secondary addresses also participate in EIGRP neighbor relationships
            String exportPolicyName = iface.getEigrp().getExportPolicy();
            assert exportPolicyName != null; // VI conversion should ensure this
            neighborsBuilder.add(
                EigrpNeighborConfig.builder()
                    .setExportPolicy(exportPolicyName)
                    .setHostname(config.getHostname())
                    .setInterfaceName(iface.getName())
                    .setIp(iface.getConcreteAddress().getIp())
                    .setVrfName(vrf.getName())
                    .setAsn(iface.getEigrp().getAsn())
                    .setPassive(iface.getEigrp().getPassive())
                    .build());
          }
          proc.addNeighbors(neighborsBuilder.build());
        }
      }
    }
  }

  /** Ensure links in the network are bi-directional */
  @VisibleForTesting
  static void trimLinks(MutableNetwork<EigrpNeighborConfigId, EigrpEdge> network) {
    for (EigrpEdge edge : ImmutableSet.copyOf(network.edges())) {
      // Reverse edge must exist, otherwise remove existing edge
      if (!network.hasEdgeConnecting(edge.getNode2(), edge.getNode1())) {
        network.removeEdge(edge);
      }
    }
  }
}
