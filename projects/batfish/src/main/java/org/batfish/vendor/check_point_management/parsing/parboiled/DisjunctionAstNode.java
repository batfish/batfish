package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a disjunction of boolean expressions. */
@ParametersAreNonnullByDefault
public final class DisjunctionAstNode implements BooleanExprAstNode {

  @VisibleForTesting
  public DisjunctionAstNode(BooleanExprAstNode... disjuncts) {
    _disjuncts = ImmutableList.copyOf(disjuncts);
  }

  DisjunctionAstNode(Iterable<BooleanExprAstNode> disjuncts) {
    _disjuncts = ImmutableList.copyOf(disjuncts);
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitDisjunctionAstNode(this, arg);
  }

  @Override
  public @Nonnull BooleanExprAstNode or(BooleanExprAstNode disjunct) {
    return new DisjunctionAstNode(
        ImmutableList.<BooleanExprAstNode>builder().addAll(_disjuncts).add(disjunct).build());
  }

  public @Nonnull List<BooleanExprAstNode> getDisjuncts() {
    return _disjuncts;
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
