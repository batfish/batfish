package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.getLocallyBrokenStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.junit.Test;

/** Tests of static methods of {@link BgpSessionAnswerer} */
public class BgpSessionAnswererTest {

  @Test
  public void testLocalIpUnknownStatically() {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig peer = nf.bgpNeighborBuilder().setPeerAddress(new Ip("1.1.1.1")).build();
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
  public void testNoRemoteIp() {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig peer = nf.bgpNeighborBuilder().setLocalIp(new Ip("1.1.1.1")).build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_IP);
  }

  @Test
  public void testNoRemoteAs() {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig peer =
        nf.bgpNeighborBuilder()
            .setPeerAddress(new Ip("1.1.1.1"))
            .setLocalIp(new Ip("2.2.2.2"))
            .build();
    assertStatusMatchesForAllSessionTypes(peer, ConfiguredSessionStatus.NO_REMOTE_AS);
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
}
