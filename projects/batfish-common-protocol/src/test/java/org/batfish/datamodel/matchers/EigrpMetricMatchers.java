package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.matchers.EigrpMetricMatchersImpl.HasBandwidth;
import org.batfish.datamodel.matchers.EigrpMetricMatchersImpl.HasCost;
import org.batfish.datamodel.matchers.EigrpMetricMatchersImpl.HasDelay;
import org.hamcrest.Matcher;

public class EigrpMetricMatchers {
  /** Provides a matcher that matches if the {@link EigrpMetric}'s cost is {@code expectedCost}. */
  public static @Nonnull Matcher<EigrpMetric> hasCost(long expectedCost) {
    return new HasCost(equalTo(expectedCost));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpMetric}'s cost.
   */
  public static @Nonnull Matcher<EigrpMetric> hasCost(@Nonnull Matcher<? super Long> subMatcher) {
    return new HasCost(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link EigrpMetric}'s delay is {@code expectedDelay}.
   */
  public static @Nonnull Matcher<EigrpMetric> hasDelay(double expectedDelay) {
    return new HasDelay(equalTo(expectedDelay));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpMetric}'s delay.
   */
  public static @Nonnull Matcher<EigrpMetric> hasDelay(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasDelay(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link EigrpMetric}'s bandwidth is {@code
   * expectedBandwidth}.
   */
  public static @Nonnull Matcher<EigrpMetric> hasBandwidth(double expectedBandwidth) {
    return new HasBandwidth(equalTo(expectedBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * EigrpMetric}'s bandwidth.
   */
  public static @Nonnull Matcher<EigrpMetric> hasBandwidth(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasBandwidth(subMatcher);
  }
}
