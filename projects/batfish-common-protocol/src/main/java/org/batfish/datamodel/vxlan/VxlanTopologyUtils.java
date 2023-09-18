package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.InterfaceType.TUNNEL;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_3;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
  static void addLayer2VniEdge(
      MutableGraph<VxlanNode> graph,
      VrfId vrfTail,
      Layer2Vni vniSettingsTail,
      VrfId vrfHead,
      Layer2Vni vniSettingsHead) {
    if (compatibleLayer2VniSettings(vniSettingsTail, vniSettingsHead)) {
      VxlanNode nodeTail = buildLayer2VxlanNode(vrfTail, vniSettingsTail);
      VxlanNode nodeHead = buildLayer2VxlanNode(vrfHead, vniSettingsHead);
      graph.putEdge(nodeTail, nodeHead);
    }
  }

  /**
   * Add edges to the {@code graph} between all VRFs that have the same layer-2 VNI configured on
   * them (excludes self-edges), if they are compatible. Note that all values of {@code vrfs} must
   * have the same VNI.
   */
  @VisibleForTesting
  static void addLayer2VniEdges(MutableGraph<VxlanNode> graph, Map<VrfId, Layer2Vni> vrfs) {
    vrfs.forEach(
        (vrfTail, vrfTailSettings) ->
            vrfs.forEach(
                (vrfHead, vrfHeadSettings) -> {
                  // Generate all VRF combinations without repetition
                  if (vrfTail.equals(vrfHead)) {
                    return;
                  }
                  assert vrfTailSettings.getVni() == vrfHeadSettings.getVni();
                  addLayer2VniEdge(graph, vrfTail, vrfTailSettings, vrfHead, vrfHeadSettings);
                }));
  }

  /**
   * Add an edge to the {@code graph} between one pair of {@link Layer3Vni}, if their configurations
   * are compatible
   */
  @VisibleForTesting
  static void addLayer3VniEdge(
      MutableGraph<VxlanNode> graph,
      VrfId vrfTail,
      Layer3Vni vniSettingsTail,
      VrfId vrfHead,
      Layer3Vni vniSettingsHead) {
    if (compatibleLayer3VniSettings(vniSettingsTail, vniSettingsHead)) {
      VxlanNode nodeTail = buildLayer3VxlanNode(vrfTail, vniSettingsTail);
      VxlanNode nodeHead = buildLayer3VxlanNode(vrfHead, vniSettingsHead);
      graph.putEdge(nodeTail, nodeHead);
    }
  }

  /**
   * Add edges to the {@code graph} between all VRFs that have the same layer-3 VNI configured on
   * them (excludes self-edges), if they are compatible. Note that all values of {@code vrfs} must
   * have the same VNI.
   */
  @VisibleForTesting
  static void addLayer3VniEdges(MutableGraph<VxlanNode> graph, Map<VrfId, Layer3Vni> vrfs) {
    vrfs.forEach(
        (vrfTail, vrfTailSettings) ->
            vrfs.forEach(
                (vrfHead, vrfHeadSettings) -> {
                  // Generate all VRF combinations without repetition
                  if (vrfTail.equals(vrfHead)) {
                    return;
                  }
                  assert vrfTailSettings.getVni() == vrfHeadSettings.getVni();
                  addLayer3VniEdge(graph, vrfTail, vrfTailSettings, vrfHead, vrfHeadSettings);
                }));
  }

  /** Build a {@link VxlanNode} for the given {@link Layer2Vni} and {@link VrfId}. */
  @VisibleForTesting
  static VxlanNode buildLayer2VxlanNode(VrfId vrf, Layer2Vni vniSettings) {
    return VxlanNode.builder()
        .setHostname(vrf._hostname)
        .setVni(vniSettings.getVni())
        .setVniLayer(LAYER_2)
        .build();
  }

  /** Build a {@link VxlanNode} for the given {@link Layer3Vni} and {@link VrfId}. */
  @VisibleForTesting
  static VxlanNode buildLayer3VxlanNode(VrfId vrf, Layer3Vni vniSettings) {
    return VxlanNode.builder()
        .setHostname(vrf._hostname)
        .setVni(vniSettings.getVni())
        .setVniLayer(LAYER_3)
        .build();
  }

  /** Check if two {@link Layer2Vni} have compatible configurations */
  @VisibleForTesting
  static boolean compatibleLayer2VniSettings(Layer2Vni vniSettingsTail, Layer2Vni vniSettingsHead) {
    return vniSettingsTail.getBumTransportMethod() == vniSettingsHead.getBumTransportMethod()
        && vniSettingsTail.getUdpPort() == vniSettingsHead.getUdpPort()
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

  /**
   * Check if two {@link Layer3Vni} have compatible configurations, modulo reachability. We consider
   * Layer-3 VNI configurations for two VTEPs to be compatible if at least one VTEP has learned a
   * route from the other for the given VNI with the other's source address as the VTEP IP of the
   * next hop of the route.
   *
   * <p>Note that edges between layer-3 VNIs with compatible VNIs may still be pruned by later
   * checks.
   */
  @VisibleForTesting
  static boolean compatibleLayer3VniSettings(Layer3Vni vniSettingsTail, Layer3Vni vniSettingsHead) {
    return vniSettingsTail.getUdpPort() == vniSettingsHead.getUdpPort()
        && vniSettingsTail.getSourceAddress() != null
        && vniSettingsHead.getSourceAddress() != null
        && !vniSettingsTail.getSourceAddress().equals(vniSettingsHead.getSourceAddress())
        && (vniSettingsTail.getLearnedNexthopVtepIps().contains(vniSettingsHead.getSourceAddress())
            || vniSettingsHead
                .getLearnedNexthopVtepIps()
                .contains(vniSettingsTail.getSourceAddress()));
  }

  @VisibleForTesting
  static @Nonnull String getVniSrcVrf(Vni vniSettings) {
    return vniSettings.getSrcVrf();
  }

  /**
   * Compute the intiial VXLAN topology based on the {@link Layer2Vni}s and {@link Layer3Vni}s
   * extracted from the given {@code configurations}.
   */
  public static @Nonnull VxlanTopology computeInitialVxlanTopology(
      Map<String, Configuration> configurations) {
    return internalComputeVxlanTopology(
        computeInitialVniSettingsTable(configurations, Vrf::getLayer2Vnis),
        computeInitialVniSettingsTable(configurations, Vrf::getLayer3Vnis));
  }

  /**
   * Compute the VXLAN topology based on the {@link Layer2Vni}s and {@link Layer3Vni}s extracted
   * from the given tables. May contain edges that will be pruned later because there is no
   * reachability between VTEP IPs given the corresponding settings.
   *
   * @param layer2VniSettings table of the layer-2 VNI settings. Row is hostname, Column is VRF
   *     name.
   * @param layer3VniSettings table of the layer-3 VNI settings. Row is hostname, Column is VRF
   *     name.
   */
  public static @Nonnull VxlanTopology computeNextVxlanTopologyModuloReachability(
      Table<String, String, ? extends Collection<Layer2Vni>> layer2VniSettings,
      Table<String, String, ? extends Collection<Layer3Vni>> layer3VniSettings) {
    return internalComputeVxlanTopology(
        computeVniIndexedVniSettingsTable(layer2VniSettings),
        computeVniIndexedVniSettingsTable(layer3VniSettings));
  }

  /**
   * Convert initial VNI settings from {@code configurations} into a table indexed by ({@link
   * VrfId}, VNI).
   */
  @VisibleForTesting
  static @Nonnull <V extends Vni> Table<VrfId, Integer, V> computeInitialVniSettingsTable(
      Map<String, Configuration> configurations, Function<Vrf, Map<Integer, V>> vniGetter) {
    Table<VrfId, Integer, V> table = HashBasedTable.create();
    for (Configuration c : configurations.values()) {
      for (Vrf vrf : c.getVrfs().values()) {
        for (V vniSettings : vniGetter.apply(vrf).values()) {
          table.put(new VrfId(c.getHostname(), vrf.getName()), vniSettings.getVni(), vniSettings);
        }
      }
    }
    return table;
  }

  /**
   * Convert VNI setting table indexed by (node, vrf) obtained from the dataplane into a table
   * indexed by ({@link VrfId}, VNI).
   */
  private static @Nonnull <V extends Vni>
      Table<VrfId, Integer, V> computeVniIndexedVniSettingsTable(
          Table<String, String, ? extends Collection<V>> nodeVrfIndexedVniSettingsTable) {
    Table<VrfId, Integer, V> table = HashBasedTable.create();
    for (Cell<String, String, ? extends Collection<V>> cell :
        nodeVrfIndexedVniSettingsTable.cellSet()) {
      assert cell.getValue() != null;
      assert cell.getRowKey() != null;
      assert cell.getColumnKey() != null;
      for (V vni : cell.getValue()) {
        table.put(new VrfId(cell.getRowKey(), cell.getColumnKey()), vni.getVni(), vni);
      }
    }
    return table;
  }

  /** Compute the VXLAN topology. Adds edges per VNI. */
  private static @Nonnull VxlanTopology internalComputeVxlanTopology(
      Table<VrfId, Integer, Layer2Vni> layer2VniSettings,
      Table<VrfId, Integer, Layer3Vni> layer3VniSettings) {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    layer2VniSettings
        .columnMap() // group by vni
        .values() // Map<NodeVrf, Layer2Vni> where all entries have same VNI
        .forEach(vrfs -> addLayer2VniEdges(graph, vrfs));
    layer3VniSettings
        .columnMap() // group by vni
        .values() // Map<NodeVrf, Layer3Vni> where all entries have same VNI
        .forEach(vrfs -> addLayer3VniEdges(graph, vrfs));
    return new VxlanTopology(graph);
  }

  /**
   * Compute {@link VxlanTopology} that results after pruning edges corresponding to endpoints that
   * cannot reach each other from a {@code vxlanTopologyModuloReachability}.
   */
  public static @Nonnull VxlanTopology prunedVxlanTopology(
      VxlanTopology vxlanTopologyModuloReachability,
      Map<String, Configuration> configurations,
      TracerouteEngine tracerouteEngine) {
    NetworkConfigurations nc = NetworkConfigurations.of(configurations);
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    vxlanTopologyModuloReachability.getGraph().edges().parallelStream()
        .filter(edge -> reachableEdge(edge, nc, tracerouteEngine))
        .collect(ImmutableList.toImmutableList())
        .forEach(edge -> graph.putEdge(edge.nodeU(), edge.nodeV()));
    return new VxlanTopology(graph);
  }

  /**
   * Return {@code true} iff the two VTEPs of the given {@code edge} can reach each other.
   *
   * <p>Technically, VXLAN layer-3 VNI reachability is unidirectional. However, since we currently
   * generate only up to one NVE interface per (node,vni), it is impossible to implement
   * unidirectional connectivity via layer-3 edge(s) between two generated NVE interfaces. A future
   * implementation could support unidirectional connectivity by generating a single interface per
   * (vni, learned VTEP IP). Then this function would only check reachability in one direction.
   */
  @VisibleForTesting
  static boolean reachableEdge(
      EndpointPair<VxlanNode> edge, NetworkConfigurations nc, TracerouteEngine tracerouteEngine) {
    VxlanNode nodeU = edge.nodeU();
    VxlanNode nodeV = edge.nodeV();
    String hostU = nodeU.getHostname();
    String hostV = nodeV.getHostname();
    int vni = nodeU.getVni();
    // nodeU and nodeV must be compatible coming in to this function
    assert nodeU.getVniLayer() == nodeV.getVniLayer();
    Vni vniU;
    Vni vniV;
    if (nodeU.getVniLayer() == LAYER_2) {
      vniU = nc.getVniSettings(hostU, vni, Vrf::getLayer2Vnis).get();
      // early exit if unsupported
      if (((Layer2Vni) vniU).getBumTransportMethod() != BumTransportMethod.UNICAST_FLOOD_GROUP) {
        // TODO: support multicast transport
        return false;
      }
      vniV = nc.getVniSettings(hostV, vni, Vrf::getLayer2Vnis).get();
    } else {
      assert nodeU.getVniLayer() == LAYER_3;
      vniU = nc.getVniSettings(hostU, vni, Vrf::getLayer3Vnis).get();
      vniV = nc.getVniSettings(hostV, vni, Vrf::getLayer3Vnis).get();
    }
    String vrfU = getVniSrcVrf(vniU);
    String vrfV = getVniSrcVrf(vniV);
    Ip srcIpU = vniU.getSourceAddress();
    Ip srcIpV = vniV.getSourceAddress();
    assert srcIpU != null;
    assert srcIpV != null;
    int udpPort = vniU.getUdpPort();
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

    private final @Nonnull String _hostname;
    private final @Nonnull String _vrfName;

    VrfId(String hostname, String vrfName) {
      _hostname = hostname;
      _vrfName = vrfName;
    }

    public @Nonnull String getHostname() {
      return _hostname;
    }

    public @Nonnull String getVrfName() {
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
