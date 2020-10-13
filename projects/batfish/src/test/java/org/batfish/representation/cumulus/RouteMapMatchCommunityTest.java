package org.batfish.representation.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class RouteMapMatchCommunityTest {

  @Test
  public void testGetNames() {
    RouteMapMatchCommunity match = new RouteMapMatchCommunity(ImmutableList.of("M1", "M2", "M3"));
    assertThat(match.getNames(), equalTo(ImmutableList.of("M1", "M2", "M3")));
  }
}
