package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.Prefix.ZERO;
import static org.batfish.datamodel.RoutingProtocol.HMM;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.tracking.TrackRoute.RibType;
import org.junit.Test;

/** Test of {@link TrackRoute}. */
public final class TrackRouteTest {

  @Test
  public void testJavaSerialization() {
    TrackRoute obj = TrackRoute.of(ZERO, ImmutableSet.of(HMM), RibType.MAIN, "foo");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackRoute obj = TrackRoute.of(ZERO, ImmutableSet.of(HMM), RibType.MAIN, "foo");
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackRoute obj = TrackRoute.of(ZERO, ImmutableSet.of(), RibType.MAIN, "foo");
    new EqualsTester()
        .addEqualityGroup(obj, TrackRoute.of(ZERO, ImmutableSet.of(), RibType.MAIN, "foo"))
        .addEqualityGroup(
            TrackRoute.of(Prefix.strict("192.0.2.0/24"), ImmutableSet.of(), RibType.MAIN, "foo"))
        .addEqualityGroup(TrackRoute.of(ZERO, ImmutableSet.of(HMM), RibType.MAIN, "foo"))
        .addEqualityGroup(TrackRoute.of(ZERO, ImmutableSet.of(), RibType.BGP, "foo"))
        .addEqualityGroup(TrackRoute.of(ZERO, ImmutableSet.of(), RibType.MAIN, "bar"))
        .testEquals();
  }
}
