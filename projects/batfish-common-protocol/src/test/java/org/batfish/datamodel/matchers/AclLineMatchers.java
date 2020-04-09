package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.matchers.AclLineMatchersImpl.HasTraceElement;
import org.batfish.datamodel.matchers.AclLineMatchersImpl.IsExprAclLineThat;
import org.hamcrest.Matcher;

public class AclLineMatchers {

  /**
   * Provides a matcher that matches {@link ExprAclLine}s that match the provided {@code
   * subMatcher}.
   */
  public static IsExprAclLineThat isExprAclLineThat(
      @Nonnull Matcher<? super ExprAclLine> subMatcher) {
    return new IsExprAclLineThat(subMatcher);
  }

  public static HasTraceElement hasTraceElement(@Nonnull TraceElement traceElement) {
    return new HasTraceElement(equalTo(traceElement));
  }
}
