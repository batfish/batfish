package org.batfish.datamodel.tracking;

import static org.batfish.datamodel.tracking.TrackMethods.alwaysFalse;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link TrackAll}. */
final class TrackAllTest {

  @Test
  void testJavaSerialization() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  void testEquals() {
    TrackAll obj = TrackAll.of(ImmutableList.of(alwaysTrue()));
    new EqualsTester()
        .addEqualityGroup(obj, TrackAll.of(ImmutableList.of(alwaysTrue())))
        .addEqualityGroup(TrackAll.of(ImmutableList.of(alwaysFalse())))
        .testEquals();
  }
}
