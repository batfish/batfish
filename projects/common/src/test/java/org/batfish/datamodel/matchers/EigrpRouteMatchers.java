package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class EigrpRouteMatchers {

  private EigrpRouteMatchers() {}

  /** Returns a matcher asserting that the EIGRP route has the specified metric */
  public static Matcher<EigrpRoute> hasEigrpMetric(EigrpMetric metric) {
    return new HasEigrpMetric(equalTo(metric));
  }

  /** Returns a matcher asserting that the EIGRP route has a matching metric */
  public static Matcher<EigrpRoute> hasEigrpMetric(Matcher<? super EigrpMetric> metricMatcher) {
    return new HasEigrpMetric(metricMatcher);
  }

  private static final class HasEigrpMetric extends FeatureMatcher<EigrpRoute, EigrpMetric> {
    HasEigrpMetric(@Nonnull Matcher<? super EigrpMetric> subMatcher) {
      super(subMatcher, "An EigrpRoute with metric:", "metric");
    }

    @Override
    protected EigrpMetric featureValueOf(EigrpRoute actual) {
      return actual.getEigrpMetric();
    }
  }
}
