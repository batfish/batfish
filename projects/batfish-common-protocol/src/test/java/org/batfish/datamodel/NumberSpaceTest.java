package org.batfish.datamodel;

import static org.batfish.datamodel.NumberSpace.numSpaceToString;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Range;
import org.junit.Test;

public class NumberSpaceTest {

  private static final NumberSpace SPACE1 = LongSpace.of(Range.closed(0L, 10000L));

  @Test
  public void testNumSpaceToString_none() {
    assertEquals("none", numSpaceToString(LongSpace.EMPTY, SPACE1));
  }

  @Test
  public void testNumSpaceToString_all() {
    assertEquals("all", numSpaceToString(SPACE1, SPACE1));
  }

  @Test
  public void testNumSpaceToString_mid() {
    // if not empty or the complete space, use LongSpace.toString
    LongSpace space = LongSpace.of(Range.closed(10L, 1000L));
    assertEquals(space.toString(), numSpaceToString(space, SPACE1));
  }
}
