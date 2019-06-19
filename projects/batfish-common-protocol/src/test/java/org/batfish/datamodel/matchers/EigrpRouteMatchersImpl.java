package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class EigrpRouteMatchersImpl {

  private EigrpRouteMatchersImpl() {}

  static final class HasEigrpMetric extends FeatureMatcher<EigrpRoute, EigrpMetric> {
    HasEigrpMetric(@Nonnull Matcher<? super EigrpMetric> subMatcher) {
      super(subMatcher, "An EigrpRoute with metric:", "metric");
    }

    @Override
    protected EigrpMetric featureValueOf(EigrpRoute actual) {
      return actual.getEigrpMetric();
    }
  }
}
