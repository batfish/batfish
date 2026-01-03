package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link NegatedTrackMethod}. */
public final class NegatedTrackMethodTest {
  @Test
  public void testJavaSerialization() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    NegatedTrackMethod obj = NegatedTrackMethod.of(TrackMethodReference.of("a"));
    new EqualsTester()
        .addEqualityGroup(obj, NegatedTrackMethod.of(TrackMethodReference.of("a")))
        .addEqualityGroup(NegatedTrackMethod.of(TrackMethodReference.of("b")))
        .testEquals();
  }
}
