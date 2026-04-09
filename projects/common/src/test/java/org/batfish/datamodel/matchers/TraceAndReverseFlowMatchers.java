package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link TraceAndReverseFlow}. */
public final class TraceAndReverseFlowMatchers {
  private TraceAndReverseFlowMatchers() {}

  /** {@link Matcher} for the reverse {@link Flow}. */
  public static Matcher<TraceAndReverseFlow> hasReverseFlow(Matcher<? super Flow> flowMatcher) {
    return new HasReverseFlow(flowMatcher);
  }

  /** {@link Matcher} for the reverse {@link Flow}. */
  public static Matcher<TraceAndReverseFlow> hasReverseFlow(Flow flow) {
    return new HasReverseFlow(equalTo(flow));
  }

  /** {@link Matcher} for the {@link Trace}. */
  public static Matcher<TraceAndReverseFlow> hasTrace(Matcher<? super Trace> traceMatcher) {
    return new HasTrace(traceMatcher);
  }

  public static Matcher<TraceAndReverseFlow> hasNewFirewallSessions(
      Matcher<? super Set<FirewallSessionTraceInfo>> matcher) {
    return new HasNewFirewallSessions(matcher);
  }

  private static final class HasReverseFlow extends FeatureMatcher<TraceAndReverseFlow, Flow> {
    HasReverseFlow(Matcher<? super Flow> subMatcher) {
      super(subMatcher, "a TraceAndReverseFlow with returnFlow", "returnFlow");
    }

    @Override
    protected Flow featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getReverseFlow();
    }
  }

  private static final class HasNewFirewallSessions
      extends FeatureMatcher<TraceAndReverseFlow, Set<FirewallSessionTraceInfo>> {
    HasNewFirewallSessions(Matcher<? super Set<FirewallSessionTraceInfo>> subMatcher) {
      super(subMatcher, "a TraceAndReverseFlow with newFirewallSessions", "newFirewallSessions");
    }

    @Override
    protected Set<FirewallSessionTraceInfo> featureValueOf(
        TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getNewFirewallSessions();
    }
  }

  private static final class HasTrace extends FeatureMatcher<TraceAndReverseFlow, Trace> {
    HasTrace(Matcher<? super Trace> subMatcher) {
      super(subMatcher, "a TraceAndReverseFlow with trace", "trace");
    }

    @Override
    protected Trace featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getTrace();
    }
  }
}
