package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.matchers.OrMatchExprMatchersImpl.HasDisjuncts;
import org.batfish.datamodel.matchers.OrMatchExprMatchersImpl.IsOrMatchExprThat;
import org.hamcrest.Matcher;

public class OrMatchExprMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OrMatchExpr's
   * disjuncts.
   */
  public static HasDisjuncts hasDisjuncts(Matcher<? super List<AclLineMatchExpr>> subMatcher) {
    return new HasDisjuncts(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link OrMatchExpr} matched by the provided
   * {@code subMatcher}.
   */
  public static IsOrMatchExprThat isOrMatchExprThat(
      @Nonnull Matcher<? super OrMatchExpr> subMatcher) {
    return new IsOrMatchExprThat(subMatcher);
  }

  private OrMatchExprMatchers() {}
}
