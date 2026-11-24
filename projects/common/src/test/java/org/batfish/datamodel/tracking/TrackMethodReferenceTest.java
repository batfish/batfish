package org.batfish.datamodel.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link TrackMethodReference}. */
final class TrackMethodReferenceTest {
  @Test
  void testJavaSerialization() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  void testEquals() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    new EqualsTester()
        .addEqualityGroup(obj, TrackMethodReference.of("a"))
        .addEqualityGroup(TrackMethodReference.of("b"))
        .testEquals();
  }
}
