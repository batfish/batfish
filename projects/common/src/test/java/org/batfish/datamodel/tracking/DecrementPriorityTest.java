package org.batfish.datamodel.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link DecrementPriority}. */
final class DecrementPriorityTest {
  @Test
  void testJavaSerialization() {
    DecrementPriority obj = new DecrementPriority(1);
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    DecrementPriority obj = new DecrementPriority(1);
    assertEquals(obj, BatfishObjectMapper.clone(obj, TrackAction.class));
  }

  @Test
  void testEquals() {
    DecrementPriority obj = new DecrementPriority(1);
    new EqualsTester()
        .addEqualityGroup(obj, new DecrementPriority(1))
        .addEqualityGroup(new DecrementPriority(2))
        .testEquals();
  }
}
