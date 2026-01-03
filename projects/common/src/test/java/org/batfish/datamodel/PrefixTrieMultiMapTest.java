package org.batfish.datamodel;

import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.datamodel.PrefixTrieMultiMap.legalLeftChildPrefix;
import static org.batfish.datamodel.PrefixTrieMultiMap.legalRightChildPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.PrefixTrieMultiMap.FoldOperator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link PrefixTrieMultiMap} */
public class PrefixTrieMultiMapTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static List<Prefix> keysInPostOrder(PrefixTrieMultiMap<Integer> map) {
    List<Prefix> prefixes = new ArrayList<>();
    map.traverseEntries((prefix, elems) -> prefixes.add(prefix));
    return prefixes;
  }

  private static List<Prefix> keysInPostOrderFiltered(
      PrefixTrieMultiMap<Integer> map, Predicate<Prefix> prefixFilter) {
    List<Prefix> prefixes = new ArrayList<>();
    map.traverseEntries(
        (prefix, elems) -> prefixes.add(prefix), (prefix, elems) -> prefixFilter.test(prefix));
    return prefixes;
  }

  private static <T> List<Entry<Prefix, Set<T>>> entriesPostOrder(PrefixTrieMultiMap<T> map) {
    List<Entry<Prefix, Set<T>>> entries = new ArrayList<>();
    map.traverseEntries((prefix, elems) -> entries.add(immutableEntry(prefix, elems)));
    return entries;
  }

  @Test
  public void testEquals() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    ptm1.put(Prefix.ZERO, 1);
    PrefixTrieMultiMap<Integer> ptm2 = new PrefixTrieMultiMap<>();
    ptm2.put(Prefix.parse("1.1.1.0/24"), 1);
    PrefixTrieMultiMap<Integer> ptm2b = new PrefixTrieMultiMap<>();
    ptm2b.put(Prefix.parse("1.1.1.0/24"), 2);
    new EqualsTester()
        .addEqualityGroup(new PrefixTrieMultiMap<Integer>(), new PrefixTrieMultiMap<Integer>())
        .addEqualityGroup(ptm1)
        .addEqualityGroup(ptm2)
        .addEqualityGroup(ptm2b)
        .testEquals();
  }

  @Test
  public void testAdd() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    assertTrue("Element was added", ptm1.put(Prefix.ZERO, 1));
    assertThat(ptm1.getAllElements(), contains(1));
    assertThat(ptm1.getNumElements(), equalTo(1));
    assertThat(ptm1.get(Prefix.ZERO), equalTo(ImmutableSet.of(1)));
  }

  @Test
  public void testAddDeeper() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    assertTrue("Element was added", ptm1.put(p, 1));
    assertThat(ptm1.get(p), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.get(Prefix.ZERO), empty());
  }

  @Test
  public void testAddAll() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    ptm1.putAll(Prefix.ZERO, ImmutableSet.of(1, 2, 3));
    assertThat(ptm1.getAllElements(), containsInAnyOrder(1, 2, 3));
    assertThat(ptm1.getNumElements(), equalTo(3));
    assertThat(ptm1.get(Prefix.ZERO), containsInAnyOrder(1, 2, 3));
  }

  @Test
  public void testRemove() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    ptm1.put(Prefix.ZERO, 1);
    assertFalse("Nothing to remove", ptm1.remove(Prefix.ZERO, 2));
    assertTrue("Element removed", ptm1.remove(Prefix.ZERO, 1));
    assertThat(ptm1.getAllElements(), empty());
    assertThat(ptm1.getNumElements(), equalTo(0));
  }

  @Test
  public void testRemoveDeeper() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    Prefix p = Prefix.parse("1.1.1.0/24");
    ptm1.put(p, 1);
    ptm1.put(p, 2);
    assertFalse("Nothing to remove", ptm1.remove(Prefix.ZERO, 2));
    assertTrue("Element removed", ptm1.remove(p, 2));
    assertThat(ptm1.get(Prefix.ZERO), empty());
    assertThat(ptm1.get(p), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.getAllElements(), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.getNumElements(), equalTo(1));
  }

  @Test
  public void testLongestPrefixMatch() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    Prefix p1 = Prefix.parse("1.1.1.0/24");
    Prefix p2 = Prefix.parse("1.1.1.128/25");
    Prefix p3 = Prefix.parse("1.1.1.129/32");
    ptm1.put(p1, 1);
    ptm1.put(p2, 2);
    ptm1.put(p3, 3);
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.1")), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.128")), equalTo(ImmutableSet.of(2)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.128"), 1), empty());
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.129")), equalTo(ImmutableSet.of(3)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.130")), equalTo(ImmutableSet.of(2)));
  }

  @Test
  public void testPutAtRoot() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix prefix = Prefix.parse("128.0.0.0/1");
    map.put(prefix, 3);
    assertThat(keysInPostOrder(map), contains(prefix));
    // true because map is modified
    assertTrue(map.put(Prefix.ZERO, 1));
    assertThat(
        entriesPostOrder(map),
        contains(
            immutableEntry(prefix, ImmutableSet.of(3)),
            immutableEntry(Prefix.ZERO, ImmutableSet.of(1))));
  }

  @Test
  public void testPutAllAtRoot() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix prefix = Prefix.parse("128.0.0.0/1");
    map.put(prefix, 1);
    assertThat(entriesPostOrder(map), contains(immutableEntry(prefix, ImmutableSet.of(1))));
    // true because map is modified
    ImmutableSet<Integer> zeroValues = ImmutableSet.of(1, 2);
    assertTrue(map.putAll(Prefix.ZERO, zeroValues));
    assertThat(
        entriesPostOrder(map),
        contains(
            immutableEntry(prefix, ImmutableSet.of(1)), immutableEntry(Prefix.ZERO, zeroValues)));
  }

  @Test
  public void testRemoveWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>();
    ptm1.put(Prefix.parse("128.0.0.0/1"), 1);
    assertFalse(ptm1.remove(Prefix.ZERO, 1));
  }

  @Test
  public void testReplaceNewRoot() {
    Prefix prefix = Prefix.parse("128.0.0.0/1");
    ImmutableSet<Integer> prefixValues = ImmutableSet.of(4, 5, 6);
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    map.putAll(prefix, prefixValues);
    assertThat(entriesPostOrder(map), contains(immutableEntry(prefix, prefixValues)));

    // true because we modified the map
    assertTrue(map.replaceAll(Prefix.ZERO, 1));

    assertThat(
        entriesPostOrder(map),
        contains(
            immutableEntry(prefix, prefixValues), immutableEntry(Prefix.ZERO, ImmutableSet.of(1))));
  }

  @Test
  public void testLegalLeftChildPrefix() {
    Prefix parent = Prefix.parse("1.0.0.0/8");

    // child prefix cannot equal parent prefix
    assertFalse(legalLeftChildPrefix(parent, parent));

    // shortest possible child prefix
    Prefix child = Prefix.parse("1.0.0.0/9");
    assertTrue(legalLeftChildPrefix(parent, child));

    // 9th bit cannot be 1
    child = Prefix.parse("1.128.0.0/9");
    assertFalse(legalLeftChildPrefix(parent, child));

    // longer prefixes are allowed; everything after the 9th bit can be anything
    child = Prefix.parse("1.127.255.0/24");
    assertTrue(legalLeftChildPrefix(parent, child));
  }

  @Test
  public void testLegalRightChildPrefix() {
    Prefix parent = Prefix.parse("1.0.0.0/8");

    // child prefix cannot equal parent prefix
    assertFalse(legalRightChildPrefix(parent, parent));

    // shortest possible child prefix
    Prefix child = Prefix.parse("1.128.0.0/9");
    assertTrue(legalRightChildPrefix(parent, child));

    // 9th bit cannot be 0
    child = Prefix.parse("1.0.0.0/9");
    assertFalse(legalRightChildPrefix(parent, child));

    // longer prefixes are allowed; everything after the 9th bit can be anything
    child = Prefix.parse("1.255.255.0/24");
    assertTrue(legalRightChildPrefix(parent, child));
  }

  @Test
  public void testTraverseEntries() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    map.put(Prefix.ZERO, 0);
    map.put(l, 0);
    map.put(ll, 0);
    map.put(lr, 0);

    // adding in different order just for fun
    map.put(rr, 0);
    map.put(rl, 0);
    map.put(r, 0);

    List<Prefix> prefixes = keysInPostOrder(map);
    assertThat(prefixes, contains(ll, lr, l, rl, rr, r, Prefix.ZERO));
  }

  @Test
  public void testTraverseEntriesFiltered() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    map.put(l, 0);
    map.put(ll, 0);
    map.put(lr, 0);

    // adding in different order just for fun
    map.put(rr, 0);
    map.put(rl, 0);
    map.put(r, 0);

    List<Prefix> prefixes = keysInPostOrderFiltered(map, prefix -> prefix.getPrefixLength() <= 8);
    assertThat(prefixes, contains(l, r));
  }

  @Test
  public void testTraverseEntriesFilteredNodeVisitedNoChildren() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    map.put(Prefix.ZERO, 1);

    List<Prefix> prefixes = keysInPostOrderFiltered(map, prefix -> prefix.getPrefixLength() <= 8);
    assertThat(prefixes, contains(Prefix.ZERO));
  }

  @Test
  public void testTraverseEntriesFilteredNodeVisitedMixedChildren() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix r = Prefix.parse("128.0.0.0/16");
    map.put(Prefix.ZERO, 0);
    map.put(l, 0);
    map.put(r, 0);

    List<Prefix> prefixes = keysInPostOrderFiltered(map, prefix -> prefix.getPrefixLength() <= 8);
    assertThat(prefixes, contains(l, Prefix.ZERO));
  }

  @Test
  public void testTraverseEntriesFilteredNodeNotVisitedValidChild() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    map.put(l, 0);
    map.put(ll, 0);

    List<Prefix> prefixes = keysInPostOrderFiltered(map, prefix -> prefix.getPrefixLength() != 8);
    assertThat(prefixes, empty());
  }

  @Test
  public void testTraverseEntriesFilteredRootNotVisitedValidChild() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    map.put(Prefix.ZERO, 0);
    map.put(l, 0);

    List<Prefix> prefixes = keysInPostOrderFiltered(map, prefix -> prefix.getPrefixLength() != 0);
    assertThat(prefixes, empty());
  }

  @Test
  public void testPutWithCombineAtRoot() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    assertThat(entriesPostOrder(map), empty());

    Prefix l = Prefix.parse("127.0.0.0/8");
    Prefix r = Prefix.parse("128.0.0.0/8");
    map.put(l, 1);
    map.put(r, 2);
    assertThat(
        entriesPostOrder(map),
        contains(immutableEntry(l, ImmutableSet.of(1)), immutableEntry(r, ImmutableSet.of(2))));
  }

  @Test
  public void testPutWithCombineInternal() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    assertThat(entriesPostOrder(map), empty());

    Prefix l = Prefix.parse("0.127.0.0/16");
    Prefix r = Prefix.parse("0.128.0.0/16");
    map.put(l, 1);
    map.put(r, 2);
    assertThat(
        entriesPostOrder(map),
        contains(immutableEntry(l, ImmutableSet.of(1)), immutableEntry(r, ImmutableSet.of(2))));
  }

  @Test
  public void test() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    assertThat(entriesPostOrder(map), empty());

    map.put(Prefix.ZERO, 0);
    assertThat(entriesPostOrder(map), contains(immutableEntry(Prefix.ZERO, ImmutableSet.of(0))));

    Prefix l = Prefix.parse("0.0.0.0/32");
    Prefix r = Prefix.parse("0.0.0.1/32");
    map.put(l, 1);
    map.put(r, 2);
    assertThat(
        entriesPostOrder(map),
        contains(
            immutableEntry(l, ImmutableSet.of(1)),
            immutableEntry(r, ImmutableSet.of(2)),
            immutableEntry(Prefix.ZERO, ImmutableSet.of(0))));

    // Since the entry for 0.0.0.0/31 has no elements, return the elements for Prefix.ZERO
    assertThat(map.longestPrefixMatch(Ip.parse("0.0.0.0"), 31), equalTo(ImmutableSet.of(0)));
  }

  @Test
  public void testClear() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    map.put(l, 0);
    map.put(ll, 0);
    map.put(lr, 0);
    map.put(rr, 0);
    map.put(rl, 0);
    map.put(r, 0);
    map.clear();

    assertThat(map.getAllElements(), hasSize(0));
    assertThat(map.getNumElements(), equalTo(0));
  }

  @Test
  public void testFold() {
    // Use a fold to construct a postorder list of prefixes
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    map.put(l, 0);
    map.put(ll, 0);
    map.put(lr, 0);

    // adding in different order just for fun
    map.put(rr, 0);
    map.put(rl, 0);
    map.put(r, 0);

    List<Prefix> prefixes =
        map.fold(
            new FoldOperator<Integer, List<Prefix>>() {
              @Override
              public @Nonnull List<Prefix> fold(
                  Prefix prefix,
                  Set<Integer> elems,
                  @Nullable List<Prefix> leftResult,
                  @Nullable List<Prefix> rightResult) {
                List<Prefix> result = new ArrayList<>();
                if (leftResult != null) {
                  result.addAll(leftResult);
                }
                if (rightResult != null) {
                  result.addAll(rightResult);
                }
                result.add(prefix);
                return result;
              }
            });

    assertThat(prefixes, contains(ll, lr, l, rl, rr, r, Prefix.ZERO));
  }

  @Test
  public void testIntersectsPrefixRange() {
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>();
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    // empty map does not crash
    map.intersectsPrefixSpace(new PrefixSpace(PrefixRange.fromPrefix(l)));

    // proper tests
    map.put(l, 0);
    map.put(ll, 0);
    map.put(lr, 0);
    map.put(rr, 0);
    map.put(rl, 0);
    map.put(r, 0);

    // prefixes exist with proper length range, but do not match
    assertFalse(
        map.intersectsPrefixSpace(
            new PrefixSpace(PrefixRange.fromPrefix(Prefix.strict("1.128.0.0/16")))));
    assertFalse(
        map.intersectsPrefixSpace(
            new PrefixSpace(PrefixRange.fromPrefix(Prefix.strict("0.192.0.0/16")))));

    // full matches exist but are too short
    assertFalse(
        map.intersectsPrefixSpace(
            new PrefixSpace(PrefixRange.fromPrefix(Prefix.strict("128.128.0.0/24")))));
    // full matches exist but are too long
    assertFalse(map.intersectsPrefixSpace(new PrefixSpace(PrefixRange.fromPrefix(Prefix.ZERO))));
    assertFalse(
        map.intersectsPrefixSpace(
            new PrefixSpace(new PrefixRange(Prefix.ZERO, SubRange.singleton(4)))));

    // matched by 128.0.0.0/16
    assertTrue(
        map.intersectsPrefixSpace(
            new PrefixSpace(PrefixRange.fromPrefix(Prefix.strict("128.0.0.0/16")))));
    assertTrue(
        map.intersectsPrefixSpace(
            new PrefixSpace(new PrefixRange(Prefix.strict("128.0.0.0/12"), new SubRange(14, 18)))));
  }

  @Test
  public void testGetOverlappingEntries() {
    PrefixTrieMultiMap<Prefix> trie = new PrefixTrieMultiMap<>();

    Prefix p111 = Prefix.parse("1.1.1.0/24");
    Prefix p123 = Prefix.parse("1.2.3.0/24");
    Prefix p1234 = Prefix.parse("1.2.3.4/32");
    Prefix p222 = Prefix.parse("2.2.2.0/24");

    trie.put(p111, p111);
    trie.put(p123, p123);
    trie.put(p1234, p1234);
    trie.put(p222, p222);

    // for this test, getOverlappingEntries should always return entries whose value is a singleton
    // set containing the key. call it, check that invariant, and then return the keys.
    Function<String, Set<Prefix>> getOverlappingKeys =
        (prefixStr) -> {
          Prefix prefix = Prefix.parse(prefixStr);
          return trie.getOverlappingEntries(toRangeSet(prefix))
              .map(
                  entry -> {
                    assertThat(entry.getValue(), equalTo(ImmutableSet.of(entry.getKey())));
                    return entry.getKey();
                  })
              .collect(ImmutableSet.toImmutableSet());
        };

    assertEquals(ImmutableSet.of(p123, p1234), getOverlappingKeys.apply("1.2.0.0/16"));
    assertEquals(ImmutableSet.of(p111), getOverlappingKeys.apply("1.1.0.0/16"));
    assertEquals(ImmutableSet.of(p111), getOverlappingKeys.apply("1.1.1.1/32"));
    assertEquals(ImmutableSet.of(p123, p1234), getOverlappingKeys.apply("1.2.3.4/32"));
    assertEquals(ImmutableSet.of(p123), getOverlappingKeys.apply("1.2.3.5/32"));
  }

  @Test
  public void testSerialization() {
    PrefixTrieMultiMap<Integer> ptmm = new PrefixTrieMultiMap<>();
    int i = 0;
    ptmm.put(Prefix.ZERO, ++i);
    ptmm.put(Prefix.parse("10.0.0.0/24"), ++i);
    ptmm.put(Prefix.parse("10.0.0.1/24"), ++i);
    ptmm.put(Prefix.parse("10.0.0.0/26"), ++i);
    ptmm.put(Prefix.parse("10.0.0.0/26"), ++i);
    ptmm.put(Prefix.parse("10.0.0.64/26"), ++i);
    ptmm.put(Prefix.parse("20.0.0.0/8"), ++i);
    ptmm.put(Prefix.parse("192.168.0.0/16"), ++i);
    assertThat(ptmm, equalTo(SerializationUtils.clone(ptmm)));
  }

  private static RangeSet<Ip> toRangeSet(Prefix prefix) {
    return ImmutableRangeSet.of(Range.closed(prefix.getStartIp(), prefix.getEndIp()));
  }
}
