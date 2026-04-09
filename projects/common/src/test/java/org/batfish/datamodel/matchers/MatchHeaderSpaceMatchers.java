package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class MatchHeaderSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * MatchHeaderSpace's headerSpace.
   */
  public static Matcher<MatchHeaderSpace> hasHeaderSpace(Matcher<? super HeaderSpace> subMatcher) {
    return new HasHeaderSpace(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link MatchHeaderSpace} matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<AclLineMatchExpr> isMatchHeaderSpaceThat(
      @Nonnull Matcher<? super MatchHeaderSpace> subMatcher) {
    return new IsMatchHeaderSpaceThat(subMatcher);
  }

  private MatchHeaderSpaceMatchers() {}

  private static final class HasHeaderSpace extends FeatureMatcher<MatchHeaderSpace, HeaderSpace> {

    public HasHeaderSpace(@Nonnull Matcher<? super HeaderSpace> subMatcher) {
      super(subMatcher, "A MatchHeaderSpace with headerSpace:", "headerSpace");
    }

    @Override
    protected HeaderSpace featureValueOf(MatchHeaderSpace actual) {
      return actual.getHeaderspace();
    }
  }

  private static final class IsMatchHeaderSpaceThat
      extends IsInstanceThat<AclLineMatchExpr, MatchHeaderSpace> {
    IsMatchHeaderSpaceThat(@Nonnull Matcher<? super MatchHeaderSpace> subMatcher) {
      super(MatchHeaderSpace.class, subMatcher);
    }
  }
}
