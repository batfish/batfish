package org.batfish.bddreachability;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.z3.expr.StateExpr;

/** An edge in the graph modeling network behavior. */
@ParametersAreNonnullByDefault
final class Edge {
  final @Nonnull StateExpr _postState;
  final @Nonnull StateExpr _preState;
  final @Nonnull Function<BDD, BDD> _traverseBackward;
  final @Nonnull Function<BDD, BDD> _traverseForward;

  Edge(StateExpr preState, StateExpr postState, BDD constraint) {
    _preState = preState;
    _postState = postState;
    _traverseBackward = constraint::and;
    _traverseForward = constraint::and;
  }

  Edge(
      StateExpr preState,
      StateExpr postState,
      Function<BDD, BDD> traverseBackward,
      Function<BDD, BDD> traverseForward) {
    _postState = postState;
    _preState = preState;
    _traverseBackward = traverseBackward;
    _traverseForward = traverseForward;
  }

  public @Nonnull BDD traverseForward(BDD bdd) {
    return _traverseForward.apply(bdd);
  }

  public @Nonnull BDD traverseBackward(BDD bdd) {
    return _traverseBackward.apply(bdd);
  }
}
