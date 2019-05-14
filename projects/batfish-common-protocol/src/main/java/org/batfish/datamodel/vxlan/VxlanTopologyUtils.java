package org.batfish.datamodel.vxlan;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  @VisibleForTesting
  static void addVniEdge(
      MutableGraph<VxlanNode> graph,
      Map<Vrf, String> vrfHostnames,
      Integer vni,
      Vrf vrfTail,
      VniSettings vniSettingsTail,
      Vrf vrfHead) {
    VniSettings vniSettingsHead = vrfHead.getVniSettings().get(vni);
    if (compatibleVniSettings(vniSettingsTail, vniSettingsHead)) {
      VxlanNode nodeTail = buildVxlanNode(vrfHostnames, vrfTail, vniSettingsTail);
      VxlanNode nodeHead = buildVxlanNode(vrfHostnames, vrfHead, vniSettingsHead);
      graph.putEdge(nodeTail, nodeHead);
    }
  }

  @VisibleForTesting
  static void addVniEdges(
      MutableGraph<VxlanNode> graph, Map<Vrf, String> vrfHostnames, Integer vni, List<Vrf> vrfs) {
    for (Vrf vrfTail : vrfs) {
      VniSettings vniSettingsTail = vrfTail.getVniSettings().get(vni);
      vrfs.stream()
          .filter(vrfHead -> vrfTail != vrfHead)
          .forEach(
              vrfHead -> {
                addVniEdge(graph, vrfHostnames, vni, vrfTail, vniSettingsTail, vrfHead);
              });
    }
  }

  @VisibleForTesting
  static VxlanNode buildVxlanNode(Map<Vrf, String> vrfHostnames, Vrf vrf, VniSettings vniSettings) {
    return VxlanNode.builder()
        .setHostname(vrfHostnames.get(vrf))
        .setVni(vniSettings.getVni())
        .build();
  }

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

  public static @Nonnull VxlanTopology initialVxlanTopology(
      Map<String, Configuration> configurations) {
    MutableGraph<VxlanNode> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
    Map<Integer, List<Vrf>> vrfsByVni = initVniVrfAssociations(configurations);
    Map<Vrf, String> vrfHostnames = initVrfHostnameMap(configurations);
    vrfsByVni.forEach((vni, vrfs) -> addVniEdges(graph, vrfHostnames, vni, vrfs));
    return new VxlanTopology(graph);
  }

  /** Associate VNIs with VRFs that have {@link VniSettings} mentioning them */
  @VisibleForTesting
  static Map<Integer, List<Vrf>> initVniVrfAssociations(Map<String, Configuration> configurations) {
    Map<Integer, ImmutableList.Builder<Vrf>> vrfsByVni = new HashMap<>();
    for (Configuration c : configurations.values()) {
      for (Vrf v : c.getVrfs().values()) {
        for (VniSettings vniSettings : v.getVniSettings().values()) {
          vrfsByVni.computeIfAbsent(vniSettings.getVni(), vni -> ImmutableList.builder()).add(v);
        }
      }
    }
    return toImmutableMap(
        vrfsByVni, Entry::getKey, vrfsByVniEntry -> vrfsByVniEntry.getValue().build());
  }

  /** Associate VRFs with hostnames of configs on which they sit */
  @VisibleForTesting
  static Map<Vrf, String> initVrfHostnameMap(Map<String, Configuration> configurations) {
    Map<Vrf, String> vrfHostnames = new IdentityHashMap<>();
    for (Configuration c : configurations.values()) {
      for (Vrf v : c.getVrfs().values()) {
        vrfHostnames.put(v, c.getHostname());
      }
    }
    // Use unmodifiableMap to lock the implementation
    return Collections.unmodifiableMap(vrfHostnames);
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
    // early exit if unuspported
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

  private VxlanTopologyUtils() {}
}
