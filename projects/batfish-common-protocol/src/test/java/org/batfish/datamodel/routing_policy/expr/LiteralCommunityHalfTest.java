package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.matchers.CommunityHalfExprMatchers.matches;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class LiteralCommunityHalfTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new LiteralCommunityHalf(1), new LiteralCommunityHalf(1))
        .addEqualityGroup(new LiteralCommunityHalf(2))
        .testEquals();
  }

  @Test
  public void testMatches() {
    LiteralCommunityHalf l = new LiteralCommunityHalf(1);

    assertThat(l, matches(1));
    assertThat(l, not(matches(2)));
  }
}
