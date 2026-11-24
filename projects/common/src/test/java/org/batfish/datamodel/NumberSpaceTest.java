package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;

/** Tests of {@link NumberSpace} */
class NumberSpaceTest {

  private static final LongSpace SPACE1 = LongSpace.of(Range.closed(0L, 10000L));

  @Test
  void testToStringAsSubsetOf_none() {
    assertEquals("none", LongSpace.EMPTY.toStringAsSubsetOf(SPACE1));
  }

  @Test
  void testToStringAsSubsetOf_all() {
    assertEquals("all", SPACE1.toStringAsSubsetOf(SPACE1));
  }

  @Test
  void testToStringAsSubsetOf_mid() {
    // if not empty or the complete space, use LongSpace.toString
    LongSpace space = LongSpace.of(Range.closed(10L, 1000L));
    assertEquals(space.toString(), space.toStringAsSubsetOf(SPACE1));
  }

  @Test
  void testToStringAsSubsetOf_invalid() {
    assertThrows(IllegalArgumentException.class, () -> SPACE1.toStringAsSubsetOf(LongSpace.EMPTY));
  }
}
