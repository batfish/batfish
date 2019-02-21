package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AndMatchExprMatchersImpl {

  static class HasConjuncts extends FeatureMatcher<AndMatchExpr, SortedSet<AclLineMatchExpr>> {

    public HasConjuncts(@Nonnull Matcher<? super SortedSet<AclLineMatchExpr>> subMatcher) {
      super(subMatcher, "An AndMatchExpr with conjuncts:", "conjuncts");
    }

    @Override
    protected SortedSet<AclLineMatchExpr> featureValueOf(AndMatchExpr actual) {
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
