package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.AdministrativeCostExpr;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class SetAdministrativeCostMatchersImpl {

  static class HasAdmin extends FeatureMatcher<SetAdministrativeCost, AdministrativeCostExpr> {

    public HasAdmin(@Nonnull Matcher<? super AdministrativeCostExpr> subMatcher) {
      super(subMatcher, "A SetAdministrativeCost statement with adminCost:", "adminCost");
    }

    @Override
    protected AdministrativeCostExpr featureValueOf(SetAdministrativeCost actual) {
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
