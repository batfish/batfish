package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.SetAdministrativeCostMatchersImpl.HasAdmin;
import org.batfish.datamodel.matchers.SetAdministrativeCostMatchersImpl.IsSetAdministrativeCostThat;
import org.batfish.datamodel.routing_policy.expr.AdministrativeCostExpr;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.hamcrest.Matcher;

public class SetAdministrativeCostMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * AdministrativeCost's admin.
   */
  public static HasAdmin hasAdmin(Matcher<AdministrativeCostExpr> subMatcher) {
    return new SetAdministrativeCostMatchersImpl.HasAdmin(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link SetAdministrativeCost} matched by the
   * provided {@code subMatcher}.
   */
  public static IsSetAdministrativeCostThat isSetAdministrativeCostThat(
      @Nonnull Matcher<? super SetAdministrativeCost> subMatcher) {
    return new IsSetAdministrativeCostThat(subMatcher);
  }

  private SetAdministrativeCostMatchers() {}
}
