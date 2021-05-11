package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Map;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link NodeInterfacePairsByVlanRange} */
public class NodeInterfacePairsByVlanRangeTest {
  private NodeInterfacePairsByVlanRange _nodeInterfacePairsByVlanRange;

  private static final NodeInterfacePair NI1 = NodeInterfacePair.of("c1", "i1");
  private static final NodeInterfacePair NI2 = NodeInterfacePair.of("c1", "i2");
  private static final NodeInterfacePair NI3 = NodeInterfacePair.of("c1", "i3");

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
    ImmutableSet<NodeInterfacePair> set1 = ImmutableSet.of(NI1);
    ImmutableSet<NodeInterfacePair> set2 = ImmutableSet.of(NI2);
    ImmutableSet<NodeInterfacePair> set3 = ImmutableSet.of(NI3);
    _nodeInterfacePairsByVlanRange.add(range1, set1);
    _nodeInterfacePairsByVlanRange.add(range2, set2);
    _nodeInterfacePairsByVlanRange.add(range3, set3);
    assertThat(
        _nodeInterfacePairsByVlanRange.asMap(),
        allOf(
            hasEntry(Range.closedOpen(1, 5), set1),
            hasEntry(Range.closedOpen(5, 11), ImmutableSet.of(NI1, NI3)),
            hasEntry(Range.closedOpen(11, 20), set3),
            hasEntry(Range.closedOpen(20, 26), ImmutableSet.of(NI2, NI3)),
            hasEntry(Range.closedOpen(26, 31), set2)));
  }

  @Test
  public void testAddSubset() {
    Range<Integer> range1 = Range.closed(1, 20);
    Range<Integer> range2 = Range.closed(5, 10);
    ImmutableSet<NodeInterfacePair> set1 = ImmutableSet.of(NI1);
    ImmutableSet<NodeInterfacePair> set2 = ImmutableSet.of(NI2);
    _nodeInterfacePairsByVlanRange.add(range1, set1);
    _nodeInterfacePairsByVlanRange.add(range2, set2);
    assertThat(
        _nodeInterfacePairsByVlanRange.asMap(),
        allOf(
            hasEntry(Range.closedOpen(1, 5), set1),
            hasEntry(Range.closedOpen(5, 11), ImmutableSet.of(NI1, NI2)),
            hasEntry(Range.closedOpen(11, 21), set1)));
  }

  @Test
  public void testSplitByNode_empty() {
    assertThat(_nodeInterfacePairsByVlanRange.splitByNode(), anEmptyMap());
  }

  @Test
  public void testSplitByNode() {
    NodeInterfacePair otherNodeNi = NodeInterfacePair.of("c2", "i1");
    Range<Integer> range1 = Range.closedOpen(1, 2);
    Range<Integer> range2 = Range.closedOpen(2, 3);
    _nodeInterfacePairsByVlanRange.add(range1, ImmutableList.of(NI1, otherNodeNi));
    _nodeInterfacePairsByVlanRange.add(range2, ImmutableList.of(NI2, NI3));
    Map<String, InterfacesByVlanRange> byNode = _nodeInterfacePairsByVlanRange.splitByNode();

    // note that NI1, NI2, and NI3 all have the same hostname
    assertThat(byNode.keySet(), containsInAnyOrder(NI1.getHostname(), otherNodeNi.getHostname()));
    assertThat(
        byNode.get(NI1.getHostname()).getMap(),
        equalTo(
            ImmutableMap.of(
                range1,
                ImmutableSet.of(NI1.getInterface()),
                range2,
                ImmutableSet.of(NI2.getInterface(), NI3.getInterface()))));
    assertThat(
        byNode.get(otherNodeNi.getHostname()).getMap(),
        equalTo(ImmutableMap.of(range1, ImmutableSet.of(otherNodeNi.getInterface()))));
  }
}
