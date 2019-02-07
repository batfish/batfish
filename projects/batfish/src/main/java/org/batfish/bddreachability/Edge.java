package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.constraint;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Identity;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.z3.expr.StateExpr;

/** An edge in the graph modeling network behavior. */
@ParametersAreNonnullByDefault
final class Edge {
  private final @Nonnull StateExpr _postState;
  private final @Nonnull StateExpr _preState;
  private final @Nonnull Transition _transition;

  Edge(StateExpr preState, StateExpr postState) {
    _preState = preState;
    _postState = postState;
    _transition = Identity.INSTANCE;
  }

  Edge(StateExpr preState, StateExpr postState, BDD constraint) {
    _preState = preState;
    _postState = postState;
    _transition = constraint(constraint);
  }

  public Edge(StateExpr preState, StateExpr postState, Transition transition) {
    _preState = preState;
    _postState = postState;
    _transition = transition;
  }

  @Nonnull
  StateExpr getPostState() {
    return _postState;
  }

  @Nonnull
  StateExpr getPreState() {
    return _preState;
  }

  @Nonnull
  Transition getTransition() {
    return _transition;
  }

  @Nonnull
  BDD traverseBackward(BDD bdd) {
    return _transition.transitBackward(bdd);
  }

  @Nonnull
  BDD traverseForward(BDD bdd) {
    return _transition.transitForward(bdd);
  }
}
