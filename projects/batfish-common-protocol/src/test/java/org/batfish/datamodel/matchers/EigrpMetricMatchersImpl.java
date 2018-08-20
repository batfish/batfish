package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

class EigrpMetricMatchersImpl {

  static final class HasBandwidth extends FeatureMatcher<EigrpMetric, Double> {
    HasBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "An EigrpMetric with bandwidth:", "bandwidth");
    }

    @Override
    @Nonnull
    protected Double featureValueOf(EigrpMetric actual) {
      return actual.getBandwidth();
    }
  }

  static final class HasCost extends FeatureMatcher<EigrpMetric, Long> {
    HasCost(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    @Nonnull
    protected Long featureValueOf(EigrpMetric actual) {
      return actual.getCost();
    }
  }

  static final class HasDelay extends FeatureMatcher<EigrpMetric, Double> {
    HasDelay(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    @Nonnull
    protected Double featureValueOf(EigrpMetric actual) {
      return actual.getDelay();
    }
  }
}
