package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.matchers.TraceAndReturnFlowMatchersImpl.HasReverseFlow;
import org.batfish.datamodel.matchers.TraceAndReturnFlowMatchersImpl.HasTrace;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link TraceAndReverseFlow}. */
public final class TraceAndReverseFlowMatchers {
  private TraceAndReverseFlowMatchers() {}

  /** {@link Matcher} for the reverse {@link Flow}. */
  public static HasReverseFlow hasReverseFlow(Matcher<? super Flow> flowMatcher) {
    return new HasReverseFlow(flowMatcher);
  }

  /** {@link Matcher} for the {@link Trace}. */
  public static HasTrace hasTrace(Matcher<? super Trace> traceMatcher) {
    return new HasTrace(traceMatcher);
  }
}
