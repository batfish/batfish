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
}
