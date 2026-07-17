package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

/** Tests of {@link AsPath} */
public class AsPathTest {
  @Test
  public void testRemoveConfederations() {
    AsPath path = AsPath.of(AsSet.of(1L));
    AsPath updated = path.removeConfederations();
    assertThat(path, equalTo(updated));

    path = AsPath.of(AsSet.confed(1L));
    updated = path.removeConfederations();
    assertThat(updated, equalTo(AsPath.empty()));

    path = AsPath.of(ImmutableList.of(AsSet.confed(1L), AsSet.of(2L)));
    updated = path.removeConfederations();
    assertThat(updated, equalTo(AsPath.ofSingletonAsSets(2L)));

    path = AsPath.of(ImmutableList.of(AsSet.confed(1L), AsSet.of(2L), AsSet.confed(3L)));
    updated = path.removeConfederations();
    assertThat(updated, equalTo(AsPath.ofSingletonAsSets(2L)));
  }

  @Test
  public void testAsPathLength() {
    AsPath path = AsPath.of(AsSet.confed(1L));
    assertThat(path.length(), equalTo(0));

    path = AsPath.of(ImmutableList.of(AsSet.confed(1L), AsSet.of(2L)));
    assertThat(path.length(), equalTo(1));
    assertThat(path.size(), equalTo(2));

    path = AsPath.of(ImmutableList.of(AsSet.confed(1L), AsSet.of(2L, 3L), AsSet.confed(3L)));
    assertThat(path.length(), equalTo(1));
    assertThat(path.size(), equalTo(3));
  }

  @Test
  public void testAggregateContributorsEmpty() {
    assertThat(AsPath.aggregateContributors(ImmutableList.of()), equalTo(AsPath.empty()));
  }

  @Test
  public void testAggregateContributorsSingle() {
    // A single contributor yields its own leading AS_SEQUENCE.
    AsPath path = AsPath.ofSingletonAsSets(65000L, 65001L);
    assertThat(AsPath.aggregateContributors(ImmutableList.of(path)), equalTo(path));
  }

  @Test
  public void testAggregateContributorsIdentical() {
    AsPath path = AsPath.ofSingletonAsSets(65000L, 65001L);
    assertThat(
        AsPath.aggregateContributors(ImmutableList.of(path, path)),
        equalTo(AsPath.ofSingletonAsSets(65000L, 65001L)));
  }

  @Test
  public void testAggregateContributorsCommonPrefixThenDiverge() {
    // The lab case: "65000 65001" and "65000 65003" share the leading 65000, then diverge; the
    // divergent tail is dropped.
    AsPath a = AsPath.ofSingletonAsSets(65000L, 65001L);
    AsPath b = AsPath.ofSingletonAsSets(65000L, 65003L);
    assertThat(
        AsPath.aggregateContributors(ImmutableList.of(a, b)),
        equalTo(AsPath.ofSingletonAsSets(65000L)));
  }

  @Test
  public void testAggregateContributorsNoCommonPrefix() {
    AsPath a = AsPath.ofSingletonAsSets(65001L);
    AsPath b = AsPath.ofSingletonAsSets(65002L);
    assertThat(AsPath.aggregateContributors(ImmutableList.of(a, b)), equalTo(AsPath.empty()));
  }

  @Test
  public void testAggregateContributorsEmptyContributorPath() {
    // A contributor with an empty path (e.g. a locally redistributed route) shares no prefix.
    AsPath a = AsPath.ofSingletonAsSets(65000L);
    assertThat(
        AsPath.aggregateContributors(ImmutableList.of(a, AsPath.empty())), equalTo(AsPath.empty()));
  }

  @Test
  public void testAggregateContributorsStopsAtAsSet() {
    // A non-singleton (AS_SET) segment cannot extend the common AS_SEQUENCE.
    AsPath a = AsPath.of(ImmutableList.of(AsSet.of(65000L), AsSet.of(65001L, 65002L)));
    AsPath b = AsPath.of(ImmutableList.of(AsSet.of(65000L), AsSet.of(65001L, 65002L)));
    assertThat(
        AsPath.aggregateContributors(ImmutableList.of(a, b)),
        equalTo(AsPath.ofSingletonAsSets(65000L)));
  }
}
