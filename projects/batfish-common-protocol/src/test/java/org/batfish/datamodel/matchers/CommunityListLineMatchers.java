package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class CommunityListLineMatchers {

  private static final class HasMatchCondition
      extends FeatureMatcher<CommunityListLine, CommunitySetExpr> {

    public HasMatchCondition(@Nonnull Matcher<? super CommunitySetExpr> subMatcher) {
      super(subMatcher, "A CommunityListLine with matchCondition:", "matchCondition");
    }

    @Override
    protected CommunitySetExpr featureValueOf(CommunityListLine actual) {
      return actual.getMatchCondition();
    }
  }

  public static @Nonnull Matcher<CommunityListLine> hasMatchCondition(
      @Nonnull Matcher<? super CommunitySetExpr> subMatcher) {
    return new HasMatchCondition(subMatcher);
  }

  private CommunityListLineMatchers() {}
}
