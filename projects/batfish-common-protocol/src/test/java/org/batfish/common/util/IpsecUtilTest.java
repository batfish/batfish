package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Before;
import org.junit.Test;

public class IpsecUtilTest {
  private Topology _topology;
  private ValueGraph<IpsecPeerConfigId, IpsecSession> _ipsecTopology;
  private Map<String, Configuration> _configurations = new HashMap<>();

  @Before
  public void setup() {
    // creates a topology with the following edges with edge between Tunnel9 and Tunnel10 having no
    // corresponding IPsec edges/nodes
    /*
    * +----------------+                      +----------------+
      |                |                      | Tunnel2        |
      |        Tunnel1 +----------------------+                |
      |                |                      |                |
      |                |                      |                |
      |        Tunnel3 +----------------------+ Tunnel4        |
      |                |                      |                |
      |        interface5                     |                |
      |                +----------------------+interface6      |
      |                |                      |                |
      |                |                      |                |
      |       Tunnel7  +----------------------+Tunnel8         |
      |                |                      |                |
      |                |                      |                |
      |       Tunnel9  +----------------------+Tunnel10        |
      +----------------+                      +----------------+
    *
    *
    * */
    IpsecStaticPeerConfig ipsecPeerConfig1 =
        IpsecStaticPeerConfig.builder()
            .setPhysicalInterface("interface1")
            .setTunnelInterface("Tunnel1")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig2 =
        IpsecStaticPeerConfig.builder()
            .setPhysicalInterface("interface2")
            .setTunnelInterface("Tunnel2")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig3 =
        IpsecStaticPeerConfig.builder()
            .setPhysicalInterface("interface3")
            .setTunnelInterface("Tunnel3")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig4 =
        IpsecStaticPeerConfig.builder()
            .setPhysicalInterface("interface4")
            .setTunnelInterface("Tunnel4")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig5 =
        IpsecStaticPeerConfig.builder().setPhysicalInterface("interface5").build();
    IpsecStaticPeerConfig ipsecPeerConfig6 =
        IpsecStaticPeerConfig.builder().setPhysicalInterface("interface6").build();
    IpsecStaticPeerConfig ipsecPeerConfig7 =
        IpsecStaticPeerConfig.builder()
            .setPhysicalInterface("interface7")
            .setTunnelInterface("Tunnel7")
            .build();

    IpsecSession.Builder ipseeSessionBuilder = IpsecSession.builder();
    IpsecSession unEstablishedSession = IpsecSession.builder().build();
    ipseeSessionBuilder.setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal());
    IpsecSession establishedSession = ipseeSessionBuilder.build();

    MutableValueGraph<IpsecPeerConfigId, IpsecSession> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();

    // populate IPsec topology
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer1", "host1"),
        new IpsecPeerConfigId("peer2", "host2"),
        establishedSession);
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer3", "host1"),
        new IpsecPeerConfigId("peer4", "host2"),
        unEstablishedSession);
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer5", "host1"),
        new IpsecPeerConfigId("peer6", "host2"),
        unEstablishedSession);
    // a dangling IPsec peer with no corresponding endpoint
    graph.addNode(new IpsecPeerConfigId("peer7", "host1"));
    _ipsecTopology = ImmutableValueGraph.copyOf(graph);

    // populate Configurations to get IPsecPeerConfig objects from IPsecPeerConfigIds
    Configuration c1 = new Configuration("host1", ConfigurationFormat.CISCO_IOS);
    Configuration c2 = new Configuration("host2", ConfigurationFormat.CISCO_IOS);
    c1.setIpsecPeerConfigs(
        ImmutableSortedMap.of(
            "peer1",
            ipsecPeerConfig1,
            "peer3",
            ipsecPeerConfig3,
            "peer5",
            ipsecPeerConfig5,
            "peer7",
            ipsecPeerConfig7));

    c2.setIpsecPeerConfigs(
        ImmutableSortedMap.of(
            "peer2", ipsecPeerConfig2, "peer4", ipsecPeerConfig4, "peer6", ipsecPeerConfig6));
    _configurations.put("host1", c1);
    _configurations.put("host2", c2);

    // populating L3 topology
    ImmutableSortedSet.Builder<Edge> edgeBuilder = ImmutableSortedSet.naturalOrder();
    _topology =
        new Topology(
            edgeBuilder
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel1"),
                        new NodeInterfacePair("host2", "Tunnel2")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel3"),
                        new NodeInterfacePair("host2", "Tunnel4")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "interface5"),
                        new NodeInterfacePair("host2", "interface6")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel9"),
                        new NodeInterfacePair("host2", "Tunnel10")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel7"),
                        new NodeInterfacePair("host2", "Tunnel8")))
                .build());
  }

  @Test
  public void testPruneFailedIpsecSessionEdges() {
    IpsecUtil.pruneFailedIpsecSessionEdges(_topology, _ipsecTopology, _configurations);

    // Edges between host1:Tunnel3->host2:Tunnel4 and host1:Tunnel7->host2:Tunnel8 should be pruned
    assertThat(
        _topology.getEdges(),
        equalTo(
            ImmutableSet.of(
                new Edge(
                    new NodeInterfacePair("host1", "Tunnel1"),
                    new NodeInterfacePair("host2", "Tunnel2")),
                new Edge(
                    new NodeInterfacePair("host1", "interface5"),
                    new NodeInterfacePair("host2", "interface6")),
                new Edge(
                    new NodeInterfacePair("host1", "Tunnel9"),
                    new NodeInterfacePair("host2", "Tunnel10")))));
  }
}
