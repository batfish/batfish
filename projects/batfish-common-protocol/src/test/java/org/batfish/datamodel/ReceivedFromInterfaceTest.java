package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ReceivedFromInterface}. */
public final class ReceivedFromInterfaceTest {

  @Test
  public void testJacksonSerialiation() {
    ReceivedFrom obj = ReceivedFromInterface.of("foo", Ip.parse("169.254.0.1"));
    assertThat(BatfishObjectMapper.clone(obj, ReceivedFrom.class), sameInstance(obj));
  }

  @Test
  public void testJavaSerialization() {
    ReceivedFrom obj = ReceivedFromInterface.of("foo", Ip.parse("169.254.0.1"));
    assertThat(SerializationUtils.clone(obj), sameInstance(obj));
  }

  @Test
  public void testEquals() {
    ReceivedFrom obj = ReceivedFromInterface.of("foo", Ip.parse("169.254.0.1"));
    new EqualsTester()
        .addEqualityGroup(obj, ReceivedFromInterface.of("foo", Ip.parse("169.254.0.1")))
        .addEqualityGroup(ReceivedFromInterface.of("foo", Ip.parse("169.254.0.2")))
        .addEqualityGroup(ReceivedFromInterface.of("bar", Ip.parse("169.254.0.1")))
        .testEquals();
  }
}
