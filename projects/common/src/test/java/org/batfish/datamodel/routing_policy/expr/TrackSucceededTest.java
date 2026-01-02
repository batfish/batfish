package org.batfish.datamodel.routing_policy.expr;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TrackSucceeded}. */
public final class TrackSucceededTest {

  @Test
  public void testJavaSerialization() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    assertEquals(SerializationUtils.clone(obj), obj);
  }

  @Test
  public void testJacksonSerialization() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    assertEquals(BatfishObjectMapper.clone(obj, BooleanExpr.class), obj);
  }

  @Test
  public void testEquals() {
    TrackSucceeded obj = new TrackSucceeded("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new TrackSucceeded("foo"))
        .addEqualityGroup(new TrackSucceeded("bar"))
        .testEquals();
  }
}
