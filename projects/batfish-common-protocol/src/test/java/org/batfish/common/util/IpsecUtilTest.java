package org.batfish.common.util;

import static org.batfish.common.util.IpsecUtil.getIpsecSession;
import static org.batfish.common.util.IpsecUtil.retainCompatibleTunnelEdges;
import static org.batfish.common.util.IpsecUtil.toEdgeSet;
import static org.batfish.datamodel.ConfigurationFormat.AWS;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.junit.Before;
import org.junit.Test;

public class IpsecUtilTest {
  private IpsecTopology _ipsecTopology;
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
            .setSourceInterface("interface1")
            .setTunnelInterface("Tunnel1")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig2 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface2")
            .setTunnelInterface("Tunnel2")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig3 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface3")
            .setTunnelInterface("Tunnel3")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig4 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface4")
            .setTunnelInterface("Tunnel4")
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig5 =
        IpsecStaticPeerConfig.builder().setSourceInterface("interface5").build();
    IpsecStaticPeerConfig ipsecPeerConfig6 =
        IpsecStaticPeerConfig.builder().setSourceInterface("interface6").build();
    IpsecStaticPeerConfig ipsecPeerConfig7 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface7")
            .setTunnelInterface("Tunnel7")
            .build();

    IpsecSession establishedSession =
        IpsecSession.builder().setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal()).build();
    IpsecSession unEstablishedSession = IpsecSession.builder().build();

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
    // peer5 and peer6 are not running IPsec on tunnel interfaces
    graph.putEdgeValue(
        new IpsecPeerConfigId("peer5", "host1"),
        new IpsecPeerConfigId("peer6", "host2"),
        unEstablishedSession);
    // a dangling IPsec peer with no corresponding endpoint
    graph.addNode(new IpsecPeerConfigId("peer7", "host1"));
    _ipsecTopology = new IpsecTopology(graph);

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
  }

  @Test
  public void testRetainCompatibleTunnelEdges() {
    Set<Edge> compatibleIpsecEdges =
        toEdgeSet(retainCompatibleTunnelEdges(_ipsecTopology, _configurations), _configurations);

    assertThat(
        compatibleIpsecEdges,
        containsInAnyOrder(
            new Edge(
                new NodeInterfacePair("host1", "Tunnel1"),
                new NodeInterfacePair("host2", "Tunnel2")),
            new Edge(
                new NodeInterfacePair("host2", "Tunnel2"),
                new NodeInterfacePair("host1", "Tunnel1"))));
  }

  @Test
  public void testGetIpsecSesssionCloud() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    IpsecStaticPeerConfig.Builder ipsecPeerConfigBuilder = IpsecStaticPeerConfig.builder();

    assertTrue(
        getIpsecSession(
                cb.setConfigurationFormat(AWS).build(),
                cb.setConfigurationFormat(AWS).build(),
                ipsecPeerConfigBuilder.build(),
                ipsecPeerConfigBuilder.build())
            .isCloud());

    assertTrue(
        getIpsecSession(
                cb.setConfigurationFormat(CISCO_IOS).build(),
                cb.setConfigurationFormat(AWS).build(),
                ipsecPeerConfigBuilder.build(),
                ipsecPeerConfigBuilder.build())
            .isCloud());

    assertFalse(
        getIpsecSession(
                cb.setConfigurationFormat(CISCO_IOS).build(),
                cb.setConfigurationFormat(CISCO_IOS).build(),
                ipsecPeerConfigBuilder.build(),
                ipsecPeerConfigBuilder.build())
            .isCloud());
  }
}
