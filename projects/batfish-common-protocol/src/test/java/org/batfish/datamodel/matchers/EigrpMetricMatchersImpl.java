package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

class EigrpMetricMatchersImpl {

  static final class HasBandwidth extends FeatureMatcher<EigrpMetric, Long> {
    HasBandwidth(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with bandwidth:", "bandwidth");
    }

    @Override
    @Nonnull
    protected Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getBandwidth();
    }
  }

  static final class HasCost extends FeatureMatcher<EigrpMetric, Long> {
    HasCost(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    @Nonnull
    protected Long featureValueOf(EigrpMetric actual) {
      return actual.cost();
    }
  }

  static final class HasDelay extends FeatureMatcher<EigrpMetric, Long> {
    HasDelay(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    @Nonnull
    protected Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getDelay();
    }
  }
}
