package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class LiteralIntMatcher {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the LiteralInt's
   * value.
   */
  public static Matcher<LiteralInt> hasVal(Integer val) {
    return new HasVal(equalTo(val));
  }

  /**
   * Provides a matcher that matches if the object is a {@link LiteralInt} matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<IntExpr> isLiteralIntThat(@Nonnull Matcher<? super LiteralInt> subMatcher) {
    return new IsLiteralIntThat(subMatcher);
  }

  private LiteralIntMatcher() {}

  private static final class HasVal extends FeatureMatcher<LiteralInt, Integer> {

    public HasVal(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A LiteralInt with value:", "value");
    }

    @Override
    protected Integer featureValueOf(LiteralInt actual) {
      return actual.getValue();
    }
  }

  private static final class IsLiteralIntThat extends IsInstanceThat<IntExpr, LiteralInt> {
    IsLiteralIntThat(@Nonnull Matcher<? super LiteralInt> subMatcher) {
      super(LiteralInt.class, subMatcher);
    }
  }
}
