package org.batfish.minesweeper.question.searchroutepolicies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Range;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.LongSpace.Builder;
import org.junit.Test;

/** Tests for {@link BgpRouteConstraints}. */
public class BgpRouteConstraintsTest {

  @Test
  public void testProcessBuilder() {

    Range<Long> include = Range.closed(4L, 44L);
    Range<Long> exclude = Range.singleton(43L);

    // b3 only has exclusions and so will be transformed by processBuilder; the others will not
    Builder b1 = null;
    Builder b2 = LongSpace.builder().including(include);
    Builder b3 = LongSpace.builder().excluding(exclude);
    Builder b4 = LongSpace.builder().including(include).excluding(exclude);

    LongSpace b3Expected =
        LongSpace.builder().including(Range.closed(0L, 4294967295L)).excluding(exclude).build();

    assertNull(BgpRouteConstraints.processBuilder(b1));
    assertEquals(b2.build(), BgpRouteConstraints.processBuilder(b2));
    assertEquals(b3Expected, BgpRouteConstraints.processBuilder(b3));
    assertEquals(b4.build(), BgpRouteConstraints.processBuilder(b4));
  }

  @Test
  public void testIs32BitRange() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(4L, 44L)).build();
    // negative numbers are not allowed
    LongSpace s2 = LongSpace.builder().including(Range.closed(-4L, 44L)).build();
    // numbers higher than 2^32 - 1 are not allowed
    LongSpace s3 = LongSpace.builder().including(Range.singleton(4294967296L)).build();

    assertTrue(BgpRouteConstraints.is32BitRange(s1));
    assertFalse(BgpRouteConstraints.is32BitRange(s2));
    assertFalse(BgpRouteConstraints.is32BitRange(s3));
  }
}
