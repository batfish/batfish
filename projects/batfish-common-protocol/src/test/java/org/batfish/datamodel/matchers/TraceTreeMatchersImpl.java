package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.trace.TraceTree;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class TraceTreeMatchersImpl {
  private TraceTreeMatchersImpl() {}

  public static final class HasChildren extends FeatureMatcher<TraceTree, List<TraceTree>> {
    public HasChildren(Matcher<? super List<TraceTree>> subMatcher) {
      super(subMatcher, "a TraceTree with children: ", "children");
    }

    @Override
    protected List<TraceTree> featureValueOf(TraceTree traceTree) {
      return traceTree.getChildren();
    }
  }

  public static final class HasTraceElement extends FeatureMatcher<TraceTree, TraceElement> {
    public HasTraceElement(Matcher<? super TraceElement> subMatcher) {
      super(subMatcher, "a TraceTree with traceElement: ", "traceElement");
    }

    @Override
    protected TraceElement featureValueOf(TraceTree traceTree) {
      return traceTree.getTraceElement();
    }
  }
}
