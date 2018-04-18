package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FlowTraceHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class FlowTraceHopMatchersImpl {

  static class HasEdge extends FeatureMatcher<FlowTraceHop, Edge> {
    HasEdge(@Nonnull Matcher<? super Edge> subMatcher) {
      super(subMatcher, "a FlowTraceHop with edge:", "edge");
    }

    @Override
    protected Edge featureValueOf(FlowTraceHop flowTraceHop) {
      return flowTraceHop.getEdge();
    }
  }

  private FlowTraceHopMatchersImpl() {}
}
