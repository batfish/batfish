package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.BgpTopologyUtils.initBgpTopology;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BgpTopologyUtilsTest {

  private static BgpProcess _node1BgpProcess = new BgpProcess();
  private static BgpProcess _node2BgpProcess = new BgpProcess();
  private static BgpProcess _node3BgpProcess = new BgpProcess();
  private static Map<String, Configuration> _configs;

  /** Sets up three nodes with a BGP process on each. Tests can populate BGP processes. */
  @BeforeClass
  public static void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Vrf vrf1 = new Vrf("vrf1");
    vrf1.setBgpProcess(_node1BgpProcess);
    Configuration c1 = cb.setHostname("node1").build();
    c1.setVrfs(ImmutableMap.of("vrf1", vrf1));

    Vrf vrf2 = new Vrf("vrf2");
    vrf2.setBgpProcess(_node2BgpProcess);
    Configuration c2 = cb.setHostname("node2").build();
    c2.setVrfs(ImmutableMap.of("vrf2", vrf2));

    Vrf vrf3 = new Vrf("vrf3");
    vrf3.setBgpProcess(_node3BgpProcess);
    Configuration c3 = cb.setHostname("node3").build();
    c3.setVrfs(ImmutableMap.of("vrf3", vrf3));

    _configs = ImmutableMap.of("node1", c1, "node2", c2, "node3", c3);
  }

  @Before
  public void clearBgpProcesses() {
    _node1BgpProcess.setNeighbors(ImmutableSortedMap.of());
    _node2BgpProcess.setNeighbors(ImmutableSortedMap.of());
    _node3BgpProcess.setNeighbors(ImmutableSortedMap.of());
    _node1BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of());
    _node2BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of());
    _node3BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of());
  }

  @Test
  public void testInitTopologyRemotePrefixNotMatchingLocalIp() {
    // Peer 1 on node1 with IP 1.1.1.1 is active, set up to peer with 2.2.2.2
    // Peer 2 on node2 with IP 2.2.2.2 is passive, with remote prefix 1.1.1.0/24
    // Should see one edge in BGP topology: peer 1 to peer 2

    Ip ip1 = new Ip("1.1.1.1");
    Ip ip2 = new Ip("2.2.2.2");

    Prefix peer1PeerPrefix = new Prefix(ip2, 32);
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(1L)
            .setPeerAddress(ip2)
            .setRemoteAs(2L)
            .build();
    _node1BgpProcess.setNeighbors(ImmutableSortedMap.of(peer1PeerPrefix, peer1));

    Prefix peer2PeerPrefix = new Prefix(ip1, 24);
    BgpPassivePeerConfig peer2 =
        BgpPassivePeerConfig.builder()
            .setLocalIp(Ip.AUTO)
            .setLocalAs(2L)
            .setRemoteAs(ImmutableList.of(1L))
            .setPeerPrefix(peer2PeerPrefix)
            .build();
    _node2BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of(peer2PeerPrefix, peer2));

    Map<Ip, Set<String>> ipOwners =
        ImmutableMap.of(ip1, ImmutableSet.of("node1"), ip2, ImmutableSet.of("node2"));

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        initBgpTopology(_configs, ipOwners, true, false, null, null);
    assertThat(bgpTopology.edges(), hasSize(1));
    EndpointPair<BgpPeerConfigId> edge = bgpTopology.edges().iterator().next();
    assertThat(edge.source().getHostname(), equalTo("node1"));
    assertThat(edge.target().getHostname(), equalTo("node2"));
  }

  @Test
  public void testInitTopologyPeerAddressNotMatchingRemoteHost() {
    // Peer 1 on node1 with IP 1.1.1.1 is active, set up to peer with 1.1.1.2
    // Peer 2 on node2 with IP 1.1.1.2 is passive, able to peer with peer 1
    // Peer 3 has the same configuration as peer 2, but on node3 with IP 1.1.1.3
    // Should see one edge in BGP topology: peer 1 to peer 2

    Ip ip1 = new Ip("1.1.1.1");
    Ip ip2 = new Ip("1.1.1.2");
    Ip ip3 = new Ip("1.1.1.3");

    Prefix peer1PeerPrefix = new Prefix(ip2, 32);
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(1L)
            .setPeerAddress(ip2)
            .setRemoteAs(2L)
            .build();
    _node1BgpProcess.setNeighbors(ImmutableSortedMap.of(peer1PeerPrefix, peer1));

    Prefix prefixForPeer1 = new Prefix(ip1, 24);
    BgpPassivePeerConfig.Builder passivePeerBuilder =
        BgpPassivePeerConfig.builder()
            .setLocalIp(Ip.AUTO)
            .setLocalAs(2L)
            .setRemoteAs(ImmutableList.of(1L))
            .setPeerPrefix(prefixForPeer1);

    BgpPassivePeerConfig peer2 = passivePeerBuilder.build();
    BgpPassivePeerConfig peer3 = passivePeerBuilder.build();
    _node2BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of(prefixForPeer1, peer2));
    _node3BgpProcess.setPassiveNeighbors(ImmutableSortedMap.of(prefixForPeer1, peer3));

    Map<Ip, Set<String>> ipOwners =
        ImmutableMap.of(
            ip1,
            ImmutableSet.of("node1"),
            ip2,
            ImmutableSet.of("node2"),
            ip3,
            ImmutableSet.of("node3"));

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        initBgpTopology(_configs, ipOwners, true, false, null, null);
    assertThat(bgpTopology.edges(), hasSize(1));
    EndpointPair<BgpPeerConfigId> edge = bgpTopology.edges().iterator().next();
    assertThat(edge.source().getHostname(), equalTo("node1"));
    assertThat(edge.target().getHostname(), equalTo("node2"));
  }
}
