package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasDisposition;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasHops;
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

  public static HasDisposition hasDisposition(
      @Nonnull Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  public static HasHops hasHops(@Nonnull Matcher<? super List<? extends FlowTraceHop>> subMatcher) {
    return new HasHops(subMatcher);
  }

  private FlowTraceMatchers() {}
}
