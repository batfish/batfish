package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.matchers.FlowTraceHopMatchersImpl.HasEdge;
import org.hamcrest.Matcher;

public final class FlowTraceHopMatchers {

  public static HasEdge hasEdge(@Nonnull Matcher<? super Edge> subMatcher) {
    return new HasEdge(subMatcher);
  }

  private FlowTraceHopMatchers() {}
}
