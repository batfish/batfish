package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class FlowTraceMatchersImpl {

  static class HasDisposition extends FeatureMatcher<FlowTrace, FlowDisposition> {
    public HasDisposition(@Nonnull Matcher<? super FlowDisposition> subMatcher) {
      super(subMatcher, "a FlowTrace with disposition:", "disposition");
    }

    @Override
    protected FlowDisposition featureValueOf(FlowTrace flowTrace) {
      return flowTrace.getDisposition();
    }
  }

  static class HasHops extends FeatureMatcher<FlowTrace, List<FlowTraceHop>> {
    public HasHops(@Nonnull Matcher<? super List<? extends FlowTraceHop>> subMatcher) {
      super(subMatcher, "a FlowTrace with hops:", "hops");
    }

    @Override
    protected List<FlowTraceHop> featureValueOf(FlowTrace flowTrace) {
      return flowTrace.getHops();
    }
  }

  private FlowTraceMatchersImpl() {}
}
