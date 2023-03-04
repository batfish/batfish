package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.tracking.TrackMethods.alwaysFalse;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TrackAll}. */
public final class TrackAllTest {

  @Test
  public void testJavaSerialization() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    new EqualsTester()
        .addEqualityGroup(obj, TrackAll.of(ImmutableList.of(alwaysTrue())))
        .addEqualityGroup(TrackAll.of(ImmutableList.of(alwaysFalse())))
        .testEquals();
  }
}
