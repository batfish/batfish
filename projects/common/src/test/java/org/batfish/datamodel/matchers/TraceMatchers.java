package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasDisposition;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasHops;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasLastHop;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasNthHop;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Trace}. */
@ParametersAreNonnullByDefault
public final class TraceMatchers {

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition is equal to the specified {@link FlowDisposition}
   */
  public static HasDisposition hasDisposition(FlowDisposition flowDisposition) {
    return new HasDisposition(equalTo(flowDisposition));
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition matches the given subMatcher
   */
  public static HasDisposition hasDisposition(Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  /** A {@link Matcher} for {@link Trace} {@link Hop hops}. */
  public static HasHops hasHops(Matcher<? super List<? extends Hop>> hopsMatcher) {
    return new HasHops(hopsMatcher);
  }

  /** A {@link Matcher} for a {@link Trace} with a single {@link Hop}. */
  public static HasHops hasHop(Matcher<? super Hop> hopMatcher) {
    return hasHops(contains(hopMatcher));
  }

  /** A {@link Matcher} for the last hop in a {@link Trace}. */
  public static HasLastHop hasLastHop(Matcher<? super Hop> hopMatcher) {
    return new HasLastHop(hopMatcher);
  }

  /** A {@link Matcher} for the nth hop in a {@link Trace}. */
  public static HasNthHop hasNthHop(int n, Matcher<? super Hop> hopMatcher) {
    return new HasNthHop(n, hopMatcher);
  }

  private TraceMatchers() {}
}
