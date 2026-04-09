package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OrMatchExprMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OrMatchExpr's
   * disjuncts.
   */
  public static Matcher<OrMatchExpr> hasDisjuncts(
      Matcher<? super List<AclLineMatchExpr>> subMatcher) {
    return new HasDisjuncts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link OrMatchExpr} matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<AclLineMatchExpr> isOrMatchExprThat(
      @Nonnull Matcher<? super OrMatchExpr> subMatcher) {
    return new IsOrMatchExprThat(subMatcher);
  }

  private OrMatchExprMatchers() {}

  private static final class HasDisjuncts
      extends FeatureMatcher<OrMatchExpr, List<AclLineMatchExpr>> {

    public HasDisjuncts(@Nonnull Matcher<? super List<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An OrMatchExpr with disjuncts:", "disjuncts");
    }

    @Override
    protected List<AclLineMatchExpr> featureValueOf(OrMatchExpr actual) {
      return actual.getDisjuncts();
    }
  }

  private static final class IsOrMatchExprThat
      extends IsInstanceThat<AclLineMatchExpr, OrMatchExpr> {
    IsOrMatchExprThat(@Nonnull Matcher<? super OrMatchExpr> subMatcher) {
      super(OrMatchExpr.class, subMatcher);
    }
  }
}
