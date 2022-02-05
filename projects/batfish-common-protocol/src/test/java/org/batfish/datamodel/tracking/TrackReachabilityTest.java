package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link TrackReachability}. */
public final class TrackReachabilityTest {

  @Test
  public void testJavaSerialization() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo");
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackReachability obj = TrackReachability.of(Ip.ZERO, "foo");
    new EqualsTester()
        .addEqualityGroup(obj, TrackReachability.of(Ip.ZERO, "foo"))
        .addEqualityGroup(TrackReachability.of(Ip.MAX, "foo"))
        .addEqualityGroup(TrackReachability.of(Ip.ZERO, "bar"))
        .testEquals();
  }
}
