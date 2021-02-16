package org.batfish.representation.cumulus_nclu;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class RouteMapMatchCommunityTest {

  @Test
  public void testGetNames() {
    RouteMapMatchCommunity match = new RouteMapMatchCommunity(ImmutableList.of("M1", "M2", "M3"));
    assertThat(match.getNames(), equalTo(ImmutableList.of("M1", "M2", "M3")));
  }
}
