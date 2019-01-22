package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.matchers.TraceAndReturnFlowMatchersImpl.HasReturnFlow;
import org.batfish.datamodel.matchers.TraceAndReturnFlowMatchersImpl.HasReturnFlow.HasTrace;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link TraceAndReverseFlow}. */
public final class TraceAndReturnFlowMatchers {
  private TraceAndReturnFlowMatchers() {}

  public static HasReturnFlow hasReturnFlow(Matcher<? super Flow> flowMatcher) {
    return new HasReturnFlow(flowMatcher);
  }

  public static HasTrace hasTrace(Matcher<? super Trace> traceMatcher) {
    return new HasTrace(traceMatcher);
  }
}
