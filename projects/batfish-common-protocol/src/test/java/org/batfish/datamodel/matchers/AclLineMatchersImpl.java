package org.batfish.datamodel.matchers;

import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.TraceElement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AclLineMatchersImpl {

  static final class IsExprAclLineThat extends IsInstanceThat<AclLine, ExprAclLine> {
    IsExprAclLineThat(Matcher<? super ExprAclLine> subMatcher) {
      super(ExprAclLine.class, subMatcher);
    }
  }

  static final class HasTraceElement extends FeatureMatcher<AclLine, TraceElement> {
    public HasTraceElement(Matcher<? super TraceElement> subMatcher) {
      super(subMatcher, "an AclLine with traceElement: ", "traceElement");
    }

    @Override
    protected TraceElement featureValueOf(AclLine aclLine) {
      return aclLine.getTraceElement();
    }
  }

  private AclLineMatchersImpl() {}
}
