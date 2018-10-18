package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Trace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class TraceMatchersImpl {

  static class HasDisposition extends FeatureMatcher<Trace, FlowDisposition> {
    public HasDisposition(@Nonnull Matcher<? super FlowDisposition> subMatcher) {
      super(subMatcher, "a Trace with disposition:", "disposition");
    }

    @Override
    protected FlowDisposition featureValueOf(Trace flowTrace) {
      return flowTrace.getDisposition();
    }
  }

  private TraceMatchersImpl() {}
}
