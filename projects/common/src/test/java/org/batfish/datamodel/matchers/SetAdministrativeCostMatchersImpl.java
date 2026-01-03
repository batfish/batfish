package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class SetAdministrativeCostMatchersImpl {

  static class HasAdmin extends FeatureMatcher<SetAdministrativeCost, IntExpr> {

    public HasAdmin(@Nonnull Matcher<? super IntExpr> subMatcher) {
      super(subMatcher, "A SetAdministrativeCost statement with adminCost:", "adminCost");
    }

    @Override
    protected IntExpr featureValueOf(SetAdministrativeCost actual) {
      return actual.getAdmin();
    }
  }

  static class IsSetAdministrativeCostThat
      extends IsInstanceThat<Statement, SetAdministrativeCost> {
    IsSetAdministrativeCostThat(@Nonnull Matcher<? super SetAdministrativeCost> subMatcher) {
      super(SetAdministrativeCost.class, subMatcher);
    }
  }

  private SetAdministrativeCostMatchersImpl() {}
}
