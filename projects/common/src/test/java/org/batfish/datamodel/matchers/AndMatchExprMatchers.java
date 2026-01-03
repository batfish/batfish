package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.matchers.AndMatchExprMatchersImpl.HasConjuncts;
import org.batfish.datamodel.matchers.AndMatchExprMatchersImpl.IsAndMatchExprThat;
import org.hamcrest.Matcher;

public class AndMatchExprMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the AndMatchExpr's
   * conjuncts.
   */
  public static HasConjuncts hasConjuncts(Matcher<? super List<AclLineMatchExpr>> subMatcher) {
    return new HasConjuncts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link AndMatchExpr} matched by the provided
   * {@code subMatcher}.
   */
  public static IsAndMatchExprThat isAndMatchExprThat(
      @Nonnull Matcher<? super AndMatchExpr> subMatcher) {
    return new IsAndMatchExprThat(subMatcher);
  }

  private AndMatchExprMatchers() {}
}
