package org.batfish.datamodel.tracking;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TrackTrue}. */
public final class TrackTrueTest {

  @Test
  public void testJavaSerialization() {
    TrackTrue obj = TrackTrue.instance();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    TrackTrue obj = TrackTrue.instance();
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackMethod.class));
  }

  @Test
  public void testEquals() {
    TrackTrue obj = TrackTrue.instance();
    new EqualsTester().addEqualityGroup(obj, TrackTrue.instance()).testEquals();
  }
}
