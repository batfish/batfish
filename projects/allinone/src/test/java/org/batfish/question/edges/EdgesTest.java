package org.batfish.question.edges;

import static org.batfish.question.edges.EdgesAnswerer.COL_MULTICAST_GROUP;
import static org.batfish.question.edges.EdgesAnswerer.COL_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VTEP_ADDRESS;
import static org.batfish.question.edges.EdgesAnswerer.COL_UDP_PORT;
import static org.batfish.question.edges.EdgesAnswerer.COL_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_VNI;
import static org.batfish.question.edges.EdgesAnswerer.COL_VTEP_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EdgeType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Test;

/** End-to-end tests of {@link org.batfish.question.edges}. */
public final class EdgesTest {

  @Test
  public void testAnswerVxlan() {
    Ip multicastGroup = Ip.parse("224.0.0.1");
    String node1 = "n1";
    String node2 = "n2";
    Ip srcIp1 = Ip.parse("1.1.1.1");
    Ip srcIp2 = Ip.parse("2.2.2.2");
    int udpPort = 5555;
    int vlan1 = 1;
    int vlan2 = 2;
    int vni = 5000;
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.setHostname(node1).build();
    Configuration c2 = cb.setHostname(node2).build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf v1 = vb.setOwner(c1).build();
    Vrf v2 = vb.setOwner(c2).build();
    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(node1, c1, node2, c2);
    VniSettings.Builder vniSettingsBuilder =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(multicastGroup))
            .setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP)
            .setUdpPort(udpPort)
            .setVni(vni);
    VniSettings vniSettingsTail =
        vniSettingsBuilder.setSourceAddress(srcIp1).setVlan(vlan1).build();
    v1.setVniSettings(ImmutableSortedMap.of(vni, vniSettingsTail));
    v2.setVniSettings(
        ImmutableSortedMap.of(
            vni, vniSettingsBuilder.setSourceAddress(srcIp2).setVlan(vlan2).build()));
    TableAnswerElement answer =
        (TableAnswerElement)
            new EdgesAnswerer(
                    new EdgesQuestion(NodesSpecifier.ALL, NodesSpecifier.ALL, EdgeType.VXLAN),
                    new IBatfishTestAdapter() {
                      @Override
                      public SortedMap<String, Configuration> loadConfigurations(
                          NetworkSnapshot snapshot) {
                        return configurations;
                      }

                      @Override
                      public SortedMap<String, Configuration> loadConfigurations() {
                        return configurations;
                      }

                      @Override
                      public Topology getEnvironmentTopology() {
                        return new Topology(ImmutableSortedSet.of());
                      }

                      @Override
                      public NetworkSnapshot getNetworkSnapshot() {
                        return new NetworkSnapshot(new NetworkId("a"), new SnapshotId("b"));
                      }
                    })
                .answer();

    assertThat(
        answer.getRowsList(),
        containsInAnyOrder(
            Row.builder()
                .put(COL_VNI, vni)
                .put(COL_NODE, new Node(node1))
                .put(COL_REMOTE_NODE, new Node(node2))
                .put(COL_VTEP_ADDRESS, srcIp1)
                .put(COL_REMOTE_VTEP_ADDRESS, srcIp2)
                .put(COL_VLAN, vlan1)
                .put(COL_REMOTE_VLAN, vlan2)
                .put(COL_UDP_PORT, udpPort)
                .put(COL_MULTICAST_GROUP, multicastGroup)
                .build(),
            Row.builder()
                .put(COL_VNI, vni)
                .put(COL_NODE, new Node(node2))
                .put(COL_REMOTE_NODE, new Node(node1))
                .put(COL_VTEP_ADDRESS, srcIp2)
                .put(COL_REMOTE_VTEP_ADDRESS, srcIp1)
                .put(COL_VLAN, vlan2)
                .put(COL_REMOTE_VLAN, vlan1)
                .put(COL_UDP_PORT, udpPort)
                .put(COL_MULTICAST_GROUP, multicastGroup)
                .build()));
  }
}
