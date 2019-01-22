package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** */
public final class TraceAndReturnFlowMatchersImpl {
  private TraceAndReturnFlowMatchersImpl() {}

  /** */
  public static final class HasReturnFlow extends FeatureMatcher<TraceAndReverseFlow, Flow> {
    HasReturnFlow(Matcher<? super Flow> subMatcher) {
      super(subMatcher, "a TraceAndReturnFlow with returnFlow", "returnFlow");
    }

    @Override
    protected Flow featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
      return traceAndReverseFlow.getReverseFlow();
    }

    /** */
    public static final class HasTrace extends FeatureMatcher<TraceAndReverseFlow, Trace> {
      HasTrace(Matcher<? super Trace> subMatcher) {
        super(subMatcher, "a TraceAndReturnFlow with trace", "trace");
      }

      @Override
      protected Trace featureValueOf(TraceAndReverseFlow traceAndReverseFlow) {
        return traceAndReverseFlow.getTrace();
      }
    }
  }
}
