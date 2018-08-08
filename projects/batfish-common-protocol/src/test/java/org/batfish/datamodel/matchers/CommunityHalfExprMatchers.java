package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.CommunityHalfExpr;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class CommunityHalfExprMatchers {

  private static final class Matches extends TypeSafeDiagnosingMatcher<CommunityHalfExpr> {

    private final int _communityHalf;

    private Matches(int communityHalf) {
      _communityHalf = communityHalf;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("A CommunityHalfExpr matching: %d", _communityHalf));
    }

    @Override
    protected boolean matchesSafely(CommunityHalfExpr item, Description mismatchDescription) {
      if (!item.matches(_communityHalf)) {
        mismatchDescription.appendText(
            String.format("CommunityHalfExpr: '%s' did not match: %d", item, _communityHalf));
        return false;
      }
      return true;
    }
  }

  public static @Nonnull Matcher<CommunityHalfExpr> matches(int communityHalf) {
    return new Matches(communityHalf);
  }

  private CommunityHalfExprMatchers() {}
}
