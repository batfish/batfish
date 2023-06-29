package org.batfish.datamodel.route.nh;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link NextHopBgpPeerAddress}. */
public final class NextHopBgpPeerAddressTest {

  @Test
  public void testJavaSerialiation() {
    NextHopBgpPeerAddress obj = NextHopBgpPeerAddress.instance();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

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
