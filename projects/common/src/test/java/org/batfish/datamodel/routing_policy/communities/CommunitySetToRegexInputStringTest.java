package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunitySetToRegexInputString}. */
public final class CommunitySetToRegexInputStringTest {

  @Test
  public void testVisitTypesFirstAscendingSpaceSeparated() {
    CommunitySetRendering r =
        new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance());
    assertThat(
        r.accept(
            CommunitySetToRegexInputString.instance(),
            CommunitySet.of(
                LargeCommunity.of(0L, 0L, 1L),
                ExtendedCommunity.of(0, 0L, 3L),
                ExtendedCommunity.of(0, 0L, 4L),
                StandardCommunity.of(5L),
                LargeCommunity.of(0L, 0L, 2L),
                StandardCommunity.of(6L))),
        equalTo("5 6 3 4 1 2"));
  }
}
