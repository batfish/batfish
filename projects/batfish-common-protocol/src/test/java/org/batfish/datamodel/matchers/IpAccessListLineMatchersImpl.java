package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class IpAccessListLineMatchersImpl {

  static class HasMatchCondition extends FeatureMatcher<IpAccessListLine, AclLineMatchExpr> {

    public HasMatchCondition(@Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
      super(subMatcher, "An IpAcessListLine with matchCondition:", "matchCondition");
    }

    @Override
    protected AclLineMatchExpr featureValueOf(IpAccessListLine actual) {
      return actual.getMatchCondition();
    }
  }

  private IpAccessListLineMatchersImpl() {}
}
