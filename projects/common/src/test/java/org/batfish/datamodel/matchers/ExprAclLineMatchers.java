package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class ExprAclLineMatchers {
  /** Provides a matcher that matches if the provided {@code subMatcher} matches the line action. */
  public static Matcher<ExprAclLine> hasAction(@Nonnull Matcher<? super LineAction> subMatcher) {
    return new HasAction(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.ExprAclLine}'s name.
   */
  public static Matcher<ExprAclLine> hasName(@Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code name} is equal to the {@link
   * org.batfish.datamodel.ExprAclLine}'s name.
   */
  public static Matcher<ExprAclLine> hasName(@Nonnull String name) {
    return new HasName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.ExprAclLine}'s match condition.
   */
  public static Matcher<ExprAclLine> hasMatchCondition(
      @Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
    return new HasMatchCondition(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code expr} equals the HeaderSpace's match
   * condition.
   */
  public static Matcher<ExprAclLine> hasMatchCondition(@Nonnull AclLineMatchExpr expr) {
    return new HasMatchCondition(equalTo(expr));
  }

  private ExprAclLineMatchers() {}

  private static final class HasAction extends FeatureMatcher<ExprAclLine, LineAction> {

    public HasAction(@Nonnull Matcher<? super LineAction> subMatcher) {
      super(subMatcher, "An IpAcessListLine with action:", "action");
    }

    @Override
    protected LineAction featureValueOf(ExprAclLine actual) {
      return actual.getAction();
    }
  }

  private static final class HasName extends FeatureMatcher<ExprAclLine, String> {
    public HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IpAccessListLine with name:", "name");
    }

    @Override
    protected String featureValueOf(ExprAclLine actual) {
      return actual.getName();
    }
  }

  private static final class HasMatchCondition
      extends FeatureMatcher<ExprAclLine, AclLineMatchExpr> {

    public HasMatchCondition(@Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
      super(subMatcher, "An IpAccessListLine with matchCondition:", "matchCondition");
    }

    @Override
    protected AclLineMatchExpr featureValueOf(ExprAclLine actual) {
      return actual.getMatchCondition();
    }
  }
}
