package org.batfish.datamodel;

import static org.batfish.datamodel.PrefixTrieMultiMap.legalLeftChildPrefix;
import static org.batfish.datamodel.PrefixTrieMultiMap.legalRightChildPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link PrefixTrieMultiMap} */
public class PrefixTrieMultiMapTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    ptm1.add(Prefix.ZERO, 1);
    PrefixTrieMultiMap<Integer> ptm2 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    ptm2.add(Prefix.parse("1.1.1.0/24"), 1);
    new EqualsTester()
        .addEqualityGroup(
            new PrefixTrieMultiMap<Integer>(Prefix.ZERO),
            new PrefixTrieMultiMap<Integer>(Prefix.ZERO))
        .addEqualityGroup(new PrefixTrieMultiMap<Integer>(Prefix.parse("1.1.1.1/32")))
        .addEqualityGroup(ptm1)
        .addEqualityGroup(ptm2)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testAdd() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    assertThat("Element was added", ptm1.add(Prefix.ZERO, 1));
    assertThat(ptm1.getElements(), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.getElements(Prefix.ZERO), equalTo(ImmutableSet.of(1)));
  }

  @Test
  public void testAddDeeper() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    assertThat("Element was added", ptm1.add(p, 1));
    assertThat(ptm1.getElements(p), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.getElements(Prefix.ZERO), empty());
  }

  @Test
  public void testAddAll() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    ptm1.addAll(Prefix.ZERO, ImmutableSet.of(1, 2, 3));
    assertThat(ptm1.getElements(), equalTo(ImmutableSet.of(1, 2, 3)));
    assertThat(ptm1.getElements(Prefix.ZERO), equalTo(ImmutableSet.of(1, 2, 3)));
  }

  @Test
  public void testRemove() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    ptm1.add(Prefix.ZERO, 1);
    assertThat("Nothing to remove", !ptm1.remove(Prefix.ZERO, 2));
    assertThat("Element removed", ptm1.remove(Prefix.ZERO, 1));
    assertThat(ptm1.getElements(), empty());
  }

  @Test
  public void testRemoveDeeper() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    Prefix p = Prefix.parse("1.1.1.0/24");
    ptm1.add(p, 1);
    ptm1.add(p, 2);
    assertThat("Nothing to remove", !ptm1.remove(Prefix.ZERO, 2));
    assertThat("Element removed", ptm1.remove(p, 2));
    assertThat(ptm1.getElements(Prefix.ZERO), empty());
    assertThat(ptm1.getElements(p), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.getAllElements(), equalTo(ImmutableSet.of(1)));
  }

  @Test
  public void testLongestPrefixMatch() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    Prefix p1 = Prefix.parse("1.1.1.0/24");
    Prefix p2 = Prefix.parse("1.1.1.128/25");
    Prefix p3 = Prefix.parse("1.1.1.129/32");
    ptm1.add(p1, 1);
    ptm1.add(p2, 2);
    ptm1.add(p3, 3);
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.1")), equalTo(ImmutableSet.of(1)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.128")), equalTo(ImmutableSet.of(2)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.128"), 1), empty());
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.129")), equalTo(ImmutableSet.of(3)));
    assertThat(ptm1.longestPrefixMatch(Ip.parse("1.1.1.130")), equalTo(ImmutableSet.of(2)));
  }

  @Test
  public void testAddWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    thrown.expect(IllegalArgumentException.class);
    ptm1.add(Prefix.ZERO, 1);
  }

  @Test
  public void testAddAllWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    thrown.expect(IllegalArgumentException.class);
    ptm1.addAll(Prefix.ZERO, ImmutableSet.of(1, 2));
  }

  @Test
  public void testRemoveWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    ptm1.add(Prefix.parse("128.0.0.0/1"), 1);
    thrown.expect(IllegalArgumentException.class);
    ptm1.remove(Prefix.ZERO, 1);
  }

  @Test
  public void testReplaceWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    thrown.expect(IllegalArgumentException.class);
    ptm1.replaceAll(Prefix.ZERO, 1);
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
    PrefixTrieMultiMap<Integer> map = new PrefixTrieMultiMap<>(Prefix.ZERO);
    Prefix l = Prefix.parse("0.0.0.0/8");
    Prefix ll = Prefix.parse("0.0.0.0/16");
    Prefix lr = Prefix.parse("0.128.0.0/16");
    Prefix r = Prefix.parse("128.0.0.0/8");
    Prefix rl = Prefix.parse("128.0.0.0/16");
    Prefix rr = Prefix.parse("128.128.0.0/16");

    map.add(l, 0);
    map.add(ll, 0);
    map.add(lr, 0);

    // adding in different order just for fun
    map.add(rr, 0);
    map.add(rl, 0);
    map.add(r, 0);

    List<Prefix> prefixes = new ArrayList<>();
    map.traverseEntries((prefix, elems) -> prefixes.add(prefix));
    assertThat(prefixes, contains(ll, lr, l, rl, rr, r, Prefix.ZERO));
  }
}
