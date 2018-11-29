package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.getLocallyBrokenStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.junit.Before;
import org.junit.Test;

/** Tests of static methods of {@link BgpSessionAnswerer} */
public class BgpSessionAnswererTest {

  private NetworkFactory _nf;

  @Before
  public void createNetworkFactory() {
    _nf = new NetworkFactory();
  }

  @Test
  public void testLocalIpUnknownStatically() {
    BgpActivePeerConfig peer = _nf.bgpNeighborBuilder().setPeerAddress(new Ip("1.1.1.1")).build();
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
    BgpActivePeerConfig peer = _nf.bgpNeighborBuilder().setLocalIp(new Ip("1.1.1.1")).build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_LOCAL_AS);
  }

  @Test
  public void testNoRemoteIp() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder().setLocalIp(new Ip("1.1.1.1")).setLocalAs(1L).build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_IP);
  }

  @Test
  public void testNoRemoteAs() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder()
            .setPeerAddress(new Ip("1.1.1.1"))
            .setLocalIp(new Ip("2.2.2.2"))
            .setLocalAs(1L)
            .build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_AS);
  }

  @Test
  public void testNotLocallyBroken() {
    BgpActivePeerConfig peer =
        _nf.bgpNeighborBuilder()
            .setPeerAddress(new Ip("1.1.1.1"))
            .setLocalIp(new Ip("2.2.2.2"))
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
            .setPeerPrefix(new Prefix(new Ip("1.1.1.1"), 24))
            .setRemoteAs(ImmutableList.of())
            .build();
    assertThat(getLocallyBrokenStatus(peer), equalTo(ConfiguredSessionStatus.NO_REMOTE_AS));
  }

  @Test
  public void testPassiveNotLocallyBroken() {
    BgpPassivePeerConfig peer =
        _nf.bgpDynamicNeighborBuilder()
            .setLocalAs(1L)
            .setPeerPrefix(new Prefix(new Ip("1.1.1.1"), 24))
            .setRemoteAs(ImmutableList.of(1L))
            .build();
    assertThat(getLocallyBrokenStatus(peer), nullValue());
  }
}
