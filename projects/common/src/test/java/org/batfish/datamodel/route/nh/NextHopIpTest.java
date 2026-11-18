package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link NextHopIp}. */
final class NextHopIpTest {

  @Test
  void testJavaSerialiation() {
    NextHopIp obj = NextHopIp.of(Ip.parse("1.1.1.1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testJacksonSerialization() {
    NextHopIp obj = NextHopIp.of(Ip.parse("1.1.1.1"));
    assertThat(BatfishObjectMapper.clone(obj, NextHop.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    NextHopIp obj = NextHopIp.of(Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(obj, NextHopIp.of(Ip.parse("1.1.1.1")))
        .addEqualityGroup(NextHopIp.of(Ip.parse("2.2.2.2")))
        .testEquals();
  }
}
