package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.matchers.CommunityHalfExprMatchers.matches;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

public final class RangeCommunityHalfTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new RangeCommunityHalf(new SubRange(1, 2)), new RangeCommunityHalf(new SubRange(1, 2)))
        .addEqualityGroup(new RangeCommunityHalf(new SubRange(1, 3)))
        .testEquals();
  }

  @Test
  public void testMatches() {
    RangeCommunityHalf l = new RangeCommunityHalf(new SubRange(1, 3));

    assertThat(l, matches(1));
    assertThat(l, matches(2));
    assertThat(l, matches(3));
    assertThat(l, not(matches(0)));
    assertThat(l, not(matches(4)));
  }
}
