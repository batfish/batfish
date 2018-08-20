package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.matchers.EigrpRouteMatchersImpl.HasEigrpMetric;
import org.hamcrest.Matcher;

public final class EigrpRouteMatchers {

  private EigrpRouteMatchers() {}

  /** Returns a matcher asserting that the EIGRP route has the specified metric */
  public static HasEigrpMetric hasEigrpMetric(EigrpMetric metric) {
    return new HasEigrpMetric(equalTo(metric));
  }

  /** Returns a matcher asserting that the EIGRP route has a matching metric */
  public static HasEigrpMetric hasEigrpMetric(Matcher<? super EigrpMetric> metricMatcher) {
    return new HasEigrpMetric(metricMatcher);
  }
}
