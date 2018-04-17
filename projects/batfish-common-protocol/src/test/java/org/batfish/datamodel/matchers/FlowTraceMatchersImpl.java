package org.batfish.datamodel.matchers;

import static org.batfish.datamodel.matchers.FlowTraceMatchers.hasHops;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

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

  static class HasHop extends FeatureMatcher<FlowTrace, FlowTraceHop> {
    private final int _index;

    public HasHop(int index, Matcher<? super FlowTraceHop> subMatcher) {
      super(
          subMatcher,
          String.format("a FlowTrace with hop %d:", index),
          String.format("hop %d", index));
      _index = index;
    }

    @Override
    protected FlowTraceHop featureValueOf(FlowTrace flowTrace) {
      assertThat(flowTrace, hasHops(hasSize(greaterThan(_index))));
      return flowTrace.getHops().get(_index);
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
