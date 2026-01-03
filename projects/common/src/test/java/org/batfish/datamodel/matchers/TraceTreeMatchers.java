package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.matchers.TraceTreeMatchersImpl.HasChildren;
import org.batfish.datamodel.matchers.TraceTreeMatchersImpl.HasTraceElement;
import org.batfish.datamodel.trace.TraceTree;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

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

  /**
   * A {@link TraceTree} matcher combining {@link TraceTreeMatchers#hasTraceElement(String)} and
   * {@link TraceTreeMatchers#hasNoChildren()}.
   */
  public static Matcher<TraceTree> isTraceTree(String text) {
    return allOf(hasTraceElement(text), hasNoChildren());
  }

  /**
   * A {@link TraceTree} matcher combining {@link TraceTreeMatchers#hasTraceElement(TraceElement)}
   * and {@link TraceTreeMatchers#hasNoChildren()}.
   */
  public static Matcher<TraceTree> isTraceTree(TraceElement traceElement) {
    return allOf(hasTraceElement(traceElement), hasNoChildren());
  }

  /**
   * A {@link TraceTree} matcher combining {@link TraceTreeMatchers#hasTraceElement(TraceElement)},
   * {@link TraceTreeMatchers#hasChildren}, and {@link Matchers#contains}.
   */
  @SafeVarargs
  @SuppressWarnings({"varargs"})
  public static Matcher<TraceTree> isTraceTree(
      TraceElement traceElement, Matcher<? super TraceTree>... childMatchers) {
    return allOf(hasTraceElement(traceElement), hasChildren(contains(childMatchers)));
  }

  /**
   * A {@link TraceTree} matcher combining {@link TraceTreeMatchers#hasTraceElement(String)}, {@link
   * TraceTreeMatchers#hasChildren}, and {@link Matchers#contains}.
   */
  @SafeVarargs
  @SuppressWarnings({"varargs"})
  public static Matcher<TraceTree> isTraceTree(
      String traceElement, Matcher<? super TraceTree>... childMatchers) {
    return allOf(hasTraceElement(traceElement), hasChildren(contains(childMatchers)));
  }

  /**
   * A {@link TraceTree} matcher combining {@link TraceTreeMatchers#hasTraceElement(Matcher)},
   * {@link TraceTreeMatchers#hasChildren}, and {@link Matchers#contains}.
   */
  @SafeVarargs
  @SuppressWarnings({"varargs"})
  public static Matcher<TraceTree> isTraceTree(
      Matcher<? super TraceElement> traceElementMatcher,
      Matcher<? super TraceTree>... childMatchers) {
    return allOf(hasTraceElement(traceElementMatcher), hasChildren(contains(childMatchers)));
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  public static Matcher<TraceTree> hasChildren(Matcher<? super List<TraceTree>> subMatcher) {
    return new HasChildren(subMatcher);
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  @SafeVarargs
  @SuppressWarnings({"varargs"})
  public static Matcher<TraceTree> hasChildren(Matcher<? super TraceTree>... subMatchers) {
    return new HasChildren(contains(subMatchers));
  }

  /** A {@link TraceTree} matcher on {@link TraceTree#getChildren()}. */
  public static Matcher<TraceTree> hasNoChildren() {
    return new HasChildren(empty());
  }

  /**
   * A {@link TraceTree} matcher matching trees where:
   * <li>the root and all descendants each have at most one child, such that the tree has no
   *     branching
   * <li>the tree is the same size as {@code traceElements}
   * <li>each node's {@link TraceElement}, starting at the root, matches the same-index element in
   *     {@code traceElements}
   */
  public static Matcher<TraceTree> isChainOfSingleChildren(TraceElement... traceElements) {
    if (traceElements.length == 0) {
      return hasNoChildren();
    }

    // Iterate backwards through traceElements to start from the leaf child's trace element
    ListIterator<TraceElement> iterator =
        Arrays.asList(traceElements).listIterator(traceElements.length);

    // Matcher for last child's trace element
    Matcher<TraceTree> matcher = isTraceTree(iterator.previous());

    // For each parent going up, apply the matcher to its child and assert on its trace element
    while (iterator.hasPrevious()) {
      matcher = isTraceTree(iterator.previous(), matcher);
    }

    // Finalized matcher should match on the root node
    return matcher;
  }
}
