package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class RegexCommunitySetMatchers {
  private static final class HasRegex extends FeatureMatcher<RegexCommunitySet, String> {

    private HasRegex(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A RegexCommunitySet with regex:", "regex");
    }

    @Override
    protected String featureValueOf(RegexCommunitySet actual) {
      return actual.getRegex();
    }
  }

  private static final class IsRegexCommunitySet
      extends IsInstanceThat<CommunitySetExpr, RegexCommunitySet> {
    private IsRegexCommunitySet(@Nonnull Matcher<? super RegexCommunitySet> subMatcher) {
      super(RegexCommunitySet.class, subMatcher);
    }
  }

  public static @Nonnull Matcher<RegexCommunitySet> hasRegex(@Nonnull String expectedRegex) {
    return new HasRegex(equalTo(expectedRegex));
  }

  public static @Nonnull Matcher<CommunitySetExpr> isRegexCommunitySet(
      @Nonnull Matcher<? super RegexCommunitySet> subMatcher) {
    return new IsRegexCommunitySet(subMatcher);
  }

  private RegexCommunitySetMatchers() {}
}
