package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class OrMatchExprMatchersImpl {

  static class HasDisjuncts extends FeatureMatcher<OrMatchExpr, SortedSet<AclLineMatchExpr>> {

    public HasDisjuncts(@Nonnull Matcher<? super SortedSet<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An OrMatchExpr with disjuncts:", "disjuncts");
    }

    @Override
    protected SortedSet<AclLineMatchExpr> featureValueOf(OrMatchExpr actual) {
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
