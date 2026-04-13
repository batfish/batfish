package org.batfish.datamodel.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
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

/** {@link Matcher Matchers} for {@link Trace}. */
@ParametersAreNonnullByDefault
public final class TraceMatchers {

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition is equal to the specified {@link FlowDisposition}
   */
  public static Matcher<Trace> hasDisposition(FlowDisposition flowDisposition) {
    return new HasDisposition(equalTo(flowDisposition));
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition matches the given subMatcher
   */
  public static Matcher<Trace> hasDisposition(Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  /** A {@link Matcher} for {@link Trace} {@link Hop hops}. */
  public static Matcher<Trace> hasHops(Matcher<? super List<? extends Hop>> hopsMatcher) {
    return new HasHops(hopsMatcher);
  }

  /** A {@link Matcher} for a {@link Trace} with a single {@link Hop}. */
  public static Matcher<Trace> hasHop(Matcher<? super Hop> hopMatcher) {
    return hasHops(contains(hopMatcher));
  }

  /** A {@link Matcher} for the last hop in a {@link Trace}. */
  public static Matcher<Trace> hasLastHop(Matcher<? super Hop> hopMatcher) {
    return new HasLastHop(hopMatcher);
  }

  /** A {@link Matcher} for the nth hop in a {@link Trace}. */
  public static Matcher<Trace> hasNthHop(int n, Matcher<? super Hop> hopMatcher) {
    return new HasNthHop(n, hopMatcher);
  }

  private TraceMatchers() {}

  private static final class HasDisposition extends FeatureMatcher<Trace, FlowDisposition> {
    HasDisposition(Matcher<? super FlowDisposition> subMatcher) {
      super(subMatcher, "a Trace with disposition:", "disposition");
    }

    @Override
    protected FlowDisposition featureValueOf(Trace flowTrace) {
      return flowTrace.getDisposition();
    }
  }

  private static final class HasHops extends FeatureMatcher<Trace, List<Hop>> {
    HasHops(Matcher<? super List<Hop>> subMatcher) {
      super(subMatcher, "a Trace with hops:", "hops");
    }

    @Override
    protected List<Hop> featureValueOf(Trace trace) {
      return trace.getHops();
    }
  }

  private static final class HasNthHop extends FeatureMatcher<Trace, Hop> {
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

  private static final class HasLastHop extends FeatureMatcher<Trace, Hop> {
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
}
