package org.batfish.datamodel.acl;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.TraceTreeMatchersImpl.HasChildren;
import org.batfish.datamodel.acl.TraceTreeMatchersImpl.HasTraceElement;
import org.batfish.datamodel.trace.TraceTree;
import org.hamcrest.Matcher;

public final class TraceTreeMatchers {
  private TraceTreeMatchers() {}

  /** A {@link TraceTree} matcher on {@link TraceTree#getTraceElement()}. */
  public static Matcher<TraceTree> hasTraceElement(Matcher<? super TraceElement> subMatcher) {
    return new HasTraceElement(subMatcher);
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getTraceElement()}. */
  public static Matcher<TraceTree> hasTraceElement(String text) {
    return new HasTraceElement(equalTo(TraceElement.of(text)));
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getTraceElement()}. */
  public static Matcher<TraceTree> hasTraceElement(TraceElement traceElement) {
    return new HasTraceElement(equalTo(traceElement));
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  public static Matcher<TraceTree> hasChildren(Matcher<? super List<TraceTree>> subMatcher) {
    return new HasChildren(subMatcher);
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  public static Matcher<TraceTree> hasNoChildren() {
    return new HasChildren(empty());
  }
}
