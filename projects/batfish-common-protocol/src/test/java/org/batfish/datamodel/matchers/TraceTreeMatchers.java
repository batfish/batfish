package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.matchers.TraceTreeMatchersImpl.HasChildren;
import org.batfish.datamodel.matchers.TraceTreeMatchersImpl.HasTraceElement;
import org.batfish.datamodel.trace.TraceTree;
import org.hamcrest.Matcher;

public final class TraceTreeMatchers {
  private TraceTreeMatchers() {}

  /** A {@link TraceTree} matcher on {@link TraceTree#getTraceElement()}. */
  public static Matcher<TraceTree> hasTraceElement(Matcher<? super TraceElement> subMatcher) {
    return new HasTraceElement(subMatcher);
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getTraceElement()}. */
  public static Matcher<TraceTree> hasTraceElement(TraceElement traceElement) {
    return new HasTraceElement(equalTo(traceElement));
  }

  /** A {@link TraceTree} matcher on a tree with one child matching {@code subMatcher}. */
  public static Matcher<TraceTree> hasChild(Matcher<? super TraceTree> subMatcher) {
    return hasChildren(contains(subMatcher));
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  public static Matcher<TraceTree> hasChildren(Matcher<? super List<TraceTree>> subMatcher) {
    return new HasChildren(subMatcher);
  }

  /**
   * A {@link TraceTree} matcher matching non-branching trees with the given {@code traceElements},
   * starting at the root
   *
   * @see TraceTreeMatchers#isChainOfSingleChildren(List)
   */
  public static Matcher<TraceTree> isChainOfSingleChildren(TraceElement... traceElements) {
    return isChainOfSingleChildren(
        Stream.of(traceElements)
            .map(TraceTreeMatchers::hasTraceElement)
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * A {@link TraceTree} matcher matching trees where:
   * <li>the root and all descendants each have at most one child, such that the tree has no
   *     branching
   * <li>the tree is the same size as {@code nodeMatchers}
   * <li>each node, starting at the root, matches the same-index element in {@code nodeMatchers}
   */
  public static Matcher<TraceTree> isChainOfSingleChildren(
      List<Matcher<? super TraceTree>> nodeMatchers) {
    // Doesn't make sense with 0 traceElements
    assert !nodeMatchers.isEmpty();

    // Reverse nodeMatchers list to start from the leaf child's trace element matcher
    Iterator<Matcher<? super TraceTree>> iterator = Lists.reverse(nodeMatchers).iterator();

    // Matcher for last child
    Matcher<TraceTree> matcher = allOf(iterator.next(), hasChildren(empty()));

    // For each node, going up, apply the matcher so far to its child and the next matcher to itself
    while (iterator.hasNext()) {
      matcher = allOf(iterator.next(), hasChild(matcher));
    }

    // Finalized matcher should match on the root node
    return matcher;
  }
}
