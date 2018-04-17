package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FlowTraceHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class FlowTraceHopMatchers {
  public static FlowTraceHopEdgeMatcher hasEdge(Matcher<Edge> subMatcher) {
    return new FlowTraceHopEdgeMatcher(subMatcher);
  }

  private static class FlowTraceHopEdgeMatcher extends FeatureMatcher<FlowTraceHop, Edge> {
    FlowTraceHopEdgeMatcher(Matcher<Edge> subMatcher) {
      super(subMatcher, "a FlowTraceHop with edge:", "edge");
    }

    @Override protected Edge featureValueOf(FlowTraceHop flowTraceHop) {
      return flowTraceHop.getEdge();
    }
  }
}
