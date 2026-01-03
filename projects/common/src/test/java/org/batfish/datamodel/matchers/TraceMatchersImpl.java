package org.batfish.datamodel.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

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

  static class HasNthHop extends FeatureMatcher<Trace, Hop> {
    private final int _n;

    HasNthHop(int n, Matcher<? super Hop> subMatcher) {
      super(subMatcher, "a Trace with hop " + n + ":", "hop " + n);
      _n = n;
    }

    @Override
    protected Hop featureValueOf(Trace trace) {
      assertThat(trace.getHops(), hasSize(greaterThan(_n)));
      return trace.getHops().get(_n);
    }
  }

  static class HasLastHop extends FeatureMatcher<Trace, Hop> {
    HasLastHop(Matcher<? super Hop> subMatcher) {
      super(subMatcher, "a Trace with last hop:", "last hop");
    }

    @Override
    protected Hop featureValueOf(Trace trace) {
      List<Hop> hops = trace.getHops();
      assertThat(hops, not(empty()));
      return hops.get(hops.size() - 1);
    }
  }

  private TraceMatchersImpl() {}
}
