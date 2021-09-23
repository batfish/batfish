package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DisjunctionAstNode extends BooleanExprAstNode {

  DisjunctionAstNode(BooleanExprAstNode... disjuncts) {
    _disjuncts = ImmutableList.copyOf(disjuncts);
  }

  DisjunctionAstNode(Iterable<BooleanExprAstNode> disjuncts) {
    _disjuncts = ImmutableList.copyOf(disjuncts);
  }

  @Nonnull
  @Override
  public BooleanExprAstNode or(BooleanExprAstNode disjunct) {
    return new DisjunctionAstNode(
        ImmutableList.<BooleanExprAstNode>builder().addAll(_disjuncts).add(disjunct).build());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof DisjunctionAstNode)) {
      return false;
    }
    DisjunctionAstNode that = (DisjunctionAstNode) o;
    return _disjuncts.equals(that._disjuncts);
  }

  @Override
  public int hashCode() {
    return _disjuncts.hashCode();
  }

  private final @Nonnull List<BooleanExprAstNode> _disjuncts;
}
