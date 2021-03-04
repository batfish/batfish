package org.batfish.bddreachability;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.bddreachability.EdgeMatchersImpl.HasPostState;
import org.batfish.bddreachability.EdgeMatchersImpl.HasPreState;
import org.batfish.bddreachability.EdgeMatchersImpl.HasTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Edge Edges}. */
public final class EdgeMatchers {
  /**
   * Matches {@link Edge#getPreState}, {@link Edge#getPostState}, and {@link Edge#getTransition}.
   */
  public static Matcher<Edge> edge(
      StateExpr preState, StateExpr postState, Matcher<Transition> transitionMatcher) {
    return allOf(
        new HasPreState(equalTo(preState)),
        new HasPostState(equalTo(postState)),
        new HasTransition(transitionMatcher));
  }

  /** Matches {@link Edge#getTransition()}. */
  public static Matcher<Edge> hasTransition(Matcher<Transition> transitionMatcher) {
    return new HasTransition(transitionMatcher);
  }
}
