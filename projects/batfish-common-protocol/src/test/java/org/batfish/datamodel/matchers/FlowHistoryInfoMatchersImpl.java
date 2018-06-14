package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory.FlowHistoryInfo;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class FlowHistoryInfoMatchersImpl {

  static class HasFlow extends FeatureMatcher<FlowHistoryInfo, Flow> {
    HasFlow(Matcher<? super Flow> subMatcher) {
      super(subMatcher, "the Flow", "flow");
    }

    @Override
    protected Flow featureValueOf(FlowHistoryInfo flowHistoryInfo) {
      return flowHistoryInfo.getFlow();
    }
  }
}
