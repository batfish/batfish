package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OrMatchExprMatchersImpl {

  static class HasDisjuncts extends FeatureMatcher<OrMatchExpr, List<AclLineMatchExpr>> {

    public HasDisjuncts(@Nonnull Matcher<? super List<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An OrMatchExpr with disjuncts:", "disjuncts");
    }

    @Override
    protected List<AclLineMatchExpr> featureValueOf(OrMatchExpr actual) {
      return actual.getDisjuncts();
    }
  }

  static class IsOrMatchExprThat extends IsInstanceThat<AclLineMatchExpr, OrMatchExpr> {
    IsOrMatchExprThat(@Nonnull Matcher<? super OrMatchExpr> subMatcher) {
      super(OrMatchExpr.class, subMatcher);
    }
  }

  private OrMatchExprMatchersImpl() {}
}
