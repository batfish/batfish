package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class ExprAclLineMatchersImpl {

  static class HasAction extends FeatureMatcher<ExprAclLine, LineAction> {

    public HasAction(@Nonnull Matcher<? super LineAction> subMatcher) {
      super(subMatcher, "An IpAcessListLine with action:", "action");
    }

    @Override
    protected LineAction featureValueOf(ExprAclLine actual) {
      return actual.getAction();
    }
  }

  static class HasName extends FeatureMatcher<ExprAclLine, String> {
    public HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IpAccessListLine with name:", "name");
    }

    @Override
    protected String featureValueOf(ExprAclLine actual) {
      return actual.getName();
    }
  }

  static class HasMatchCondition extends FeatureMatcher<ExprAclLine, AclLineMatchExpr> {

    public HasMatchCondition(@Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
      super(subMatcher, "An IpAccessListLine with matchCondition:", "matchCondition");
    }

    @Override
    protected AclLineMatchExpr featureValueOf(ExprAclLine actual) {
      return actual.getMatchCondition();
    }
  }

  private ExprAclLineMatchersImpl() {}
}
