package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AndMatchExprMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the AndMatchExpr's
   * conjuncts.
   */
  public static Matcher<AndMatchExpr> hasConjuncts(
      Matcher<? super List<AclLineMatchExpr>> subMatcher) {
    return new HasConjuncts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link AndMatchExpr} matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<AclLineMatchExpr> isAndMatchExprThat(
      @Nonnull Matcher<? super AndMatchExpr> subMatcher) {
    return new IsAndMatchExprThat(subMatcher);
  }

  private AndMatchExprMatchers() {}

  private static final class HasConjuncts
      extends FeatureMatcher<AndMatchExpr, List<AclLineMatchExpr>> {

    public HasConjuncts(@Nonnull Matcher<? super List<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An AndMatchExpr with conjuncts:", "conjuncts");
    }

    @Override
    protected List<AclLineMatchExpr> featureValueOf(AndMatchExpr actual) {
      return actual.getConjuncts();
    }
  }

  private static final class IsAndMatchExprThat
      extends IsInstanceThat<AclLineMatchExpr, AndMatchExpr> {
    IsAndMatchExprThat(@Nonnull Matcher<? super AndMatchExpr> subMatcher) {
      super(AndMatchExpr.class, subMatcher);
    }
  }
}
