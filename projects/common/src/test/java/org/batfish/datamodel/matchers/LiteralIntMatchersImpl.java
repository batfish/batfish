package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class LiteralIntMatchersImpl {

  static class HasVal extends FeatureMatcher<LiteralInt, Integer> {

    public HasVal(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A LiteralInt with value:", "value");
    }

    @Override
    protected Integer featureValueOf(LiteralInt actual) {
      return actual.getValue();
    }
  }

  static class IsLiteralIntThat extends IsInstanceThat<IntExpr, LiteralInt> {
    IsLiteralIntThat(@Nonnull Matcher<? super LiteralInt> subMatcher) {
      super(LiteralInt.class, subMatcher);
    }
  }

  private LiteralIntMatchersImpl() {}
}
