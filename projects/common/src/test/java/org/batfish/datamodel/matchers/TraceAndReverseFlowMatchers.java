package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.matchers.TraceAndReverseFlowMatchersImpl.HasNewFirewallSessions;
import org.batfish.datamodel.matchers.TraceAndReverseFlowMatchersImpl.HasReverseFlow;
import org.batfish.datamodel.matchers.TraceAndReverseFlowMatchersImpl.HasTrace;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link TraceAndReverseFlow}. */
public final class TraceAndReverseFlowMatchers {
  private TraceAndReverseFlowMatchers() {}

  /** {@link Matcher} for the reverse {@link Flow}. */
  public static HasReverseFlow hasReverseFlow(Matcher<? super Flow> flowMatcher) {
    return new HasReverseFlow(flowMatcher);
  }

  /** {@link Matcher} for the reverse {@link Flow}. */
  public static HasReverseFlow hasReverseFlow(Flow flow) {
    return new HasReverseFlow(equalTo(flow));
  }

  /** {@link Matcher} for the {@link Trace}. */
  public static HasTrace hasTrace(Matcher<? super Trace> traceMatcher) {
    return new HasTrace(traceMatcher);
  }

  public static HasNewFirewallSessions hasNewFirewallSessions(
      Matcher<? super Set<FirewallSessionTraceInfo>> matcher) {
    return new HasNewFirewallSessions(matcher);
  }
}
