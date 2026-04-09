package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.TraceElement;
import org.hamcrest.FeatureMatcher;
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

  private static final class IsExprAclLineThat extends IsInstanceThat<AclLine, ExprAclLine> {
    IsExprAclLineThat(Matcher<? super ExprAclLine> subMatcher) {
      super(ExprAclLine.class, subMatcher);
    }
  }

  private static final class HasTraceElement extends FeatureMatcher<AclLine, TraceElement> {
    public HasTraceElement(Matcher<? super TraceElement> subMatcher) {
      super(subMatcher, "an AclLine with traceElement: ", "traceElement");
    }

    @Override
    protected TraceElement featureValueOf(AclLine aclLine) {
      return aclLine.getTraceElement();
    }
  }
}
