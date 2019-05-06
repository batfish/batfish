package org.batfish.bddreachability;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.bddreachability.EdgeMatchersImpl.HasPostState;
import org.batfish.bddreachability.EdgeMatchersImpl.HasPreState;
import org.batfish.bddreachability.EdgeMatchersImpl.HasTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Edge Edges}. */
public final class EdgeMatchers {
  /** Matches {@link Edge#getPostState}. */
  public static Matcher<Edge> hasPostState(StateExpr expr) {
    return new HasPostState(equalTo(expr));
  }

  /** Matches {@link Edge#getPreState}. */
  public static Matcher<Edge> hasPreState(StateExpr expr) {
    return new HasPreState(equalTo(expr));
  }

  /** Matches {@link Edge#getTransition()}. */
  public static Matcher<Edge> hasTransition(Matcher<Transition> matcher) {
    return new HasTransition(matcher);
  }
}
