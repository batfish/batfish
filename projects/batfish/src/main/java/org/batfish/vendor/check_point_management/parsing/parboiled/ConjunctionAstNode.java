package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ConjunctionAstNode extends BooleanExprAstNode {

  ConjunctionAstNode(BooleanExprAstNode... conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  ConjunctionAstNode(Iterable<BooleanExprAstNode> conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  @Nonnull
  @Override
  public BooleanExprAstNode and(BooleanExprAstNode conjunct) {
    return new ConjunctionAstNode(
        ImmutableList.<BooleanExprAstNode>builder().addAll(_conjuncts).add(conjunct).build());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ConjunctionAstNode)) {
      return false;
    }
    ConjunctionAstNode that = (ConjunctionAstNode) o;
    return _conjuncts.equals(that._conjuncts);
  }

  @Override
  public int hashCode() {
    return _conjuncts.hashCode();
  }

  private final @Nonnull List<BooleanExprAstNode> _conjuncts;
}
