package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class FlowTraceMatchers {
  public static FlowTraceDispositionMatcher hasDisposition(
      Matcher<FlowDisposition> flowDispositionMatcher) {
    return new FlowTraceDispositionMatcher(flowDispositionMatcher);
  }

  public static FlowTraceDispositionMatcher hasDisposition(FlowDisposition flowDisposition) {
    return new FlowTraceDispositionMatcher(equalTo(flowDisposition));
  }

  public static FlowTraceHopsMatcher hasHops(Matcher<Iterable<? extends FlowTraceHop>> hopsMatcher) {
    return new FlowTraceHopsMatcher(hopsMatcher);
  }

  private static class FlowTraceDispositionMatcher extends FeatureMatcher<FlowTrace, FlowDisposition> {
    public FlowTraceDispositionMatcher(Matcher<FlowDisposition> flowDispositionMatcher) {
      super(flowDispositionMatcher, "a FlowTrace with disposition:", "disposition");
    }

    @Override protected FlowDisposition featureValueOf(FlowTrace flowTrace) {
      return flowTrace.getDisposition();
    }
  }

  private static class FlowTraceHopsMatcher extends FeatureMatcher<FlowTrace, List<FlowTraceHop>> {
    public FlowTraceHopsMatcher(Matcher<Iterable<? extends FlowTraceHop>> hopsMatcher) {
      super(hopsMatcher, "a FlowTrace with hops:", "hops");
    }

    @Override protected List<FlowTraceHop> featureValueOf(FlowTrace flowTrace) {
      return flowTrace.getHops();
    }
  }
}
