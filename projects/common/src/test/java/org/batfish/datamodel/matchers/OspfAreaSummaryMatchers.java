package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.InstallsDiscard;
import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.IsAdvertised;
import org.hamcrest.Matcher;

/** Matchers for {@link org.batfish.datamodel.ospf.OspfAreaSummary}. */
public final class OspfAreaSummaryMatchers {

  /** Returns a matcher asserting that the OSPF summary route has the specified metric. */
  public static HasMetric hasMetric(long metric) {
    return new HasMetric(equalTo(metric));
  }

  /** Returns a matcher asserting that the OSPF summary route has a matching metric. */
  public static HasMetric hasMetric(Matcher<? super Long> metricMatcher) {
    return new HasMetric(metricMatcher);
  }

  /** Returns a matcher asserting that the OSPF summary route will be advertised. */
  public static IsAdvertised isAdvertised() {
    return new IsAdvertised(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area
   * summary's advertised flag.
   */
  public static IsAdvertised isAdvertised(Matcher<? super Boolean> subMatcher) {
    return new IsAdvertised(subMatcher);
  }

  /** Returns a matcher asserting that the OSPF summary route will be advertised. */
  public static InstallsDiscard installsDiscard() {
    return new InstallsDiscard(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the OSPF area
   * summary's advertised flag.
   */
  public static InstallsDiscard installsDiscard(Matcher<? super Boolean> subMatcher) {
    return new InstallsDiscard(subMatcher);
  }

  private OspfAreaSummaryMatchers() {}
}
