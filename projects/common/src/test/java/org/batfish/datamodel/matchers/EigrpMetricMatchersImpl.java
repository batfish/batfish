package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

class EigrpMetricMatchersImpl {

  static final class HasBandwidth extends FeatureMatcher<EigrpMetric, Long> {
    HasBandwidth(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with bandwidth:", "bandwidth");
    }

    @Override
    protected @Nullable Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getBandwidth();
    }
  }

  static final class HasDelay extends FeatureMatcher<EigrpMetric, Long> {
    HasDelay(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    protected @Nonnull Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getDelay();
    }
  }
}
