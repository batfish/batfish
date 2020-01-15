package org.batfish.datamodel.acl;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.TraceNodeMatchersImpl.HasChildren;
import org.batfish.datamodel.acl.TraceNodeMatchersImpl.HasTraceElement;
import org.batfish.datamodel.trace.TraceNode;
import org.hamcrest.Matcher;

public final class TraceNodeMatchers {
  private TraceNodeMatchers() {}

  /** A {@link TraceNode} matcher on {@link TraceNode#getTraceElement()}. */
  public static Matcher<TraceNode> hasTraceElement(Matcher<? super TraceElement> subMatcher) {
    return new HasTraceElement(subMatcher);
  }

  /** A {@link TraceNode} matcher on {@link TraceNode#getTraceElement()}. */
  public static Matcher<TraceNode> hasTraceElement(TraceElement traceElement) {
    return new HasTraceElement(equalTo(traceElement));
  }

  /** A {@link TraceNode} matcher on {@link TraceNode#getChildren()}. */
  public static Matcher<TraceNode> hasChildren(Matcher<? super List<TraceNode>> subMatcher) {
    return new HasChildren(subMatcher);
  }
}
