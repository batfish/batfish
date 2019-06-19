package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasHead;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasNode1;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasNode2;
import org.batfish.datamodel.matchers.EdgeMatchersImpl.HasTail;
import org.hamcrest.Matcher;

/** Matchers for {@link Edge}. */
@ParametersAreNonnullByDefault
public final class EdgeMatchers {

  /**
   * Returns a matcher that matches an {@link Edge} whose head is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Edge> hasHead(Matcher<? super NodeInterfacePair> subMatcher) {
    return new HasHead(subMatcher);
  }

  /** Returns a matcher that matches an {@link Edge} whose head is equal to {@code expectedHead}. */
  public static @Nonnull Matcher<Edge> hasHead(NodeInterfacePair expectedHead) {
    return new HasHead(equalTo(expectedHead));
  }

  /**
   * Returns a matcher that matches an {@link Edge} whose node1 is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Edge> hasNode1(Matcher<? super String> subMatcher) {
    return new HasNode1(subMatcher);
  }

  /**
   * Returns a matcher that matches an {@link Edge} whose node1 is equal to {@code expectedNode1}.
   */
  public static @Nonnull Matcher<Edge> hasNode1(String expectedNode1) {
    return new HasNode1(equalTo(expectedNode1));
  }

  /**
   * Returns a matcher that matches an {@link Edge} whose node2 is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Edge> hasNode2(Matcher<? super String> subMatcher) {
    return new HasNode2(subMatcher);
  }

  /**
   * Returns a matcher that matches an {@link Edge} whose node2 is equal to {@code expectedNode2}.
   */
  public static @Nonnull Matcher<Edge> hasNode2(String expectedNode2) {
    return new HasNode2(equalTo(expectedNode2));
  }

  /**
   * Returns a matcher that matches an {@link Edge} whose tail is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Edge> hasTail(Matcher<? super NodeInterfacePair> subMatcher) {
    return new HasTail(subMatcher);
  }

  /** Returns a matcher that matches an {@link Edge} whose tail is equal to {@code expectedTail}. */
  public static @Nonnull Matcher<Edge> hasTail(NodeInterfacePair expectedTail) {
    return new HasTail(equalTo(expectedTail));
  }

  private EdgeMatchers() {}
}
