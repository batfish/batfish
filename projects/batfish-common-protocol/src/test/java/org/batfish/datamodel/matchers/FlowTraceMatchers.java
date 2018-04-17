package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasDisposition;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasHops;
import org.hamcrest.Matcher;

public class FlowTraceMatchers {

  public static HasDisposition hasDisposition(
      @Nonnull Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  public static HasHops hasHops(@Nonnull Matcher<? super List<? extends FlowTraceHop>> subMatcher) {
    return new HasHops(subMatcher);
  }

  private FlowTraceMatchers() {}
}
