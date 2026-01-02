package org.batfish.common.topology;

import static org.batfish.common.topology.InterfacesByVlanRange.isInvalidRange;
import static org.batfish.common.topology.InterfacesByVlanRange.rangesDoNotOverlap;
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
  public void testIsInvalidRange() {
    // Canonical but empty
    assertTrue(isInvalidRange(Range.closedOpen(1, 1)));
    // Not empty but non-canonical
    assertTrue(isInvalidRange(Range.closed(1, 10)));
    assertTrue(isInvalidRange(Range.openClosed(1, 10)));
    assertTrue(isInvalidRange(Range.open(1, 10)));
    // Not empty and canonical
    assertFalse(isInvalidRange(Range.closedOpen(1, 10)));
  }

  @Test
  public void testRangesDoNotOverlap_overlapping() {
    assertFalse(
        rangesDoNotOverlap(ImmutableSet.of(Range.closedOpen(0, 3), Range.closedOpen(2, 6))));
    assertFalse(
        rangesDoNotOverlap(ImmutableSet.of(Range.closedOpen(3, 4), Range.closedOpen(0, 8))));
  }

  @Test
  public void testRangesDoNotOverlap() {
    assertTrue(rangesDoNotOverlap(ImmutableSet.of()));
    assertTrue(rangesDoNotOverlap(ImmutableSet.of(Range.closedOpen(1, 10))));
    assertTrue(
        rangesDoNotOverlap(
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
