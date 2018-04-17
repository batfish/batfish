package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasDisposition;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasHop;
import org.batfish.datamodel.matchers.FlowTraceMatchersImpl.HasHops;
import org.hamcrest.Matcher;

public class FlowTraceMatchers {
  public static HasDisposition hasDisposition(@Nonnull FlowDisposition flowDisposition) {
    return new HasDisposition(equalTo(flowDisposition));
  }

  public static HasDisposition hasDisposition(
      @Nonnull Matcher<? super FlowDisposition> subMatcher) {
    return new HasDisposition(subMatcher);
  }

  public static HasHops hasHops(@Nonnull Matcher<? super List<? extends FlowTraceHop>> subMatcher) {
    return new HasHops(subMatcher);
  }

  public static HasHop hasHop(int index, @Nonnull Matcher<? super FlowTraceHop> subMatcher) {
    return new HasHop(index, subMatcher);
  }

  private FlowTraceMatchers() {}
}
