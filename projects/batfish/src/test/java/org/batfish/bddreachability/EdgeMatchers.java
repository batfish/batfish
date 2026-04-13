package org.batfish.bddreachability;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Edge Edges}. */
public final class EdgeMatchers {
  /**
   * Matches {@link Edge#getPreState}, {@link Edge#getPostState}, and {@link Edge#getTransition}.
   */
  public static Matcher<Edge> edge(StateExpr preState, StateExpr postState, Transition transition) {
    return allOf(
        new HasPreState(equalTo(preState)),
        new HasPostState(equalTo(postState)),
        new HasTransition(equalTo(transition)));
  }

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

  private static final class HasPostState extends FeatureMatcher<Edge, StateExpr> {
    HasPostState(Matcher<? super StateExpr> matcher) {
      super(matcher, "an Edge with postState", "postState");
    }

    @Override
    protected StateExpr featureValueOf(Edge edge) {
      return edge.getPostState();
    }
  }

  private static final class HasPreState extends FeatureMatcher<Edge, StateExpr> {
    HasPreState(Matcher<? super StateExpr> matcher) {
      super(matcher, "an Edge with preState", "preState");
    }

    @Override
    protected StateExpr featureValueOf(Edge edge) {
      return edge.getPreState();
    }
  }

  private static final class HasTransition extends FeatureMatcher<Edge, Transition> {
    HasTransition(Matcher<? super Transition> matcher) {
      super(matcher, "an Edge with transition", "transition");
    }

    @Override
    protected Transition featureValueOf(Edge edge) {
      return edge.getTransition();
    }
  }
}
