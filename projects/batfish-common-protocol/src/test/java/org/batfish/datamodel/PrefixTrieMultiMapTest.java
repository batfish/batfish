package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
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
  public void testClear() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.ZERO);
    ptm1.addAll(Prefix.ZERO, ImmutableSet.of(1, 2, 3));
    ptm1.clear(Prefix.ZERO);
    assertThat(ptm1.getElements(), empty());
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
    assertThat("Nothing to remove", !ptm1.remove(Prefix.ZERO, 1));
  }

  @Test
  public void testReplaceWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    assertThat("Nothing to replace", !ptm1.replaceAll(Prefix.ZERO, 1));
  }

  @Test
  public void testClearAddWrongNode() {
    PrefixTrieMultiMap<Integer> ptm1 = new PrefixTrieMultiMap<>(Prefix.parse("128.0.0.0/1"));
    thrown.expect(IllegalArgumentException.class);
    ptm1.add(Prefix.ZERO, 1);
  }
}
