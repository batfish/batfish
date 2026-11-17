package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link NextHopInterface}. */
final class NextHopInterfaceTest {

  @Test
  void testJavaSerialiation() {
    NextHopInterface obj = NextHopInterface.of("foo", Ip.parse("1.1.1.1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testJacksonSerialization() {
    NextHopInterface obj = NextHopInterface.of("foo", Ip.parse("1.1.1.1"));
    assertThat(BatfishObjectMapper.clone(obj, NextHop.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    NextHopInterface obj = NextHopInterface.of("foo");
    new EqualsTester()
        .addEqualityGroup(obj, NextHopInterface.of("foo"))
        .addEqualityGroup(NextHopInterface.of("bar"))
        .addEqualityGroup(NextHopInterface.of("bar", Ip.parse("1.1.1.1")))
        .testEquals();
  }
}
