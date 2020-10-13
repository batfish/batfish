package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.constraint;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Identity;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;

/** An edge in the graph modeling network behavior. */
@ParametersAreNonnullByDefault
public final class Edge {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge edge = (Edge) o;
    return _postState.equals(edge._postState)
        && _preState.equals(edge._preState)
        && _transition.equals(edge._transition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_postState, _preState, _transition);
  }

  @Nonnull
  public StateExpr getPostState() {
    return _postState;
  }

  @Nonnull
  public StateExpr getPreState() {
    return _preState;
  }

  @Nonnull
  public Transition getTransition() {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Edge.class)
        .add("preState", _preState)
        .add("postState", _postState)
        .add("transition", _transition)
        .toString();
  }
}
