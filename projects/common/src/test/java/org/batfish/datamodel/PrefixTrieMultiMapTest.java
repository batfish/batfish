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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
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

  /** A naive model of the multimap, for differential testing. */
  private static final class Model {
    private final Map<Prefix, Set<Integer>> _map = new HashMap<>();

    boolean put(Prefix p, int e) {
      return _map.computeIfAbsent(p, k -> new HashSet<>()).add(e);
    }

    boolean putAll(Prefix p, Collection<Integer> es) {
      return _map.computeIfAbsent(p, k -> new HashSet<>()).addAll(es);
    }

    boolean remove(Prefix p, int e) {
      Set<Integer> s = _map.get(p);
      return s != null && s.remove(e);
    }

    boolean replaceAll(Prefix p, int e) {
      Set<Integer> old = _map.put(p, new HashSet<>(ImmutableSet.of(e)));
      return old == null || !old.equals(ImmutableSet.of(e));
    }

    Set<Integer> get(Prefix p) {
      return _map.getOrDefault(p, ImmutableSet.of());
    }

    Set<Integer> longestPrefixMatch(Ip ip, int maxLen) {
      Prefix best = null;
      for (Map.Entry<Prefix, Set<Integer>> e : _map.entrySet()) {
        Prefix p = e.getKey();
        if (e.getValue().isEmpty() || p.getPrefixLength() > maxLen || !p.containsIp(ip)) {
          continue;
        }
        if (best == null || p.getPrefixLength() > best.getPrefixLength()) {
          best = p;
        }
      }
      return best == null ? ImmutableSet.of() : _map.get(best);
    }

    Set<Integer> allElements() {
      return _map.values().stream().flatMap(Set::stream).collect(ImmutableSet.toImmutableSet());
    }

    int numElements() {
      return _map.values().stream().mapToInt(Set::size).sum();
    }

    /** Post-order over a prefix trie is ascending end IP, then descending start IP. */
    List<Prefix> keysInPostOrder() {
      return _map.entrySet().stream()
          .filter(e -> !e.getValue().isEmpty())
          .map(Map.Entry::getKey)
          .sorted(
              Comparator.comparing(Prefix::getEndIp)
                  .thenComparing(Prefix::getStartIp, Comparator.reverseOrder()))
          .collect(ImmutableList.toImmutableList());
    }

    boolean intersectsPrefixSpace(PrefixSpace space) {
      return _map.entrySet().stream()
          .filter(e -> !e.getValue().isEmpty())
          .anyMatch(
              e ->
                  space.getPrefixRanges().stream()
                      .anyMatch(r -> r.includesPrefixRange(PrefixRange.fromPrefix(e.getKey()))));
    }

    Set<Prefix> overlappingKeys(RangeSet<Ip> ips) {
      return _map.entrySet().stream()
          .filter(e -> !e.getValue().isEmpty())
          .map(Map.Entry::getKey)
          .filter(p -> ips.intersects(Range.closed(p.getStartIp(), p.getEndIp())))
          .collect(ImmutableSet.toImmutableSet());
    }
  }

  /** Random prefixes clustered enough that they nest, collide, and sit in the same subtrees. */
  private static Prefix randomPrefix(Random rng) {
    int len = rng.nextInt(33);
    long ip;
    switch (rng.nextInt(3)) {
      case 0:
        ip = rng.nextInt() & 0xFFFFFFFFL;
        break;
      case 1:
        ip = 0x0A000000L | rng.nextInt(1 << 16);
        break;
      default:
        ip = 0x0A000000L | (rng.nextInt(8) << 8) | rng.nextInt(4);
        break;
    }
    return Prefix.create(Ip.create(ip), len);
  }

  private static void assertMatchesModel(
      PrefixTrieMultiMap<Integer> trie, Model model, Random rng) {
    assertThat(trie.getNumElements(), equalTo(model.numElements()));
    assertThat(trie.getAllElements(), equalTo(model.allElements()));
    assertThat(keysInPostOrder(trie), equalTo(model.keysInPostOrder()));
    for (int i = 0; i < 50; ++i) {
      Prefix p = randomPrefix(rng);
      assertThat(trie.get(p), equalTo(model.get(p)));
      Ip ip = p.getStartIp();
      int maxLen = rng.nextInt(33);
      assertThat(
          "lpm " + ip + " maxLen " + maxLen,
          trie.longestPrefixMatch(ip, maxLen),
          equalTo(model.longestPrefixMatch(ip, maxLen)));
      assertThat(trie.longestPrefixMatch(ip), equalTo(model.longestPrefixMatch(ip, 32)));
      PrefixSpace space = new PrefixSpace(new PrefixRange(p, new SubRange(rng.nextInt(33), 32)));
      assertThat(
          space.toString(),
          trie.intersectsPrefixSpace(space),
          equalTo(model.intersectsPrefixSpace(space)));
      RangeSet<Ip> ips = toRangeSet(p);
      assertThat(
          trie.getOverlappingEntries(ips)
              .map(Map.Entry::getKey)
              .collect(ImmutableSet.toImmutableSet()),
          equalTo(model.overlappingKeys(ips)));
    }
  }

  @Test
  public void testRandomizedAgainstModel() {
    Random rng = new Random(20260901);
    for (int trial = 0; trial < 20; ++trial) {
      PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
      Model model = new Model();
      List<Prefix> used = new ArrayList<>();
      int ops = 50 + rng.nextInt(400);
      for (int i = 0; i < ops; ++i) {
        Prefix p =
            !used.isEmpty() && rng.nextInt(3) == 0
                ? used.get(rng.nextInt(used.size()))
                : randomPrefix(rng);
        used.add(p);
        int e = rng.nextInt(6);
        switch (rng.nextInt(5)) {
          case 0:
          case 1:
            assertThat(trie.put(p, e), equalTo(model.put(p, e)));
            break;
          case 2:
            List<Integer> es = ImmutableList.of(e, rng.nextInt(6), rng.nextInt(6));
            assertThat(trie.putAll(p, es), equalTo(model.putAll(p, es)));
            break;
          case 3:
            assertThat(trie.remove(p, e), equalTo(model.remove(p, e)));
            break;
          default:
            assertThat(trie.replaceAll(p, e), equalTo(model.replaceAll(p, e)));
            break;
        }
        if (rng.nextInt(20) == 0) {
          trie.trimToSize();
        }
      }
      assertMatchesModel(trie, model, rng);

      // Equality and hashing are over entries, whatever the insertion history.
      PrefixTrieMultiMap<Integer> rebuilt = new PrefixTrieMultiMap<>();
      List<Prefix> keys = new ArrayList<>(model.keysInPostOrder());
      Collections.shuffle(keys, rng);
      for (Prefix k : keys) {
        rebuilt.putAll(k, model.get(k));
      }
      assertThat(rebuilt, equalTo(trie));
      assertThat(rebuilt.hashCode(), equalTo(trie.hashCode()));
      assertThat(SerializationUtils.clone(trie), equalTo(trie));

      // Adding one more element breaks equality.
      if (!keys.isEmpty()) {
        rebuilt.put(keys.get(0), 100);
        assertThat(rebuilt, not(equalTo(trie)));
      }
    }
  }

  @Test
  public void testManyHostRoutes() {
    // Enough nodes to grow the backing arrays several times, in a shape that maximizes depth.
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    int n = 5000;
    for (int i = 0; i < n; ++i) {
      assertTrue(trie.put(Prefix.create(Ip.create(0x0A000000L + i * 7L), 32), i));
    }
    assertThat(trie.getNumElements(), equalTo(n));
    for (int i = 0; i < n; ++i) {
      Ip ip = Ip.create(0x0A000000L + i * 7L);
      assertThat(trie.longestPrefixMatch(ip), equalTo(ImmutableSet.of(i)));
      assertThat(trie.longestPrefixMatch(Ip.create(ip.asLong() + 1)), empty());
    }
    trie.put(Prefix.parse("10.0.0.0/8"), -1);
    for (int i = 0; i < n; ++i) {
      Ip ip = Ip.create(0x0A000000L + i * 7L + 1);
      assertThat(trie.longestPrefixMatch(ip), equalTo(ImmutableSet.of(-1)));
    }
    for (int i = 0; i < n; i += 2) {
      assertTrue(trie.remove(Prefix.create(Ip.create(0x0A000000L + i * 7L), 32), i));
    }
    assertThat(trie.getNumElements(), equalTo(n / 2 + 1));
    assertThat(trie.getAllElements(), hasSize(n / 2 + 1));
  }

  @Test
  public void testValueTransitions() {
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    Prefix p = Prefix.parse("10.0.0.0/8");
    // empty -> single -> multi -> single -> empty
    assertTrue(trie.put(p, 1));
    assertFalse(trie.put(p, 1));
    assertTrue(trie.put(p, 2));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(1, 2)));
    assertFalse(trie.putAll(p, ImmutableList.of(1, 2)));
    assertTrue(trie.remove(p, 1));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(2)));
    assertThat(trie.getNumElements(), equalTo(1));
    assertFalse(trie.remove(p, 1));
    assertTrue(trie.remove(p, 2));
    assertThat(trie.get(p), empty());
    assertThat(trie.getNumElements(), equalTo(0));
    assertThat(trie.longestPrefixMatch(Ip.parse("10.1.1.1")), empty());
    // empty node stays in the structure; a longer prefix still resolves past it
    assertTrue(trie.put(Prefix.ZERO, 0));
    assertThat(trie.longestPrefixMatch(Ip.parse("10.1.1.1")), equalTo(ImmutableSet.of(0)));
    // replaceAll on multi and on single
    trie.putAll(p, ImmutableList.of(3, 4));
    assertTrue(trie.replaceAll(p, 5));
    assertFalse(trie.replaceAll(p, 5));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(5)));
    assertThat(trie.getNumElements(), equalTo(2));
    // putAll of a single element onto an equal single element is a no-op
    assertFalse(trie.putAll(p, ImmutableList.of(5, 5)));
    assertTrue(trie.putAll(p, ImmutableList.of(5, 6)));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(5, 6)));
  }

  @Test
  public void testHandle() {
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    Prefix p = Prefix.parse("10.0.0.0/8");
    PrefixTrieMultiMap<Integer>.Handle h = trie.handle(p);
    assertThat(h.get(), empty());
    assertThat(trie.getNumElements(), equalTo(0));
    assertTrue(h.add(1));
    assertFalse(h.add(1));
    assertThat(h.get(), equalTo(ImmutableSet.of(1)));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(1)));
    assertTrue(h.add(2));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(1, 2)));
    assertTrue(h.replaceAll(3));
    assertFalse(h.replaceAll(3));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(3)));
    assertFalse(h.remove(1));
    assertTrue(h.remove(3));
    assertThat(trie.get(p), empty());
    assertThat(trie.getNumElements(), equalTo(0));
    // The handle stays valid while the trie grows around it, including above it.
    for (int i = 0; i < 1000; ++i) {
      trie.put(Prefix.create(Ip.create(0x0B000000L + i), 32), i);
    }
    trie.put(Prefix.ZERO, -1);
    assertTrue(h.add(4));
    assertThat(trie.get(p), equalTo(ImmutableSet.of(4)));
    assertThat(trie.longestPrefixMatch(Ip.parse("10.1.1.1")), equalTo(ImmutableSet.of(4)));
    assertThat(trie.handle(p).get(), equalTo(ImmutableSet.of(4)));
    assertThat(trie.existingHandle(p).get(), equalTo(ImmutableSet.of(4)));
  }

  @Test
  public void testExistingHandle() {
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    trie.put(Prefix.parse("10.0.0.0/16"), 1);
    trie.put(Prefix.parse("10.1.0.0/16"), 2);
    // Prefixes without elements, including the branching node the two puts created, have no
    // handle...
    assertThat(trie.existingHandle(Prefix.parse("10.0.0.0/15")), nullValue());
    assertThat(trie.existingHandle(Prefix.parse("10.0.0.0/8")), nullValue());
    assertThat(trie.existingHandle(Prefix.parse("10.0.0.0/24")), nullValue());
    assertThat(trie.existingHandle(Prefix.parse("11.0.0.0/16")), nullValue());
    assertThat(new PrefixTrieMultiMap<Integer>().existingHandle(Prefix.ZERO), nullValue());
    // ...and asking did not add them.
    assertThat(keysInPostOrder(trie), hasSize(2));
    PrefixTrieMultiMap<Integer>.Handle h = trie.existingHandle(Prefix.parse("10.1.0.0/16"));
    assertThat(h.get(), equalTo(ImmutableSet.of(2)));
    assertTrue(h.remove(2));
    assertThat(trie.existingHandle(Prefix.parse("10.1.0.0/16")), nullValue());
  }

  @Test
  public void testTraverseEntriesAround() {
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    trie.put(Prefix.ZERO, 0);
    trie.put(Prefix.parse("10.0.0.0/8"), 8);
    trie.put(Prefix.parse("10.1.0.0/16"), 16);
    trie.put(Prefix.parse("10.1.1.0/24"), 24);
    trie.put(Prefix.parse("10.1.1.1/32"), 32);
    trie.put(Prefix.parse("10.1.2.0/24"), -24);
    trie.put(Prefix.parse("10.2.0.0/16"), 17);
    trie.put(Prefix.parse("11.0.0.0/8"), 11);
    // Ancestors and everything below, in post-order.
    assertThat(
        aroundKeys(trie, Prefix.parse("10.1.0.0/16"), s -> true),
        contains(
            Prefix.parse("10.1.1.1/32"),
            Prefix.parse("10.1.1.0/24"),
            Prefix.parse("10.1.2.0/24"),
            Prefix.parse("10.1.0.0/16"),
            Prefix.parse("10.0.0.0/8"),
            Prefix.ZERO));
    // Below the prefix, stop at (and exclude) nodes with negative elements; ancestors are exempt.
    assertThat(
        aroundKeys(trie, Prefix.parse("10.1.0.0/16"), s -> s.stream().allMatch(e -> e >= 0)),
        contains(
            Prefix.parse("10.1.1.1/32"),
            Prefix.parse("10.1.1.0/24"),
            Prefix.parse("10.1.0.0/16"),
            Prefix.parse("10.0.0.0/8"),
            Prefix.ZERO));
    assertThat(
        aroundKeys(trie, Prefix.parse("10.1.0.0/16"), s -> !s.contains(24)),
        contains(
            Prefix.parse("10.1.2.0/24"),
            Prefix.parse("10.1.0.0/16"),
            Prefix.parse("10.0.0.0/8"),
            Prefix.ZERO));
    // A prefix with no node of its own: ancestors plus the contained subtree.
    assertThat(
        aroundKeys(trie, Prefix.parse("10.0.0.0/12"), s -> true),
        contains(
            Prefix.parse("10.1.1.1/32"),
            Prefix.parse("10.1.1.0/24"),
            Prefix.parse("10.1.2.0/24"),
            Prefix.parse("10.1.0.0/16"),
            Prefix.parse("10.2.0.0/16"),
            Prefix.parse("10.0.0.0/8"),
            Prefix.ZERO));
    // A prefix disjoint from everything but the default route.
    assertThat(aroundKeys(trie, Prefix.parse("12.0.0.0/8"), s -> true), contains(Prefix.ZERO));
    assertThat(aroundKeys(new PrefixTrieMultiMap<Integer>(), Prefix.ZERO, s -> true), empty());
    // Agrees with the general traversal for random prefixes.
    Random rng = new Random(3);
    for (int i = 0; i < 300; ++i) {
      Prefix p = randomPrefix(rng);
      int excluded = rng.nextInt(40) - 30;
      Predicate<Set<Integer>> descend = s -> !s.contains(excluded);
      List<Prefix> expected = new ArrayList<>();
      trie.traverseEntries(
          (k, v) -> expected.add(k),
          (k, v) -> k.containsPrefix(p) || (p.containsPrefix(k) && descend.test(v)));
      assertThat(p.toString(), aroundKeys(trie, p, descend), equalTo(expected));
    }
  }

  private static List<Prefix> aroundKeys(
      PrefixTrieMultiMap<Integer> trie, Prefix prefix, Predicate<Set<Integer>> descendThrough) {
    List<Prefix> keys = new ArrayList<>();
    trie.traverseEntriesAround(prefix, descendThrough, (k, v) -> keys.add(k));
    return keys;
  }

  @Test
  public void testPutOnInternalNode() {
    PrefixTrieMultiMap<Integer> trie = new PrefixTrieMultiMap<>();
    trie.put(Prefix.parse("10.0.0.0/16"), 1);
    trie.put(Prefix.parse("10.1.0.0/16"), 2);
    // 10.0.0.0/15 is the internal node created above; give it elements.
    Prefix internal = Prefix.parse("10.0.0.0/15");
    assertThat(trie.get(internal), empty());
    assertTrue(trie.put(internal, 3));
    assertThat(trie.get(internal), equalTo(ImmutableSet.of(3)));
    assertThat(trie.longestPrefixMatch(Ip.parse("10.1.2.3")), equalTo(ImmutableSet.of(2)));
    assertThat(trie.longestPrefixMatch(Ip.parse("10.1.2.3"), 15), equalTo(ImmutableSet.of(3)));
    assertThat(
        keysInPostOrder(trie),
        contains(Prefix.parse("10.0.0.0/16"), Prefix.parse("10.1.0.0/16"), internal));
  }

  private static RangeSet<Ip> toRangeSet(Prefix prefix) {
    return ImmutableRangeSet.of(Range.closed(prefix.getStartIp(), prefix.getEndIp()));
  }

  @Test
  public void testJavaSerializationCompact() {
    PrefixTrieMultiMap<String> map = new PrefixTrieMultiMap<>();
    map.put(Prefix.parse("0.0.0.0/0"), "default");
    map.put(Prefix.parse("10.0.0.0/8"), "a");
    map.put(Prefix.parse("10.0.0.0/8"), "b");
    map.put(Prefix.parse("255.255.255.255/32"), "c");
    map.put(Prefix.parse("128.0.0.0/1"), "d");
    // A node whose only element was removed keeps its slot with no value.
    map.put(Prefix.parse("192.168.0.0/16"), "gone");
    map.remove(Prefix.parse("192.168.0.0/16"), "gone");
    PrefixTrieMultiMap<String> clone = org.apache.commons.lang3.SerializationUtils.clone(map);
    assertThat(clone, equalTo(map));
    assertThat(clone.get(Prefix.parse("10.0.0.0/8")), equalTo(ImmutableSet.of("a", "b")));
    assertThat(clone.getAllElements(), equalTo(map.getAllElements()));
    assertThat(clone.get(Prefix.parse("192.168.0.0/16")), equalTo(ImmutableSet.of()));
    assertThat(clone.longestPrefixMatch(Ip.parse("192.168.1.1")), equalTo(ImmutableSet.of("d")));
    assertThat(
        org.apache.commons.lang3.SerializationUtils.clone(new PrefixTrieMultiMap<String>()),
        equalTo(new PrefixTrieMultiMap<String>()));
  }
}
