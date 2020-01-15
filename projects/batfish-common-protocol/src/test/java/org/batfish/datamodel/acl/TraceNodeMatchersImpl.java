package org.batfish.datamodel.acl;

import java.util.List;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.trace.TraceNode;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class TraceNodeMatchersImpl {
  private TraceNodeMatchersImpl() {}

  public static final class HasChildren extends FeatureMatcher<TraceNode, List<TraceNode>> {
    public HasChildren(Matcher<? super List<TraceNode>> subMatcher) {
      super(subMatcher, "a TraceNode with children: ", "children");
    }

    @Override
    protected List<TraceNode> featureValueOf(TraceNode traceNode) {
      return traceNode.getChildren();
    }
  }

  public static final class HasTraceElement extends FeatureMatcher<TraceNode, TraceElement> {
    public HasTraceElement(Matcher<? super TraceElement> subMatcher) {
      super(subMatcher, "a TraceNode with traceElement: ", "traceElement");
    }

    @Override
    protected TraceElement featureValueOf(TraceNode traceNode) {
      return traceNode.getTraceElement();
    }
  }
}
