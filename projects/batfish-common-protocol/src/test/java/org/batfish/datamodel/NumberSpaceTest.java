package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Range;
import org.junit.Test;

/** Tests of {@link NumberSpace} */
public class NumberSpaceTest {

  private static final LongSpace SPACE1 = LongSpace.of(Range.closed(0L, 10000L));

  @Test
  public void testToStringAsSubsetOf_none() {
    assertEquals("none", LongSpace.EMPTY.toStringAsSubsetOf(SPACE1));
  }

  @Test
  public void testToStringAsSubsetOf_all() {
    assertEquals("all", SPACE1.toStringAsSubsetOf(SPACE1));
  }

  @Test
  public void testToStringAsSubsetOf_mid() {
    // if not empty or the complete space, use LongSpace.toString
    LongSpace space = LongSpace.of(Range.closed(10L, 1000L));
    assertEquals(space.toString(), space.toStringAsSubsetOf(SPACE1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToStringAsSubsetOf_invalid() {
    SPACE1.toStringAsSubsetOf(LongSpace.EMPTY);
  }
}
