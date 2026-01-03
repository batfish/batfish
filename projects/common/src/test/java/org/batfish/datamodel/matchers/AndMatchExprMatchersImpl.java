package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AndMatchExprMatchersImpl {

  static class HasConjuncts extends FeatureMatcher<AndMatchExpr, List<AclLineMatchExpr>> {

    public HasConjuncts(@Nonnull Matcher<? super List<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An AndMatchExpr with conjuncts:", "conjuncts");
    }

    @Override
    protected List<AclLineMatchExpr> featureValueOf(AndMatchExpr actual) {
      return actual.getConjuncts();
    }
  }

  static class IsAndMatchExprThat extends IsInstanceThat<AclLineMatchExpr, AndMatchExpr> {
    IsAndMatchExprThat(@Nonnull Matcher<? super AndMatchExpr> subMatcher) {
      super(AndMatchExpr.class, subMatcher);
    }
  }

  private AndMatchExprMatchersImpl() {}
}
