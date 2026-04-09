package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class EigrpMetricMatchers {

  /**
   * Provides a matcher that matches if the {@link EigrpMetric}'s delay is {@code expectedDelay}.
   */
  public static @Nonnull Matcher<EigrpMetric> hasDelay(long expectedDelay) {
    return new HasDelay(equalTo(expectedDelay));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpMetric}'s delay.
   */
  public static @Nonnull Matcher<EigrpMetric> hasDelay(@Nonnull Matcher<? super Long> subMatcher) {
    return new HasDelay(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link EigrpMetric}'s bandwidth is {@code
   * expectedBandwidth}.
   */
  public static @Nonnull Matcher<EigrpMetric> hasBandwidth(long expectedBandwidth) {
    return new HasBandwidth(equalTo(expectedBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpMetric}'s bandwidth.
   */
  public static @Nonnull Matcher<EigrpMetric> hasBandwidth(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasBandwidth(subMatcher);
  }

  private static final class HasBandwidth extends FeatureMatcher<EigrpMetric, Long> {
    HasBandwidth(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with bandwidth:", "bandwidth");
    }

    @Override
    protected @Nullable Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getBandwidth();
    }
  }

  private static final class HasDelay extends FeatureMatcher<EigrpMetric, Long> {
    HasDelay(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An EigrpMetric with delay:", "delay");
    }

    @Override
    protected @Nonnull Long featureValueOf(EigrpMetric actual) {
      return actual.getValues().getDelay();
    }
  }
}
