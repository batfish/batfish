package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.InterfaceType.TUNNEL;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/** Utility class for computing {@link VxlanTopology} instances from network data. */
public final class VxlanTopologyUtils {

  /**
   * Add an edge to the {@code graph} between one pair of {@link Layer2Vni}, if their configurations
   * are compatible
   */
  @VisibleForTesting
  static void addVniEdge(
      MutableGraph<VxlanNode> graph,
      VrfId vrfTail,
      Layer2Vni vniSettingsTail,
      VrfId vrfHead,
      Layer2Vni vniSettingsHead) {
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
      Table<VrfId, Integer, Layer2Vni> allVniSettings,
      Integer vni,
      Map<VrfId, Layer2Vni> vrfs) {
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

  /** Build a {@link VxlanNode} for the given {@link Layer2Vni} and {@link VrfId}. */
  @VisibleForTesting
  static VxlanNode buildVxlanNode(VrfId vrf, Layer2Vni vniSettings) {
    return VxlanNode.builder()
        .setHostname(vrf._hostname)
        .setVni(vniSettings.getVni())
        .setVniLayer(LAYER_2)
        .build();
  }

  /** Check if two {@link Layer2Vni} have compatible configurations */
  @VisibleForTesting
  static boolean compatibleVniSettings(Layer2Vni vniSettingsTail, Layer2Vni vniSettingsHead) {
    return vniSettingsTail.getBumTransportMethod() == vniSettingsHead.getBumTransportMethod()
        && vniSettingsTail.getUdpPort().equals(vniSettingsHead.getUdpPort())
        && vniSettingsTail.getSourceAddress() != null
        && vniSettingsHead.getSourceAddress() != null
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
  static @Nonnull String getVniSrcVrf(Layer2Vni vniSettings) {
    return vniSettings.getSrcVrf();
  }

  /**
   * Compute the VXLAN topology based on the {@link Layer2Vni} extracted from the given
   * configurations
   */
  public static @Nonnull VxlanTopology computeVxlanTopology(
      Map<String, Configuration> configurations) {
    return internalComputeVxlanTopology(computeVniSettingsTable(configurations));
  }

  /**
   * Compute the VXLAN topology based on the {@link Layer2Vni} extracted from the given table.
   *
   * @param allVniSettings table of the VNI settings. Row is hostname, Column is VRF name.
   */
  public static @Nonnull VxlanTopology computeVxlanTopology(
      Table<String, String, ? extends Collection<Layer2Vni>> allVniSettings) {
    return internalComputeVxlanTopology(computeVniSettingsTable(allVniSettings));
  }

  /** Convert configurations into a table format that's easier to work with */
  @VisibleForTesting
  @Nonnull
  static Table<VrfId, Integer, Layer2Vni> computeVniSettingsTable(
      Map<String, Configuration> configurations) {
    Table<VrfId, Integer, Layer2Vni> table = HashBasedTable.create();
    for (Configuration c : configurations.values()) {
      for (Vrf vrf : c.getVrfs().values()) {
        for (Layer2Vni vniSettings : vrf.getLayer2Vnis().values()) {
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
  private static Table<VrfId, Integer, Layer2Vni> computeVniSettingsTable(
      Table<String, String, ? extends Collection<Layer2Vni>> allVniSettings) {
    Table<VrfId, Integer, Layer2Vni> table = HashBasedTable.create();
    for (Cell<String, String, ? extends Collection<Layer2Vni>> cell : allVniSettings.cellSet()) {
      assert cell.getValue() != null;
      assert cell.getRowKey() != null;
      assert cell.getColumnKey() != null;
      for (Layer2Vni vni : cell.getValue()) {
        table.put(new VrfId(cell.getRowKey(), cell.getColumnKey()), vni.getVni(), vni);
      }
    }
    return table;
  }

  /** Compute the VXLAN topology. Adds edges per VNI. */
  private static @Nonnull VxlanTopology internalComputeVxlanTopology(
      Table<VrfId, Integer, Layer2Vni> allVniSettings) {
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
    Span span = GlobalTracer.get().buildSpan("VxlanTopologyUtils.prunedVxlanTopology").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null;
      NetworkConfigurations nc = NetworkConfigurations.of(configurations);
      MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
      initialVxlanTopology.getGraph().edges().parallelStream()
          .filter(edge -> reachableEdge(edge, nc, tracerouteEngine))
          .collect(ImmutableList.toImmutableList())
          .forEach(edge -> graph.putEdge(edge.nodeU(), edge.nodeV()));
      return new VxlanTopology(graph);
    } finally {
      span.finish();
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
    Layer2Vni vniSettingsU = nc.getVniSettings(hostU, vni).get();
    // early exit if unsupported
    if (vniSettingsU.getBumTransportMethod() != BumTransportMethod.UNICAST_FLOOD_GROUP) {
      // TODO: support multicast transport
      return false;
    }
    Layer2Vni vniSettingsV = nc.getVniSettings(hostV, vni).get();
    String vrfU = getVniSrcVrf(vniSettingsU);
    String vrfV = getVniSrcVrf(vniSettingsV);
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
            .setIngressNode(sender)
            .setIngressVrf(senderVrf)
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(udpPort)
            .build();
    return tracerouteEngine
        .computeTraceDags(ImmutableSet.of(flow), ImmutableSet.of(), false)
        .get(flow)
        .getTraces()
        .map(TraceAndReverseFlow::getTrace)
        .anyMatch(
            trace -> {
              List<Hop> hops = trace.getHops();
              return !hops.isEmpty()
                  && hops.get(hops.size() - 1).getNode().getName().equals(receiver)
                  && trace.getDisposition() == FlowDisposition.ACCEPTED;
            });
  }

  // TODO: does this value matter?
  private static final Ip TENANT_VNI_INTERFACE_LINK_LOCAL_ADDRESS = Ip.parse("169.254.0.1");

  /** Add an interface for each l3vni to its tenant VRF to represent endpoints of a VXLAN tunnel. */
  @VisibleForTesting
  public static void addTenantVniInterfaces(Configuration c) {
    for (Vrf vrf : c.getVrfs().values()) {
      vrf.getLayer3Vnis()
          .forEach(
              (vni, l3vni) -> {
                if (l3vni.getSourceAddress() == null) {
                  // defective layer-3 VNI, so don't generate an interface
                  return;
                }
                // TODO: support sessions
                Interface.builder()
                    .setName(generatedTenantVniInterfaceName(vni))
                    .setOwner(c)
                    .setVrf(vrf)
                    .setAdditionalArpIps(l3vni.getSourceAddress().toIpSpace())
                    .setAddresses(LinkLocalAddress.of(TENANT_VNI_INTERFACE_LINK_LOCAL_ADDRESS))
                    .setActive(true)
                    .setProxyArp(false)
                    .setType(TUNNEL)
                    .setSwitchportMode(SwitchportMode.NONE)
                    .build();
              });
    }
  }

  public static @Nonnull Set<Edge> vxlanTopologyToLayer3Edges(
      VxlanTopology vxlanTopology, Map<String, Configuration> configurations) {
    ImmutableSet.Builder<Edge> edgesBuilder = ImmutableSet.builder();
    vxlanTopology
        .getLayer3VniEdges()
        .forEach(
            endpointPair -> {
              VxlanNode node1 = endpointPair.nodeU();
              VxlanNode node2 = endpointPair.nodeV();
              String node1Name = node1.getHostname();
              String node2Name = node2.getHostname();
              int vni1 = node1.getVni();
              int vni2 = node2.getVni();
              String iface1Name = generatedTenantVniInterfaceName(vni1);
              String iface2Name = generatedTenantVniInterfaceName(vni2);
              Configuration c1 = configurations.get(node1Name);
              Configuration c2 = configurations.get(node2Name);
              assert c1 != null;
              assert c2 != null;
              if (!(c1.getAllInterfaces().containsKey(iface1Name)
                  && c2.getAllInterfaces().containsKey(iface2Name))) {
                // VNI was defective, so no interface was generated.
                return;
              }
              edgesBuilder.add(Edge.of(node1Name, iface1Name, node2Name, iface2Name));
              edgesBuilder.add(Edge.of(node2Name, iface2Name, node1Name, iface1Name));
            });
    return edgesBuilder.build();
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
