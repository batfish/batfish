package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link NodeInterfacePairsByVlanRange} */
public class NodeInterfacePairsByVlanRangeTest {
  private NodeInterfacePairsByVlanRange _nodeInterfacePairsByVlanRange;

  private static final NodeInterfacePair NI_1 = NodeInterfacePair.of("c1", "i1");
  private static final NodeInterfacePair NI_2 = NodeInterfacePair.of("c1", "i2");
  private static final NodeInterfacePair NI_3 = NodeInterfacePair.of("c1", "i3");

  @Before
  public void setUp() {
    _nodeInterfacePairsByVlanRange = NodeInterfacePairsByVlanRange.create();
  }

  @Test
  public void testStartsEmpty() {
    assertThat(_nodeInterfacePairsByVlanRange.asMap(), anEmptyMap());
  }

  @Test
  public void testAddToEmpty() {
    Range<Integer> range = Range.closed(1, 2);
    Range<Integer> canonical = Range.closedOpen(1, 3);
    ImmutableSet<NodeInterfacePair> nis =
        ImmutableSet.of(NodeInterfacePair.of("c1", "i1"), NodeInterfacePair.of("c1", "i2"));
    _nodeInterfacePairsByVlanRange.add(range, nis);
    assertThat(_nodeInterfacePairsByVlanRange.asMap(), hasEntry(canonical, nis));
  }

  @Test
  public void testAddWithIntersection() {
    Range<Integer> range1 = Range.closed(1, 10);
    Range<Integer> range2 = Range.closed(5, 20);
    NodeInterfacePair ni1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair ni2 = NodeInterfacePair.of("c1", "i2");
    ImmutableSet<NodeInterfacePair> set1 = ImmutableSet.of(ni1);
    ImmutableSet<NodeInterfacePair> set2 = ImmutableSet.of(ni2);
    _nodeInterfacePairsByVlanRange.add(range1, set1);
    _nodeInterfacePairsByVlanRange.add(range2, set2);
    assertThat(
        _nodeInterfacePairsByVlanRange.asMap(),
        allOf(
            hasEntry(Range.closedOpen(1, 5), set1),
            hasEntry(Range.closedOpen(5, 11), ImmutableSet.of(ni1, ni2)),
            hasEntry(Range.closedOpen(11, 21), set2)));
  }

  @Test
  public void testAddDoubleIntersection() {
    Range<Integer> range1 = Range.closed(1, 10);
    Range<Integer> range2 = Range.closed(20, 30);
    Range<Integer> range3 = Range.closed(5, 25);
    ImmutableSet<NodeInterfacePair> set1 = ImmutableSet.of(NI_1);
    ImmutableSet<NodeInterfacePair> set2 = ImmutableSet.of(NI_2);
    ImmutableSet<NodeInterfacePair> set3 = ImmutableSet.of(NI_3);
    _nodeInterfacePairsByVlanRange.add(range1, set1);
    _nodeInterfacePairsByVlanRange.add(range2, set2);
    _nodeInterfacePairsByVlanRange.add(range3, set3);
    assertThat(
        _nodeInterfacePairsByVlanRange.asMap(),
        allOf(
            hasEntry(Range.closedOpen(1, 5), set1),
            hasEntry(Range.closedOpen(5, 11), ImmutableSet.of(NI_1, NI_3)),
            hasEntry(Range.closedOpen(11, 20), set3),
            hasEntry(Range.closedOpen(20, 26), ImmutableSet.of(NI_2, NI_3)),
            hasEntry(Range.closedOpen(26, 31), set2)));
  }

  @Test
  public void testAddSubset() {
    Range<Integer> range1 = Range.closed(1, 20);
    Range<Integer> range2 = Range.closed(5, 10);
    ImmutableSet<NodeInterfacePair> set1 = ImmutableSet.of(NI_1);
    ImmutableSet<NodeInterfacePair> set2 = ImmutableSet.of(NI_2);
    _nodeInterfacePairsByVlanRange.add(range1, set1);
    _nodeInterfacePairsByVlanRange.add(range2, set2);
    assertThat(
        _nodeInterfacePairsByVlanRange.asMap(),
        allOf(
            hasEntry(Range.closedOpen(1, 5), set1),
            hasEntry(Range.closedOpen(5, 11), ImmutableSet.of(NI_1, NI_2)),
            hasEntry(Range.closedOpen(11, 21), set1)));
  }
}
