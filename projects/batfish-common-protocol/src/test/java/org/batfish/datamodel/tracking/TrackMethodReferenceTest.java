package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TrackMethodReference}. */
public final class TrackMethodReferenceTest {
  @Test
  public void testJavaSerialization() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackMethodReference obj = TrackMethodReference.of("a");
    new EqualsTester()
        .addEqualityGroup(obj, TrackMethodReference.of("a"))
        .addEqualityGroup(TrackMethodReference.of("b"))
        .testEquals();
  }
}
