package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.IsAdvertised;
import org.hamcrest.Matcher;

/** Matchers for {@link org.batfish.datamodel.OspfAreaSummary}. */
public final class OspfAreaSummaryMatchers {

  /** Returns a matcher asserting that the OSPF summary route will be advertised. */
  public static IsAdvertised isAdvertised() {
    return new IsAdvertised();
  }

  /** Returns a matcher asserting that the OSPF summary route has the specified metric. */
  public static HasMetric hasMetric(long metric) {
    return new HasMetric(equalTo(metric));
  }

  /** Returns a matcher asserting that the OSPF summary route has a matching metric. */
  public static HasMetric hasMetric(Matcher<? super Long> metricMatcher) {
    return new HasMetric(metricMatcher);
  }

  private OspfAreaSummaryMatchers() {}
}
