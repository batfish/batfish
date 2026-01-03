package org.batfish.common.util;

import static org.batfish.common.util.IpsecUtil.retainReachableIpsecEdges;
import static org.batfish.common.util.IpsecUtil.toEdgeSet;
import static org.batfish.datamodel.ConfigurationFormat.AWS;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.junit.Before;
import org.junit.Test;

public class IpsecUtilHybridCloudTest {
  private IpsecTopology _ipsecTopology;
  private Map<String, Configuration> _configurations = new HashMap<>();

  @Before
  public void setup() {
    // creates a topology with the following edges with edge between Tunnel9 and Tunnel10 having no
    // corresponding IPsec edges/nodes
    /*                                              AWS
    * +----------------+                      +----------------+
      |                |                      | Tunnel2        |
      |        Tunnel1 +----------------------+                |
      |    int 1 (shut)|                      |                |
      +----------------+                      +----------------+
    *
    *
    * */
    IpsecStaticPeerConfig ipsecPeerConfig1 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface1")
            .setTunnelInterface("Tunnel1")
            .setLocalAddress(Ip.parse("1.1.1.1"))
            .build();
    IpsecStaticPeerConfig ipsecPeerConfig2 =
        IpsecStaticPeerConfig.builder()
            .setSourceInterface("interface2")
            .setTunnelInterface("Tunnel2")
            .setLocalAddress(Ip.parse("2.2.2.2"))
            .build();

    IpsecSession establishedSession =
        IpsecSession.builder().setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal()).build();

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

    _ipsecTopology = new IpsecTopology(graph);

    // populate Configurations to get IPsecPeerConfig objects from IPsecPeerConfigIds
    Configuration c1 = new Configuration("host1", CISCO_IOS);
    Configuration c2 = new Configuration("host2", AWS);
    c1.setIpsecPeerConfigs(ImmutableSortedMap.of("peer1", ipsecPeerConfig1));
    c2.setIpsecPeerConfigs(ImmutableSortedMap.of("peer2", ipsecPeerConfig2));

    ImmutableSortedMap.Builder<String, Interface> interfaceBuilder =
        ImmutableSortedMap.naturalOrder();
    c1.setInterfaces(
        interfaceBuilder
            .put("Tunnel1", TestInterface.builder().setName("Tunnel1").setOwner(c1).build())
            .put(
                "interface1",
                TestInterface.builder()
                    .setName("interface1")
                    .setOwner(c1)
                    .setAdminUp(false)
                    .build())
            .build());
    interfaceBuilder = ImmutableSortedMap.naturalOrder();
    c2.setInterfaces(
        interfaceBuilder
            .put("Tunnel2", TestInterface.builder().setName("Tunnel2").setOwner(c2).build())
            .build());

    _configurations.put("host1", c1);
    _configurations.put("host2", c2);
  }

  @Test
  public void testSessionNotEstablishedToShutdownInterface() {
    Set<Edge> compatibleIpsecEdges =
        toEdgeSet(
            retainReachableIpsecEdges(_ipsecTopology, _configurations, null), _configurations);
    assertThat(compatibleIpsecEdges, empty());
  }
}
