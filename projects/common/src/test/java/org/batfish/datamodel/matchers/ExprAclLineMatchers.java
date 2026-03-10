package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.matchers.ExprAclLineMatchersImpl.HasAction;
import org.batfish.datamodel.matchers.ExprAclLineMatchersImpl.HasMatchCondition;
import org.batfish.datamodel.matchers.ExprAclLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public class ExprAclLineMatchers {
  /** Provides a matcher that matches if the provided {@code subMatcher} matches the line action. */
  public static HasAction hasAction(@Nonnull Matcher<? super LineAction> subMatcher) {
    return new HasAction(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.ExprAclLine}'s name.
   */
  public static HasName hasName(@Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code name} is equal to the {@link
   * org.batfish.datamodel.ExprAclLine}'s name.
   */
  public static HasName hasName(@Nonnull String name) {
    return new HasName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.ExprAclLine}'s match condition.
   */
  public static HasMatchCondition hasMatchCondition(
      @Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
    return new HasMatchCondition(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code expr} equals the HeaderSpace's match
   * condition.
   */
  public static HasMatchCondition hasMatchCondition(@Nonnull AclLineMatchExpr expr) {
    return new HasMatchCondition(equalTo(expr));
  }

  private ExprAclLineMatchers() {}
}
