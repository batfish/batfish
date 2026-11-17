package org.batfish.datamodel.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link TrackTrue}. */
final class TrackTrueTest {

  @Test
  void testJavaSerialization() {
    TrackTrue obj = TrackTrue.instance();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    TrackTrue obj = TrackTrue.instance();
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  void testEquals() {
    TrackTrue obj = TrackTrue.instance();
    new EqualsTester().addEqualityGroup(obj, TrackTrue.instance()).testEquals();
  }
}
