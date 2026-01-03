package org.batfish.datamodel.matchers;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class TraceAndReverseFlowMatchersImpl {
  private TraceAndReverseFlowMatchersImpl() {}

  static final class HasReverseFlow extends FeatureMatcher<TraceAndReverseFlow, Flow> {
    HasReverseFlow(Matcher<? super Flow> subMatcher) {
      super(subMatcher, "a TraceAndReverseFlow with returnFlow", "returnFlow");
    }

    @Override
    protected Flow featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getReverseFlow();
    }
  }

  static final class HasNewFirewallSessions
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

  static final class HasTrace extends FeatureMatcher<TraceAndReverseFlow, Trace> {
    HasTrace(Matcher<? super Trace> subMatcher) {
      super(subMatcher, "a TraceAndReverseFlow with trace", "trace");
    }

    @Override
    protected Trace featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getTrace();
    }
  }
}
