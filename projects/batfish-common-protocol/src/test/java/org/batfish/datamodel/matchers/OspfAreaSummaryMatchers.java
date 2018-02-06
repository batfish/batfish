package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nullable;
import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.OspfAreaSummaryMatchersImpl.IsAdvertised;
import org.hamcrest.Matcher;

/** Matchers for {@link org.batfish.datamodel.OspfAreaSummary}. */
public final class OspfAreaSummaryMatchers {

  public static IsAdvertised isAdvertised() {
    return new IsAdvertised();
  }

  public static HasMetric hasMetric(@Nullable Long metric) {
    return new HasMetric(equalTo(metric));
  }

  public static HasMetric hasMetric(Matcher<? super Long> metric) {
    return new HasMetric(metric);
  }

  private OspfAreaSummaryMatchers() {}
}
