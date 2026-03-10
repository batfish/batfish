package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class PsThensTest {
  @Test
  public void testCommunities() {
    PsThenCommunitySet set = new PsThenCommunitySet("set");
    PsThenCommunityAdd add1 = new PsThenCommunityAdd("add1");
    PsThenCommunityAdd add2 = new PsThenCommunityAdd("add2");

    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(set), empty());
    assertThat(ps.addPsThen(set), contains("community set"));
    assertThat(ps.addPsThen(add1), empty());
    assertThat(ps.addPsThen(add2), empty());
    assertThat(ps.addPsThen(set), contains("community set", "community add"));
  }

  @Test
  public void testAsPath() {
    PsThenAsPathPrepend prepend = new PsThenAsPathPrepend(ImmutableList.of(1L, 2L, 3L));
    PsThenAsPathExpand expand = new PsThenAsPathExpandLastAs(3);

    PsThens ps = new PsThens();
    assertThat(ps.addPsThen(prepend), empty());
    assertThat(ps.addPsThen(prepend), contains("as-path-prepend"));
    assertThat(ps.addPsThen(expand), empty());
    assertThat(ps.addPsThen(expand), contains("as-path-expand"));
  }
}
