package org.batfish.datamodel.vxlan;

import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;

/** VXLAN topology with edges for each compatible VNI-endpoint pair */
@ParametersAreNonnullByDefault
public final class VxlanTopology {

  @VisibleForTesting
  static void addVniEdge(
      MutableNetwork<VxlanNode, VxlanEdge> graph,
      Map<Vrf, String> vrfHostnames,
      Integer vni,
      Vrf vrfTail,
      VniSettings vniSettingsTail,
      Vrf vrfHead) {
    VniSettings vniSettingsHead = vrfHead.getVniSettings().get(vni);
    if (compatibleVniSettings(vniSettingsTail, vniSettingsHead)) {
      VxlanNode nodeTail = buildVxlanNode(vrfHostnames, vrfTail, vniSettingsTail);
      VxlanNode nodeHead = buildVxlanNode(vrfHostnames, vrfHead, vniSettingsHead);
      VxlanEdge edge = buildVxlanEdge(vni, vniSettingsTail, nodeTail, nodeHead);
      graph.addEdge(nodeTail, nodeHead, edge);
    }
  }

  @VisibleForTesting
  static void addVniEdges(
      MutableNetwork<VxlanNode, VxlanEdge> graph,
      Map<Vrf, String> vrfHostnames,
      Integer vni,
      List<Vrf> vrfs) {
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
  static VxlanEdge buildVxlanEdge(
      Integer vni, VniSettings vniSettingsCommon, VxlanNode nodeTail, VxlanNode nodeHead) {
    VxlanEdge edge =
        VxlanEdge.builder()
            .setMulticastGroup(
                vniSettingsCommon.getBumTransportMethod() == BumTransportMethod.MULTICAST_GROUP
                    ? vniSettingsCommon.getBumTransportIps().first()
                    : null)
            .setTail(nodeTail)
            .setHead(nodeHead)
            .setUdpPort(vniSettingsCommon.getUdpPort())
            .setVni(vni)
            .build();
    return edge;
  }

  @VisibleForTesting
  static VxlanNode buildVxlanNode(Map<Vrf, String> vrfHostnames, Vrf vrf, VniSettings vniSettings) {
    VxlanNode node1 =
        VxlanNode.builder()
            .setHostname(vrfHostnames.get(vrf))
            .setSourceAddress(vniSettings.getSourceAddress())
            .setVlan(vniSettings.getVlan())
            .setVrf(vrf.getName())
            .build();
    return node1;
  }

  @VisibleForTesting
  static boolean compatibleVniSettings(VniSettings vniSettingsTail, VniSettings vniSettingsHead) {
    return vniSettingsTail.getBumTransportMethod() == vniSettingsHead.getBumTransportMethod()
        && vniSettingsTail.getUdpPort() != null
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

  private final ImmutableNetwork<VxlanNode, VxlanEdge> _graph;

  public VxlanTopology(Map<String, Configuration> configurations) {
    MutableNetwork<VxlanNode, VxlanEdge> graph =
        NetworkBuilder.directed().allowsParallelEdges(false).allowsSelfLoops(false).build();
    Map<Integer, List<Vrf>> vrfsByVni = initVniVrfAssociations(configurations);
    Map<Vrf, String> vrfHostnames = initVrfHostnameMap(configurations);
    vrfsByVni.forEach((vni, vrfs) -> addVniEdges(graph, vrfHostnames, vni, vrfs));
    _graph = ImmutableNetwork.copyOf(graph);
  }

  public Set<VxlanEdge> getEdges() {
    return ImmutableSet.copyOf(_graph.edges());
  }
}
