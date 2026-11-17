package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link ReceivedFromIp}. */
final class ReceivedFromIpTest {

  @Test
  void testJacksonSerialiation() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    assertThat(BatfishObjectMapper.clone(obj, ReceivedFrom.class), sameInstance(obj));
  }

  @Test
  void testJavaSerialization() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    assertThat(SerializationUtils.clone(obj), sameInstance(obj));
  }

  @Test
  void testEquals() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    new EqualsTester()
        .addEqualityGroup(obj, ReceivedFromIp.of(Ip.parse("192.0.2.1")))
        .addEqualityGroup(ReceivedFromIp.of(Ip.parse("192.0.2.2")))
        .testEquals();
  }
}
