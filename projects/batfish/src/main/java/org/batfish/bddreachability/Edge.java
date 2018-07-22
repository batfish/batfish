package org.batfish.bddreachability;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.z3.expr.StateExpr;

final class Edge {
  final @Nonnull BDD _constraint;
  final @Nonnull StateExpr _postState;
  final @Nonnull StateExpr _preState;
  final @Nullable List<BDDSourceNat> _sourceNats;

  Edge(@Nonnull StateExpr preState, @Nonnull StateExpr postState, @Nonnull BDD constraint) {
    _constraint = constraint;
    _postState = postState;
    _preState = preState;
    _sourceNats = null;
  }

  Edge(
      @Nonnull StateExpr preState,
      @Nonnull StateExpr postState,
      @Nullable List<BDDSourceNat> sourceNats) {
    _constraint = BDDPacket.factory.one();
    _postState = postState;
    _preState = preState;
    _sourceNats = sourceNats;
  }

  public BDD getConstraint() {
    return _constraint;
  }

  public List<BDDSourceNat> getSourceNats() {
    return _sourceNats;
  }
}
