package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TrackInterface}. */
public final class TrackInterfaceTest {

  @Test
  public void testJavaSerialization() {
    TrackInterface obj = new TrackInterface("foo");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackInterface obj = new TrackInterface("foo");
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackInterface obj = new TrackInterface("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new TrackInterface("foo"))
        .addEqualityGroup(new TrackInterface("bar"))
        .testEquals();
  }
}
