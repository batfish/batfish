package org.batfish.common.util;

import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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
import org.batfish.datamodel.Interface;
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
        new IpsecPeerConfigId("peer2", "host2"),
        new IpsecPeerConfigId("peer1", "host1"),
        establishedSession);
    // between peer3 and peer4 session is broken in one direction
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer3", "host1"),
        new IpsecPeerConfigId("peer4", "host2"),
        unEstablishedSession);
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer4", "host2"),
        new IpsecPeerConfigId("peer3", "host1"),
        establishedSession);
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

    ImmutableSortedMap.Builder<String, Interface> interfaceBuilder =
        ImmutableSortedMap.naturalOrder();
    c1.setInterfaces(
        interfaceBuilder
            .put("Tunnel1", new Interface("Tunnel1", c1))
            .put("Tunnel3", new Interface("Tunnel3", c1))
            .put("interface5", new Interface("interface5", c1))
            .put("Tunnel9", new Interface("Tunnel9", c1))
            .put("Tunnel7", new Interface("Tunnel7", c1))
            .build());
    interfaceBuilder = ImmutableSortedMap.naturalOrder();
    c2.setInterfaces(
        interfaceBuilder
            .put("Tunnel2", new Interface("Tunnel2", c2))
            .put("Tunnel4", new Interface("Tunnel4", c2))
            .put("interface6", new Interface("interface6", c2))
            .put("Tunnel8", new Interface("Tunnel8", c2))
            .put("Tunnel10", new Interface("Tunnel10", c2))
            .build());

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
                        new NodeInterfacePair("host2", "Tunnel2"),
                        new NodeInterfacePair("host1", "Tunnel1")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel3"),
                        new NodeInterfacePair("host2", "Tunnel4")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host2", "Tunnel4"),
                        new NodeInterfacePair("host1", "Tunnel3")))
                // next two edges don't run IPsec
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "interface5"),
                        new NodeInterfacePair("host2", "interface6")))
                .add(
                    new Edge(
                        new NodeInterfacePair("host1", "Tunnel9"),
                        new NodeInterfacePair("host2", "Tunnel10")))
                // Tunnel7 is a dangling IPsec peer
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
                    new NodeInterfacePair("host2", "Tunnel2"),
                    new NodeInterfacePair("host1", "Tunnel1")),
                new Edge(
                    new NodeInterfacePair("host1", "interface5"),
                    new NodeInterfacePair("host2", "interface6")),
                new Edge(
                    new NodeInterfacePair("host2", "Tunnel4"),
                    new NodeInterfacePair("host1", "Tunnel3")),
                new Edge(
                    new NodeInterfacePair("host1", "Tunnel9"),
                    new NodeInterfacePair("host2", "Tunnel10")))));

    // only Tunnel7 on host1 will be inactive because it is the only node in the final IPsec
    // topology with no neighbors (Since Tunnel8 on host 2 is not in IPsec topology it will be
    // unaffected)
    assertThat(_configurations.get("host1").getAllInterfaces().get("Tunnel1"), isActive());
    assertThat(_configurations.get("host1").getAllInterfaces().get("Tunnel3"), isActive());
    assertThat(_configurations.get("host1").getAllInterfaces().get("interface5"), isActive());
    assertThat(_configurations.get("host1").getAllInterfaces().get("Tunnel9"), isActive());
    assertThat(_configurations.get("host1").getAllInterfaces().get("Tunnel7"), not(isActive()));

    assertThat(_configurations.get("host2").getAllInterfaces().get("Tunnel2"), isActive());
    assertThat(_configurations.get("host2").getAllInterfaces().get("Tunnel4"), isActive());
    assertThat(_configurations.get("host2").getAllInterfaces().get("interface6"), isActive());
    assertThat(_configurations.get("host2").getAllInterfaces().get("Tunnel10"), isActive());
    assertThat(_configurations.get("host2").getAllInterfaces().get("Tunnel8"), isActive());
  }
}
