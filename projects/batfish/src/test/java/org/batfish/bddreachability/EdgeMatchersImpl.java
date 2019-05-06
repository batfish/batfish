package org.batfish.bddreachability;

import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class EdgeMatchersImpl {
  static final class HasPostState extends FeatureMatcher<Edge, StateExpr> {
    HasPostState(Matcher<? super StateExpr> matcher) {
      super(matcher, "an Edge with postState", "postState");
    }

    @Override
    protected StateExpr featureValueOf(Edge edge) {
      return edge.getPostState();
    }
  }

  static final class HasPreState extends FeatureMatcher<Edge, StateExpr> {
    HasPreState(Matcher<? super StateExpr> matcher) {
      super(matcher, "an Edge with preState", "preState");
    }

    @Override
    protected StateExpr featureValueOf(Edge edge) {
      return edge.getPreState();
    }
  }

  static final class HasTransition extends FeatureMatcher<Edge, Transition> {
    HasTransition(Matcher<? super Transition> matcher) {
      super(matcher, "an Edge with transition", "transition");
    }

    @Override
    protected Transition featureValueOf(Edge edge) {
      return edge.getTransition();
    }
  }
}
