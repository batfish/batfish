package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.LiteralIntMatchersImpl.HasVal;
import org.batfish.datamodel.matchers.LiteralIntMatchersImpl.IsLiteralIntThat;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.hamcrest.Matcher;

public class LiteralIntMatcher {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the LiteralInt's
   * value.
   */
  public static HasVal hasVal(Integer val) {
    return new HasVal(equalTo(val));
  }

  /**
   * Provides a matcher that matches if the object is a {@link LiteralInt} matched by the provided
   * {@code subMatcher}.
   */
  public static IsLiteralIntThat isLiteralIntThat(@Nonnull Matcher<? super LiteralInt> subMatcher) {
    return new IsLiteralIntThat(subMatcher);
  }

  private LiteralIntMatcher() {}
}
