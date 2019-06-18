package org.batfish.datamodel.vxlan;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;

/** Utility class for computing {@link VxlanTopology} instances from network data. */
public final class VxlanTopologyUtils {

  /**
   * Add an edge to the {@code graph} between one pair of {@link VniSettings}, if their
   * configurations are compatible
   */
  @VisibleForTesting
  static void addVniEdge(
      MutableGraph<VxlanNode> graph,
      VrfId vrfTail,
      VniSettings vniSettingsTail,
      VrfId vrfHead,
      VniSettings vniSettingsHead) {
    if (compatibleVniSettings(vniSettingsTail, vniSettingsHead)) {
      VxlanNode nodeTail = buildVxlanNode(vrfTail, vniSettingsTail);
      VxlanNode nodeHead = buildVxlanNode(vrfHead, vniSettingsHead);
      graph.putEdge(nodeTail, nodeHead);
    }
  }

  /**
   * Add edges to the {@code graph} between all VRFs that have {@code vni} configured on them
   * (excludes self-edges).
   */
  @VisibleForTesting
  static void addVniEdges(
      MutableGraph<VxlanNode> graph,
      Table<VrfId, Integer, VniSettings> allVniSettings,
      Integer vni,
      Map<VrfId, VniSettings> vrfs) {
    for (VrfId vrfTail : vrfs.keySet()) {
      for (VrfId vrfHead : vrfs.keySet()) {
        // Generate all VRF combinations without repetition
        if (vrfTail.equals(vrfHead)) {
          continue;
        }
        addVniEdge(
            graph,
            vrfTail,
            allVniSettings.get(vrfTail, vni),
            vrfHead,
            allVniSettings.get(vrfHead, vni));
      }
    }
  }

  /** Build a {@link VxlanNode} for the given {@link VniSettings} and {@link VrfId}. */
  @VisibleForTesting
  static VxlanNode buildVxlanNode(VrfId vrf, VniSettings vniSettings) {
    return VxlanNode.builder().setHostname(vrf._hostname).setVni(vniSettings.getVni()).build();
  }

  /** Check if two {@link VniSettings} have compatible configurations */
  @VisibleForTesting
  static boolean compatibleVniSettings(VniSettings vniSettingsTail, VniSettings vniSettingsHead) {
    return vniSettingsTail.getBumTransportMethod() == vniSettingsHead.getBumTransportMethod()
        && vniSettingsTail.getUdpPort().equals(vniSettingsHead.getUdpPort())
        && vniSettingsTail.getSourceAddress() != null
        && vniSettingsHead.getSourceAddress() != null
        && vniSettingsTail.getVlan() != null
        && vniSettingsHead.getVlan() != null
        && !vniSettingsTail.getSourceAddress().equals(vniSettingsHead.getSourceAddress())
        && ((vniSettingsTail.getBumTransportMethod() == BumTransportMethod.MULTICAST_GROUP
                && vniSettingsTail
                    .getBumTransportIps()
                    .equals(vniSettingsHead.getBumTransportIps()))
            || (vniSettingsTail.getBumTransportMethod() == BumTransportMethod.UNICAST_FLOOD_GROUP
                && vniSettingsTail.getBumTransportIps().contains(vniSettingsHead.getSourceAddress())
                && vniSettingsHead
                    .getBumTransportIps()
                    .contains(vniSettingsTail.getSourceAddress())));
  }

  @VisibleForTesting
  static @Nonnull String getVniVrf(NetworkConfigurations nc, String host, VniSettings vniSettings) {
    return nc.get(host).get().getVrfs().entrySet().stream()
        .filter(vrfEntry -> vrfEntry.getValue().getVniSettings().containsKey(vniSettings.getVni()))
        .map(Entry::getKey)
        .findAny()
        .get();
  }

  /**
   * Compute the VXLAN topology based on the {@link VniSettings} extracted from the given
   * configurations
   */
  public static @Nonnull VxlanTopology computeVxlanTopology(
      Map<String, Configuration> configurations) {
    return internalComputeVxlanTopology(computeVniSettingsTable(configurations));
  }

  /**
   * Compute the VXLAN topology based on the {@link VniSettings} extracted from the given table.
   *
   * @param allVniSettings table of the VNI settings. Row is hostname, Column is VRF name.
   */
  public static @Nonnull VxlanTopology computeVxlanTopology(
      Table<String, String, ? extends Collection<VniSettings>> allVniSettings) {
    return internalComputeVxlanTopology(computeVniSettingsTable(allVniSettings));
  }

  /** Convert configurations into a table format that's easier to work with */
  @VisibleForTesting
  @Nonnull
  static Table<VrfId, Integer, VniSettings> computeVniSettingsTable(
      Map<String, Configuration> configurations) {
    Table<VrfId, Integer, VniSettings> table = HashBasedTable.create();
    for (Configuration c : configurations.values()) {
      for (Vrf vrf : c.getVrfs().values()) {
        for (VniSettings vniSettings : vrf.getVniSettings().values()) {
          table.put(new VrfId(c.getHostname(), vrf.getName()), vniSettings.getVni(), vniSettings);
        }
      }
    }
    return table;
  }

  /**
   * Convert VNI setting table obtained from the dataplane into a table format that's easier to work
   * with
   */
  @Nonnull
  private static Table<VrfId, Integer, VniSettings> computeVniSettingsTable(
      Table<String, String, ? extends Collection<VniSettings>> allVniSettings) {
    Table<VrfId, Integer, VniSettings> table = HashBasedTable.create();
    for (Cell<String, String, ? extends Collection<VniSettings>> cell : allVniSettings.cellSet()) {
      assert cell.getValue() != null;
      assert cell.getRowKey() != null;
      assert cell.getColumnKey() != null;
      for (VniSettings vni : cell.getValue()) {
        table.put(new VrfId(cell.getRowKey(), cell.getColumnKey()), vni.getVni(), vni);
      }
    }
    return table;
  }

  /** Compute the VXLAN topology. Adds edges per VNI. */
  private static @Nonnull VxlanTopology internalComputeVxlanTopology(
      Table<VrfId, Integer, VniSettings> allVniSettings) {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    allVniSettings
        .columnMap() // group by vni
        .forEach((vni, vrfs) -> addVniEdges(graph, allVniSettings, vni, vrfs));
    return new VxlanTopology(graph);
  }

  /**
   * Compute {@link VxlanTopology} that results after pruning edges corresponding to endpoints that
   * cannot reach each other from an {@code initialVxlanTopology}.
   */
  public static @Nonnull VxlanTopology prunedVxlanTopology(
      VxlanTopology initialVxlanTopology,
      Map<String, Configuration> configurations,
      TracerouteEngine tracerouteEngine) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("VxlanTopologyUtils.prunedVxlanTopology").startActive()) {
      assert span != null;
      NetworkConfigurations nc = NetworkConfigurations.of(configurations);
      MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
      initialVxlanTopology.getGraph().edges().stream()
          .filter(edge -> reachableEdge(edge, nc, tracerouteEngine))
          .forEach(edge -> graph.putEdge(edge.nodeU(), edge.nodeV()));
      return new VxlanTopology(graph);
    }
  }

  @VisibleForTesting
  static boolean reachableEdge(
      EndpointPair<VxlanNode> edge, NetworkConfigurations nc, TracerouteEngine tracerouteEngine) {
    VxlanNode nodeU = edge.nodeU();
    VxlanNode nodeV = edge.nodeV();
    String hostU = nodeU.getHostname();
    String hostV = nodeV.getHostname();
    // nodeU and nodeV must be compatible coming in to this function
    int vni = nodeU.getVni();
    VniSettings vniSettingsU = nc.getVniSettings(hostU, vni).get();
    // early exit if unsupported
    if (vniSettingsU.getBumTransportMethod() != BumTransportMethod.UNICAST_FLOOD_GROUP) {
      // TODO: support multicast transport
      return false;
    }
    VniSettings vniSettingsV = nc.getVniSettings(hostV, vni).get();
    String vrfU = getVniVrf(nc, hostU, vniSettingsU);
    String vrfV = getVniVrf(nc, hostV, vniSettingsV);
    Ip srcIpU = vniSettingsU.getSourceAddress();
    Ip srcIpV = vniSettingsV.getSourceAddress();
    int udpPort = vniSettingsU.getUdpPort();
    return vxlanFlowDelivered(hostU, vrfU, srcIpU, hostV, srcIpV, udpPort, tracerouteEngine)
        && vxlanFlowDelivered(hostV, vrfV, srcIpV, hostU, srcIpU, udpPort, tracerouteEngine);
  }

  @VisibleForTesting
  static boolean vxlanFlowDelivered(
      String sender,
      String senderVrf,
      Ip srcIp,
      String receiver,
      Ip dstIp,
      int udpPort,
      TracerouteEngine tracerouteEngine) {
    Flow flow =
        Flow.builder()
            .setIpProtocol(IpProtocol.UDP)
            .setTag("neighbor-resolution")
            .setIngressNode(sender)
            .setIngressVrf(senderVrf)
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(udpPort)
            .build();
    List<Trace> traces = tracerouteEngine.computeTraces(ImmutableSet.of(flow), false).get(flow);
    return traces.stream()
        .anyMatch(
            trace -> {
              List<Hop> hops = trace.getHops();
              return !hops.isEmpty()
                  && hops.get(hops.size() - 1).getNode().getName().equals(receiver)
                  && trace.getDisposition() == FlowDisposition.ACCEPTED;
            });
  }

  /** A unique identifier for a {@link Vrf} in a network */
  static final class VrfId {

    @Nonnull private final String _hostname;
    @Nonnull private final String _vrfName;

    VrfId(String hostname, String vrfName) {
      _hostname = hostname;
      _vrfName = vrfName;
    }

    @Nonnull
    public String getHostname() {
      return _hostname;
    }

    @Nonnull
    public String getVrfName() {
      return _vrfName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof VrfId)) {
        return false;
      }
      VrfId vrfId = (VrfId) o;
      return _hostname.equals(vrfId._hostname) && _vrfName.equals(vrfId._vrfName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_hostname, _vrfName);
    }
  }

  private VxlanTopologyUtils() {}
}
