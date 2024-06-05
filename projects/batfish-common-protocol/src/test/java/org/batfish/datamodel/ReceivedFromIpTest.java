package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ReceivedFromIp}. */
public final class ReceivedFromIpTest {

  @Test
  public void testJacksonSerialiation() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    assertThat(BatfishObjectMapper.clone(obj, ReceivedFrom.class), sameInstance(obj));
  }

  @Test
  public void testJavaSerialization() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    assertThat(SerializationUtils.clone(obj), sameInstance(obj));
  }

  @Test
  public void testEquals() {
    ReceivedFrom obj = ReceivedFromIp.of(Ip.parse("192.0.2.1"));
    new EqualsTester()
        .addEqualityGroup(obj, ReceivedFromIp.of(Ip.parse("192.0.2.1")))
        .addEqualityGroup(ReceivedFromIp.of(Ip.parse("192.0.2.2")))
        .testEquals();
  }
}
