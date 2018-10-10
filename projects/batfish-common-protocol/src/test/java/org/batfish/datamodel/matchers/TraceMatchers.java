package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.matchers.TraceMatchersImpl.HasDisposition;
import org.hamcrest.Matcher;

public class TraceMatchers {
  public static HasDisposition hasDisposition(@Nonnull FlowDisposition flowDisposition) {
    return new HasDisposition(equalTo(flowDisposition));
  }

  public static HasDisposition hasDisposition(
      @Nonnull Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  private TraceMatchers() {}
}
