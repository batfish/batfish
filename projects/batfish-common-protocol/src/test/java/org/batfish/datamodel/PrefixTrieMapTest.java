package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link PrefixTrieMap} */
public class PrefixTrieMapTest {
  @Test
  public void testEquals() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    ptm1.add(1);
    PrefixTrieMap<Integer> ptm2 = new PrefixTrieMap<>(Prefix.ZERO);
    PrefixTrieMap<Integer> ptm3 = ptm2.findOrCreateNode(Prefix.parse("1.1.1.0/24"));
    new EqualsTester()
        .addEqualityGroup(
            new PrefixTrieMap<Integer>(Prefix.ZERO), new PrefixTrieMap<Integer>(Prefix.ZERO))
        .addEqualityGroup(new PrefixTrieMap<Integer>(Prefix.parse("1.1.1.1/32")))
        .addEqualityGroup(ptm1)
        .addEqualityGroup(ptm2)
        .addEqualityGroup(ptm3)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testAdd() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    assertThat("Element was added", ptm1.add(1));
    assertThat(ptm1.getElements(), equalTo(ImmutableSet.of(1)));
  }

  @Test
  public void testAddAll() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    ptm1.addAll(ImmutableSet.of(1, 2, 3));
    assertThat(ptm1.getElements(), equalTo(ImmutableSet.of(1, 2, 3)));
  }

  @Test
  public void testRemove() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    ptm1.add(1);
    assertThat("Nothing to remove", !ptm1.remove(2));
    assertThat("Element removed", ptm1.remove(1));
    assertThat(ptm1.getElements(), empty());
  }

  @Test
  public void testClear() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    ptm1.addAll(ImmutableSet.of(1, 2, 3));
    ptm1.clear();
    assertThat(ptm1.getElements(), empty());
  }

  @Test
  public void testFindNodeWhenMissing() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    assertThat(ptm1.findNode(Prefix.parse("1.1.1.1/32")), nullValue());
  }

  @Test
  public void testFindOrCreateNode() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    PrefixTrieMap<Integer> ptm2 = ptm1.findOrCreateNode(Prefix.parse("1.1.1.1/32"));
    assertThat(ptm2, notNullValue());
    assertThat(ptm2.getElements(), empty());
  }

  @Test
  public void testFindNodeWhenNotMissing() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    PrefixTrieMap<Integer> ptm2 = ptm1.findOrCreateNode(Prefix.parse("1.1.1.1/32"));
    assertThat(ptm1.findNode(Prefix.parse("1.1.1.1/32")), sameInstance(ptm2));
  }

  @Test
  public void testFindNodeWhichIsMoreGeneral() {
    PrefixTrieMap<Integer> ptm1 = new PrefixTrieMap<>(Prefix.ZERO);
    PrefixTrieMap<Integer> ptm2 = ptm1.findOrCreateNode(Prefix.parse("1.1.1.1/32"));
    assertThat(ptm2.findNode(Prefix.ZERO), nullValue());
    assertThat(ptm2.getElements(Prefix.ZERO), empty());
  }
}
