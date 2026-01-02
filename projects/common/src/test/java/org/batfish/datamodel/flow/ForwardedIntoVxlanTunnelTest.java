package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ForwardedIntoVxlanTunnel}. */
public final class ForwardedIntoVxlanTunnelTest {

  @Test
  public void testJacksonSerialization() {
    ForwardedIntoVxlanTunnel obj = ForwardedIntoVxlanTunnel.of(1, Ip.parse("1.1.1.1"));
    assertThat(BatfishObjectMapper.clone(obj, ForwardingDetail.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    ForwardedIntoVxlanTunnel obj = ForwardedIntoVxlanTunnel.of(1, Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(obj, ForwardedIntoVxlanTunnel.of(1, Ip.parse("1.1.1.1")))
        .addEqualityGroup(ForwardedIntoVxlanTunnel.of(2, Ip.parse("1.1.1.1")))
        .addEqualityGroup(ForwardedIntoVxlanTunnel.of(1, Ip.parse("2.2.2.2")))
        .testEquals();
  }
}
