package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.batfish.datamodel.matchers.FlowHistoryInfoMatchersImpl.HasFlow;
import org.hamcrest.Matcher;

public final class FlowHistoryInfoMatchers {
  private FlowHistoryInfoMatchers() {}

  public static Matcher<FlowHistoryInfo> hasFlow(Matcher<? super Flow> flowMatcher) {
    return new HasFlow(flowMatcher);
  }
}
