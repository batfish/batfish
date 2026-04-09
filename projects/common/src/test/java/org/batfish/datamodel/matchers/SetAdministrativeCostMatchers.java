package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.AdministrativeCostExpr;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class SetAdministrativeCostMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * AdministrativeCost's admin.
   */
  public static Matcher<SetAdministrativeCost> hasAdmin(
      Matcher<AdministrativeCostExpr> subMatcher) {
    return new HasAdmin(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link SetAdministrativeCost} matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<Statement> isSetAdministrativeCostThat(
      @Nonnull Matcher<? super SetAdministrativeCost> subMatcher) {
    return new IsSetAdministrativeCostThat(subMatcher);
  }

  private SetAdministrativeCostMatchers() {}

  private static final class HasAdmin
      extends FeatureMatcher<SetAdministrativeCost, AdministrativeCostExpr> {

    public HasAdmin(@Nonnull Matcher<? super AdministrativeCostExpr> subMatcher) {
      super(subMatcher, "A SetAdministrativeCost statement with adminCost:", "adminCost");
    }

    @Override
    protected AdministrativeCostExpr featureValueOf(SetAdministrativeCost actual) {
      return actual.getAdmin();
    }
  }

  private static final class IsSetAdministrativeCostThat
      extends IsInstanceThat<Statement, SetAdministrativeCost> {
    IsSetAdministrativeCostThat(@Nonnull Matcher<? super SetAdministrativeCost> subMatcher) {
      super(SetAdministrativeCost.class, subMatcher);
    }
  }
}
