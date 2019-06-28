package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionAnswererUtils.getConfiguredStatus;
import static org.batfish.question.bgpsessionstatus.BgpSessionAnswererUtils.getLocallyBrokenStatus;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.junit.Before;
import org.junit.Test;

/** Tests of static methods of {@link BgpSessionAnswererUtils} */
public final class BgpSessionAnswererUtilsTest {

  private NetworkFactory _nf;

  @Before
  public void createNetworkFactory() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testLocalIpUnknownStatically() {
    BgpActivePeerConfig peer = _nf.bgpNeighborBuilder().setPeerAddress(Ip.parse("1.1.1.1")).build();
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.EBGP_SINGLEHOP),
        equalTo(ConfiguredSessionStatus.NO_LOCAL_IP));
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.EBGP_MULTIHOP),
        equalTo(ConfiguredSessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.IBGP),
        equalTo(ConfiguredSessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
  }

  @Test
  public void testNoLocalAs() {
    BgpActivePeerConfig peer = _nf.bgpNeighborBuilder().setLocalIp(Ip.parse("1.1.1.1")).build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_LOCAL_AS);
  }

  @Test
  public void testNoRemoteIp() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder().setLocalIp(Ip.parse("1.1.1.1")).setLocalAs(1L).build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_IP);
  }

  @Test
  public void testNoRemoteAs() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(1L)
            .build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_AS);
  }

  @Test
  public void testNotLocallyBroken() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(1L)
            .setRemoteAs(1L)
            .build();
    assertStatusMatchesForAllSessionTypes(peer, null);
  }

  private void assertStatusMatchesForAllSessionTypes(
      BgpActivePeerConfig peer, ConfiguredSessionStatus status) {
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.EBGP_SINGLEHOP),
        equalTo(status));
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.EBGP_MULTIHOP),
        equalTo(status));
    assertThat(
        getLocallyBrokenStatus(peer, BgpSessionProperties.SessionType.IBGP), equalTo(status));
  }

  @Test
  public void testUnnumberedStatuses() {
    BgpPeerConfigId id = new BgpPeerConfigId("c", "vrf", "iface");
    BgpUnnumberedPeerConfig.Builder peerBuilder =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface("iface")
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> topology =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();

    // First issue should be no local AS (not currently possible for peer interface to be missing)
    assertThat(
        getConfiguredStatus(id, peerBuilder.build(), topology),
        equalTo(ConfiguredSessionStatus.NO_LOCAL_AS));

    // Once local AS is set, status should be no remote AS
    BgpUnnumberedPeerConfig peer = peerBuilder.setLocalAs(1L).build();
    assertThat(
        getConfiguredStatus(id, peer, topology), equalTo(ConfiguredSessionStatus.NO_REMOTE_AS));

    // With local and remote AS set but no edges in topology, status should be HALF_OPEN
    peer = peerBuilder.setRemoteAs(1L).build();
    topology.addNode(id);
    assertThat(getConfiguredStatus(id, peer, topology), equalTo(ConfiguredSessionStatus.HALF_OPEN));

    // With one edge, status should be UNIQUE_MATCH
    // Peers need local IPs to avoid breaking assumptions in BgpSessionProperties.from()
    Ip unnumIp = Ip.parse("169.254.0.1");
    peer = peerBuilder.setLocalIp(unnumIp).build();
    BgpPeerConfigId id2 = new BgpPeerConfigId("c2", "vrf", "iface2");
    BgpUnnumberedPeerConfig peer2 = peerBuilder.setPeerInterface("iface2").build();
    topology.addNode(id2);
    topology.putEdgeValue(id, id2, BgpSessionProperties.from(peer, peer2, false));
    topology.putEdgeValue(id2, id, BgpSessionProperties.from(peer, peer2, true));
    assertThat(
        getConfiguredStatus(id, peer, topology), equalTo(ConfiguredSessionStatus.UNIQUE_MATCH));

    // With multiple edges, status should be MULTIPLE_REMOTES
    BgpPeerConfigId id3 = new BgpPeerConfigId("c3", "vrf", "iface3");
    BgpUnnumberedPeerConfig peer3 = peerBuilder.setPeerInterface("iface3").build();
    topology.addNode(id3);
    topology.putEdgeValue(id, id3, BgpSessionProperties.from(peer, peer3, false));
    topology.putEdgeValue(id3, id, BgpSessionProperties.from(peer, peer3, true));
    assertThat(
        getConfiguredStatus(id, peer, topology), equalTo(ConfiguredSessionStatus.MULTIPLE_REMOTES));
  }

  @Test
  public void testPassiveNoLocalAs() {
    BgpPassivePeerConfig peer = _nf.bgpDynamicNeighborBuilder().build();
    assertThat(getLocallyBrokenStatus(peer), equalTo(ConfiguredSessionStatus.NO_LOCAL_AS));
  }

  @Test
  public void testPassiveNoRemotePrefix() {
    BgpPassivePeerConfig peer = _nf.bgpDynamicNeighborBuilder().setLocalAs(1L).build();
    assertThat(getLocallyBrokenStatus(peer), equalTo(ConfiguredSessionStatus.NO_REMOTE_PREFIX));
  }

  @Test
  public void testPassiveNoRemoteAs() {
    BgpPassivePeerConfig peer =
        _nf.bgpDynamicNeighborBuilder()
            .setLocalAs(1L)
            .setPeerPrefix(Prefix.create(Ip.parse("1.1.1.1"), 24))
            .setRemoteAsns(LongSpace.EMPTY)
            .build();
    assertThat(getLocallyBrokenStatus(peer), equalTo(ConfiguredSessionStatus.NO_REMOTE_AS));
  }

  @Test
  public void testPassiveNotLocallyBroken() {
    BgpPassivePeerConfig peer =
        _nf.bgpDynamicNeighborBuilder()
            .setLocalAs(1L)
            .setPeerPrefix(Prefix.create(Ip.parse("1.1.1.1"), 24))
            .setRemoteAs(1L)
            .build();
    assertThat(getLocallyBrokenStatus(peer), nullValue());
  }
}
