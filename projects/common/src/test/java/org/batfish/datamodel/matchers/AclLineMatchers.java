package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.matchers.AclLineMatchersImpl.HasTraceElement;
import org.batfish.datamodel.matchers.AclLineMatchersImpl.IsExprAclLineThat;
import org.hamcrest.Matcher;

public final class AclLineMatchers {

  /**
   * Provides a matcher that matches an {@link ExprAclLine}s matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<AclLine> isExprAclLineThat(
      @Nonnull Matcher<? super ExprAclLine> subMatcher) {
    return new IsExprAclLineThat(subMatcher);
  }

  public static @Nonnull Matcher<AclLine> hasTraceElement(@Nonnull TraceElement traceElement) {
    return new HasTraceElement(equalTo(traceElement));
  }

  private AclLineMatchers() {}
}
