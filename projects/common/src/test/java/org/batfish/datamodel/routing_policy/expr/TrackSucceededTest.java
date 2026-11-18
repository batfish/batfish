package org.batfish.datamodel.routing_policy.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link TrackSucceeded}. */
final class TrackSucceededTest {

  @Test
  void testJavaSerialization() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    assertEquals(SerializationUtils.clone(obj), obj);
  }

  @Test
  void testJacksonSerialization() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    assertEquals(BatfishObjectMapper.clone(obj, BooleanExpr.class), obj);
  }

  @Test
  void testEquals() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new TrackSucceeded("foo"))
        .addEqualityGroup(new TrackSucceeded("bar"))
        .testEquals();
  }
}
