package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class TraceMatchersImpl {

  static class HasDisposition extends FeatureMatcher<Trace, FlowDisposition> {
    HasDisposition(Matcher<? super FlowDisposition> subMatcher) {
      super(subMatcher, "a Trace with disposition:", "disposition");
    }

    @Override
    protected FlowDisposition featureValueOf(Trace flowTrace) {
      return flowTrace.getDisposition();
    }
  }

  static class HasHops extends FeatureMatcher<Trace, List<Hop>> {
    HasHops(Matcher<? super List<Hop>> subMatcher) {
      super(subMatcher, "a Trace with hops:", "hops");
    }

    @Override
    protected List<Hop> featureValueOf(Trace trace) {
      return trace.getHops();
    }
  }

  private TraceMatchersImpl() {}
}
