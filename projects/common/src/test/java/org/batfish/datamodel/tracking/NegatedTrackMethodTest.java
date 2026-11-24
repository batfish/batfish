package org.batfish.datamodel.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link NegatedTrackMethod}. */
final class NegatedTrackMethodTest {
  @Test
  void testJavaSerialization() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  void testEquals() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    new EqualsTester()
        .addEqualityGroup(obj, NegatedTrackMethod.of(TrackMethodReference.of("a")))
        .addEqualityGroup(NegatedTrackMethod.of(TrackMethodReference.of("b")))
        .testEquals();
  }
}
