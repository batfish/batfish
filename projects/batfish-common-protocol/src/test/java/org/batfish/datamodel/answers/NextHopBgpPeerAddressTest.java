package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link NextHopBgpPeerAddress}. */
public final class NextHopBgpPeerAddressTest {

  @Test
  public void testJacksonSerialization() {
    NextHopBgpPeerAddress obj = NextHopBgpPeerAddress.instance();
    assertThat(BatfishObjectMapper.clone(obj, NextHopResult.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NextHopBgpPeerAddress obj = NextHopBgpPeerAddress.instance();
    new EqualsTester().addEqualityGroup(obj, NextHopBgpPeerAddress.instance()).testEquals();
  }
}
