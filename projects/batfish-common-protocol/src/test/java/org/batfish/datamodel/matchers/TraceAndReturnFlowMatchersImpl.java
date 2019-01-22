package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReturnFlow;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** */
public final class TraceAndReturnFlowMatchersImpl {
  private TraceAndReturnFlowMatchersImpl() {}

  /** */
  public static final class HasReturnFlow extends FeatureMatcher<TraceAndReturnFlow, Flow> {
    HasReturnFlow(Matcher<? super Flow> subMatcher) {
      super(subMatcher, "a TraceAndReturnFlow with returnFlow", "returnFlow");
    }

    @Override
    protected Flow featureValueOf(TraceAndReturnFlow traceAndReturnFlow) {
      return traceAndReturnFlow.getReturnFlow();
    }

    /** */
    public static final class HasTrace extends FeatureMatcher<TraceAndReturnFlow, Trace> {
      HasTrace(Matcher<? super Trace> subMatcher) {
        super(subMatcher, "a TraceAndReturnFlow with trace", "trace");
      }

      @Override
      protected Trace featureValueOf(TraceAndReturnFlow traceAndReturnFlow) {
        return traceAndReturnFlow.getTrace();
      }
    }
  }
}
