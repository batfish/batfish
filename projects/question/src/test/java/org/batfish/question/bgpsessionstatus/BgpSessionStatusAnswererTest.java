package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.getLocallyBrokenStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpActivePeerConfig.Builder;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.junit.Test;

/** Tests of {@link BgpSessionStatusAnswerer} */
public class BgpSessionStatusAnswererTest {

  @Test
  public void testLocalIpUnknownStatically() {
    NetworkFactory nf = new NetworkFactory();
    Builder bgpb = nf.bgpNeighborBuilder();
    assertThat(
        getLocallyBrokenStatus(
            bgpb.setPeerAddress(new Ip("1.1.1.1")).build(),
            BgpSessionProperties.SessionType.EBGP_SINGLEHOP),
        equalTo(SessionStatus.NO_LOCAL_IP));
    assertThat(
        getLocallyBrokenStatus(
            bgpb.setPeerAddress(new Ip("1.1.1.1")).build(),
            BgpSessionProperties.SessionType.EBGP_MULTIHOP),
        equalTo(SessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
    assertThat(
        getLocallyBrokenStatus(
            bgpb.setPeerAddress(new Ip("1.1.1.1")).build(), BgpSessionProperties.SessionType.IBGP),
        equalTo(SessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
  }
}
