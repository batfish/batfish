package org.batfish.datamodel.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link TrackReachability}. */
final class TrackReachabilityTest {

  @Test
  void testJavaSerialization() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo", Ip.ZERO);
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo", Ip.ZERO);
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  void testEquals() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo", Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, TrackReachability.of(Ip.ZERO, "foo", Ip.ZERO))
        .addEqualityGroup(TrackReachability.of(Ip.MAX, "foo", Ip.ZERO))
        .addEqualityGroup(TrackReachability.of(Ip.ZERO, "fob", Ip.ZERO))
        .addEqualityGroup(TrackReachability.of(Ip.ZERO, "foo", Ip.parse("192.0.2.1")))
        .testEquals();
  }
}
