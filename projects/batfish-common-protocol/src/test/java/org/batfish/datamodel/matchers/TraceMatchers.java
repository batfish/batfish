package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasDisposition;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasHops;
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

  private TraceMatchers() {}
}
