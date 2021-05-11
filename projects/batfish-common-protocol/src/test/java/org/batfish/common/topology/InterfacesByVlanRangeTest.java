package org.batfish.common.topology;

import static org.batfish.common.topology.InterfacesByVlanRange.rangesAreCanonicalAndDoNotOverlap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Set;
import org.junit.Test;

/** Tests of {@link InterfacesByVlanRange} */
public class InterfacesByVlanRangeTest {

  @Test
  public void testRangesAreCanonicalAndDoNotOverlap_emptyRange() {
    assertFalse(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of(Range.closedOpen(1, 1))));
  }

  @Test
  public void testRangesAreCanonicalAndDoNotOverlap_noncanonical() {
    assertFalse(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of(Range.closed(1, 10))));
    assertFalse(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of(Range.openClosed(1, 10))));
    assertFalse(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of(Range.open(1, 10))));
  }

  @Test
  public void testRangesAreCanonicalAndDoNotOverlap_overlapping() {
    assertFalse(
        rangesAreCanonicalAndDoNotOverlap(
            ImmutableSet.of(Range.closedOpen(0, 3), Range.closedOpen(2, 6))));
    assertFalse(
        rangesAreCanonicalAndDoNotOverlap(
            ImmutableSet.of(Range.closedOpen(3, 4), Range.closedOpen(0, 8))));
  }

  @Test
  public void testRangesAreCanonicalAndDoNotOverlap() {
    assertTrue(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of()));
    assertTrue(rangesAreCanonicalAndDoNotOverlap(ImmutableSet.of(Range.closedOpen(1, 10))));
    assertTrue(
        rangesAreCanonicalAndDoNotOverlap(
            ImmutableSet.of(
                Range.closedOpen(10, 15), Range.closedOpen(0, 3), Range.closedOpen(3, 4))));
  }

  @Test
  public void testGet() {
    Range<Integer> range = Range.closedOpen(1, 11);
    Set<String> ifaces = ImmutableSet.of("iface1", "iface2");
    InterfacesByVlanRange ifacesByRange = new InterfacesByVlanRange(ImmutableMap.of(range, ifaces));
    for (int i = 1; i <= 10; i++) {
      assertThat(ifacesByRange.get(i), equalTo(ifaces));
    }
    assertThat(ifacesByRange.get(11), empty());
  }

  @Test
  public void testGetRange() {
    Range<Integer> range = Range.closedOpen(1, 11);
    Set<String> ifaces = ImmutableSet.of("iface1", "iface2");
    InterfacesByVlanRange ifacesByRange = new InterfacesByVlanRange(ImmutableMap.of(range, ifaces));
    for (int i = 1; i <= 10; i++) {
      assertThat(ifacesByRange.getRange(i), equalTo(range));
    }
    assertThat(ifacesByRange.getRange(11), nullValue());
  }
}
