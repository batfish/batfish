package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.getLocallyBrokenStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Prefix;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;
import org.junit.Test;

/** Tests of {@link BgpSessionStatusAnswerer} */
public class BgpSessionStatusAnswererTest {

  @Test
  public void testLocalIpUnknownStatically() {
    assertThat(
        getLocallyBrokenStatus(
            new BgpPeerConfig(Prefix.parse("1.1.1.1/32"), null, true), SessionType.EBGP_SINGLEHOP),
        equalTo(SessionStatus.DYNAMIC_LISTEN));
    assertThat(
        getLocallyBrokenStatus(
            new BgpPeerConfig(Prefix.parse("1.1.1.1/32"), null, false), SessionType.EBGP_SINGLEHOP),
        equalTo(SessionStatus.NO_LOCAL_IP));
    assertThat(
        getLocallyBrokenStatus(
            new BgpPeerConfig(Prefix.parse("1.1.1.1/32"), null, false), SessionType.EBGP_MULTIHOP),
        equalTo(SessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
    assertThat(
        getLocallyBrokenStatus(
            new BgpPeerConfig(Prefix.parse("1.1.1.1/32"), null, false), SessionType.IBGP),
        equalTo(SessionStatus.LOCAL_IP_UNKNOWN_STATICALLY));
  }
}
