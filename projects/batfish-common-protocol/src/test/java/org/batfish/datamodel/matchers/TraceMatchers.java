package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasDisposition;
import org.hamcrest.Matcher;

public class TraceMatchers {

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition is equal to the specified {@link FlowDisposition}
   */
  public static HasDisposition hasDisposition(@Nonnull FlowDisposition flowDisposition) {
    return new HasDisposition(equalTo(flowDisposition));
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.flow.Trace}'s
   * flowDisposition matches the given subMatcher
   */
  public static HasDisposition hasDisposition(
      @Nonnull Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  private TraceMatchers() {}
}
